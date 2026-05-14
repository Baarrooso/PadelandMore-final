package com.example.tmanager;

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

import com.google.firebase.firestore.FirebaseFirestore;

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

        // Inicializar Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Leer extras
        String jugadores = getIntent().getStringExtra("jugadores");
        String rival = getIntent().getStringExtra("rival");
        String lugar = getIntent().getStringExtra("lugar");
        String pista = getIntent().getStringExtra("pista");
        String tipoPista = getIntent().getStringExtra("tipo_pista");
        String fecha = getIntent().getStringExtra("fecha");
        String resultado = getIntent().getStringExtra("resultado");
        String partidoId = getIntent().getStringExtra("partidoId");

        if (jugadores == null && rival != null) {
            jugadores = "Tú vs " + rival;
        }

        txtJugadores.setText(jugadores != null ? jugadores : "-");
        txtLugar.setText(lugar != null ? lugar : "-");
        txtPista.setText(pista != null ? pista : "-");
        txtTipoPista.setText(tipoPista != null ? tipoPista : "-");
        txtFecha.setText(fecha != null ? fecha : "-");
        txtResultado.setText(resultado != null ? resultado : "Pendiente");

        btnAgregarResultado.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Añadir resultado");

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setHint("Ej: 6-2, 4-6, 7-6");
            builder.setView(input);

            builder.setPositiveButton("Guardar", (dialog, which) -> {
                String valor = input.getText().toString().trim();
                if (valor.isEmpty()) {
                    Toast.makeText(PartidoDetalleActivity.this, "Introduce un resultado", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (partidoId != null && !partidoId.isEmpty()) {
                    db.collection("partidos").document(partidoId).update("resultado", valor)
                            .addOnSuccessListener(aVoid -> {
                                txtResultado.setText(valor);
                                Toast.makeText(PartidoDetalleActivity.this, "Resultado guardado", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(PartidoDetalleActivity.this, "Error guardando resultado", Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(PartidoDetalleActivity.this, "ID de partido no disponible", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
            builder.show();
        });
    }
}

