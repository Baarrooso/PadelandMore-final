package com.example.tmanager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ReservasFragment extends Fragment {

    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_reservas, container, false);

        db = FirebaseFirestore.getInstance();

        // Botones de las 4 pistas
        view.findViewById(R.id.btnReservarPista1).setOnClickListener(v ->
            mostrarDialogoReserva("Pista 1 - Central", "Club Padel Madrid", "1", "pista"));
        view.findViewById(R.id.btnReservarPista2).setOnClickListener(v ->
            mostrarDialogoReserva("Pista 2 - Lateral", "Club Padel Madrid", "2", "pista"));
        view.findViewById(R.id.btnReservarPista3).setOnClickListener(v ->
            mostrarDialogoReserva("Pista 3 - Premium", "Club Padel Alcalá", "3", "pista"));
        view.findViewById(R.id.btnReservarPista4).setOnClickListener(v ->
            mostrarDialogoReserva("Pista 4 - Indoor", "Club Padel Getafe", "4", "pista"));

        // Botones de las 4 clases
        view.findViewById(R.id.btnReservarClase1).setOnClickListener(v ->
            mostrarDialogoReserva("Clase de Iniciación", "Lunes y Miércoles - 10:00", "1", "clase"));
        view.findViewById(R.id.btnReservarClase2).setOnClickListener(v ->
            mostrarDialogoReserva("Clase de Nivel Intermedio", "Martes y Jueves - 14:00", "2", "clase"));
        view.findViewById(R.id.btnReservarClase3).setOnClickListener(v ->
            mostrarDialogoReserva("Clase de Avanzado", "Miércoles y Viernes - 18:00", "3", "clase"));
        view.findViewById(R.id.btnReservarClase4).setOnClickListener(v ->
            mostrarDialogoReserva("Taller de Técnica", "Sábados - 11:00", "4", "clase"));

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

        return view;
    }

    private void mostrarDialogoReserva(String nombre, String detalle, String numero, String tipo) {
        View form = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_reserva_pista, null, false);

        EditText edtClub = form.findViewById(R.id.edtClub);
        EditText edtPista = form.findViewById(R.id.edtPista);
        EditText edtFecha = form.findViewById(R.id.edtFecha);
        EditText edtHora = form.findViewById(R.id.edtHora);

        // Pre-rellenar los campos según el tipo
        if ("pista".equals(tipo)) {
            edtClub.setText(detalle);
            edtPista.setText(nombre);
        } else if ("clase".equals(tipo)) {
            edtClub.setText(detalle);
            edtPista.setText(nombre);
        }

        new AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                .setTitle("Reservar " + nombre)
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

                    guardarReserva(clubText, pistaText, fecha, hora, tipo);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardarReserva(String club, String pista, String fecha, String hora, String tipo) {
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

        String coleccion = "clase".equals(tipo) ? "reservas_clases" : "reservas_padel";

        db.collection(coleccion)
                .add(data)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(requireContext(), "Reserva creada", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "No se pudo guardar", Toast.LENGTH_SHORT).show()
                );
    }
}

