package com.example.tmanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextView tvTotal;
    private Button btnPagar;
    private String clubNombre = "";
    private String pistaNombre = "";
    private String dia = "";
    private String hora = "";
    private String duracion = "";
    private double precio = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        tvTotal = findViewById(R.id.tvTotal);
        btnPagar = findViewById(R.id.btnPagar);

        // Obtener datos del intent
        Intent intent = getIntent();
        clubNombre = intent.getStringExtra("clubNombre");
        pistaNombre = intent.getStringExtra("pistaNombre");
        dia = intent.getStringExtra("dia");
        hora = intent.getStringExtra("hora");
        duracion = intent.getStringExtra("duracion");
        precio = intent.getDoubleExtra("precio", 0.0);

        // Mostrar detalles
        mostrarDetalles();

        btnPagar.setOnClickListener(v -> procesarPago());
    }

    private void mostrarDetalles() {
        StringBuilder detalle = new StringBuilder();
        detalle.append("Club: ").append(clubNombre).append("\n");
        detalle.append("Pista: ").append(pistaNombre).append("\n");
        detalle.append("Día: ").append(dia).append("\n");
        detalle.append("Hora: ").append(hora).append("\n");
        detalle.append("Duración: ").append(duracion).append(" min\n");
        detalle.append("Total: €").append(String.format("%.2f", precio));

        tvTotal.setText(detalle.toString());
    }

    private void procesarPago() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        // Guardar reserva en Firestore
        Map<String, Object> reserva = new HashMap<>();
        reserva.put("userUid", user.getUid());
        reserva.put("club", clubNombre);
        reserva.put("pista", pistaNombre);
        reserva.put("dia", dia);
        reserva.put("hora", hora);
        reserva.put("duracion", duracion);
        reserva.put("precio", precio);
        reserva.put("estado", "confirmada");
        reserva.put("timestamp", System.currentTimeMillis());

        db.collection("reservas_pagadas")
                .add(reserva)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(PaymentActivity.this, "¡Pago realizado exitosamente!", Toast.LENGTH_SHORT).show();
                    // Volver a Reservas
                    Intent intent = new Intent(PaymentActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PaymentActivity.this, "Error al procesar pago: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

