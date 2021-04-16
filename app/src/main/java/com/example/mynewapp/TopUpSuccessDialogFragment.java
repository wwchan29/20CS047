package com.example.mynewapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;

public class TopUpSuccessDialogFragment extends DialogFragment {
    private TopUp topUpItem;

    public TopUpSuccessDialogFragment(TopUp topUpItem){
        this.topUpItem = topUpItem;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_topup_success, null);

        //set the top up transaction time for the dialog
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM d, yyyy HH:mm:ss");
        TextView topUpDate = view.findViewById(R.id.top_up_date_value);
        String date = simpleDateFormat.format(topUpItem.getTopUpDate());
        topUpDate.setText(date);

        //set the top up amount for the dialog
        TextView topUpAmount = view.findViewById(R.id.top_up_amount_value);
        String amount = "$" + String.format("%.2f", topUpItem.getTopUpAmount());
        topUpAmount.setText(amount);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .setView(view);

        return builder.create();
    }

}
