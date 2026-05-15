package com.example.padelandmore;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.padelandmore.network.AWSConnection;
import com.example.padelandmore.network.Backend;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class SorteosFragment extends Fragment {

    private final List<String> sorteos = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sorteos, container, false);

        ImageButton btnVolver = view.findViewById(R.id.btn_volver_sorteos);
        btnVolver.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        ListView listSorteos = view.findViewById(R.id.listSorteos);
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, sorteos);
        listSorteos.setAdapter(adapter);

        cargarSorteos();

        com.google.android.material.floatingactionbutton.FloatingActionButton fabCrearSorteo = view.findViewById(R.id.fabCrearSorteo);
        AuthUtil.isAdmin(requireContext(), isAdmin -> {
            if (isAdmin) {
                fabCrearSorteo.setVisibility(View.VISIBLE);
                fabCrearSorteo.show();
                fabCrearSorteo.setOnClickListener(v -> mostrarDialogoSorteo());
            } else {
                fabCrearSorteo.setVisibility(View.GONE);
                fabCrearSorteo.hide();
            }
        });

        // Al pulsar un sorteo, el usuario puede apuntarse (si está logueado)
        listSorteos.setOnItemClickListener((parent, v, position, id) -> {
            if (sorteos.isEmpty()) return;
            Toast.makeText(requireContext(), "Inscribiendo...", Toast.LENGTH_SHORT).show();

            String userUid = Backend.getCurrentUid(requireContext());
            if (userUid == null) {
                Toast.makeText(requireContext(), "Debes iniciar sesión para apuntarte", Toast.LENGTH_SHORT).show();
                return;
            }

            String seleccionado = sorteos.get(position);
            long   creado       = System.currentTimeMillis();

            // Inscripción directa a AWS RDS en hilo secundario
            Executors.newSingleThreadExecutor().execute(() -> {
                boolean ok = AWSConnection.inscribirEnSorteo(userUid, seleccionado, creado);
                requireActivity().runOnUiThread(() -> {
                    if (ok) {
                        Toast.makeText(requireContext(), "¡Inscrito en el sorteo!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "No se pudo inscribir", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        return view;
    }

    private void mostrarDialogoSorteo() {
        View form = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_sorteo_padel, null, false);
        EditText edtNombre = form.findViewById(R.id.edtNombreSorteo);
        EditText edtPremio = form.findViewById(R.id.edtPremioSorteo);
        EditText edtFecha = form.findViewById(R.id.edtFechaSorteo);

        new AlertDialog.Builder(requireContext())
                .setTitle("Nuevo Sorteo")
                .setView(form)
                .setPositiveButton("Crear", (dialog, which) -> {
                    String nombre = edtNombre.getText().toString().trim();
                    String premio = edtPremio.getText().toString().trim();
                    String fecha = edtFecha.getText().toString().trim();

                    if (nombre.isEmpty() || premio.isEmpty() || fecha.isEmpty()) {
                        Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    crearSorteo(nombre, premio, fecha);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ─── Admin: crear sorteo directo en AWS RDS ───────────────────────────────
    private void crearSorteo(String nombre, String premio, String fecha) {
        long creado = System.currentTimeMillis();

        Executors.newSingleThreadExecutor().execute(() -> {
            boolean ok = AWSConnection.insertarSorteo(nombre, premio, fecha, creado);
            requireActivity().runOnUiThread(() -> {
                if (ok) {
                    Toast.makeText(requireContext(), "Sorteo creado", Toast.LENGTH_SHORT).show();
                    cargarSorteos();
                } else {
                    Toast.makeText(requireContext(), "Error al crear sorteo", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void cargarSorteos() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<AWSConnection.Sorteo> lista = AWSConnection.obtenerSorteos();
            requireActivity().runOnUiThread(() -> {
                sorteos.clear();
                for (AWSConnection.Sorteo s : lista) {
                    String info = s.nombre;
                    if (s.premio != null) info += " - Premio: " + s.premio;
                    if (s.fecha != null) info += " (" + s.fecha + ")";
                    sorteos.add(info);
                }
                adapter.notifyDataSetChanged();
            });
        });
    }

    private String valorTexto(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String texto = value.toString();
        return texto.isEmpty() ? fallback : texto;
    }
}

