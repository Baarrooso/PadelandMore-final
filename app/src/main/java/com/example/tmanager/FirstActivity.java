package com.example.tmanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tmanager.network.Backend;

public class FirstActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ IMPORTANTE: Cargar el layout SIEMPRE primero para evitar pantalla negra
        setContentView(R.layout.activity_first);

        // Comprobar sesión activa DESPUÉS de inflar el layout
        if (Backend.getCurrentUid(this) != null) {
            irAMainEventos();
            return;
        }

        TextView iniciar = findViewById(R.id.txtIniciar);
        TextView registrar = findViewById(R.id.txtRegistrar);

        iniciar.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class))
        );

        registrar.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    private void irAMainEventos() {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("open", "inicio");
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }
}
