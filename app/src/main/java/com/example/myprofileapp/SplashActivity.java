package com.example.myprofileapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String name = sharedPreferences.getString("name", null);
            String email = sharedPreferences.getString("email", null);

            Intent intent;
            if (name != null && email != null) {
                // If user data exists, navigate to ProfileActivity
                intent = new Intent(SplashActivity.this, ProfileActivity.class);
            } else {
                // Otherwise, navigate to RegistrationActivity
                intent = new Intent(SplashActivity.this, RegistrationActivity.class);
            }

            startActivity(intent);
            finish();
        }, 3000); // 3 seconds delay
    }
}
