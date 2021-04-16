package com.example.mynewapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;

public class PaymentAuthenticationTask extends AsyncTask<Void, Void, Void> {
    private PayPaymentWifiActivity payPaymentWifiActivity;
    private ProgressDialog authenticationLoadingMessage;

    public PaymentAuthenticationTask(PayPaymentWifiActivity payPaymentWifiActivity, ProgressDialog authenticationLoadingMessage){
        this.payPaymentWifiActivity = payPaymentWifiActivity;
        this.authenticationLoadingMessage = authenticationLoadingMessage;
    }

    protected void onPreExecute() {
        authenticationLoadingMessage.show(payPaymentWifiActivity, "Payment Authentication", "Loading..", false, false);
    }
    protected Void doInBackground(Void... JSONArray) {
        try {
            // Sleep while the firestore is retreiving the data
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(Void unused) {
        //dismiss the progressdialog
        authenticationLoadingMessage.dismiss();
    }
}
