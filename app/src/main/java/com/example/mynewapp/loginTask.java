package com.example.mynewapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;

public class loginTask extends AsyncTask<Void, Void, Void> {
    private ProgressDialog loadingMessage;
    private MainActivity mainActivity;

    public loginTask(MainActivity mainActivity, ProgressDialog loadingMessage){
        this.mainActivity = mainActivity;
        this.loadingMessage = loadingMessage;
    }

    protected void onPreExecute() {
        loadingMessage.show(mainActivity, "Login", "Loading..", false, false);
    }

    protected Void doInBackground(Void... JSONArray) {
        Intent i = new Intent(mainActivity, HomeActivity.class);
        mainActivity.startActivity(i);
        return null;
    }

    protected void onPostExecute(Void unused) {
        //dismiss the progressdialog
        loadingMessage.dismiss();
    }
}