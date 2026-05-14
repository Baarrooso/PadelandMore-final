package com.example.padelandmore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.padelandmore.network.Backend;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class NotificacionesActivity extends AppCompatActivity {

    private NotificacionesAdapter adapter;
    private final List<NotificacionModel> lista = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificaciones);

        ImageButton btnRetroceso = findViewById(R.id.btnRetroceso);
        btnRetroceso.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        RecyclerView recycler = findViewById(R.id.recyclerNotificaciones);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificacionesAdapter(this, lista);
        recycler.setAdapter(adapter);

        adapter.setOnNotificacionClickListener(this::onNotificacionClick);

        cargarNotificaciones();

    }

    private void cargarNotificaciones() {

        String uid = Backend.getCurrentUid(this);

        if (uid == null) return;

        Backend.getCollection(this, "notificaciones", resp -> {
            lista.clear();

            Object data = resp.get("data");
            if (data instanceof List) {
                for (Object item : (List<?>) data) {
                    if (!(item instanceof Map)) {
                        continue;
                    }

                    @SuppressWarnings("unchecked")
                    Map<String, Object> doc = (Map<String, Object>) item;
                    if (!uid.equals(valorTexto(doc.get("userUid")))) {
                        continue;
                    }

                    NotificacionModel n = new NotificacionModel();
                    n.id = valorTexto(doc.get("id"));
                    n.tipo = valorTexto(doc.get("tipo"));
                    n.titulo = valorTexto(doc.get("titulo"));
                    n.mensaje = valorTexto(doc.get("mensaje"));
                    n.eventoId = valorTexto(doc.get("eventoId"));
                    n.leida = Boolean.TRUE.equals(doc.get("leida"));
                    Long ts = millis(doc.get("timestamp"));
                    if (ts == null) {
                        ts = millis(doc.get("creado"));
                    }
                    if (ts != null) {
                        n.timestamp = new Timestamp(new java.util.Date(ts));
                    }
                    lista.add(n);
                }
            }

            Collections.sort(lista, new Comparator<NotificacionModel>() {
                @Override
                public int compare(NotificacionModel a, NotificacionModel b) {
                    long ta = a.timestamp != null ? a.timestamp.toDate().getTime() : 0L;
                    long tb = b.timestamp != null ? b.timestamp.toDate().getTime() : 0L;
                    return Long.compare(tb, ta);
                }
            });

            adapter.notifyDataSetChanged();
        }, t -> {
            lista.clear();
            adapter.notifyDataSetChanged();
        });
    }


    private void onNotificacionClick(NotificacionModel n) {

        Intent i;

        switch (n.tipo) {

            // 💬 MENSAJES
            case "mensaje":
                i = new Intent(this, MensajesActivity.class);
                startActivity(i);
                break;

            // ✅ ASISTENCIA
            case "asistencia":
                // ⚠️ RegistroAsistenciaActivity no existe - comentado temporalmente
                // i = new Intent(this, RegistroAsistenciaActivity.class);
                // startActivity(i);
                break;

            // ⚽ CONVOCATORIAS
            case "convocatoria_partido":
            case "convocatoria_evento":
                if (n.eventoId != null) {
                    i = new Intent(this, MainActivity.class);
                    i.putExtra("open", "eventos");
                    i.putExtra("eventoId", n.eventoId);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
                break;

            // 👥 NUEVO JUGADOR
            case "union_equipo":
                // ⚠️ MiembrosActivity no existe - comentado temporalmente
                // i = new Intent(this, MiembrosActivity.class);
                // startActivity(i);
                break;
        }

        finish();
    }



    private void marcarComoLeidas() {
        for (NotificacionModel n : lista) {
            if (n.leida || n.id == null || n.id.isEmpty()) {
                continue;
            }

            Map<String, Object> update = new java.util.HashMap<>();
            update.put("leida", true);
            update.put("actualizado", System.currentTimeMillis());
            Backend.updateCollectionItem(this, "notificaciones", n.id, update, resp -> {}, t -> {});
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        marcarComoLeidas();
    }

    private String valorTexto(Object value) {
        return value == null ? null : value.toString();
    }

    private Long millis(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }


}
