package com.example.mynewapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;

public class PaymentSuccessDialogFragment extends DialogFragment {
    private Payment paymentItem;

    public PaymentSuccessDialogFragment(Payment paymentItem){
        this.paymentItem = paymentItem;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_payment_success, null);

        //set the payment recipient for the dialog
        TextView paymentRecipient = view.findViewById(R.id.payment_recipient_value);
        String recipient = paymentItem.getPaymentUserName();
        paymentRecipient.setText(recipient);

        //set the payment amount for the dialog
        TextView paymentAmount = view.findViewById(R.id.payment_amount_value);
        String amount = "$" + String.format("%.2f", paymentItem.getPaymentAmount());
        paymentAmount.setText(amount);

        //set the payment date
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        TextView paymentDate = view.findViewById(R.id.payment_date_value);
        String date = simpleDateFormat.format(paymentItem.getPaymentDate());
        paymentDate.setText(date);

        //set the payment time
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        TextView paymentTime = view.findViewById(R.id.payment_time_value);
        String time = simpleDateFormat.format(paymentItem.getPaymentDate());
        paymentTime.setText(time);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent i = new Intent(getActivity(), HomeActivity2.class);
                        startActivity(i);
                        getActivity().finish();
                        dialog.dismiss();
                    }
                })
                .setView(view);

        return builder.create();
    }

}

