package com.example.padelandmore;

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

import com.example.padelandmore.network.Backend;

import java.util.HashMap;
import java.util.Map;

public class ReservasFragment extends Fragment {

    // backend client

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_reservas, container, false);

        // no Firestore: use Backend

        // Botón para abrir lista de Clubs (luego al seleccionar un club se abre ReservarPistaFragment)
        view.findViewById(R.id.btnReservarPistaMain).setOnClickListener(v ->
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new ClubsFragment())
                    .addToBackStack(null)
                    .commit());

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

        android.widget.EditText edtClub = form.findViewById(R.id.edtClub);
        android.widget.EditText edtPista = form.findViewById(R.id.edtPista);
        android.widget.EditText edtFecha = form.findViewById(R.id.edtFecha);
        android.widget.Spinner spnHora = form.findViewById(R.id.spnHora);
        android.widget.Button btnVer = form.findViewById(R.id.btnVerHorarios);

        if ("pista".equals(tipo)) {
            edtClub.setText(detalle);
            edtPista.setText(nombre);
        } else {
            edtClub.setText(detalle);
            edtPista.setText(nombre);
        }

        btnVer.setOnClickListener(v -> {
            String club = edtClub.getText().toString().trim();
            String pista = edtPista.getText().toString().trim();
            String fecha = edtFecha.getText().toString().trim();

            if (fecha.isEmpty()) {
                Toast.makeText(requireContext(), "Introduce una fecha primero", Toast.LENGTH_SHORT).show();
                return;
            }

            cargarHorariosDisponibles(tipo, club, pista, fecha, spnHora);
        });

        new AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                .setTitle("Reservar " + nombre)
                .setView(form)
                .setPositiveButton("Reservar", (dialog, which) -> {
                    if (spnHora.getVisibility() != View.VISIBLE || spnHora.getSelectedItem() == null) {
                        Toast.makeText(requireContext(), "Primero consulta y selecciona un horario", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String clubText = edtClub.getText().toString().trim();
                    String pistaText = edtPista.getText().toString().trim();
                    String fecha = edtFecha.getText().toString().trim();
                    String hora = spnHora.getSelectedItem().toString();

                    guardarReserva(clubText, pistaText, fecha, hora, tipo);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void cargarHorariosDisponibles(String tipo, String club, String pista, String fecha, android.widget.Spinner spinner) {
        String tabla = "clase".equals(tipo) ? "reservas_clases" : "reservas_pistas";
        
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            java.util.List<String> ocupadas = com.example.padelandmore.network.AWSConnection.obtenerHorasReservadas(tabla, club, pista, fecha);
            
            // Generar todos los slots posibles (de 08:00 a 22:00 cada 30 min)
            java.util.List<String> disponibles = new java.util.ArrayList<>();
            for (int h = 8; h <= 22; h++) {
                for (int m : new int[]{0, 30}) {
                    String slot = String.format("%02d:%02d", h, m);
                    if (isSlotLibre(slot, ocupadas)) {
                        disponibles.add(slot);
                    }
                }
            }

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_dropdown_item, disponibles);
                    spinner.setAdapter(adapter);
                    spinner.setVisibility(View.VISIBLE);
                    if (disponibles.isEmpty()) {
                        Toast.makeText(requireContext(), "No hay horarios libres para este día", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private boolean isSlotLibre(String slot, java.util.List<String> ocupadas) {
        int slotMinutos = convertirAMinutos(slot);
        for (String ocupada : ocupadas) {
            int ocupadaMinutos = convertirAMinutos(ocupada);
            // Si el slot está dentro del rango de 90 min de una reserva existente
            // O si la reserva existente empieza dentro del rango de 90 min de nuestro slot
            if (Math.abs(slotMinutos - ocupadaMinutos) < 90) {
                return false;
            }
        }
        return true;
    }

    private int convertirAMinutos(String hora) {
        try {
            String[] parts = hora.split(":");
            return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        } catch (Exception e) { return 0; }
    }

    private void guardarReserva(String club, String pista, String fecha, String hora, String tipo) {
        String uid = Backend.getCurrentUid(requireContext());
        if (uid == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesion", Toast.LENGTH_SHORT).show();
            return;
        }

        String tabla = "clase".equals(tipo) ? "reservas_clases" : "reservas_pistas";

        // Inserción directa en AWS RDS
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            boolean ok = com.example.padelandmore.network.AWSConnection.insertarReserva(tabla, uid, club, pista, fecha, hora);
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    if (ok) {
                        Toast.makeText(requireContext(), "Reserva creada en AWS", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Error al guardar reserva", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}

