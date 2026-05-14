package com.example.padelandmore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.padelandmore.network.Backend;

import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    // use Backend for session and persistence
    private TextView tvTotal;
    private Button btnPagar;
    private RadioGroup rgMetodosPago;
    private String clubNombre = "";
    private String pistaNombre = "";
    private String dia = "";
    private String hora = "";
    private String duracion = "";
    private double precio = 0.0;
    private String metodoPagoSeleccionado = "Tarjeta de Crédito";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // no Firebase initialization

        tvTotal = findViewById(R.id.tvTotal);
        btnPagar = findViewById(R.id.btnPagar);
        rgMetodosPago = findViewById(R.id.rgMetodosPago);

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

        rgMetodosPago.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = findViewById(checkedId);
            if (rb != null) {
                metodoPagoSeleccionado = rb.getText().toString();
            }
        });

        btnPagar.setOnClickListener(v -> procesarPago());
    }

    private void mostrarDetalles() {
        StringBuilder detalle = new StringBuilder();
        detalle.append("Club: ").append(clubNombre).append("\n");
        detalle.append("Pista: ").append(pistaNombre).append("\n");
        detalle.append("Día: ").append(dia).append("\n");
        detalle.append("Hora: ").append(hora).append("\n");
        detalle.append("Duración: ").append(duracion).append(" min\n");
        detalle.append("Método de pago: ").append(metodoPagoSeleccionado).append("\n");
        detalle.append("Total: €").append(String.format("%.2f", precio));

        tvTotal.setText(detalle.toString());
    }

    private void procesarPago() {
        String uid = Backend.getCurrentUid(this);
        if (uid == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        // Guardar reserva en Firestore
        Map<String, Object> reserva = new HashMap<>();
        reserva.put("userUid", uid);
        reserva.put("club", clubNombre);
        reserva.put("pista", pistaNombre);
        reserva.put("dia", dia);
        reserva.put("hora", hora);
        reserva.put("duracion", duracion);
        reserva.put("precio", precio);
        reserva.put("metodoPago", metodoPagoSeleccionado);
        reserva.put("estado", "confirmada");
        reserva.put("timestamp", System.currentTimeMillis());

        Backend.postToCollection(PaymentActivity.this, "reservas_pagadas", reserva, resp -> {
            Toast.makeText(PaymentActivity.this, "¡Pago realizado exitosamente!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(PaymentActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }, t -> Toast.makeText(PaymentActivity.this, "Error al procesar pago: " + t.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

