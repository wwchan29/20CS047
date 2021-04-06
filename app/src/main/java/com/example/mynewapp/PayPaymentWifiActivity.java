package com.example.mynewapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class PayPaymentWifiActivity extends AppCompatActivity {

    public static final String TAG = "test";

    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private WifiP2pManager manager;
    private BroadcastReceiver receiver;

    private RecyclerView wifiPeerListView;
    private WifiPeerListAdapter adapter;
    private ArrayList<WifiP2pDevice> deviceList = new ArrayList<>();

    Handler handler = new Handler();
    Runnable runnable;
    int delay = 15000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_payment_wifi);

        wifiPeerListView = findViewById(R.id.recyclerView);
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

}