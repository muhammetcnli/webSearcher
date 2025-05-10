package com.example.websearcher.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.websearcher.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase Authentication başlat
        mAuth = FirebaseAuth.getInstance();

        // XML view'larını bağla
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> attemptLogin());

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Lütfen e-posta ve şifre giriniz.", Toast.LENGTH_SHORT).show();
            return;
        }

        loginUser(email, password);
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            verifyUserInDatabase(user.getUid());
                        } else {
                            Toast.makeText(this, "Kullanıcı bilgisi alınamadı.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Giriş başarısız.";
                        Toast.makeText(this, "Giriş hatası: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void verifyUserInDatabase(@NonNull String uid) {
        FirebaseDatabase.getInstance().getReference("users").child(uid)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        Toast.makeText(this, "Giriş başarılı!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Kullanıcı bilgileri veritabanında bulunamadı.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}