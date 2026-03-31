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

        // Botones de las 4 pistas
        view.findViewById(R.id.btnReservarPista1).setOnClickListener(v ->
            mostrarDialogoReserva("Pista 1 - Central", "Club Padel Madrid", "1"));
        view.findViewById(R.id.btnReservarPista2).setOnClickListener(v ->
            mostrarDialogoReserva("Pista 2 - Lateral", "Club Padel Madrid", "2"));
        view.findViewById(R.id.btnReservarPista3).setOnClickListener(v ->
            mostrarDialogoReserva("Pista 3 - Premium", "Club Padel Alcalá", "3"));
        view.findViewById(R.id.btnReservarPista4).setOnClickListener(v ->
            mostrarDialogoReserva("Pista 4 - Indoor", "Club Padel Getafe", "4"));

        // Botones de Sorteos y Torneos
        view.findViewById(R.id.btnSorteosReserva).setOnClickListener(v ->
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new SorteosFragment())
                    .addToBackStack(null)
                    .commit());

        view.findViewById(R.id.btnTorneosReserva).setOnClickListener(v ->
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new TorneosFragment())
                    .addToBackStack(null)
                    .commit());

        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, reservas);
        listReservas.setAdapter(adapter);

        cargarReservas();

        return view;
    }

    private void mostrarDialogoReserva(String nombrePista, String club, String numeroPista) {
        View form = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_reserva_pista, null, false);

        EditText edtClub = form.findViewById(R.id.edtClub);
        EditText edtPista = form.findViewById(R.id.edtPista);
        EditText edtFecha = form.findViewById(R.id.edtFecha);
        EditText edtHora = form.findViewById(R.id.edtHora);

        // Pre-rellenar los campos
        edtClub.setText(club);
        edtPista.setText(nombrePista);

        new AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                .setTitle("Reservar " + nombrePista)
                .setView(form)
                .setPositiveButton("Reservar", (dialog, which) -> {
                    String clubText = edtClub.getText().toString().trim();
                    String pistaText = edtPista.getText().toString().trim();
                    String fecha = edtFecha.getText().toString().trim();
                    String hora = edtHora.getText().toString().trim();

                    if (clubText.isEmpty() || pistaText.isEmpty() || fecha.isEmpty() || hora.isEmpty()) {
                        Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    guardarReserva(clubText, pistaText, fecha, hora);
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

