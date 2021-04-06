package com.example.mynewapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Date;

public class TopUpActivity extends AppCompatActivity {

    private EditText topUpAmount;
    private Button clearButton;
    private Button confirmTopUpButton;
    private Button backButton;

    private FirebaseAuth auth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user;
    private static final String TABLE_USER = "User";
    private String userID;

    private int amountTopUp;
    private int currentBalance;
    private int newBalance;
    private ArrayList<TopUp> currentListOfTopUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_up);

        confirmTopUpListener();
        backOnClickListener();
        clearOnClickListener();
    }

    public void confirmTopUpListener(){
        confirmTopUpButton = findViewById(R.id.confirm);

        confirmTopUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                topUpAmount = findViewById(R.id.topup_amount);
                amountTopUp = Integer.parseInt(topUpAmount.getText().toString());
                auth = FirebaseAuth.getInstance();
                user = auth.getCurrentUser();
                userID = user.getUid();

                db.collection(TABLE_USER).document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            // Get balance from current user and update it by adding the top-up amount
                            User currentUser = documentSnapshot.toObject(User.class);
                            currentBalance = currentUser.getBalance();
                            newBalance = currentBalance + amountTopUp;
                            currentUser.setBalance(newBalance);

                            // Initialize a new top-up item with the top-up time and amount, update the top-up history of the current users by adding the item to the list
                            currentListOfTopUp = currentUser.getListOfTopUp();
                            TopUp topUpItem = new TopUp(amountTopUp, new Date());
                            currentListOfTopUp.add(topUpItem);
                            currentUser.setListOfTopUp(currentListOfTopUp);

                            db.collection(TABLE_USER).document(userID).set(currentUser).addOnSuccessListener(new OnSuccessListener<Void>() {

                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(TopUpActivity.this, "Top Up Success", Toast.LENGTH_SHORT ).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(TopUpActivity.this, "Top Up Fail", Toast.LENGTH_SHORT ).show();
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(TopUpActivity.this, "Top Up Fail", Toast.LENGTH_SHORT ).show();
                    }
                });

            }
        });
    }

    public void clearOnClickListener(){
        clearButton = findViewById(R.id.clear);

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                topUpAmount = findViewById(R.id.topup_amount);
                topUpAmount.getText().clear();
            }
        });
    }

    public void backOnClickListener(){
        backButton = findViewById(R.id.back_button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TopUpActivity.this, HomeActivity2.class);
                startActivity(i);
            }
        });
    }

}