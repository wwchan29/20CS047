package com.example.mynewapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PaymentHistoryAdapter extends ArrayAdapter<Payment> {
    private Context context;

    public PaymentHistoryAdapter(@NonNull Context context, int resource, @NonNull List<Payment> paymentItems) {
        super(context, resource, paymentItems);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Payment payment = getItem(position);
        Date paymentDate = payment.getPaymentDate();
        double paymentAmount = payment.getPaymentAmount();
        String paymentAction = payment.getPaymentAction();
        String paymentUser = payment.getPaymentUserName();

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_payment_history, parent, false);
        }

        ImageView imageAction = convertView.findViewById(R.id.image_action);
        TextView userView = convertView.findViewById(R.id.text_payment_user);
        TextView timeView = convertView.findViewById(R.id.text_payment_time);
        TextView amountView = convertView.findViewById(R.id.text_payment_amount);

        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String time = timeFormat.format(paymentDate);
        String amount = String.valueOf(paymentAmount);
        timeView.setText(time);
        userView.setText(paymentUser);

        if(paymentAction.equals("pay")){
            imageAction.setImageResource(R.drawable.ic_pay_to);
            String payAmount = "- $" + amount;
            amountView.setText(payAmount);
            amountView.setTextColor(Color.parseColor("#FF0000"));
        }else if (paymentAction.equals("receive")){
            imageAction.setImageResource(R.drawable.ic_receive_from);
            imageAction.setScaleX(-1);
            String receiveAmount = "+ $" + amount;
            amountView.setText(receiveAmount);
            amountView.setTextColor(Color.parseColor("#32CD32"));
        }

        return convertView;
    }
}
