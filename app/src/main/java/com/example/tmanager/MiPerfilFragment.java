package com.example.tmanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class MiPerfilFragment extends Fragment {

    private Spinner spinnerNivel, spinnerMano;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private static final int NIVEL_MAXIMO = 5;

    // Vistas de datos
    private TextView txtNombreUsuario, txtPartidosCount, txtSeguidoresCount, txtSeguidosCount;
    private TextView txtFiabilidadPorcentaje, txtNivelActual, txtPuntosJuego;
    private TextView txtPartidosGanados, txtPartidosPerdidos, txtEfectividadPorcentaje;
    private ImageView imgFotoPerfil, imgNivelPadel;
    private RecyclerView recyclerUltimosPartidos, recyclerCompaneros, recyclerClubes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_mi_perfil, container, false);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Inicializar todas las vistas
        inicializarVistas(v);

        // Cargar datos del usuario
        // Cargar datos una vez la vista ya está lista
        if (isAdded()) {
            cargarDatosUsuario();
            cargarEstadisticas();
            cargarUltimosPartidos();
            cargarCompanerosHabituales();
            cargarClubesHabituales();
        }

        return v;
    }

    private void inicializarVistas(View v) {
        // Textos
        txtNombreUsuario = v.findViewById(R.id.txtNombreUsuario);
        txtPartidosCount = v.findViewById(R.id.txtPartidosCount);
        txtSeguidoresCount = v.findViewById(R.id.txtSeguidoresCount);
        txtSeguidosCount = v.findViewById(R.id.txtSeguidosCount);
        txtFiabilidadPorcentaje = v.findViewById(R.id.txtFiabilidadPorcentaje);
        txtNivelActual = v.findViewById(R.id.txtNivelActual);
        txtPuntosJuego = v.findViewById(R.id.txtPuntosJuego);
        txtPartidosGanados = v.findViewById(R.id.txtPartidosGanados);
        txtPartidosPerdidos = v.findViewById(R.id.txtPartidosPerdidos);
        txtEfectividadPorcentaje = v.findViewById(R.id.txtEfectividadPorcentaje);

        // Imágenes
        imgFotoPerfil = v.findViewById(R.id.imgFotoPerfil);
        imgNivelPadel = v.findViewById(R.id.imgNivelPadel);

        // RecyclerViews
        recyclerUltimosPartidos = v.findViewById(R.id.recyclerUltimosPartidos);
        recyclerCompaneros = v.findViewById(R.id.recyclerCompaneros);
        recyclerClubes = v.findViewById(R.id.recyclerClubes);

        recyclerUltimosPartidos.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerCompaneros.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerClubes.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        // Spinners
        spinnerNivel = v.findViewById(R.id.spinnerNivel);
        spinnerMano = v.findViewById(R.id.spinnerMano);

        spinnerNivel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= NIVEL_MAXIMO) {
                    spinnerNivel.setSelection(NIVEL_MAXIMO - 1);
                    Toast.makeText(requireContext(), "Nivel máximo: Profesional", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Botones
        ImageButton btnCambiarFoto = v.findViewById(R.id.btnCambiarFoto);

        btnCambiarFoto.setOnClickListener(v1 -> Toast.makeText(requireContext(), "Funcionalidad próximamente", Toast.LENGTH_SHORT).show());
    }

    private void cargarDatosUsuario() {
        if (user == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("usuarios")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Nombre
                        String nombre = doc.getString("nombre");
                        if (nombre != null) {
                            txtNombreUsuario.setText(nombre);
                        }

                        // Nivel
                        String nivel = doc.getString("nivel");
                        if (nivel != null) {
                            int posicion = getIndexInArray(R.array.niveles_padel, nivel);
                            spinnerNivel.setSelection(posicion);
                        }

                        // Mano
                        String mano = doc.getString("mano");
                        if (mano != null) {
                            int posicion = getIndexInArray(R.array.mano_juego, mano);
                            spinnerMano.setSelection(posicion);
                        }

                        // Nivel numérico
                        Double nivelNumerico = doc.getDouble("nivelNumerico");
                        if (nivelNumerico != null) {
                            txtNivelActual.setText(String.format(Locale.getDefault(), "%.1f", nivelNumerico));
                        }

                        // Fiabilidad
                        Double fiabilidad = doc.getDouble("fiabilidad");
                        if (fiabilidad != null) {
                            txtFiabilidadPorcentaje.setText(String.format(Locale.getDefault(), "%.0f%%", fiabilidad * 100));
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Error cargando perfil", Toast.LENGTH_SHORT).show()
                );
    }

    private void cargarEstadisticas() {
        if (user == null) return;

        db.collection("partidos")
                .whereEqualTo("jugador1Uid", user.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    int ganados = 0, perdidos = 0;

                    for (QueryDocumentSnapshot doc : snapshot) {
                        String resultado = doc.getString("resultado");
                        if ("victoria".equalsIgnoreCase(resultado)) {
                            ganados++;
                        } else if ("derrota".equalsIgnoreCase(resultado)) {
                            perdidos++;
                        }
                    }

                    int total = ganados + perdidos;
                    int efectividad = total > 0 ? (ganados * 100) / total : 0;

                    txtPartidosCount.setText(String.valueOf(total));
                    txtPartidosGanados.setText(String.valueOf(ganados));
                    txtPartidosPerdidos.setText(String.valueOf(perdidos));
                    txtEfectividadPorcentaje.setText(String.format(Locale.getDefault(), "%d%%", efectividad));

                    // Valores por defecto para puntos en juego
                    txtPuntosJuego.setText("+2.1 / -1.8");
                });
    }

    private void cargarUltimosPartidos() {
        if (user == null) return;

        db.collection("partidos")
                .whereEqualTo("jugador1Uid", user.getUid())
                .orderBy("fecha")
                .limit(5)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Map<String, String>> partidos = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        Map<String, String> partido = new HashMap<>();
                        partido.put("rival", doc.getString("jugador2Nombre"));
                        partido.put("resultado", doc.getString("resultado"));
                        partido.put("fecha", doc.getString("fecha"));
                        partido.put("jugadores", doc.getString("jugadores"));
                        partido.put("lugar", doc.getString("lugar"));
                        partido.put("pista", doc.getString("pista"));
                        partido.put("tipo_pista", doc.getString("tipo_pista"));
                        partido.put("partidoId", doc.getId());
                        partidos.add(partido);
                    }

                    if (!partidos.isEmpty()) {
                        PartidosAdapter adapter = new PartidosAdapter(requireContext(), partidos);
                        recyclerUltimosPartidos.setAdapter(adapter);
                    }
                });
    }

    private void cargarCompanerosHabituales() {
        if (user == null) return;

        // Consulta los compañeros con los que más ha jugado
        db.collection("partidos")
                .whereEqualTo("jugador1Uid", user.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    Map<String, Integer> companeros = new HashMap<>();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        String rival = doc.getString("jugador2Nombre");
                        if (rival != null) {
                            companeros.put(rival, companeros.getOrDefault(rival, 0) + 1);
                        }
                    }

                    List<String> top5 = new ArrayList<>();
                    companeros.entrySet().stream()
                            .sorted((a, b) -> b.getValue() - a.getValue())
                            .limit(5)
                            .forEach(e -> top5.add(e.getKey()));

                    if (!top5.isEmpty()) {
                        CompanerosAdapter adapter = new CompanerosAdapter(top5);
                        recyclerCompaneros.setAdapter(adapter);
                    }
                });
    }

    private void cargarClubesHabituales() {
        if (user == null) return;

        db.collection("reservas_padel")
                .whereEqualTo("userUid", user.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    Map<String, Integer> clubes = new HashMap<>();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        String club = doc.getString("club");
                        if (club != null) {
                            clubes.put(club, clubes.getOrDefault(club, 0) + 1);
                        }
                    }

                    List<String> top5 = new ArrayList<>();
                    clubes.entrySet().stream()
                            .sorted((a, b) -> b.getValue() - a.getValue())
                            .limit(5)
                            .forEach(e -> top5.add(e.getKey()));

                    if (!top5.isEmpty()) {
                        ClubesAdapter adapter = new ClubesAdapter(top5);
                        recyclerClubes.setAdapter(adapter);
                    }
                });
    }

    private int getIndexInArray(int arrayId, String value) {
        String[] array = getResources().getStringArray(arrayId);
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) {
                return i;
            }
        }
        return 0;
    }

}
