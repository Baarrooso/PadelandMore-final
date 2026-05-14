package com.example.padelandmore;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.padelandmore.network.Backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultadosActivity extends AppCompatActivity {

    private TextView txtPartidos, txtVictorias, txtEmpates, txtDerrotas;
    private RecyclerView recycler;

    private final List<Map<String, String>> partidos = new ArrayList<>();
    private String uidActual;
    private String rolUsuario = "jugador";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultados);

        uidActual = Backend.getCurrentUid(this);

        txtPartidos = findViewById(R.id.txtPartidos);
        txtVictorias = findViewById(R.id.txtVictorias);
        txtEmpates = findViewById(R.id.txtEmpates);
        txtDerrotas = findViewById(R.id.txtDerrotas);
        recycler = findViewById(R.id.recyclerResultados);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(new PartidosAdapter(this, partidos));

        cargarRolYResultados();
    }

    private void cargarRolYResultados() {
        if (uidActual == null) {
            cargarResultados();
            return;
        }

        Backend.getUser(this, uidActual, resp -> {
            Object rol = resp.get("rol");
            if (rol != null) {
                rolUsuario = rol.toString();
            }

            if ("entrenador".equalsIgnoreCase(rolUsuario)) {
                activarSwipeEliminar();
            }

            cargarResultados();
        }, t -> cargarResultados());
    }

    private void cargarResultados() {
        Backend.getCollection(this, "partidos", resp -> {
            partidos.clear();

            int jugados = 0;
            int victorias = 0;
            int empates = 0;
            int derrotas = 0;

            Object data = resp.get("data");
            if (data instanceof List) {
                for (Object item : (List<?>) data) {
                    if (!(item instanceof Map)) {
                        continue;
                    }

                    @SuppressWarnings("unchecked")
                    Map<String, Object> doc = (Map<String, Object>) item;
                    if (!esVisible(doc)) {
                        continue;
                    }

                    String resultado = texto(doc.get("resultado"), "Pendiente");
                    String fecha = texto(doc.get("fecha"), "");
                    String rival = texto(doc.get("jugador2Nombre"), texto(doc.get("rival"), "Rival"));

                    Map<String, String> partido = new HashMap<>();
                    partido.put("id", texto(doc.get("id"), null));
                    partido.put("rival", rival);
                    partido.put("resultado", resultado);
                    partido.put("fecha", fecha);
                    partidos.add(partido);

                    jugados++;
                    String normalizado = resultado.toLowerCase();
                    if (normalizado.contains("vict")) {
                        victorias++;
                    } else if (normalizado.contains("emp")) {
                        empates++;
                    } else if (normalizado.contains("der")) {
                        derrotas++;
                    }
                }
            }

            txtPartidos.setText(String.valueOf(jugados));
            txtVictorias.setText(String.valueOf(victorias));
            txtEmpates.setText(String.valueOf(empates));
            txtDerrotas.setText(String.valueOf(derrotas));

            RecyclerView.Adapter<?> adapter = recycler.getAdapter();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }, t -> {
            txtPartidos.setText("0");
            txtVictorias.setText("0");
            txtEmpates.setText("0");
            txtDerrotas.setText("0");
        });
    }

    private boolean esVisible(Map<String, Object> doc) {
        if (uidActual == null) {
            return true;
        }

        String equipoId = getSharedPreferences("EQUIPO", MODE_PRIVATE).getString("equipoId", null);
        if (equipoId == null) {
            return true;
        }

        String docEquipoId = texto(doc.get("equipoId"), null);
        if (docEquipoId != null) {
            return equipoId.equals(docEquipoId);
        }

        String jugador1 = texto(doc.get("jugador1Uid"), null);
        String jugador2 = texto(doc.get("jugador2Uid"), null);
        return equipoId.equals(texto(doc.get("miEquipoId"), null)) || equipoId.equals(texto(doc.get("teamId"), null)) || uidActual.equals(jugador1) || uidActual.equals(jugador2);
    }

    private void activarSwipeEliminar() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder vh, int direction) {
                int pos = vh.getAdapterPosition();
                if (pos < 0 || pos >= partidos.size()) {
                    return;
                }

                Map<String, String> partido = partidos.get(pos);
                String id = partido.get("id");
                if (id == null || id.isEmpty()) {
                    return;
                }

                Map<String, Object> update = new HashMap<>();
                update.put("resultado", "Pendiente");
                update.put("resultadoLocal", null);
                update.put("resultadoRival", null);
                update.put("finalizado", false);
                update.put("resultadoEliminado", true);

                Backend.updateCollectionItem(ResultadosActivity.this, "partidos", id, update, resp -> cargarResultados(), t -> cargarResultados());
            }
        };

        new ItemTouchHelper(callback).attachToRecyclerView(recycler);
    }

    private String texto(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }

        String texto = value.toString();
        return texto.isEmpty() ? fallback : texto;
    }
}
