package com.example.tmanager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class AjustesBasicosActivity extends AppCompatActivity {

    private SwitchMaterial switchNotificaciones;
    private SwitchMaterial switchModoOscuro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes_basicos);

        switchNotificaciones = findViewById(R.id.switchNotificaciones);
        switchModoOscuro = findViewById(R.id.switchModoOscuro);
        Button btnGuardar = findViewById(R.id.btnGuardarAjustes);

        btnGuardar.setOnClickListener(v -> {
            boolean modoOscuro = switchModoOscuro.isChecked();
            AppCompatDelegate.setDefaultNightMode(
                    modoOscuro ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            Toast.makeText(this, "Ajustes guardados", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}

