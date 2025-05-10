package com.example.websearcher.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.websearcher.R;
import com.example.websearcher.model.User;  // Kullanıcı sınıfını import et

public class RegisterActivity extends AppCompatActivity {

    EditText etFirstName, etLastName, etEmail, etPassword, etConfirmPassword;
    Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // XML dosyasına bağlanıyoruz

        // XML'deki öğeleri tanımlıyoruz
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etRegisterEmail);
        etPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etRegisterConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        // Kayıt ol butonuna tıklanma olayını tanımlıyoruz
        btnRegister.setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // Verileri kontrol et
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()
                    || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this,getString(R.string.Fields), Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(this, getString(R.string.Password_match), Toast.LENGTH_SHORT).show();
            } else {
                // Kullanıcı nesnesini oluştur
                User user = new User(firstName, lastName, email, password);

                // Kayıt başarılı mesajı
                Toast.makeText(this, getString(R.string.Register_successful), Toast.LENGTH_SHORT).show();

                // Kullanıcı bilgisini yazdır (Veritabanına ekleme işlemi yapılabilir)
                System.out.println("New user: " + user.getFullName() + " | Email: " + user.getEmail());

                finish(); // Kayıt sonrası Login ekranına dön
            }
        });
    }
}
