package com.example.mynewapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.graphics.Color;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private ProgressDialog loadingMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Listen for the click event of the "Login" and "Sign Up" button
        registerOnClickListener();
        loginOnClickListener();
    }

    public void loginOnClickListener(){
        Button login = findViewById(R.id.loginButton);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText email = findViewById(R.id.username);
                EditText password = findViewById(R.id.password);
                String loginEmail = email.getText().toString();
                String loginPassword = password.getText().toString();

                if(checkEmptyUserNameAndPw()){
                    Toast.makeText(MainActivity.this, "Please input the correct Username and Password", Toast.LENGTH_SHORT).show();
                }else{
                    // Call the signInWithEmailAndPassword Api by Firebase Auth, with email and password input by user as the parameters
                    auth.signInWithEmailAndPassword(loginEmail, loginPassword)
                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        // Execute the login process as background task and direct user to the Home Page of this application
                                        loadingMessage = new ProgressDialog(MainActivity.this);
                                        new loginTask(MainActivity.this, loadingMessage).execute();
                                    }else{
                                        alertLoginFailedDialog();
                                    }
                                }
                            });
                }
            }
        });

    }

    public void registerOnClickListener(){
        Button register = findViewById(R.id.signUp);

        // Direct the user to the Registration Page when "Sign Up" button is clicked
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, RegistrationActivity.class);
                startActivity(i);
            }
        });
    }

    public Boolean checkEmptyUserNameAndPw(){
        EditText username = findViewById(R.id.username);
        EditText password = findViewById(R.id.password);
        boolean isEmpty = false;

        //If email address or password input by the user is empty, display corresponding error message
        if(TextUtils.isEmpty(username.getText().toString())){
            username.setError("Please enter your Email Address");
            isEmpty = true;
        }

        if(TextUtils.isEmpty(password.getText().toString())){
            password.setError("Please enter your Password");
            isEmpty = true;
        }

        return isEmpty;
    }

    public void alertLoginFailedDialog(){
        // Prepare and show the Login failure dialog
        AlertDialog.Builder loginAlert = new AlertDialog.Builder(MainActivity.this);
        loginAlert.setCancelable(false)
                .setTitle("Login Failed")
                .setMessage("Invalid Email Address or Password. Please try again.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        // Set the title color for the dialog
        AlertDialog loginFailedDialog = loginAlert.create();
        loginFailedDialog.show();
        TextView dialogTitle = loginFailedDialog.findViewById(getResources().getIdentifier( "alertTitle", "id", this.getPackageName()));
        if(dialogTitle != null) {
            dialogTitle.setTextColor(Color.parseColor("#FF5722"));
        }
    }

    @Override
    public void onBackPressed() {
        // Close the app and return to home screen when user press the Back button
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}