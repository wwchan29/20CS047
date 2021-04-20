package com.example.mynewapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.fragment.app.DialogFragment;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;

public class TopUpActivity extends AppCompatActivity {

    private EditText topUpAmount;
    private Button clearButton;
    private Button confirmTopUpButton;

    private FirebaseAuth auth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user;
    private static final String TABLE_USER = "User";
    private String userID;

    private double amountTopUp;
    private double currentBalance;
    private double newBalance;
    private ArrayList<TopUp> currentListOfTopUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_up);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_icon);

        topUpAmountChangedListener();
        confirmTopUpListener();
        clearOnClickListener();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void confirmTopUpListener(){
        confirmTopUpButton = findViewById(R.id.confirm);

        confirmTopUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                topUpAmount = findViewById(R.id.topup_amount);

                // Validate the top up amount input by the user
                try {
                    amountTopUp = Double.valueOf(topUpAmount.getText().toString());
                    if(amountTopUp == 0){
                        topUpAmount.setError("Please input a valid amount");
                        return;
                    }

                    if(amountTopUp > 50000){
                        topUpAmount.setError("The maximum amount for each top-up is $50000");
                        return;
                    }
                }catch(NumberFormatException e){
                    topUpAmount.setError("Please input a valid amount");
                    return;
                }

                // Display the confirmation dialog for the top up
                AlertDialog.Builder confirmTopUpAlert = new AlertDialog.Builder(TopUpActivity.this);
                confirmTopUpAlert.setCancelable(false)
                        .setTitle("Confirmation")
                        .setMessage("Are you sure to top up $" + String.format("%.2f", amountTopUp) + " to your wallet?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
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
                                                    DialogFragment dialogFragment = new TopUpSuccessDialogFragment(topUpItem);
                                                    dialogFragment.show(getSupportFragmentManager(), "topUpSuccess");
                                                    topUpAmount.getText().clear();
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
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                // Set the title color for the dialog
                AlertDialog confirmTopUpDialog = confirmTopUpAlert.create();
                confirmTopUpDialog.show();
                TextView dialogTitle = confirmTopUpDialog.findViewById(getResources().getIdentifier( "alertTitle", "id", TopUpActivity.this.getPackageName()));
                if(dialogTitle != null) {
                    dialogTitle.setTextColor(Color.parseColor("#FF5722"));
                }
            }
        });
    }

//    public boolean checkValidTopUpAmount(){
//        boolean validAmount = true;
//        topUpAmount = findViewById(R.id.topup_amount);
//
//        try {
//            amountTopUp = Double.valueOf(topUpAmount.getText().toString());
//            if(amountTopUp == 0){
//                topUpAmount.setError("Please input a valid amount");
//                validAmount = false;
//            }
//
//            if(amountTopUp > 50000){
//                topUpAmount.setError("The maximum amount for each top-up is $50000");
//                validAmount = false;
//            }
//        }catch(NumberFormatException e){
//            topUpAmount.setError("Please input a valid amount");
//            validAmount = false;
//        }
//
//        return validAmount;
//    }

//    public void alertConfirmTopUpdDialog(String topUpAmount){
//        AlertDialog.Builder loginAlert = new AlertDialog.Builder(TopUpActivity.this);
//        loginAlert.setCancelable(false)
//                .setTitle("Confirmation")
//                .setMessage("Are you sure to top up $" + topUpAmount + " to your wallet?")
//                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        updateBalance();
//                        dialog.dismiss();
//                    }
//                })
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//        AlertDialog loginFailedDialog = loginAlert.create();
//        loginFailedDialog.show();
//        TextView dialogTitle = loginFailedDialog.findViewById(getResources().getIdentifier( "alertTitle", "id", this.getPackageName()));
//        dialogTitle.setTextColor(Color.parseColor("#FF5722"));
//    }

    private void topUpAmountChangedListener(){
        topUpAmount = findViewById(R.id.topup_amount);
        topUpAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().indexOf(".") != -1){
                    if(s.toString().substring(s.toString().indexOf(".")+1).length() > 2){
                        topUpAmount.setText(s.toString().substring(0, s.toString().length()-1));
                        topUpAmount.setSelection(topUpAmount.getText().length());
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
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

}