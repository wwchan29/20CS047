package com.example.mynewapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class ReceivePaymentQrActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;

    private ImageView qrCodeView;
    private Button refreshQrCode;
    private Button buttonProceed;

    private String userId;
    private String currentDateTime;
    private String encodeInformation;

    private KeyGenerator aesKeyGenerator;
    private SecretKey key;
    private Cipher cipher;
    private byte[] iv;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_payment_qr);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_icon);

        try {
            generateQrCode();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        refreshOnClickListener();
        proceedOnClickListener();
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void generateQrCode() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        qrCodeView = findViewById(R.id.qrcode);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userId = user.getUid();

        //Encrypt the userId using AES-128 CBC mode with PKCS#5 Padding
        initializeAesKeyGenerator();
        aesKeyGenerator.init(128);
        key = aesKeyGenerator.generateKey();
        initializeCbcCipher();
        initializeEncryptionMode();
        byte[] byteUserId = Base64.getDecoder().decode(userId);
        byte[] encryptedUserIdInByte = cipher.doFinal(byteUserId);
        iv = cipher.getIV();
        String encryptedUserIdInString = Base64.getEncoder().encodeToString(encryptedUserIdInByte);
        System.out.println(encryptedUserIdInString);

        //Decryption of the userId
        IvParameterSpec ivps = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivps);
        byte[] bytedeUserId = Base64.getDecoder().decode(encryptedUserIdInString);
        byte[] decryptedUserIdInByte = cipher.doFinal(bytedeUserId);
        String decryptedUserIdInString = Base64.getEncoder().encodeToString(decryptedUserIdInByte);
        System.out.println(decryptedUserIdInString);

        QRCodeWriter writer = new QRCodeWriter();
        try {
            // Add timestamp to the QR code and encode it with the user ID
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            currentDateTime = String.valueOf(currentTime.getTime());
            encodeInformation = encryptedUserIdInString + "|" + currentDateTime;

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
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                try {
                    generateQrCode();
                } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void proceedOnClickListener(){
        buttonProceed = findViewById(R.id.button_receive_wifi);
        String role = "Receiver";

        buttonProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ReceivePaymentQrActivity.this, PayPaymentWifiActivity.class);
                i.putExtra("Key", key);
                i.putExtra("IV", iv);
                i.putExtra("Role", role);
                startActivity(i);
            }
        });
    }

    private void initializeAesKeyGenerator() throws NoSuchAlgorithmException {
        aesKeyGenerator = KeyGenerator.getInstance("AES");
    }

    private void initializeCbcCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
    }

    private void initializeEncryptionMode() throws InvalidKeyException {
        cipher.init(Cipher.ENCRYPT_MODE, key);
    }
}