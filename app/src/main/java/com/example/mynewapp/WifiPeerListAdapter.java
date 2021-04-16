package com.example.mynewapp;

import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WifiPeerListAdapter extends RecyclerView.Adapter<WifiPeerListAdapter.ViewHolder> {

    private static View.OnClickListener onItemClickListener;
    private ArrayList<WifiP2pDevice> deviceList;

    public void setOnItemClickListener(View.OnClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public  class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textView = (TextView) view.findViewById(R.id.text_peers);
            view.setTag(this);
            view.setOnClickListener(onItemClickListener);
        }

        public TextView getTextView() {
            return textView;
        }
    }

    public WifiPeerListAdapter(ArrayList<WifiP2pDevice> deviceList){
        this.deviceList = deviceList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_peers, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getTextView().setText(deviceList.get(position).deviceName);
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }
}
