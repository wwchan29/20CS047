package com.example.mynewapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class ImageRadioActivity extends AppCompatActivity {

    private ImageView image;
    private Button switchImage;
    private int count = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_radio);

        onRadioButtonSelected();
        onSwitchImage();
    }

    public void onSwitchImage(){
        image = findViewById(R.id.imageView);
        switchImage = findViewById(R.id.switchImage);

        switchImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(count % 2 == 1 ) {
                    image.setImageResource(R.drawable.image2);
                }else{
                    image.setImageResource(R.drawable.image);
                }
                count++;
            }
        });
    }

    public void onRadioButtonSelected(){
        RadioGroup paymentGroup = findViewById(R.id.radioGroup);
        TextView radioText = findViewById(R.id.radioText);

        paymentGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton checkedButton = findViewById(checkedId);
                String option = checkedButton.getText().toString();
                System.out.println(option);
                if(option.equals("Transfer")){
                    radioText.setText("Transfer");
                }else if (option.equals("Top Up")){
                    radioText.setText("Top Uo");
                }else if (option.equals("Receive")){
                    radioText.setText("Receive");
                }

            }
        });

    }
}