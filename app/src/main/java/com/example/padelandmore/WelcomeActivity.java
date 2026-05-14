package com.example.padelandmore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.padelandmore.network.Backend;

public class WelcomeActivity extends AppCompatActivity {

    TextView txtBienvenido, txtNombreUsuario;
    LinearLayout btnCrearEquipo, btnUnirseEquipo;

    // no FirebaseAuth: use Backend stored session
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        txtBienvenido = findViewById(R.id.txtBienvenido);
        txtNombreUsuario = findViewById(R.id.txtNombreUsuario);
        btnCrearEquipo = findViewById(R.id.btnCrearEquipo);
        btnUnirseEquipo = findViewById(R.id.btnUnirseEquipo);

        String uid = Backend.getCurrentUid(this);
        if (uid != null) {
            // por ahora mostramos el uid como identificador
            txtNombreUsuario.setText(uid);
        }

        btnCrearEquipo.setOnClickListener(v -> {
            // CrearEquipoActivity fue eliminada
        });
        btnUnirseEquipo.setOnClickListener(v -> {
            // UnirseEquipoActivity fue eliminada
        });
    }
}
