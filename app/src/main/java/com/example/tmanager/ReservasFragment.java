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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservasFragment extends Fragment {

    private final List<String> reservas = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_reservas, container, false);

        db = FirebaseFirestore.getInstance();

        ListView listReservas = view.findViewById(R.id.listReservas);
        FloatingActionButton btnNuevaReserva = view.findViewById(R.id.btnNuevaReserva);

        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, reservas);
        listReservas.setAdapter(adapter);

        cargarReservas();
        btnNuevaReserva.setOnClickListener(v -> mostrarDialogoReserva());

        return view;
    }

    private void mostrarDialogoReserva() {
        View form = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_reserva_pista, null, false);

        EditText edtClub = form.findViewById(R.id.edtClub);
        EditText edtPista = form.findViewById(R.id.edtPista);
        EditText edtFecha = form.findViewById(R.id.edtFecha);
        EditText edtHora = form.findViewById(R.id.edtHora);

        new AlertDialog.Builder(requireContext())
                .setTitle("Nueva reserva")
                .setView(form)
                .setPositiveButton("Reservar", (dialog, which) -> {
                    String club = edtClub.getText().toString().trim();
                    String pista = edtPista.getText().toString().trim();
                    String fecha = edtFecha.getText().toString().trim();
                    String hora = edtHora.getText().toString().trim();

                    if (club.isEmpty() || pista.isEmpty() || fecha.isEmpty() || hora.isEmpty()) {
                        Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    guardarReserva(club, pista, fecha, hora);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardarReserva(String club, String pista, String fecha, String hora) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesion", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userUid", user.getUid());
        data.put("club", club);
        data.put("pista", pista);
        data.put("fecha", fecha);
        data.put("hora", hora);

        db.collection("reservas_padel")
                .add(data)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(requireContext(), "Reserva creada", Toast.LENGTH_SHORT).show();
                    cargarReservas();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "No se pudo guardar", Toast.LENGTH_SHORT).show()
                );
    }

    private void cargarReservas() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        db.collection("reservas_padel")
                .whereEqualTo("userUid", user.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    reservas.clear();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        String club = doc.getString("club");
                        String pista = doc.getString("pista");
                        String fecha = doc.getString("fecha");
                        String hora = doc.getString("hora");

                        String item = club + " - Pista " + pista + "\n" + fecha + " " + hora;
                        reservas.add(item);
                    }

                    if (reservas.isEmpty()) {
                        reservas.add("Aun no tienes reservas. Pulsa + para crear la primera.");
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Error cargando reservas", Toast.LENGTH_SHORT).show()
                );
    }
}

