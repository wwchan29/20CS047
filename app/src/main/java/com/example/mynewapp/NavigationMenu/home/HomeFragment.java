package com.example.mynewapp.NavigationMenu.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mynewapp.PayPaymentWifiActivity;
import com.example.mynewapp.R;


import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;

import com.example.mynewapp.ReceivePaymentQrActivity;
import com.example.mynewapp.TopUpActivity;
import com.example.mynewapp.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

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

    private User currentUser;
    private double currentBalance;

    private String decodedInformation;
    private String qrCodeTimestamp;
    private long currentDateTime;
    private long qrCodeDateTime;

    protected View homeView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.activity_home, container, false);

        homeView = root;
        displayCurrentBalance();
        topUpClickListener();
        payClickListener();
        receiveClickListener();

        return root;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if(intentResult.getContents()!=null){
            decodedInformation = intentResult.getContents();

            Toast.makeText(getActivity(), decodedInformation, Toast.LENGTH_LONG).show();
            Intent i = new Intent(getActivity(), PayPaymentWifiActivity.class);
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

    public void displayCurrentBalance(){
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userID = user.getUid();

        text_balance = homeView.findViewById(R.id.text_acc_balance_value);
        db.collection(TABLE_USER).document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    User currentUser = documentSnapshot.toObject(User.class);
                    currentBalance = currentUser.getBalance();
                    String balance = String.format("%.2f", currentBalance);
                    balance = "$" + balance;

                    text_balance.setText(balance);
                }
            }
        });
    }

    public void topUpClickListener(){
        topUpButton = homeView.findViewById(R.id.topup_button);

        topUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), TopUpActivity.class);
                startActivity(i);
            }
        });
    }

    public void payClickListener(){
        payButton = homeView.findViewById(R.id.payButton);

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(getActivity());
                integrator.setPrompt("Scan a QR code");
                integrator.setOrientationLocked(true);
                integrator.initiateScan();
            }
        });

    }

    public void receiveClickListener(){
        receiveButton = homeView.findViewById(R.id.receiveButton);

        receiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), ReceivePaymentQrActivity.class);
                startActivity(i);
            }
        });

    }





}