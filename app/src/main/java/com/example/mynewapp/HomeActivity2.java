package com.example.mynewapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.sql.Timestamp;

public class HomeActivity2 extends AppCompatActivity{

    private AppBarConfiguration mAppBarConfiguration;

    private FirebaseAuth auth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user;
    private static final String TABLE_USER = "User";
    private String userID;
    private double currentBalance;
    private String nickName;
    private TextView text_balance;
    private TextView text_name;
    private TextView text_email;
    private String decodedInformation;
    private String qrCodeTimestamp;
    private long qrCodeDateTime;
    private long currentDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_top_up_history, R.id.nav_payment_history)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        View navigationHeaderView = navigationView.getHeaderView(0);
        displayNameAndEmail(navigationHeaderView);
        displayCurrentBalanceForNavigation(navigationHeaderView);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.home_activity2, menu);
//        return true;
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if(intentResult.getContents()!=null){
            decodedInformation = intentResult.getContents();
            String[] arrOfDecodedInformation = decodedInformation.split("\\|");

            // Check the timestamp of the QR code to see if it is valid or expired
            // If the QR code content consist more than or less than 2 parts, it is corrupted and invalid, else proceeds to the checking of timestamp
            if(arrOfDecodedInformation.length == 2){
                qrCodeTimestamp = arrOfDecodedInformation[1];
                qrCodeDateTime = Long.parseLong(qrCodeTimestamp);
                Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
                currentDateTime = currentTimestamp.getTime();

                // Compare the timestamp on the QR code with the current time
                // If the time difference is <= 30 secs, proceeds the payment process, else alert the users that the QR code has expired
                if(currentDateTime - qrCodeDateTime >= 0 && currentDateTime - qrCodeDateTime<= 30000){
                    Intent i = new Intent(HomeActivity2.this, PayPaymentWifiActivity.class);
                    i.putExtra("DecodedInformation", arrOfDecodedInformation[0]);
                    startActivity(i);
                }else{
                    Toast.makeText(this, "QR code expired. Please refresh the QR code.", Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(this, "Invalid QR code", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    @Override
    public void onBackPressed() {
        // Leave empty to prevent users from returning to login page by back press, and force them to sign out using the Logout button
    }

    public void displayCurrentBalanceForNavigation(View navigationHeaderView){
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if(user != null && navigationHeaderView != null) {
            userID = user.getUid();

            text_balance = navigationHeaderView.findViewById(R.id.text_nav_header_balance);
            db.collection(TABLE_USER).document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        User currentUser = documentSnapshot.toObject(User.class);
                        currentBalance = currentUser.getBalance();
                        String balance = String.format("%.2f", currentBalance);
                        balance = "$" + balance;

                        text_balance.setText(balance);
                    }
                }
            });
        }
    }

    public void displayNameAndEmail(View navigationHeaderView){
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if(user != null && navigationHeaderView != null) {
            userID = user.getUid();

            text_name = navigationHeaderView.findViewById(R.id.text_nav_header_username);
            text_email = navigationHeaderView.findViewById(R.id.text_nav_header_email);
            db.collection(TABLE_USER).document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        User currentUser = documentSnapshot.toObject(User.class);
                        nickName = currentUser.getName();

                        text_name.setText(nickName);
                    }
                }
            });
            text_email.setText(user.getEmail());
        }
    }


    public void onLogoutClickListener(@NonNull MenuItem item){
        FirebaseAuth.getInstance().signOut();
        Intent i = new Intent(HomeActivity2.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}