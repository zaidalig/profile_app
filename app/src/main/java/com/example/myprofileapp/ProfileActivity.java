package com.example.myprofileapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.imageview.ShapeableImageView;

public class ProfileActivity extends AppCompatActivity {
    private ShapeableImageView profileImageView;
    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImageView = findViewById(R.id.imageViewProfile);
        Button editProfileButton = findViewById(R.id.editProfileButton);
        Button openWebButton = findViewById(R.id.openWebButton);

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String profilePicUri = sharedPreferences.getString("profilePicUri", null);
// Initialize TextView for name and email
        TextView nameTextView = findViewById(R.id.textViewName);
        TextView emailTextView = findViewById(R.id.textViewEmail);

        // Retrieve profile information from SharedPreferences
        String name = sharedPreferences.getString("name", "Name not found");
        String email = sharedPreferences.getString("email", "Email not found");

        // Display profile name and email
        if (!name.equals("Name not found") && !email.equals("Email not found")) {
            nameTextView.setText(name);
            emailTextView.setText(email);
        } else {
            Toast.makeText(this, "Profile information not available.", Toast.LENGTH_SHORT).show();
        }
        // Load saved profile picture
        loadProfilePicture(profilePicUri);

        // Set up image picker
        ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        profileImageView.setImageURI(uri); // Display the selected image
                        saveProfilePictureUri(sharedPreferences, uri); // Save the URI
                    }
                });

        profileImageView.setOnClickListener(v -> {
            if (checkPermission()) {
                pickImageLauncher.launch("image/*");
            } else {
                requestPermission();
            }
        });

        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });

        openWebButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, WebActivity.class);
            startActivity(intent);
        });
    }

    private void loadProfilePicture(String profilePicUri) {
        if (profilePicUri != null) {
            try {
                Uri uri = Uri.parse(profilePicUri);
                profileImageView.setImageURI(uri); // Attempt to set the image
            } catch (Exception e) {
                Toast.makeText(this, "Unable to load profile picture.", Toast.LENGTH_SHORT).show();
                profileImageView.setImageResource(R.drawable.profile_placeholder_icon); // Set placeholder
            }
        } else {
            profileImageView.setImageResource(R.drawable.profile_placeholder_icon); // Default placeholder
        }
    }

    private void saveProfilePictureUri(SharedPreferences sharedPreferences, Uri uri) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("profilePicUri", uri.toString());
        editor.apply();
        Toast.makeText(this, "Profile picture saved.", Toast.LENGTH_SHORT).show();
    }

    private boolean checkPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
