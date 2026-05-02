package com.example.tmanager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TorneosFragment extends Fragment {

    private final List<String> torneos = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_torneos, container, false);

        db = FirebaseFirestore.getInstance();

        ListView listTorneos = view.findViewById(R.id.listTorneos);

        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, torneos);
        listTorneos.setAdapter(adapter);

        cargarTorneos();

        listTorneos.setOnItemClickListener((parent, v, position, id) -> apuntarseATorneo(position));

        return view;
    }

    private void mostrarDialogoTorneo() {
        // Antes de mostrar el diálogo, comprobar rol del usuario
        AuthUtil.isJugador(requireContext(), isJugador -> {
            if (isJugador) {
                android.widget.Toast.makeText(requireContext(), "No tienes permiso para crear torneos", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            View form = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_torneo_padel, null, false);

            EditText edtNombre = form.findViewById(R.id.edtNombreTorneo);
            EditText edtCiudad = form.findViewById(R.id.edtCiudadTorneo);
            EditText edtFecha = form.findViewById(R.id.edtFechaTorneo);
            EditText edtNivel = form.findViewById(R.id.edtNivelTorneo);

            new AlertDialog.Builder(requireContext())
                    .setTitle("Nuevo torneo")
                    .setView(form)
                    .setPositiveButton("Crear", (dialog, which) -> {
                        String nombre = edtNombre.getText().toString().trim();
                        String ciudad = edtCiudad.getText().toString().trim();
                        String fecha = edtFecha.getText().toString().trim();
                        String nivel = edtNivel.getText().toString().trim();

                        if (nombre.isEmpty() || ciudad.isEmpty() || fecha.isEmpty() || nivel.isEmpty()) {
                            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        crearTorneo(nombre, ciudad, fecha, nivel);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    private void crearTorneo(String nombre, String ciudad, String fecha, String nivel) {
        // Comprobar rol antes de crear en la base de datos
        AuthUtil.isJugador(requireContext(), isJugador -> {
            if (isJugador) {
                Toast.makeText(requireContext(), "No tienes permiso para crear torneos", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("nombre", nombre);
            data.put("ciudad", ciudad);
            data.put("fecha", fecha);
            data.put("nivel", nivel);
            data.put("inscritos", new ArrayList<String>());

            db.collection("torneos_padel")
                    .add(data)
                    .addOnSuccessListener(ref -> {
                        Toast.makeText(requireContext(), "Torneo creado", Toast.LENGTH_SHORT).show();
                        cargarTorneos();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), "No se pudo crear", Toast.LENGTH_SHORT).show()
                    );
        });
    }

    private void cargarTorneos() {
        db.collection("torneos_padel")
                .get()
                .addOnSuccessListener(snapshot -> {
                    torneos.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String nombre = doc.getString("nombre");
                        String ciudad = doc.getString("ciudad");
                        String fecha = doc.getString("fecha");
                        String nivel = doc.getString("nivel");

                        String item = (nombre == null ? "Torneo" : nombre)
                                + "\n" + (ciudad == null ? "Ciudad" : ciudad)
                                + " | " + (fecha == null ? "Fecha" : fecha)
                                + " | Nivel " + (nivel == null ? "N/A" : nivel);
                        torneos.add(item);
                    }

                    if (torneos.isEmpty()) {
                        torneos.add("No hay torneos todavia.");
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Error cargando torneos", Toast.LENGTH_SHORT).show()
                );
    }

    private void apuntarseATorneo(int position) {
        if (torneos.isEmpty()) return;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesion", Toast.LENGTH_SHORT).show();
            return;
        }

        String seleccionado = torneos.get(position);

        db.collection("inscripciones_torneo_padel")
                .add(new HashMap<String, Object>() {{
                    put("userUid", user.getUid());
                    put("torneoResumen", seleccionado);
                }})
                .addOnSuccessListener(ref ->
                        Toast.makeText(requireContext(), "Inscripcion enviada", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "No se pudo inscribir", Toast.LENGTH_SHORT).show()
                );
    }
}

