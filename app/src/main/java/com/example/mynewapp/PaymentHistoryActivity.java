package com.example.mynewapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class PaymentHistoryActivity extends AppCompatActivity {
    private ListView paymentList;
    private ArrayList<String> payment_list = new ArrayList<>();
    String a = "Payment 1";
    String b = "Payment 2";
    String c = "Payment 3";
    String d = "Payment 4";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_history);

        listPaymentHistory();
    }

    public void listPaymentHistory(){
        payment_list.add(a);
        payment_list.add(b);
        payment_list.add(c);
        payment_list.add(d);
        paymentList = findViewById(R.id.paymentList);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_payment_history, payment_list);
        paymentList.setAdapter(adapter);

        paymentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(PaymentHistoryActivity.this, "Position " + position + ": " +
                        paymentList.getItemAtPosition(position).toString(), Toast.LENGTH_LONG).show();
            }
        });
    }
}