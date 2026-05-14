package com.example.padelandmore;

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

import com.example.padelandmore.network.Backend;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword;
    Button btnEntrar;
    ImageView btnVerPassword;
    TextView txtIrRegistro;

    GoogleSignInClient googleClient;
    private static final int RC_GOOGLE = 3000;

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

        configurarGoogle();

        btnEntrar.setOnClickListener(v -> loginManual());
        btnVerPassword.setOnClickListener(v -> togglePassword());
        txtIrRegistro.setOnClickListener(v -> irARegistro());
    }

    private void configurarGoogle() {
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();

        googleClient = GoogleSignIn.getClient(this, gso);
    }

    private void iniciarGoogle() {
        Intent intent = googleClient.getSignInIntent();
        startActivityForResult(intent, RC_GOOGLE);
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

        Backend.login(this, body, resp -> comprobarEquipoYRedirigir(),
                t -> Toast.makeText(this, "Error login: " + t.getMessage(), Toast.LENGTH_SHORT).show());
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                if (account == null) {
                    Toast.makeText(this, "Google cancelado", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> body = new HashMap<>();
                body.put("nombre", account.getDisplayName());
                body.put("email", account.getEmail());
                body.put("fotoUrl", account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null);
                body.put("google", true);

                Backend.register(this, body, resp -> {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }, t -> Toast.makeText(this, "Error auth Google: " + t.getMessage(), Toast.LENGTH_SHORT).show());

            } catch (ApiException e) {
                Toast.makeText(this, "Google sign in error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void comprobarEquipoYRedirigir() {
        if (Backend.getCurrentUid(this) == null) {
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
