package com.example.myprofileapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {
    private ShapeableImageView profileImageView;
    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize UI components
        profileImageView = findViewById(R.id.imageViewProfile);
        TextView nameTextView = findViewById(R.id.textViewName);
        TextView emailTextView = findViewById(R.id.textViewEmail);
        Button editProfileButton = findViewById(R.id.editProfileButton);
        Button openWebButton = findViewById(R.id.openWebButton);

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String profilePicPath = sharedPreferences.getString("profilePicPath", null);
        String name = sharedPreferences.getString("name", "Name not found");
        String email = sharedPreferences.getString("email", "Email not found");

        // Display profile name and email
        nameTextView.setText(name);
        emailTextView.setText(email);

        // Load saved profile picture
        loadProfilePicture(profilePicPath);

        // Set up image picker
        ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        if (checkPermission()) {
                            String savedPath = saveImageToInternalStorage(uri);
                            if (savedPath != null) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("profilePicPath", savedPath);
                                editor.apply();
                                loadProfilePicture(savedPath); // Force reload
                            }
                        } else {
                            requestPermission();
                        }
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

    private void loadProfilePicture(String profilePicPath) {
        if (profilePicPath != null) {
            File file = new File(profilePicPath);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(profilePicPath);
                profileImageView.setImageBitmap(bitmap); // Use Bitmap to ensure refresh
            } else {
                Toast.makeText(this, "Profile picture not found.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String saveImageToInternalStorage(Uri imageUri) {
        try {
            ContentResolver resolver = getContentResolver();
            InputStream inputStream = resolver.openInputStream(imageUri);
            File directory = getFilesDir();
            // Create a unique file name to ensure file path changes
            String fileName = "profile_picture_" + System.currentTimeMillis() + ".jpg";
            File file = new File(directory, fileName);

            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();

            return file.getAbsolutePath(); // Return the path to save in SharedPreferences
        } catch (Exception e) {
            Toast.makeText(this, "Failed to save image.", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private boolean checkPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // For Android 13+ (API level 33)
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Request READ_MEDIA_IMAGES for Android 13+
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    PERMISSION_REQUEST_CODE);
        } else {
            // Request READ_EXTERNAL_STORAGE for Android 12 and below
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
