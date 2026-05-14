package com.example.tmanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tmanager.network.Backend;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword;
    Button btnEntrar;
    ImageView btnVerPassword;
    TextView txtIrRegistro;

    boolean passwordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnEntrar = findViewById(R.id.btnEntrar);
        btnVerPassword = findViewById(R.id.btnVerPassword);
        txtIrRegistro = findViewById(R.id.txtIrRegistro);

        btnEntrar.setOnClickListener(v -> loginManual());
        btnVerPassword.setOnClickListener(v -> togglePassword());
        txtIrRegistro.setOnClickListener(v -> irARegistro());
    }

    private void loginManual() {
        String mail = edtEmail.getText().toString().trim();
        String pass = edtPassword.getText().toString().trim();

        if (mail.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("email", mail);
        body.put("password", pass);

        Backend.login(this, body,
            resp -> {
                // Login OK → ir a MainActivity
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            },
            t -> Toast.makeText(this,
                    "Error al iniciar sesión: " + t.getMessage(),
                    Toast.LENGTH_LONG).show()
        );
    }

    private void togglePassword() {
        if (passwordVisible) {
            edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnVerPassword.setImageResource(R.drawable.ic_eye_closed);
        } else {
            edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnVerPassword.setImageResource(R.drawable.ic_eye_open);
        }
        edtPassword.setSelection(edtPassword.getText().length());
        passwordVisible = !passwordVisible;
    }

    private void irARegistro() {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }
}
