package com.example.tmanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class NotificacionesActivity extends AppCompatActivity {

    private NotificacionesAdapter adapter;
    private final List<NotificacionModel> lista = new ArrayList<>();

    private FirebaseFirestore db;

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

        db = FirebaseFirestore.getInstance();

        cargarNotificaciones();

    }

    private void cargarNotificaciones() {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = (auth.getCurrentUser() != null) ? auth.getCurrentUser().getUid() : null;

        if (uid == null) return;

        db.collection("notificaciones")
                .whereEqualTo("userUid", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {

                    if (snap == null) return;

                    lista.clear();

                    for (DocumentSnapshot d : snap) {
                        NotificacionModel n = d.toObject(NotificacionModel.class);
                        if (n != null) {
                            n.id = d.getId();
                            lista.add(n);
                        }
                    }

                    // 1️⃣ Pintar según estado actual
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

        WriteBatch batch = db.batch();

        for (NotificacionModel n : lista) {
            if (!n.leida) {
                batch.update(
                        db.collection("notificaciones").document(n.id),
                        "leida", true
                );
            }
        }

        batch.commit();
    }
    @Override
    protected void onPause() {
        super.onPause();
        marcarComoLeidas();
    }


}
