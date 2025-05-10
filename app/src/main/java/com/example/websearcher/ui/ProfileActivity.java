package com.example.websearcher.ui;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.websearcher.R;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imageViewProfile;
    private TextView textViewName, textViewEmail, textViewBio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imageViewProfile = findViewById(R.id.imageViewProfile);
        textViewName = findViewById(R.id.textViewName);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewBio = findViewById(R.id.textViewBio);

        // Varsayılan kullanıcı bilgileri
        textViewName.setText("Misafir Kullanıcı");
        textViewEmail.setText("misafir@example.com");
        textViewBio.setText("Henüz bir biyografi eklenmedi.");
        imageViewProfile.setImageResource(R.drawable.ic_profile);
    }
}
