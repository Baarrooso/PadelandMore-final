package com.example.padelandmore;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.padelandmore.network.Backend;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MensajesActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private EditText edtMensaje;
    private ImageButton btnEnviar;
    private ImageView imgLogoEquipo;
    private TextView txtNombreEquipo;

    private final List<MensajeModel> lista = new ArrayList<>();
    private MensajesAdapter adapter;

    private String equipoId;
    private String uid;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_mensajes);

        recycler = findViewById(R.id.recyclerMensajes);
        edtMensaje = findViewById(R.id.edtMensaje);
        btnEnviar = findViewById(R.id.btnEnviar);
        imgLogoEquipo = findViewById(R.id.imgLogoEquipo);
        txtNombreEquipo = findViewById(R.id.txtNombreEquipo);

        uid = Backend.getCurrentUid(this);
        equipoId = getSharedPreferences("EQUIPO", MODE_PRIVATE).getString("equipoId", null);

        adapter = new MensajesAdapter(this, lista);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        cargarDatosEquipo();
        escucharMensajes();

        btnEnviar.setOnClickListener(v -> enviarMensaje());
    }

    private void cargarDatosEquipo() {
        var prefs = getSharedPreferences("EQUIPO", MODE_PRIVATE);

        String nombreLocal = prefs.getString("nombre", null);
        String logoLocal = prefs.getString("logoUrl", null);

        if (nombreLocal != null) {
            txtNombreEquipo.setText(nombreLocal);
        }

        if (logoLocal != null) {
            Glide.with(this)
                    .load(logoLocal)
                    .circleCrop()
                    .into(imgLogoEquipo);
        }

        if (equipoId == null) return;

        Backend.getCollection(this, "equipos", resp -> {
            Object data = resp.get("data");
            if (!(data instanceof List)) {
                return;
            }

            for (Object item : (List<?>) data) {
                if (!(item instanceof Map)) {
                    continue;
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> doc = (Map<String, Object>) item;
                if (!equipoId.equals(texto(doc.get("id"), null))) {
                    continue;
                }

                String nombre = texto(doc.get("nombre"), null);
                String logo = texto(doc.get("logoUrl"), null);

                if (nombre != null) {
                    if (nombre.length() > 15) {
                        nombre = nombre.substring(0, 15) + "…";
                    }
                    txtNombreEquipo.setText(nombre);
                }

                if (logo != null) {
                    Glide.with(this)
                            .load(logo)
                            .circleCrop()
                            .into(imgLogoEquipo);
                }
                break;
            }
        }, t -> {});
    }

    private void escucharMensajes() {
        if (equipoId == null) return;

        Backend.getCollection(this, "mensajes", resp -> {
            lista.clear();

            Object data = resp.get("data");
            if (data instanceof List) {
                for (Object item : (List<?>) data) {
                    if (!(item instanceof Map)) continue;

                    @SuppressWarnings("unchecked")
                    Map<String, Object> doc = (Map<String, Object>) item;
                    if (!equipoId.equals(texto(doc.get("equipoId"), null)) && !equipoId.equals(texto(doc.get("chatId"), null))) {
                        continue;
                    }

                    MensajeModel m = new MensajeModel();
                    m.id = texto(doc.get("id"), null);
                    m.userUid = texto(doc.get("userUid"), null);
                    m.nombre = texto(doc.get("nombre"), null);
                    m.fotoUrl = texto(doc.get("fotoUrl"), null);
                    m.texto = texto(doc.get("texto"), "");

                    Long ts = numero(doc.get("timestamp"));
                    if (ts == null) {
                        ts = numero(doc.get("creado"));
                    }
                    if (ts != null) {
                        m.timestamp = new Timestamp(new java.util.Date(ts));
                    }

                    lista.add(m);
                }
            }

            lista.sort(new Comparator<MensajeModel>() {
                @Override
                public int compare(MensajeModel a, MensajeModel b) {
                    long ta = a.timestamp != null ? a.timestamp.toDate().getTime() : 0L;
                    long tb = b.timestamp != null ? b.timestamp.toDate().getTime() : 0L;
                    return Long.compare(ta, tb);
                }
            });

            adapter.notifyDataSetChanged();
            if (!lista.isEmpty()) {
                recycler.scrollToPosition(lista.size() - 1);
            }
        }, t -> {});
    }

    private void enviarMensaje() {
        String texto = edtMensaje.getText().toString().trim();
        if (texto.isEmpty() || uid == null) return;

        Backend.getUser(this, uid, resp -> {
            String nombre = texto(resp.get("alias"), null);
            if (nombre == null || nombre.trim().isEmpty()) {
                nombre = texto(resp.get("nombre"), "Usuario");
            }

            String fotoUrl = texto(resp.get("fotoUrl"), null);
            String rol = texto(resp.get("rol"), null);

            Map<String, Object> msg = new HashMap<>();
            msg.put("userUid", uid);
            msg.put("equipoId", equipoId);
            msg.put("texto", texto);
            msg.put("nombre", nombre);
            msg.put("fotoUrl", fotoUrl);
            msg.put("rol", rol);
            msg.put("timestamp", System.currentTimeMillis());

            Backend.postToCollection(this, "mensajes", msg, r -> {
                edtMensaje.setText("");
                escucharMensajes();
            }, t -> Toast.makeText(this, "No se pudo enviar", Toast.LENGTH_SHORT).show());
        }, t -> Toast.makeText(this, "No se pudo cargar tu perfil", Toast.LENGTH_SHORT).show());
    }

    private String texto(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }

        String result = value.toString();
        return result.isEmpty() ? fallback : result;
    }

    private Long numero(Object value) {
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
