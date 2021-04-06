package com.example.mynewapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.graphics.Color;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private ProgressDialog loadingMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerOnClickListener();
        loginOnClickListener();
    }

    public void loginOnClickListener(){
        Button login = findViewById(R.id.loginButton);
        EditText email = findViewById(R.id.username);
        EditText password = findViewById(R.id.password);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText email = findViewById(R.id.username);
                EditText password = findViewById(R.id.password);
                String loginEmail = email.getText().toString();
                String loginPassword = password.getText().toString();

                AlertDialog.Builder loginAlert = new AlertDialog.Builder(MainActivity.this);
                loginAlert.setCancelable(false)
                        .setMessage("Are you sure to login?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(checkEmptyUserNameAndPw()){
                                    Toast.makeText(MainActivity.this, "Please input the correct Username and Password", Toast.LENGTH_SHORT).show();
                                }

//                                if(!checkUserAgreement()){
//                                    Toast.makeText(MainActivity.this, "Please agree the terms to continue", Toast.LENGTH_SHORT).show();
//                                }

                                if(!checkEmptyUserNameAndPw()) {
                                    auth.signInWithEmailAndPassword(loginEmail, loginPassword)
                                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if(task.isSuccessful()){
                                                        loadingMessage = new ProgressDialog(MainActivity.this);
                                                        new loginTask().execute();
                                                    }else{
                                                        Toast.makeText(MainActivity.this, "Invalid Email address or password", Toast.LENGTH_SHORT ).show();
                                                    }
                                                }
                                            });
                                }
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog loginDialog = loginAlert.create();
                loginDialog.show();

            }
        });

    }

    public void registerOnClickListener(){
        Button register = findViewById(R.id.signUp);

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

        if(TextUtils.isEmpty(username.getText().toString())){
            username.setError("Username cannot be empty");
            isEmpty = true;
        }

        if(TextUtils.isEmpty(password.getText().toString())){
            password.setError("Password cannot be empty");
            isEmpty = true;
        }

        return isEmpty;
    }

    public Boolean checkUserAgreement(){
        CheckBox userAgreement = findViewById(R.id.UserAgreement);
        if (userAgreement.isChecked()){
            return true;
        }else{
            return false;
        }
    }

    public void onSpinnerItemSelected(MainActivity view){
        Spinner spinner = findViewById(R.id.spinner);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    public void onClickUserType(View view){
        Spinner spinner = findViewById(R.id.spinner);
        String userType = spinner.getSelectedItem().toString();
    }

    private class loginTask extends AsyncTask<Void, Void, Void>
    {
        protected void onPreExecute() {
            loadingMessage.show(MainActivity.this, "Login", "Loading..", false, false);
        }
        protected Void doInBackground(Void... JSONArray) {
            Intent i = new Intent(MainActivity.this, HomeActivity2.class);
            startActivity(i);
            return null;
        }

        protected void onPostExecute(Void unused) {
            //dismiss the progressdialog
            loadingMessage.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        // Close the app and return to home screen
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}