package com.example.mynewapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import javax.crypto.SecretKey;

public class PayPaymentWifiActivity extends AppCompatActivity {

    public static final String TAG = "test";

    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private WifiP2pManager manager;
    private BroadcastReceiver receiver;

    private RecyclerView wifiPeerListView;
    private WifiPeerListAdapter adapter;
    private ArrayList<WifiP2pDevice> deviceList = new ArrayList<>();
    private String decodedInformation;
    private SecretKey key;
    private byte[] iv;
    private String role;

    private TextView header;
    private TextView text_device_name;
    private EditText edittext_payment_amount;
    private Button payButton;
    private Button returnHomeButton;
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 10000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_payment_wifi);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_icon);

        Intent i = getIntent();
        Bundle extras = i.getExtras();

        if(extras!=null){
            decodedInformation = extras.getString("DecodedInformation");
            iv = extras.getByteArray("IV");
            key = (SecretKey) extras.get("Key");
            role = extras.getString("Role");

            // Update the layout for receiver side
            if(role != null){
                if(role.equals("Receiver")){
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    getSupportActionBar().setTitle("Receive Payment");
                    header = findViewById(R.id.text_transfer_amount);
                    header.setText("Your device name:");
                    text_device_name = findViewById(R.id.text_device_name);
                    text_device_name.setVisibility(View.VISIBLE);
                    edittext_payment_amount = findViewById(R.id.edittext_amount_of_payment);
                    edittext_payment_amount.setVisibility(View.INVISIBLE);
                    payButton = findViewById(R.id.button_confirm_pay);
                    payButton.setVisibility(View.GONE);
                    returnHomeButton = findViewById(R.id.button_return_home);
                    returnHomeButton.setVisibility(View.VISIBLE);
                    returnHomeListener();
                }
            }
        }

        wifiPeerListView = findViewById(R.id.recyclerView);
        wifiPeerListView.addItemDecoration(new DividerItemDecoration(PayPaymentWifiActivity.this,1));
        wifiPeerListView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WifiPeerListAdapter(deviceList);
        wifiPeerListView.setAdapter(adapter);

        // Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        paymentAmountChangedListener();
    }

    @Override
    protected void onResume() {
        super.onResume();

        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
        discoverPeers(manager, channel);

        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, delay);
                discoverPeers(manager, channel);
            }
        }, delay);
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        handler.removeCallbacks(runnable);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        // Leave empty to prevent users from returning to login page by back press, and force them to sign out using the Logout button
    }

    public void discoverPeers(WifiP2pManager manager, WifiP2pManager.Channel channel) {
        if (ActivityCompat.checkSelfPermission(PayPaymentWifiActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PayPaymentWifiActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            Toast.makeText(PayPaymentWifiActivity.this, "NO PERMISSION", Toast.LENGTH_SHORT).show();
        }

        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Discovering Peers");
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Fail to initiate the discovery of peers");
            }
        });
    }

    public String getDecodedInformation(){
        return decodedInformation;
    }

    public byte[] getIv(){
        return iv;
    }

    public SecretKey getKey(){
        return key;
    }

    private void paymentAmountChangedListener(){
        edittext_payment_amount = findViewById(R.id.edittext_amount_of_payment);
        edittext_payment_amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().indexOf(".") != -1){
                    if(s.toString().substring(s.toString().indexOf(".")+1).length() > 2){
                        edittext_payment_amount.setText(s.toString().substring(0, s.toString().length()-1));
                        edittext_payment_amount.setSelection(edittext_payment_amount.getText().length());
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void alertInsufficientBalanceDialog(){
        AlertDialog.Builder paymentAlert = new AlertDialog.Builder(PayPaymentWifiActivity.this);
        paymentAlert.setCancelable(false)
                .setTitle("Payment Failed")
                .setMessage("Insufficient balance. Please top up your wallet.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        // Set the title color for the dialog
        AlertDialog InsufficientBalanceDialog = paymentAlert.create();
        InsufficientBalanceDialog.show();
        TextView dialogTitle = InsufficientBalanceDialog.findViewById(getResources().getIdentifier( "alertTitle", "id", PayPaymentWifiActivity.this.getPackageName()));
        dialogTitle.setTextColor(Color.parseColor("#FF5722"));
    }

    public void alertPaymentFailDialog(){
        AlertDialog.Builder paymentFailAlert = new AlertDialog.Builder(PayPaymentWifiActivity.this);
        paymentFailAlert.setCancelable(false)
                .setTitle("Payment Failed")
                .setMessage("The device you have connected is not the payment receiver." + "\n" +  "Please re-scan the QR code and establish a WiFi-direct connection with the right device.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        // Set the title color for the dialog
        AlertDialog PaymentFailDialog = paymentFailAlert.create();
        PaymentFailDialog.show();
        TextView dialogTitle = PaymentFailDialog.findViewById(getResources().getIdentifier( "alertTitle", "id", PayPaymentWifiActivity.this.getPackageName()));
        dialogTitle.setTextColor(Color.parseColor("#FF5722"));
    }

    public void alertPaymentFailByConnectionDialog(){
        AlertDialog.Builder paymentFailAlert = new AlertDialog.Builder(PayPaymentWifiActivity.this);
        paymentFailAlert.setCancelable(false)
                .setTitle("Payment Failed")
                .setMessage("Please ensure you have connected with the receiver by WiFi-Direct.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        // Set the title color for the dialog
        AlertDialog PaymentFailDialog = paymentFailAlert.create();
        PaymentFailDialog.show();
        TextView dialogTitle = PaymentFailDialog.findViewById(getResources().getIdentifier( "alertTitle", "id", PayPaymentWifiActivity.this.getPackageName()));
        dialogTitle.setTextColor(Color.parseColor("#FF5722"));
    }

    public void returnHomeListener(){
        returnHomeButton = findViewById(R.id.button_return_home);
        returnHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(PayPaymentWifiActivity.this, HomeActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

}