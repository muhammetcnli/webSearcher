package com.example.websearcher.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.websearcher.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText etFirstName, etLastName, etEmail, etPassword, etConfirmPassword;
    Button btnRegister;

    FirebaseAuth mAuth;
    DatabaseReference mDatabase; // Realtime DB referansı

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference(); // "root" referansı

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etRegisterEmail);
        etPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etRegisterConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()
                    || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Şifreler uyuşmuyor", Toast.LENGTH_SHORT).show();
            } else {
                // Kullanıcıyı Firebase Auth ile oluştur
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                String userId = user.getUid(); // Benzersiz kullanıcı ID’si

                                // Kullanıcı bilgileri
                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("firstName", firstName);
                                userMap.put("lastName", lastName);
                                userMap.put("email", email);

                                // Realtime Database’e yaz: /users/userId
                                mDatabase.child("users").child(userId).setValue(userMap)
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(this, "Kayıt başarılı!", Toast.LENGTH_SHORT).show();
                                            finish(); // Giriş ekranına dönebilir
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Veritabanına kayıt hatası: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Toast.makeText(this, "Kayıt sırasında hata: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}
