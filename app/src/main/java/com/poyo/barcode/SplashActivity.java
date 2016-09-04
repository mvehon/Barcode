package com.poyo.barcode;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

//Splash screen that appears very quickly before MainActivity called
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}