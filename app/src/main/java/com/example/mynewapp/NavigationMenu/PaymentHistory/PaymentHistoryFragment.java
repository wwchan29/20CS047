package com.example.mynewapp.NavigationMenu.PaymentHistory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.mynewapp.R;

public class PaymentHistoryFragment extends Fragment {

    private PaymentHistoryViewModel paymentHistoryViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        paymentHistoryViewModel =
                new ViewModelProvider(this).get(PaymentHistoryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_payment_history, container, false);
        final TextView textView = root.findViewById(R.id.text_slideshow);
        paymentHistoryViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}