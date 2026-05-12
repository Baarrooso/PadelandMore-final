package com.example.tmanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReservarPistaFragment extends Fragment implements ClubsAdapter.OnClubClickListener {

    private FirebaseFirestore db;
    private LinearLayout containerDias;
    private GridLayout gridHoras;
    private SwitchCompat toggleDisponibles;
    private SwitchCompat toggleAlertas;
    private Button btnDuracion60, btnDuracion90, btnDuracion120;
    private String selectedDay = "";
    private String selectedHour = "";
    private String selectedDuration = "60";
    private Spinner spinnerClub;
    private RecyclerView recyclerClubes;
    private RecyclerView recyclerPistas;
    private LinearLayout containerReserva;
    private TextView tvClubSeleccionado;
    private Club clubSeleccionado;
    
    private Button btnDuracionSeleccionado;
    private Button btnHoraSeleccionado;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_reservar_pista, container, false);

        db = FirebaseFirestore.getInstance();

        // Inicializar vistas
        containerDias = view.findViewById(R.id.containerDias);
        gridHoras = view.findViewById(R.id.gridHoras);
        toggleDisponibles = view.findViewById(R.id.toggleDisponibles);
        toggleAlertas = view.findViewById(R.id.toggleAlertas);
        btnDuracion60 = view.findViewById(R.id.btnDuracion60);
        btnDuracion90 = view.findViewById(R.id.btnDuracion90);
        btnDuracion120 = view.findViewById(R.id.btnDuracion120);
        spinnerClub = view.findViewById(R.id.spinnerClub);
        recyclerClubes = view.findViewById(R.id.recyclerClubes);
        recyclerPistas = view.findViewById(R.id.recyclerPistas);
        containerReserva = view.findViewById(R.id.containerReserva);
        tvClubSeleccionado = view.findViewById(R.id.tvClubSeleccionado);

        // Botón atrás
        view.findViewById(R.id.btnBackReservarPista).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        // Cargar clubes
        cargarClubes();

        // Configurar días
        configurarDias();

        // Configurar horas
        configurarHoras();

        // Botones de duración
        btnDuracion60.setOnClickListener(v -> seleccionarDuracion(btnDuracion60, "60"));
        btnDuracion90.setOnClickListener(v -> seleccionarDuracion(btnDuracion90, "90"));
        btnDuracion120.setOnClickListener(v -> seleccionarDuracion(btnDuracion120, "120"));

        // Toggle disponibles
        toggleDisponibles.setOnCheckedChangeListener((buttonView, isChecked) -> {
            configurarHoras();
        });

        return view;
    }

    private void cargarClubes() {
        List<Club> clubes = new ArrayList<>();
        clubes.add(new Club("1", "Club Padel Madrid", "Madrid, Centro", ""));
        clubes.add(new Club("2", "Club Padel Alcalá", "Alcalá de Henares", ""));
        clubes.add(new Club("3", "Club Padel Getafe", "Getafe, Sur", ""));
        clubes.add(new Club("4", "Club Premium Tenis", "Madrid, Chamberí", ""));

        ClubsAdapter adapter = new ClubsAdapter(clubes, this);
        recyclerClubes.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerClubes.setAdapter(adapter);
    }

    @Override
    public void onClubClick(Club club) {
        clubSeleccionado = club;
        tvClubSeleccionado.setText("Club seleccionado: " + club.getNombre());
        cargarPistasPorClub(club.getId());
    }

    private void cargarPistasPorClub(String clubId) {
        List<Pista> pistas = new ArrayList<>();
        
        if ("1".equals(clubId)) {
            pistas.add(new Pista("p1", clubId, "Pista 1 - Central", 25.00, "Interior", 4, true));
            pistas.add(new Pista("p2", clubId, "Pista 2 - Lateral", 22.00, "Exterior", 4, true));
        } else if ("2".equals(clubId)) {
            pistas.add(new Pista("p3", clubId, "Pista Premium A", 30.00, "Interior", 4, true));
            pistas.add(new Pista("p4", clubId, "Pista Premium B", 30.00, "Interior", 4, false));
        } else if ("3".equals(clubId)) {
            pistas.add(new Pista("p5", clubId, "Pista Indoor 1", 28.00, "Interior", 4, true));
            pistas.add(new Pista("p6", clubId, "Pista Exterior 1", 20.00, "Exterior", 4, true));
        } else if ("4".equals(clubId)) {
            pistas.add(new Pista("p7", clubId, "Pista Profesional", 35.00, "Interior", 4, true));
            pistas.add(new Pista("p8", clubId, "Pista Standard", 25.00, "Exterior", 4, true));
        }

        PistasAdapter adapter = new PistasAdapter(pistas, pista -> {
            irAPasarelaPago(pista);
        });
        recyclerPistas.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerPistas.setAdapter(adapter);
    }

    private void irAPasarelaPago(Pista pista) {
        if (selectedDay.isEmpty() || selectedHour.isEmpty()) {
            Toast.makeText(requireContext(), "Selecciona día y hora", Toast.LENGTH_SHORT).show();
            return;
        }

        // Aquí iría la pasarela de pago
        Toast.makeText(requireContext(), 
            "Reserva: " + pista.getNombre() + " - " + selectedDay + " a " + selectedHour + 
            " por " + selectedDuration + " min - €" + pista.getPrecio(), 
            Toast.LENGTH_SHORT).show();
    }

    private void configurarDias() {
        containerDias.removeAllViews();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdfDia = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat sdfFecha = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat sdfMes = new SimpleDateFormat("MMM", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            android.widget.LinearLayout diaDia = new android.widget.LinearLayout(requireContext());
            diaDia.setOrientation(android.widget.LinearLayout.VERTICAL);
            diaDia.setLayoutParams(new android.widget.LinearLayout.LayoutParams(90, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));
            diaDia.setGravity(android.view.Gravity.CENTER);
            diaDia.setPadding(12, 12, 12, 12);
            diaDia.setBackground(getResources().getDrawable(R.drawable.bg_evento_card));
            
            // Configurar márgenes más espaciosos
            android.widget.LinearLayout.LayoutParams marginParams = new android.widget.LinearLayout.LayoutParams(90, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
            marginParams.setMargins(8, 8, 8, 8);
            diaDia.setLayoutParams(marginParams);

            String dia = sdfDia.format(calendar.getTime()).toUpperCase().substring(0, 3);
            String fecha = sdfFecha.format(calendar.getTime());
            String mes = sdfMes.format(calendar.getTime()).toUpperCase();

            TextView tvDia = new TextView(requireContext());
            tvDia.setText(dia);
            tvDia.setTextColor(getResources().getColor(android.R.color.white));
            tvDia.setTextSize(12);
            tvDia.setGravity(android.view.Gravity.CENTER);

            TextView tvFecha = new TextView(requireContext());
            tvFecha.setText(fecha);
            tvFecha.setTextColor(getResources().getColor(android.R.color.white));
            tvFecha.setTextSize(16);
            tvFecha.setTypeface(null, android.graphics.Typeface.BOLD);
            tvFecha.setGravity(android.view.Gravity.CENTER);
            tvFecha.setPadding(0, 8, 0, 8);

            TextView tvMes = new TextView(requireContext());
            tvMes.setText(mes);
            tvMes.setTextColor(getResources().getColor(android.R.color.white));
            tvMes.setTextSize(10);
            tvMes.setAlpha(0.7f);
            tvMes.setGravity(android.view.Gravity.CENTER);

            diaDia.addView(tvDia);
            diaDia.addView(tvFecha);
            diaDia.addView(tvMes);

            int finalI = i;
            diaDia.setOnClickListener(v -> {
                selectedDay = fecha + " " + mes;
                // Cambiar color del día seleccionado
                cambiarColorDiaSeleccionado(diaDia);
                Toast.makeText(requireContext(), "Día seleccionado: " + selectedDay, Toast.LENGTH_SHORT).show();
            });

            containerDias.addView(diaDia);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void cambiarColorDiaSeleccionado(android.widget.LinearLayout diaSel) {
        for (int i = 0; i < containerDias.getChildCount(); i++) {
            View view = containerDias.getChildAt(i);
            if (view instanceof android.widget.LinearLayout) {
                view.setBackground(getResources().getDrawable(R.drawable.bg_evento_card));
            }
        }
        diaSel.setBackgroundColor(getResources().getColor(R.color.accent_orange));
    }

    private void configurarHoras() {
        gridHoras.removeAllViews();

        // Horas de 08:00 a 23:00 en intervalos de 1 hora
        List<String> horas = new ArrayList<>();
        for (int h = 8; h <= 23; h++) {
            horas.add(String.format(Locale.getDefault(), "%02d:00", h));
        }

        boolean soloDisponibles = toggleDisponibles.isChecked();

        for (String hora : horas) {
            // Si solo mostrar disponibles, simular algunas horas disponibles
            if (soloDisponibles && !esHoraDisponible(hora)) {
                continue;
            }

            Button btnHora = new Button(requireContext());
            btnHora.setText(hora);
            btnHora.setTextColor(getResources().getColor(android.R.color.white));
            btnHora.setTextSize(12);
            btnHora.setPadding(8, 8, 8, 8);
            btnHora.setBackgroundResource(R.drawable.bg_evento_card);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(4, 4, 4, 4);
            btnHora.setLayoutParams(params);

            btnHora.setOnClickListener(v -> {
                selectedHour = hora;
                // Cambiar color de la hora seleccionada
                if (btnHoraSeleccionado != null) {
                    btnHoraSeleccionado.setBackgroundResource(R.drawable.bg_evento_card);
                    btnHoraSeleccionado.setTextColor(getResources().getColor(android.R.color.white));
                }
                btnHoraSeleccionado = btnHora;
                btnHora.setBackgroundColor(getResources().getColor(R.color.accent_orange));
                btnHora.setTextColor(getResources().getColor(android.R.color.white));
                Toast.makeText(requireContext(), "Hora seleccionada: " + selectedHour, Toast.LENGTH_SHORT).show();
            });

            gridHoras.addView(btnHora);
        }
    }

    private boolean esHoraDisponible(String hora) {
        // Simulación: las horas que contienen :00 son disponibles (todas)
        return hora.contains(":00");
    }

    private void seleccionarDuracion(Button button, String duracion) {
        // Resetear todos los botones
        if (btnDuracionSeleccionado != null) {
            btnDuracionSeleccionado.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }

        // Marcar el seleccionado
        button.setBackgroundColor(getResources().getColor(R.color.accent_orange));
        btnDuracionSeleccionado = button;
        selectedDuration = duracion;
        Toast.makeText(requireContext(), "Duración: " + duracion + " min", Toast.LENGTH_SHORT).show();
    }

}



