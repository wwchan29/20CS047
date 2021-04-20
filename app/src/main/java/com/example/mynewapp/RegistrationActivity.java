package com.example.mynewapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class RegistrationActivity extends AppCompatActivity {

    FirebaseAuth auth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user;
    private static final String TABLE_USER = "User";
    private String userID;
    private ArrayList<TopUp> listOfTopUp;
    private ArrayList<Payment> listOfPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Setup return button on action bar
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_icon);
        }

        // Listen for the click event of the "Submit" button
        submitOnClickListener();
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

    @Override
    public void onBackPressed() {
        // Leave empty to prevent users from returning to previous page by back press
    }

    public void submitOnClickListener(){
        Button submit = findViewById(R.id.submit_register);
        EditText nickname = findViewById(R.id.editTextNickname);
        EditText email = findViewById(R.id.editTextTextEmailAddress);
        EditText password = findViewById(R.id.editTextTextPassword);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validRegistrationInfo()) {
                    String nicknameUsed = nickname.getText().toString();
                    String emailAddress = email.getText().toString();
                    String passwordUsed = password.getText().toString();

                    // Call the createUserWithEmailAndPassword Api from Firebase Auth, with email address and password input by user as parameters
                    auth = FirebaseAuth.getInstance();
                    auth.createUserWithEmailAndPassword(emailAddress, passwordUsed)
                            .addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(RegistrationActivity.this, "Registration Success", Toast.LENGTH_LONG ).show();
                                        user = auth.getCurrentUser();
                                        userID = user.getUid();
                                        listOfTopUp = new ArrayList<>();
                                        listOfPayment = new ArrayList<>();

                                        // Initialize a new User object once the registration is success
                                        User newUser = new User(nicknameUsed, 0, listOfTopUp, listOfPayment);

                                        //Store the new User object into the Firebase FireStore db and direct the user back to Login page
                                        db.collection(TABLE_USER).document(userID).set(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Intent i = new Intent(RegistrationActivity.this, MainActivity.class);
                                                startActivity(i);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(RegistrationActivity.this, "Failed to add New User Profile", Toast.LENGTH_LONG ).show();
                                            }
                                        });

                                    } else{
                                        // Handle exception cases for registration and pop up corresponding error dialog/message
                                            // Exception(1): Email address/domain invalid
                                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                            alertInvalidEmailDialog();
                                            // Exception(2): Email is already registered
                                        }else if (task.getException() instanceof FirebaseAuthUserCollisionException){
                                            alertEmailRegisteredDialog();
                                        }else{
                                            Toast.makeText(RegistrationActivity.this, "Registration failed", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }
                            });
                }
            }
        });
    }

    public Boolean validRegistrationInfo(){
        EditText nickname = findViewById(R.id.editTextNickname);
        EditText email = findViewById(R.id.editTextTextEmailAddress);
        EditText password = findViewById(R.id.editTextTextPassword);
        boolean isValid = true;

        // Validate if the registration info input by user is empty
        if(TextUtils.isEmpty(nickname.getText().toString())){
            nickname.setError("Nickname cannot be empty");
            isValid = false;
        }

        if(TextUtils.isEmpty(email.getText().toString())){
            email.setError("Email Address cannot be empty");
            isValid = false;
        }

        if(TextUtils.isEmpty(password.getText().toString())){
            password.setError("Password cannot be empty");
            isValid = false;
        }

        // Validate the length of password chose by the user
        if(password.getText().length() < 6){
            password.setError("Password must be at least 6 characters long");
            isValid = false;
        }

        return isValid;
    }

    public void alertEmailRegisteredDialog(){
        AlertDialog.Builder loginAlert = new AlertDialog.Builder(RegistrationActivity.this);
        loginAlert.setCancelable(false)
                .setTitle("Registration Failed")
                .setMessage("The Email Address is already registered. Please use another Email address.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        // Set the title color for the dialog
        AlertDialog EmailRegisteredDialog = loginAlert.create();
        EmailRegisteredDialog.show();
        TextView dialogTitle = EmailRegisteredDialog.findViewById(getResources().getIdentifier( "alertTitle", "id", this.getPackageName()));
        if(dialogTitle != null) {
            dialogTitle.setTextColor(Color.parseColor("#FF5722"));
        }
    }

    public void alertInvalidEmailDialog(){
        AlertDialog.Builder loginAlert = new AlertDialog.Builder(RegistrationActivity.this);
        loginAlert.setCancelable(false)
                .setTitle("Registration Failed")
                .setMessage("The Email Address is invalid. Please use another Email address.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        // Set the title color for the dialog
        AlertDialog invalidEmailDialog = loginAlert.create();
        invalidEmailDialog.show();
        TextView dialogTitle = invalidEmailDialog.findViewById(getResources().getIdentifier( "alertTitle", "id", this.getPackageName()));
        if(dialogTitle != null) {
            dialogTitle.setTextColor(Color.parseColor("#FF5722"));
        }
    }


}