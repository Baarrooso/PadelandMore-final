package com.example.padelandmore;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.padelandmore.network.Backend;

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
        String uid = Backend.getCurrentUid(this);
        if (uid == null) return;

        Backend.getUser(this, uid, resp -> {
            Object nivelValue = resp.get("nivelPadel");
            Object ciudadValue = resp.get("ciudadPadel");

            if (nivelValue instanceof Number) {
                nivelPadel = Math.max(1, Math.min(7, ((Number) nivelValue).intValue()));
                seekNivel.setProgress(nivelPadel - 1);
                txtNivel.setText("Tu nivel: " + nivelPadel);
            }

            if (ciudadValue != null) {
                edtCiudad.setText(ciudadValue.toString());
            }
        }, t -> Toast.makeText(this, "No se pudieron cargar tus datos", Toast.LENGTH_SHORT).show());
    }

    private void guardarPerfilPadel(String ciudad) {
        String uid = Backend.getCurrentUid(this);
        if (uid == null) {
            Toast.makeText(this, "Debes iniciar sesion", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("nivelPadel", nivelPadel);
        data.put("ciudadPadel", ciudad);
        data.put("actualizado", System.currentTimeMillis());

        Backend.updateCollectionItem(this, "users", uid, data, resp -> {
            Toast.makeText(this, "Perfil de padel guardado", Toast.LENGTH_SHORT).show();
            finish();
        }, t -> Toast.makeText(this, "No se pudo guardar", Toast.LENGTH_SHORT).show());
    }
}

