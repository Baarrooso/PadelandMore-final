package com.example.padelandmore;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.example.padelandmore.network.Backend;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText edtNombre, edtEmail, edtPassword, edtPassword2;
    ImageView btnGoogle, btnVerPass1, btnVerPass2;
    Button btnRegistrar;
    TextView txtIrLogin;

    TextView rule1, rule2, rule3, rule4, rule5;

    boolean passVisible1 = false;
    boolean passVisible2 = false;

    // auth and db are handled via Backend REST API
    GoogleSignInClient googleClient;

    private static final int RC_GOOGLE = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtNombre = findViewById(R.id.edtNombre);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtPassword2 = findViewById(R.id.edtPassword2);

        btnVerPass1 = findViewById(R.id.btnVerPassword1);
        btnVerPass2 = findViewById(R.id.btnVerPassword2);

        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnGoogle = findViewById(R.id.btnGoogle);
        txtIrLogin = findViewById(R.id.txtIrLogin);

        rule1 = findViewById(R.id.rule1);
        rule2 = findViewById(R.id.rule2);
        rule3 = findViewById(R.id.rule3);
        rule4 = findViewById(R.id.rule4);
        rule5 = findViewById(R.id.rule5);

        // no Firebase: use Backend

        configurarGoogle();
        configurarPasswordListeners();

        btnRegistrar.setOnClickListener(v -> registrarManual());
        btnGoogle.setOnClickListener(v -> iniciarGoogle());
        txtIrLogin.setOnClickListener(v -> irALogin());

        btnVerPass1.setOnClickListener(v -> togglePassword(edtPassword, btnVerPass1, true));
        btnVerPass2.setOnClickListener(v -> togglePassword(edtPassword2, btnVerPass2, false));
    }


    private void configurarPasswordListeners() {
        edtPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                validarPassword(s.toString());
            }
            @Override public void afterTextChanged(Editable editable) {}
        });
    }

    private void validarPassword(String pass) {
        rule1.setTextColor(pass.length() >= 8 ? 0xFF00E676 : 0xFFFFFFFF);
        rule2.setTextColor(pass.matches(".*[A-Z].*") ? 0xFF00E676 : 0xFFFFFFFF);
        rule3.setTextColor(pass.matches(".*[a-z].*") ? 0xFF00E676 : 0xFFFFFFFF);
        rule4.setTextColor(pass.matches(".*[0-9].*") ? 0xFF00E676 : 0xFFFFFFFF);
        rule5.setTextColor(pass.matches(".*[@#_$%^&+=!¿?.-].*") ? 0xFF00E676 : 0xFFFFFFFF);
    }

    private void togglePassword(EditText editText, ImageView icon, boolean first) {
        boolean visible = first ? passVisible1 : passVisible2;

        if (visible) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            icon.setImageResource(R.drawable.ic_eye_closed);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            icon.setImageResource(R.drawable.ic_eye_open);
        }

        editText.setSelection(editText.getText().length());

        if (first) passVisible1 = !passVisible1;
        else passVisible2 = !passVisible2;
    }

    private void configurarGoogle() {
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .requestProfile()
                        .build();

        googleClient = GoogleSignIn.getClient(this, gso);
    }

    private void iniciarGoogle() {
        startActivityForResult(googleClient.getSignInIntent(), RC_GOOGLE);
    }

    private void registrarManual() {

        String nombre = edtNombre.getText().toString().trim();
        String correo = edtEmail.getText().toString().trim();
        String pass = edtPassword.getText().toString().trim();
        String pass2 = edtPassword2.getText().toString().trim();

        if (nombre.isEmpty() || correo.isEmpty() || pass.isEmpty() || pass2.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(pass2)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        // Call backend register
        Map<String, Object> body = new HashMap<>();
        body.put("nombre", nombre);
        body.put("email", correo);
        body.put("password", pass);

        Backend.register(this, body, resp -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }, t -> Toast.makeText(this, "Registro fallido: " + t.getMessage(), Toast.LENGTH_SHORT).show());

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE) {
            try {
                GoogleSignInAccount account =
                        GoogleSignIn.getSignedInAccountFromIntent(data)
                                .getResult(ApiException.class);

                if (account == null) return;

                // Use Google account info and register/login via backend
                String name = account.getDisplayName();
                String email = account.getEmail();
                String fotoGoogle = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null;

                Map<String, Object> body = new HashMap<>();
                body.put("nombre", name);
                body.put("email", email);
                body.put("fotoUrl", fotoGoogle);
                body.put("google", true);

                Backend.register(this, body, resp -> {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }, t -> Toast.makeText(this, "Error Google: " + t.getMessage(), Toast.LENGTH_SHORT).show());

            } catch (Exception e) {
                Toast.makeText(this, "Error Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void irALogin() {
        SessionNavigator.goToLoginClean(this);
    }
}
