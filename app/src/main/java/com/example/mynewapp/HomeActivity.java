package com.example.mynewapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.client.android.BeepManager;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.sql.Timestamp;

public class HomeActivity extends AppCompatActivity {

    private Button topUpButton;
    private Button payButton;
    private Button receiveButton;
    private TextView text_balance;

    private FirebaseAuth auth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user;
    private static final String TABLE_USER = "User";
    private static final String FIELD_BALANCE = "balance";
    private String userID;
    private int currentBalance;

    private String decodedInformation;
    private String qrCodeTimestamp;
    private long currentDateTime;
    private long qrCodeDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        displayCurrentBalance();
        topUpClickListener();
        payClickListener();
        receiveClickListener();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if(intentResult.getContents()!=null){
            decodedInformation = intentResult.getContents();

            Toast.makeText(this, decodedInformation, Toast.LENGTH_LONG).show();
            Intent i = new Intent(HomeActivity.this, PayPaymentWifiActivity.class);
            i.putExtra("PaymentInformation", decodedInformation);
            startActivity(i);

//            String[] arrOfDecodedInformation = decodedInformation.split("\\|");
//
//            // Check the timestamp of the QR code to see if it is valid or expired
//            // If the QR code content consist more than or less than 2 parts, it is corrupted and invalid, else proceeds to the checking of timestamp
//            if(arrOfDecodedInformation.length == 2){
//                qrCodeTimestamp = arrOfDecodedInformation[1];
//                qrCodeDateTime = Long.parseLong(qrCodeTimestamp);
//                Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
//                currentDateTime = currentTimestamp.getTime();
//
//                // Compare the timestamp on the QR code with the current time
//                // If the time difference is <= 30 secs, proceeds the payment process, else alert the users that the QR code has expired
//                if(currentDateTime - qrCodeDateTime >= 0 && currentDateTime - qrCodeDateTime<= 30000){
//                    Toast.makeText(this, decodedInformation, Toast.LENGTH_LONG).show();
//                    Intent i = new Intent(HomeActivity.this, PayPaymentWifiActivity.class);
//                    i.putExtra("PaymentInformation", decodedInformation);
//                    startActivity(i);
//                }else{
//                    Toast.makeText(this, "QR code expired. Please refresh the QR code.", Toast.LENGTH_LONG).show();
//                }
//            }else{
//                Toast.makeText(this, "Invalid QR code", Toast.LENGTH_SHORT).show();
//            }
        }
    }

    @Override
    public void onBackPressed() {
        // Leave empty to prevent users from returning to login page by back press, and force them to sign out using the Logoff button
    }

    public void displayCurrentBalance(){
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userID = user.getUid();

        text_balance = findViewById(R.id.text_acc_balance_value);
        db.collection(TABLE_USER).document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    currentBalance = documentSnapshot.getLong(FIELD_BALANCE).intValue();
                    String balance = String.valueOf(currentBalance);

                    text_balance.setText(balance);
                }
            }
        });
    }

    public void topUpClickListener(){
        topUpButton = findViewById(R.id.topup_button);

        topUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeActivity.this, TopUpActivity.class);
                startActivity(i);
            }
        });
    }

    public void payClickListener(){
        payButton = findViewById(R.id.payButton);

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(HomeActivity.this);
                integrator.setPrompt("Scan a QR code");
                integrator.setOrientationLocked(false);
                integrator.initiateScan();
            }
        });

    }

    public void receiveClickListener(){
        receiveButton = findViewById(R.id.receiveButton);

        receiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeActivity.this, ReceivePaymentQrActivity.class);
                startActivity(i);
            }
        });

    }
}