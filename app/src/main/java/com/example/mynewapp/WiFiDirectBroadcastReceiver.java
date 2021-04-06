package com.example.mynewapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver implements WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener {

    private PayPaymentWifiActivity payPaymentWifiActivity;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;

    private ArrayList<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private RecyclerView wifiPeerListView;
    private TextView text;

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
                Toast.makeText(payPaymentWifiActivity, peers.size() + "devices found", Toast.LENGTH_LONG).show();
                // Perform any other updates needed based on the new list of
                // peers connected to the Wi-Fi P2P network.
            }

            if (peers.size() == 0) {
                Toast.makeText(payPaymentWifiActivity, "No devices found", Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wifi P2P mode is enabled or not, alert
            // the Activity.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                //activity.setIsWifiP2pEnabled(true);
            } else {
                //activity.setIsWifiP2pEnabled(false);
                Toast.makeText(payPaymentWifiActivity, "Please turn on WiFi to enable connection", Toast.LENGTH_SHORT).show();
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
            // The peer list has changed! We should probably do something about
            // that.

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

                        // After the group negotiation, we can determine the group owner
                        // (server).
                        if (info.groupFormed && info.isGroupOwner) {
                            text = payPaymentWifiActivity.findViewById(R.id.text_connect_receiver);
                            text.setText("Server");
                        } else if (info.groupFormed) {
                            text = payPaymentWifiActivity.findViewById(R.id.text_connect_receiver);
                            text.setText("Client");
                        }
                    }
                });
            }else{
                Toast.makeText(payPaymentWifiActivity, "The device is not connected" , Toast.LENGTH_SHORT).show();
            }

            Log.d(PayPaymentWifiActivity.TAG, "Connection changed");

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            WifiP2pDevice currentDevice = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            if (currentDevice.status == 0){
                Log.d(PayPaymentWifiActivity.TAG, "Current device status: connected");
            }else if (currentDevice.status == 1){
                Log.d(PayPaymentWifiActivity.TAG, "Current device status: invited");
            }else if  (currentDevice.status == 3){
                Log.d(PayPaymentWifiActivity.TAG, "Current device status: available");
            }else{
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

            // If an AdapterView is backed by this data, notify it
            // of the change. For instance, if you have a ListView of
            // available peers, trigger an update.
            adapter.notifyDataSetChanged();
            Toast.makeText(payPaymentWifiActivity, peers.size() + "devices found", Toast.LENGTH_LONG).show();
            // Perform any other updates needed based on the new list of
            // peers connected to the Wi-Fi P2P network.
        }

        if (peers.size() == 0) {
            Toast.makeText(payPaymentWifiActivity, "No devices found", Toast.LENGTH_LONG).show();
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
        config.groupOwnerIntent = 15;

        if (ActivityCompat.checkSelfPermission(payPaymentWifiActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(payPaymentWifiActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            Toast.makeText(payPaymentWifiActivity, "NO PERMISSION", Toast.LENGTH_SHORT).show();
        }

//        manager.createGroup(channel, new WifiP2pManager.ActionListener() {
//            @Override
//            public void onSuccess() {
//                Log.d(PayPaymentWifiActivity.TAG, "Connect success ");
//                // Device is ready to accept incoming connections from peers.
//            }
//
//            @Override
//            public void onFailure(int reason) {
//                Log.d(PayPaymentWifiActivity.TAG, String.valueOf(reason));
//            }
//        });

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


    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {

    }
}
