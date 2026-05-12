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

public class ReservarPistaFragment extends Fragment {

    private FirebaseFirestore db;
    private LinearLayout containerDias;
    private GridLayout gridHoras;
    private SwitchCompat toggleDisponibles;
    private SwitchCompat toggleAlertas;
    private Button btnDuracion90, btnDuracion120, btnDuracion150;
    private String selectedDay = "";
    private String selectedHour = "";
    private String selectedDuration = "90";
    private Spinner spinnerClub;

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
        btnDuracion90 = view.findViewById(R.id.btnDuracion90);
        btnDuracion120 = view.findViewById(R.id.btnDuracion120);
        btnDuracion150 = view.findViewById(R.id.btnDuracion150);
        spinnerClub = view.findViewById(R.id.spinnerClub);

        // Botón atrás
        view.findViewById(R.id.btnBackReservarPista).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        // Configurar días
        configurarDias();

        // Configurar horas
        configurarHoras();

        // Botones de duración
        btnDuracion90.setOnClickListener(v -> seleccionarDuracion(btnDuracion90, "90"));
        btnDuracion120.setOnClickListener(v -> seleccionarDuracion(btnDuracion120, "120"));
        btnDuracion150.setOnClickListener(v -> seleccionarDuracion(btnDuracion150, "150"));

        // Toggle disponibles
        toggleDisponibles.setOnCheckedChangeListener((buttonView, isChecked) -> {
            configurarHoras();
        });

        return view;
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
            diaDia.setLayoutParams(new android.widget.LinearLayout.LayoutParams(70, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));
            diaDia.setGravity(android.view.Gravity.CENTER);
            diaDia.setPadding(8, 8, 8, 8);
            diaDia.setBackground(getResources().getDrawable(R.drawable.bg_evento_card));
            
            // Configurar márgenes
            android.widget.LinearLayout.LayoutParams marginParams = new android.widget.LinearLayout.LayoutParams(70, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
            marginParams.setMargins(0, 0, 8, 0);
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
            tvFecha.setPadding(0, 4, 0, 4);

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
                Toast.makeText(requireContext(), "Día seleccionado: " + selectedDay, Toast.LENGTH_SHORT).show();
            });

            containerDias.addView(diaDia);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void configurarHoras() {
        gridHoras.removeAllViews();

        // Horas de 08:00 a 23:30 en intervalos de 30 minutos
        List<String> horas = new ArrayList<>();
        for (int h = 8; h <= 23; h++) {
            for (int m = 0; m < 60; m += 30) {
                horas.add(String.format(Locale.getDefault(), "%02d:%02d", h, m));
            }
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
                Toast.makeText(requireContext(), "Hora seleccionada: " + selectedHour, Toast.LENGTH_SHORT).show();
            });

            gridHoras.addView(btnHora);
        }
    }

    private boolean esHoraDisponible(String hora) {
        // Simulación: las horas que contienen 0:00, 10:00, 14:00, 16:00 son disponibles
        return hora.contains("00:00") || hora.contains("10:00") ||
               hora.contains("14:00") || hora.contains("16:00") ||
               hora.contains("00:30") || hora.contains("10:30") ||
               hora.contains("15:30") || hora.contains("22:30");
    }

    private void seleccionarDuracion(Button button, String duracion) {
        // Resetear todos los botones
        btnDuracion90.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                getResources().getColor(R.color.accent_orange)));
        btnDuracion120.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                getResources().getColor(R.color.accent_orange)));
        btnDuracion150.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                getResources().getColor(R.color.accent_orange)));

        // Marcar el seleccionado
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                getResources().getColor(R.color.accent_orange)));
        selectedDuration = duracion;
        Toast.makeText(requireContext(), "Duración: " + duracion + " min", Toast.LENGTH_SHORT).show();
    }

}

