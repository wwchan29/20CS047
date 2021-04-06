package com.example.mynewapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.sql.Timestamp;

public class ReceivePaymentQrActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;

    private ImageView qrCodeView;
    private Button refreshQrCode;

    private String userId;
    private String currentDateTime;
    private String encodeInformation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_payment_qr);

        generateQrCode();
        refreshOnClickListener();
    }

    public void generateQrCode(){
        qrCodeView = findViewById(R.id.qrcode);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userId = user.getUid();

        QRCodeWriter writer = new QRCodeWriter();
        try {
            // Add timestamp to the QR code and encode it with the user ID
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            currentDateTime = String.valueOf(currentTime.getTime());
            encodeInformation = userId + "|" + currentDateTime;

            BitMatrix matrix = writer.encode(encodeInformation, BarcodeFormat.QR_CODE, 300, 300);
            Bitmap bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.RGB_565);

            for (int x = 0;x <300;x++){
                for(int y= 0;y <300;y++){
                    if(matrix.get(x,y) == true){
                        bitmap.setPixel(x,y, Color.BLACK);
                    }else{
                        bitmap.setPixel(x,y, Color.WHITE);
                    }
                }
            }

            if(bitmap != null){
                qrCodeView.setImageBitmap(bitmap);
            }else{
                Toast.makeText(this, "Fail to generate the QR code, please retry by click Refresh", Toast.LENGTH_SHORT).show();
            }

        } catch (WriterException e) {
            e.printStackTrace();
        }

    }

    public void refreshOnClickListener(){
        refreshQrCode = findViewById(R.id.refreshQrButton);

        refreshQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateQrCode();
            }
        });
    }
}