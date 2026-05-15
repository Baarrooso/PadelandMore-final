package com.example.padelandmore;

import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.padelandmore.network.Backend;

import java.util.HashMap;
import java.util.Map;

public class PartidoDetalleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Construir la UI programáticamente para evitar dependencias en recursos que puedan no estar disponibles
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        root.setPadding(padding, padding, padding, padding);

        TextView title = new TextView(this);
        title.setText("Detalle del partido");
        title.setTextSize(20);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);

        TextView labelJugadores = new TextView(this);
        labelJugadores.setText("Jugadores");
        labelJugadores.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(labelJugadores);

        TextView txtJugadores = new TextView(this);
        root.addView(txtJugadores);

        TextView labelLugar = new TextView(this);
        labelLugar.setText("Lugar");
        labelLugar.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(labelLugar);

        TextView txtLugar = new TextView(this);
        root.addView(txtLugar);

        TextView labelPista = new TextView(this);
        labelPista.setText("Pista");
        labelPista.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(labelPista);

        TextView txtPista = new TextView(this);
        root.addView(txtPista);

        TextView labelTipo = new TextView(this);
        labelTipo.setText("Tipo de pista");
        labelTipo.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(labelTipo);

        TextView txtTipoPista = new TextView(this);
        root.addView(txtTipoPista);

        TextView labelFecha = new TextView(this);
        labelFecha.setText("Fecha");
        labelFecha.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(labelFecha);

        TextView txtFecha = new TextView(this);
        root.addView(txtFecha);

        TextView labelResultado = new TextView(this);
        labelResultado.setText("Resultado");
        labelResultado.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(labelResultado);

        TextView txtResultado = new TextView(this);
        root.addView(txtResultado);

        Button btnAgregarResultado = new Button(this);
        btnAgregarResultado.setText("Añadir resultado");
        root.addView(btnAgregarResultado);

        setContentView(root);

        // Leer extras
        String club = getIntent().getStringExtra("club");
        String pista = getIntent().getStringExtra("pista");
        String fecha = getIntent().getStringExtra("fecha");
        String resultado = getIntent().getStringExtra("resultado");
        String rival = getIntent().getStringExtra("rival");
        String partidoId = getIntent().getStringExtra("partidoId");

        txtJugadores.setText("Tú vs " + (rival != null ? rival : "Pendiente"));
        txtLugar.setText(club != null ? club : "-");
        txtPista.setText(pista != null ? pista : "-");
        txtTipoPista.setText("Pádel");
        txtFecha.setText(fecha != null ? fecha : "-");
        txtResultado.setText(resultado != null && !resultado.equals("null") ? resultado : "Pendiente");

        // Botón Invitar Rival
        Button btnInvitar = new Button(this);
        btnInvitar.setText("Invitar Rival");
        root.addView(btnInvitar, root.indexOfChild(btnAgregarResultado));

        btnInvitar.setOnClickListener(v -> {
            EditText input = new EditText(this);
            input.setHint("Nombre del rival");
            new AlertDialog.Builder(this)
                .setTitle("Invitar Rival")
                .setView(input)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = input.getText().toString().trim();
                    if (!nombre.isEmpty()) {
                        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                            boolean ok = com.example.padelandmore.network.AWSConnection.actualizarRivalReserva(partidoId, nombre);
                            runOnUiThread(() -> {
                                if (ok) {
                                    txtJugadores.setText("Tú vs " + nombre);
                                    Toast.makeText(this, "Rival asignado", Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
                    }
                }).show();
        });

        btnAgregarResultado.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Añadir resultado");

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setHint("Ej: 6-2, 4-6, 7-6");
            builder.setView(input);

            builder.setPositiveButton("Guardar", (dialog, which) -> {
                String valor = input.getText().toString().trim();
                if (valor.isEmpty()) return;

                java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                    boolean ok = com.example.padelandmore.network.AWSConnection.actualizarResultadoReserva(partidoId, valor);
                    runOnUiThread(() -> {
                        if (ok) {
                            txtResultado.setText(valor);
                            Toast.makeText(this, "Resultado guardado", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });

            builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
            builder.show();
        });
    }
}

