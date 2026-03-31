package com.example.tmanager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PadelNivelActivity extends AppCompatActivity {

    private int nivelPadel = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_padel_nivel);

        TextView txtNivel = findViewById(R.id.txtNivelPerfil);
        SeekBar seekNivel = findViewById(R.id.seekNivelPerfil);
        EditText edtCiudad = findViewById(R.id.edtCiudadPerfil);
        Button btnGuardar = findViewById(R.id.btnGuardarPerfilPadel);

        txtNivel.setText("Tu nivel: " + nivelPadel);

        seekNivel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                nivelPadel = Math.max(1, progress + 1);
                txtNivel.setText("Tu nivel: " + nivelPadel);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        cargarDatos(edtCiudad, seekNivel, txtNivel);

        btnGuardar.setOnClickListener(v -> guardarPerfilPadel(edtCiudad.getText().toString().trim()));
    }

    private void cargarDatos(EditText edtCiudad, SeekBar seekNivel, TextView txtNivel) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    Long nivel = doc.getLong("nivelPadel");
                    String ciudad = doc.getString("ciudadPadel");

                    if (nivel != null) {
                        nivelPadel = Math.max(1, Math.min(7, nivel.intValue()));
                        seekNivel.setProgress(nivelPadel - 1);
                        txtNivel.setText("Tu nivel: " + nivelPadel);
                    }

                    if (ciudad != null) {
                        edtCiudad.setText(ciudad);
                    }
                });
    }

    private void guardarPerfilPadel(String ciudad) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesion", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("nivelPadel", nivelPadel);
        data.put("ciudadPadel", ciudad);

        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(user.getUid())
                .update(data)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Perfil de padel guardado", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "No se pudo guardar", Toast.LENGTH_SHORT).show()
                );
    }
}

