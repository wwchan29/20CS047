package com.example.mynewapp;

import android.Manifest;
import android.app.Activity;
import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

import static com.google.firebase.firestore.FirebaseFirestore.*;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver implements WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener {

    private PayPaymentWifiActivity payPaymentWifiActivity;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;

    private ArrayList<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private RecyclerView wifiPeerListView;

    private FirebaseAuth auth;
    private FirebaseFirestore db = getInstance();
    private FirebaseUser user;
    private static final String TABLE_USER = "User";
    private String userID;

    private TextView text_device_name;
    private EditText edittext_payment_amount;
    private TextView text_connection_status;
    private Button payButton;
    private Button disconnectButton;

    private double currentBalance;
    private double paymentAmount;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, PayPaymentWifiActivity payPaymentWifiActivity) {
        this.payPaymentWifiActivity = payPaymentWifiActivity;
        this.manager = manager;
        this.channel = channel;
    }

    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            ArrayList<WifiP2pDevice> refreshedPeers = new ArrayList<>(peerList.getDeviceList());

            wifiPeerListView = payPaymentWifiActivity.findViewById(R.id.recyclerView);
            WifiPeerListAdapter adapter = new WifiPeerListAdapter(peers);
            wifiPeerListView.setAdapter(adapter);
            adapter.setOnItemClickListener(onItemClickListener);

            if (!refreshedPeers.equals(peers)) {
                peers.clear();
                peers.addAll(refreshedPeers);

                // If an AdapterView is backed by this data, notify it
                // of the change. For instance, if you have a ListView of
                // available peers, trigger an update.
                adapter.notifyDataSetChanged();
                // Perform any other updates needed based on the new list of
                // peers connected to the Wi-Fi P2P network.
            }

            if (peers.size() == 0) {
                //Toast.makeText(payPaymentWifiActivity, "No devices found", Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        text_connection_status = payPaymentWifiActivity.findViewById(R.id.text_connection_status);
        payButton = payPaymentWifiActivity.findViewById(R.id.button_confirm_pay);
        disconnectButton = payPaymentWifiActivity.findViewById(R.id.button_disconnect);
        text_device_name = payPaymentWifiActivity.findViewById(R.id.text_device_name);
        WifiP2pDevice myDevice = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
        if(myDevice != null) {
            text_device_name.setText(myDevice.deviceName);
        }

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wifi P2P mode is enabled or not, alert
            // the Activity.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                payButton.setAlpha(0.3f);
                payButton.setClickable(false);
                disconnectButton.setClickable(false);
                text_connection_status.setText("WiFi Direct status: Available");
            } else {
                payButton.setAlpha(0.3f);
                payButton.setClickable(false);
                disconnectButton.setClickable(false);
                text_connection_status.setText("WiFi Direct status: Wi-fi is disabled");
                //Toast.makeText(payPaymentWifiActivity, "Please turn on WiFi to enable connection", Toast.LENGTH_SHORT).show();
            }

            Log.d(PayPaymentWifiActivity.TAG, "Wifi p2p state changed");

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (manager != null) {
                if (ActivityCompat.checkSelfPermission(payPaymentWifiActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(payPaymentWifiActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
                manager.requestPeers(channel, peerListListener);
                Log.d(PayPaymentWifiActivity.TAG, "Requesting new peers");
            }
            Log.d(PayPaymentWifiActivity.TAG, "P2P peers changed");

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (manager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

                Log.d(PayPaymentWifiActivity.TAG, "The device is connected");
                // We are connected with the other device, request connection
                // info to find group owner IP


                manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info) {
                        String groupOwnerAddress = info.groupOwnerAddress.getHostAddress();
                        System.out.println("Group owner address is: " + groupOwnerAddress);

                        if (info.groupFormed && info.isGroupOwner) {
                            text_connection_status.setText("WiFi Direct status: Connected");
                            disconnectButton.setClickable(true);
                            disconnectButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    disconnect();
                                }
                            });
                            new ServerAsyncTask(payPaymentWifiActivity).execute();

                        } else if (info.groupFormed) {
                            text_connection_status.setText("WiFi Direct status: Connected");

                            edittext_payment_amount = payPaymentWifiActivity.findViewById(R.id.edittext_amount_of_payment);
                            disconnectButton.setClickable(true);
                            disconnectButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    disconnect();
                                }
                            });
                            payButton.setAlpha(1);
                            payButton.setClickable(true);
                            payButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    auth = FirebaseAuth.getInstance();
                                    user = auth.getCurrentUser();
                                    userID = user.getUid();

                                    auth = FirebaseAuth.getInstance();
                                    user = auth.getCurrentUser();
                                    userID = user.getUid();

                                    db.collection(TABLE_USER).document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if (documentSnapshot.exists()) {
                                                User currentUser = documentSnapshot.toObject(User.class);
                                                currentBalance = currentUser.getBalance();

                                                try {
                                                    paymentAmount = Double.valueOf(edittext_payment_amount.getText().toString());
                                                    if(paymentAmount == 0){
                                                        edittext_payment_amount.setError("Please input a valid amount");
                                                        return;
                                                    }

                                                    if(paymentAmount > 50000){
                                                        edittext_payment_amount.setError("The maximum amount for each transfer is $50000");
                                                        return;
                                                    }

                                                    if(paymentAmount > currentBalance){
                                                        Log.d(PayPaymentWifiActivity.TAG, "currentBalance: " + currentBalance);
                                                        payPaymentWifiActivity.alertInsufficientBalanceDialog();
                                                        return;
                                                    }

                                                }catch(NumberFormatException e){
                                                    edittext_payment_amount.setError("Please input a valid amount");
                                                    return;
                                                }
                                                Log.d(PayPaymentWifiActivity.TAG, "Payment Success!");
                                                String messageToServer = payPaymentWifiActivity.getDecodedInformation() + "|" + String.valueOf(paymentAmount);
                                                new ClientAsyncTask(payPaymentWifiActivity, groupOwnerAddress, messageToServer).execute();
                                            }
                                        }
                                    });

                                }
                            });

                        }
                    }
                });
            } else {
                //Toast.makeText(payPaymentWifiActivity, "The device is not connected", Toast.LENGTH_SHORT).show();
                payButton = payPaymentWifiActivity.findViewById(R.id.button_confirm_pay);
                payButton.setClickable(false);
                disconnectButton.setClickable(false);
            }

            Log.d(PayPaymentWifiActivity.TAG, "Connection changed");

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            WifiP2pDevice currentDevice = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);

            if (currentDevice.status == 0) {
                Log.d(PayPaymentWifiActivity.TAG, "Current device status: connected");
                text_connection_status.setText("WiFi Direct status: Connected");
                payButton.setAlpha(1);
                disconnectButton.setClickable(true);
            } else if (currentDevice.status == 1) {
                text_connection_status.setText("WiFi Direct status: Invited");
                payButton.setAlpha(0.3f);
                disconnectButton.setClickable(false);
                Log.d(PayPaymentWifiActivity.TAG, "Current device status: invited");
            } else if (currentDevice.status == 3) {
                text_connection_status.setText("WiFi Direct status: Available");
                payButton.setAlpha(0.3f);
                disconnectButton.setClickable(true);
                disconnectButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        disconnect();
                    }
                });
                Log.d(PayPaymentWifiActivity.TAG, "Current device status: available");
            } else {
                text_connection_status.setText("WiFi Direct status: Not available");
                payButton.setAlpha(0.3f);
                disconnectButton.setClickable(false);
                Log.d(PayPaymentWifiActivity.TAG, "Current device status: unavailable or failed");
            }

        }
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        ArrayList<WifiP2pDevice> refreshedPeers = new ArrayList<>(peerList.getDeviceList());

        wifiPeerListView = payPaymentWifiActivity.findViewById(R.id.recyclerView);
        WifiPeerListAdapter adapter = new WifiPeerListAdapter(peers);
        wifiPeerListView.setAdapter(adapter);
        adapter.setOnItemClickListener(onItemClickListener);

        if (!refreshedPeers.equals(peers)) {
            peers.clear();
            peers.addAll(refreshedPeers);
            adapter.notifyDataSetChanged();
            //Toast.makeText(payPaymentWifiActivity, peers.size() + "devices found", Toast.LENGTH_LONG).show();
        }

        if (peers.size() == 0) {
            //Toast.makeText(payPaymentWifiActivity, "No devices found", Toast.LENGTH_LONG).show();
        }
    }

    private View.OnClickListener onItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            WifiPeerListAdapter.ViewHolder viewHolder = (WifiPeerListAdapter.ViewHolder) view.getTag();
            connect(viewHolder.getAdapterPosition());

            // viewHolder.getItemId();
            // viewHolder.getItemViewType();
            // viewHolder.itemView;
        }
    };

    public void connect(int position) {
        WifiP2pDevice device = peers.get(position);
        WifiP2pConfig config = new WifiP2pConfig();
        config.wps.setup = WpsInfo.PBC;
        config.deviceAddress = device.deviceAddress;
        System.out.println(device.deviceAddress);
        config.groupOwnerIntent = 0;

        if (ActivityCompat.checkSelfPermission(payPaymentWifiActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(payPaymentWifiActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            Toast.makeText(payPaymentWifiActivity, "NO PERMISSION", Toast.LENGTH_SHORT).show();
        }

        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(PayPaymentWifiActivity.TAG, "Connect success ");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(PayPaymentWifiActivity.TAG, String.valueOf(reason));
            }
        });
    }

    public void disconnect(){
        manager.cancelConnect(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(PayPaymentWifiActivity.TAG, "Successfully disconnected");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(PayPaymentWifiActivity.TAG, "Fail disconnected");
            }
        });

        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });

        deleteExistingWifiDirectGroup();
    }

    public void deleteExistingWifiDirectGroup() {
        try {
            Method[] methods = WifiP2pManager.class.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("deletePersistentGroup")) {
                    for (int netid = 0; netid < 32; netid++) {
                        methods[i].invoke(manager, channel, netid, null);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {

    }

    public class ServerAsyncTask extends AsyncTask {

        private Context context;
        private PayPaymentWifiActivity payPaymentWifiActivity;
        private ServerSocket serverSocket;
        private Socket client;
        private DataInputStream inputStream;
        private DataOutputStream outputStream;
        private Cipher cipher;

        private FirebaseAuth auth;
        private FirebaseFirestore db = FirebaseFirestore.getInstance();
        private FirebaseUser user;
        private static final String TABLE_USER = "User";
        private String userID;
        private double currentBalance;
        private double newBalance;
        private double paymentAmount;
        private String paymentUserName;
        private ArrayList<Payment> currentListOfPayment;
        private User currentUser;

        public ServerAsyncTask(Context context) {
            this.context = context;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected String doInBackground(Object[] objects) {
            try {
                auth = FirebaseAuth.getInstance();
                user = auth.getCurrentUser();
                userID = user.getUid();

                db.collection(TABLE_USER).document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            currentUser = documentSnapshot.toObject(User.class);
                            paymentUserName = currentUser.getName();
                            Log.d(PayPaymentWifiActivity.TAG, paymentUserName);
                        }
                    }
                });

                serverSocket = new ServerSocket(8443);
                client = serverSocket.accept();
                //The server has accepted a connection from client socket
                Log.d(PayPaymentWifiActivity.TAG, "Accepted client's connection");
                inputStream = new DataInputStream(client.getInputStream());
                String messageFromClient = inputStream.readUTF();
                String[] arrayOfmessageFromClient = messageFromClient.split("\\|");

                // if the message from client contains more or less than two parts,
                // then it is corrupted and alert the client for the failure of payment authentication
                if(arrayOfmessageFromClient.length!=3){
                    outputStream = new DataOutputStream(client.getOutputStream());
                    outputStream.writeUTF("Authentication Fail");
                    return null;
                }

                // Decrypt the message sent by Client
                payPaymentWifiActivity = (PayPaymentWifiActivity) context;
                IvParameterSpec ivps = new IvParameterSpec(payPaymentWifiActivity.getIv());
                System.out.println(ivps);
                cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
                cipher.init(Cipher.DECRYPT_MODE, payPaymentWifiActivity.getKey(), ivps);
                byte[] byteDecryptedUserId = Base64.getDecoder().decode(arrayOfmessageFromClient[0]);
                byte[] decryptedUserIdInByte = null;
                try {
                    decryptedUserIdInByte = cipher.doFinal(byteDecryptedUserId);
                } catch (BadPaddingException e) {
                    // The encrypted message from client is not padded properly and is corrupted
                    outputStream = new DataOutputStream(client.getOutputStream());
                    outputStream.writeUTF("Authentication Fail");
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    // The length of The encrypted message from client does not match the block size of the cipher and is corrupted
                    outputStream = new DataOutputStream(client.getOutputStream());
                    outputStream.writeUTF("Authentication Fail");
                    e.printStackTrace();
                }
                String decryptedUserIdInString = Base64.getEncoder().encodeToString(decryptedUserIdInByte);
                Log.d(PayPaymentWifiActivity.TAG, "Decrypted string: " + decryptedUserIdInString);
                if(decryptedUserIdInString.equals(userID)){
                    String messageToClient = "Authenticated" + "|" + paymentUserName;

                    outputStream = new DataOutputStream(client.getOutputStream());
                    outputStream.writeUTF(messageToClient);

                    // Update the balance for receiver
                    db.collection(TABLE_USER).document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                // Get balance from current user and update it by subtracting the payment amount
                                User currentUser = documentSnapshot.toObject(User.class);
                                currentBalance = currentUser.getBalance();
                                paymentAmount = Double.valueOf(arrayOfmessageFromClient[1]);
                                newBalance = currentBalance + paymentAmount;
                                currentUser.setBalance(newBalance);

                                // Initialize a new ReceivePayment item with the payment time and amount,
                                // then update the payment history of the current users by adding the item to the list
                                currentListOfPayment = currentUser.getListOfPayment();
                                Payment receivePaymentItem = new Payment(paymentAmount, new Date(), "receive", arrayOfmessageFromClient[2]);
                                currentListOfPayment.add(receivePaymentItem);
                                currentUser.setListOfPayment(currentListOfPayment);

                                db.collection(TABLE_USER).document(userID).set(currentUser).addOnSuccessListener(new OnSuccessListener<Void>() {

                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(payPaymentWifiActivity, "Receive payment fail", Toast.LENGTH_SHORT ).show();
                                    }
                                });
                            }
                        }
                    });

                }else{
                    outputStream = new DataOutputStream(client.getOutputStream());
                    outputStream.writeUTF("Authentication Fail");
                }
            } catch (IOException e) {
                Log.e(PayPaymentWifiActivity.TAG, e.getMessage());
                return null;
            } catch (InvalidKeyException e) {
                e.printStackTrace();
                return null;
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
                return null;
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
                return null;
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return null;
            }finally{
                if(inputStream != null){
                    try{
                        inputStream.close();
                    } catch (IOException e) {
                        Log.e(PayPaymentWifiActivity.TAG, e.getMessage());
                    }
                }
                if(outputStream != null){
                    try{
                        outputStream.close();
                    } catch (IOException e) {
                        Log.e(PayPaymentWifiActivity.TAG, e.getMessage());
                    }
                }
                if(client != null){
                    try{
                        client.close();
                    } catch (IOException e) {
                        Log.e(PayPaymentWifiActivity.TAG, e.getMessage());
                    }
                }
                if(serverSocket != null){
                    try{
                        serverSocket.close();
                    } catch (IOException e) {
                        Log.e(PayPaymentWifiActivity.TAG, e.getMessage());
                    }
                }
            }

            return null;
        }
    }

    public class ClientAsyncTask extends AsyncTask {

        private Context context;
        private PayPaymentWifiActivity payPaymentWifiActivity;
        private Socket socket;
        private int port = 8443;
        private String hostIpAddress;
        private String messageToServer;
        private String messageFromServer;
        private final int SOCKET_TIMEOUT = 10000;
        private DataOutputStream outputStream;
        private DataInputStream inputStream;

        private FirebaseAuth auth;
        private FirebaseFirestore db = FirebaseFirestore.getInstance();
        private FirebaseUser user;
        private static final String TABLE_USER = "User";
        private String userID;
        private double currentBalance;
        private double newBalance;
        private double paymentAmount;
        public String paymentUserName;
        private User currentUser;
        private ArrayList<Payment> currentListOfPayment;
        private ProgressDialog authenticationLoadingMessage;

        public ClientAsyncTask(Context context, String hostIpAddress, String messageToServer) {
            this.context = context;
            this.hostIpAddress = hostIpAddress;
            this.messageToServer = messageToServer;
        }

        protected void onPreExecute() {
            payPaymentWifiActivity = (PayPaymentWifiActivity) context;
            authenticationLoadingMessage = new ProgressDialog(payPaymentWifiActivity);
            authenticationLoadingMessage.show(payPaymentWifiActivity, "Payment Authentication", "Loading..", false, false);
        }

        @Override
        protected String doInBackground(Object[] objects) {
            try {
                auth = FirebaseAuth.getInstance();
                user = auth.getCurrentUser();
                userID = user.getUid();

                db.collection(TABLE_USER).document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Get balance from current user and update it by subtracting the payment amount
                            currentUser = documentSnapshot.toObject(User.class);
                            paymentUserName = currentUser.getName();
                            Log.d(PayPaymentWifiActivity.TAG, paymentUserName);
                        }
                    }
                });

                Thread.sleep(6000);
                authenticationLoadingMessage.dismiss();

                payPaymentWifiActivity = (PayPaymentWifiActivity) context;
                Log.d(PayPaymentWifiActivity.TAG, "Client Async task is called");
                socket = new Socket();
                socket.bind(null);
                socket.connect((new InetSocketAddress(hostIpAddress, port)), SOCKET_TIMEOUT);
                Log.d(PayPaymentWifiActivity.TAG, "connect to Server success");

                // Send string
                String[] arrayOfmessageToServer = messageToServer.split("\\|");
                messageToServer = messageToServer + "|" + paymentUserName;
                outputStream = new DataOutputStream(socket.getOutputStream());
                outputStream.writeUTF(messageToServer);

                // Receive response from server
                inputStream = new DataInputStream(socket.getInputStream());
                messageFromServer =  inputStream.readUTF();
                String[] arrayOfMessageFromServer = messageFromServer.split("\\|");
                System.out.println(arrayOfMessageFromServer[1]);
                if(arrayOfMessageFromServer[0].equals("Authenticated")){

                    // Update the balance for payer

                    db.collection(TABLE_USER).document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                // Get balance from current user and update it by subtracting the payment amount
                                User currentUser = documentSnapshot.toObject(User.class);
                                currentBalance = currentUser.getBalance();
                                paymentAmount = Double.valueOf(arrayOfmessageToServer[1]);
                                newBalance = currentBalance - paymentAmount;
                                currentUser.setBalance(newBalance);

                                // Initialize a new transfer payment item with the payment time and amount,
                                // then update the payment history of the current users by adding the item to the list
                                currentListOfPayment = currentUser.getListOfPayment();
                                Payment transferPaymentItem = new Payment(paymentAmount, new Date(), "pay", arrayOfMessageFromServer[1]);
                                currentListOfPayment.add(transferPaymentItem);
                                currentUser.setListOfPayment(currentListOfPayment);

                                db.collection(TABLE_USER).document(userID).set(currentUser).addOnSuccessListener(new OnSuccessListener<Void>() {

                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        DialogFragment dialogFragment = new PaymentSuccessDialogFragment(transferPaymentItem);
                                        dialogFragment.show(payPaymentWifiActivity.getSupportFragmentManager(), "paymentSuccess");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(payPaymentWifiActivity, "Payment Fail", Toast.LENGTH_SHORT ).show();
                                    }
                                });
                            }
                        }
                    });
                } else{
                    new Handler(Looper.getMainLooper()).post(new Runnable(){
                        @Override
                        public void run() {
                            payPaymentWifiActivity.alertPaymentFailDialog();
                        }
                    });
                }

                return null;
            } catch (IOException | InterruptedException e) {
                System.out.println("IOException");
                return null;
            }finally{
                //dismiss the progress dialog
                if(authenticationLoadingMessage!=null){
                    authenticationLoadingMessage.dismiss();
                }

                if(inputStream != null){
                    try{
                        inputStream.close();
                    } catch (IOException e) {
                        Log.e(PayPaymentWifiActivity.TAG, e.getMessage());
                    }
                }
                if(outputStream != null){
                    try{
                        outputStream.close();
                    } catch (IOException e) {
                        Log.e(PayPaymentWifiActivity.TAG, e.getMessage());
                    }
                }
                if(socket != null){
                    try{
                        socket.close();
                    } catch (IOException e) {
                        Log.e(PayPaymentWifiActivity.TAG, e.getMessage());
                    }
                }
            }
        }


    }
}
