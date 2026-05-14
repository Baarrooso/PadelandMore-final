package com.example.padelandmore;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.padelandmore.network.Backend;

import java.util.HashMap;
import java.util.Map;

public class MiInformacionActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 101;

    private ImageView imgPerfil;
    private ImageButton btnRetroceso;
    private EditText edtNombre, edtPassActual, edtPassNueva, edtPassConfirmar;
    private ImageView btnSaveNombre, btnSavePassword;
    private ImageView eyeActual, eyeNueva, eyeConfirmar;
    private TextView txtEmail;
    private Button btnCerrarSesion, btnEliminarCuenta;
    private TextView rule1, rule2, rule3, rule4, rule5;
    private LinearLayout layoutPassword;
    private EditText edtAlias;
    private ImageView btnSaveAlias;
    private LinearLayout layoutAlias;

    private Uri fotoUri;
    private String currentUid;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_mi_informacion);

        currentUid = Backend.getCurrentUid(this);
        if (currentUid == null) {
            SessionNavigator.goToLoginClean(this);
            return;
        }

        btnRetroceso = findViewById(R.id.btnRetroceso);
        imgPerfil = findViewById(R.id.imgPerfil);
        edtNombre = findViewById(R.id.edtNombre);
        txtEmail = findViewById(R.id.txtEmail);
        edtAlias = findViewById(R.id.edtAlias);
        btnSaveAlias = findViewById(R.id.btnSaveAlias);
        layoutAlias = findViewById(R.id.layoutAlias);
        edtPassActual = findViewById(R.id.edtPasswordActual);
        edtPassNueva = findViewById(R.id.edtPasswordNueva);
        edtPassConfirmar = findViewById(R.id.edtPasswordConfirmar);
        btnSaveNombre = findViewById(R.id.btnSaveNombre);
        btnSavePassword = findViewById(R.id.btnSavePassword);
        eyeActual = findViewById(R.id.btnEyeActual);
        eyeNueva = findViewById(R.id.btnEyeNueva);
        eyeConfirmar = findViewById(R.id.btnEyeConfirmar);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnEliminarCuenta = findViewById(R.id.btnEliminarCuenta);
        layoutPassword = findViewById(R.id.layoutPassword);
        rule1 = findViewById(R.id.rule1);
        rule2 = findViewById(R.id.rule2);
        rule3 = findViewById(R.id.rule3);
        rule4 = findViewById(R.id.rule4);
        rule5 = findViewById(R.id.rule5);

        btnRetroceso.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        imgPerfil.setOnClickListener(v -> seleccionarFoto());
        btnSaveNombre.setOnClickListener(v -> confirmarGuardarNombre());
        btnSaveAlias.setOnClickListener(v -> confirmarGuardarAlias());
        btnSavePassword.setOnClickListener(v -> confirmarGuardarPassword());
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
        btnEliminarCuenta.setOnClickListener(v -> confirmarEliminarCuenta());

        configurarOjos();
        configurarValidacionPassword();
        cargarDatos();
    }

    private void seleccionarFoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            fotoUri = data.getData();
            if (fotoUri == null) return;

            Glide.with(this)
                    .load(fotoUri)
                    .circleCrop()
                    .into(imgPerfil);

            Map<String, Object> update = new HashMap<>();
            update.put("fotoUrl", fotoUri.toString());
            update.put("actualizado", System.currentTimeMillis());

            Backend.updateCollectionItem(this, "users", currentUid, update,
                    resp -> toast("Foto de perfil actualizada"),
                    e -> toast("No se pudo guardar la foto"));
        }
    }

    private void confirmarGuardarNombre() {
        String nombre = edtNombre.getText().toString().trim();
        if (nombre.isEmpty()) {
            toast("El nombre no puede estar vacío");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Cambiar nombre")
                .setMessage("¿Deseas guardar este nombre?")
                .setPositiveButton("Guardar", (d, w) -> {
                    Map<String, Object> update = new HashMap<>();
                    update.put("nombre", nombre);
                    update.put("actualizado", System.currentTimeMillis());
                    Backend.updateCollectionItem(this, "users", currentUid, update,
                            resp -> toast("Nombre actualizado"),
                            e -> toast("No se pudo actualizar el nombre"));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarGuardarAlias() {
        String alias = edtAlias.getText().toString().trim();
        if (alias.isEmpty()) {
            toast("El alias no puede estar vacío");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Cambiar alias")
                .setMessage("¿Deseas guardar este alias?")
                .setPositiveButton("Guardar", (d, w) -> {
                    Map<String, Object> update = new HashMap<>();
                    update.put("alias", alias);
                    update.put("actualizado", System.currentTimeMillis());
                    Backend.updateCollectionItem(this, "users", currentUid, update,
                            resp -> toast("Alias actualizado"),
                            e -> toast("No se pudo actualizar el alias"));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarGuardarPassword() {
        toast("El cambio de contraseña se gestiona desde el backend y no está disponible en esta pantalla");
    }

    private void configurarOjos() {
        toggleEye(eyeActual, edtPassActual);
        toggleEye(eyeNueva, edtPassNueva);
        toggleEye(eyeConfirmar, edtPassConfirmar);
    }

    private void toggleEye(ImageView eye, EditText edt) {
        eye.setOnClickListener(v -> {
            boolean visible = edt.getInputType() == (android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            edt.setInputType(visible
                    ? android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                    : android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            eye.setImageResource(visible ? R.drawable.ic_eye_closed_dark : R.drawable.ic_eye_open_dark);
            edt.setSelection(edt.getText().length());
        });
    }

    private void configurarValidacionPassword() {
        layoutPassword.setVisibility(LinearLayout.GONE);
        rule1.setVisibility(TextView.GONE);
        rule2.setVisibility(TextView.GONE);
        rule3.setVisibility(TextView.GONE);
        rule4.setVisibility(TextView.GONE);
        rule5.setVisibility(TextView.GONE);
    }

    private void cargarDatos() {
        Backend.getUser(this, currentUid, resp -> {
            txtEmail.setText(texto(resp.get("email"), ""));
            edtNombre.setText(texto(resp.get("nombre"), ""));
            edtAlias.setText(texto(resp.get("alias"), ""));

            String fotoUrl = texto(resp.get("fotoUrl"), null);
            if (fotoUrl != null && !fotoUrl.isEmpty()) {
                Glide.with(this)
                        .load(fotoUrl)
                        .circleCrop()
                        .into(imgPerfil);
            } else {
                imgPerfil.setImageResource(R.drawable.userlogo);
            }

            String rol = texto(resp.get("rol"), null);
            Object password = resp.get("password");
            boolean mostrarAlias = "jugador".equalsIgnoreCase(rol);
            layoutAlias.setVisibility(mostrarAlias ? LinearLayout.VISIBLE : LinearLayout.GONE);
            layoutPassword.setVisibility(LinearLayout.GONE);
            if (password == null || password.toString().isEmpty()) {
                layoutPassword.setVisibility(LinearLayout.GONE);
            }
        }, t -> {
            toast("No se pudieron cargar tus datos");
            imgPerfil.setImageResource(R.drawable.userlogo);
        });
    }

    private void cerrarSesion() {
        SessionNavigator.signOutToLogin(this);
    }

    private void confirmarEliminarCuenta() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar cuenta")
                .setMessage("Esta acción no se puede deshacer")
                .setPositiveButton("Eliminar", (d, w) -> {
                    Backend.deleteCollectionItem(this, "users", currentUid, resp -> {
                        SessionNavigator.signOutToLogin(this);
                    }, e -> toast("No se pudo eliminar la cuenta"));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private String texto(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String result = value.toString();
        return result.isEmpty() ? fallback : result;
    }
}
