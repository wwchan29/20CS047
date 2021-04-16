package com.example.mynewapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TopUpHistoryAdapter extends ArrayAdapter<TopUp> {
    private Context context;

    public TopUpHistoryAdapter(@NonNull Context context, int resource, @NonNull List<TopUp> topUpItems) {
        super(context, resource, topUpItems);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TopUp topUp = getItem(position);
        Date topUpDate = topUp.getTopUpDate();
        double topUpAmount = topUp.getTopUpAmount();

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_topup_history, parent, false);
        }

        TextView dateView = convertView.findViewById(R.id.text_topup_date);
        TextView timeView = convertView.findViewById(R.id.text_topup_time);
        TextView amountView = convertView.findViewById(R.id.text_topup_amount_history);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMMM-yyyy");
        String date = dateFormat.format(topUpDate);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String time = timeFormat.format(topUpDate);

        String amount = String.valueOf(topUpAmount);

        dateView.setText(date);
        timeView.setText(time);
        amountView.setText("$" + amount);

        return convertView;
    }
}
