package com.example.tmanager;

import android.content.Intent;
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
    private Button btnDuracion60, btnDuracion90, btnDuracion120;
    private String selectedDay = "";
    private String selectedHour = "";
    private String selectedDuration = "60";
    // spinnerClub removed because selection is done via recyclerClubes
    private RecyclerView recyclerClubes;
    private RecyclerView recyclerPistas;
    private LinearLayout containerReserva;
    private LinearLayout containerResumen;
    private LinearLayout containerAcciones;
    private TextView tvClubSeleccionado;
    private Club clubSeleccionado;
    private Pista pistaSeleccionada;
    
    private Button btnDuracionSeleccionado;
    private Button btnHoraSeleccionado;
    private Button btnReservarPista;

    private boolean clubSelectionMode = false;

    // TextViews del resumen
    private TextView tvResumenClub;
    private TextView tvResumenPista;
    private TextView tvResumenDia;
    private TextView tvResumenHora;
    private TextView tvResumenDuracion;
    private TextView tvResumenPrecio;


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
        btnDuracion60 = view.findViewById(R.id.btnDuracion60);
        btnDuracion90 = view.findViewById(R.id.btnDuracion90);
        btnDuracion120 = view.findViewById(R.id.btnDuracion120);
        recyclerClubes = view.findViewById(R.id.recyclerClubes);
        recyclerPistas = view.findViewById(R.id.recyclerPistas);
        containerReserva = view.findViewById(R.id.containerReserva);
        tvClubSeleccionado = view.findViewById(R.id.tvClubSeleccionado);
        
        // Inicializar TextViews del resumen
        tvResumenClub = view.findViewById(R.id.tvResumenClub);
        tvResumenPista = view.findViewById(R.id.tvResumenPista);
        tvResumenDia = view.findViewById(R.id.tvResumenDia);
        tvResumenHora = view.findViewById(R.id.tvResumenHora);
        tvResumenDuracion = view.findViewById(R.id.tvResumenDuracion);
        tvResumenPrecio = view.findViewById(R.id.tvResumenPrecio);
        
        // Botón Reservar Principal
        btnReservarPista = view.findViewById(R.id.btnReservarPista);
        btnReservarPista.setOnClickListener(v -> procesarReserva());

        // Los botones de Sorteos y Torneos se han eliminado de la vista de jugador.

        // Botón atrás
        view.findViewById(R.id.btnBackReservarPista).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });


        // obtener referencias a contenedores que añadimos
        containerResumen = view.findViewById(R.id.containerResumen);
        containerAcciones = view.findViewById(R.id.containerAcciones);

        // Si este fragment se abrió con un club en los argumentos, cargar sus pistas
        Bundle args = getArguments();
        if (args != null && args.containsKey("clubId")) {
            String clubId = args.getString("clubId");
            String clubName = args.getString("clubName");
            clubSeleccionado = new Club(clubId, clubName, "", "");
            tvClubSeleccionado.setText("Club seleccionado: " + clubName);
            tvResumenClub.setText(clubName);
            cargarPistasPorClub(clubId);
        }

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

    // showClubsOnly/showFullReservationView removed: navigation now handled with ClubsFragment -> ReservarPistaFragment

    // cargarClubes removed: clubs list is provided by ClubsFragment

    @Override
    public void onClubClick(Club club) {
        clubSeleccionado = club;
        tvClubSeleccionado.setText("Club seleccionado: " + club.getNombre());
        tvResumenClub.setText(club.getNombre());
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
            pistaSeleccionada = pista;
            tvResumenPista.setText(pista.getNombre());
            calcularPrecioDinamico();
            Toast.makeText(requireContext(), "Pista seleccionada: " + pista.getNombre(), Toast.LENGTH_SHORT).show();
        });
        recyclerPistas.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerPistas.setAdapter(adapter);
    }


    private void configurarDias() {
        containerDias.removeAllViews();
        containerDias.setPadding(24, 12, 24, 12);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdfDia = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat sdfFecha = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat sdfMes = new SimpleDateFormat("MMM", Locale.getDefault());

        // Mostrar 30 días con estructura clara: día arriba, número en botón, mes abajo
        for (int i = 0; i < 30; i++) {
            // Contenedor vertical para cada fecha
            android.widget.LinearLayout contenedorFecha = new android.widget.LinearLayout(requireContext());
            contenedorFecha.setOrientation(android.widget.LinearLayout.VERTICAL);
            contenedorFecha.setGravity(android.view.Gravity.CENTER);
            contenedorFecha.setPadding(0, 0, 0, 0);

            android.widget.LinearLayout.LayoutParams contenedorParams = new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
            contenedorParams.setMargins(20, 8, 20, 8);
            contenedorFecha.setLayoutParams(contenedorParams);

            String dia = sdfDia.format(calendar.getTime()).toUpperCase().substring(0, 3);
            String fecha = sdfFecha.format(calendar.getTime());
            String mes = sdfMes.format(calendar.getTime()).toUpperCase();

            // Día de la semana (arriba)
            TextView tvDia = new TextView(requireContext());
            tvDia.setText(dia);
            tvDia.setTextColor(getResources().getColor(android.R.color.white));
            tvDia.setTextSize(11);
            tvDia.setGravity(android.view.Gravity.CENTER);
            tvDia.setAlpha(0.8f);

            // Botón con solo el número (clickeable)
            android.widget.Button btnFecha = new android.widget.Button(requireContext());
            btnFecha.setText(fecha);
            btnFecha.setTextColor(getResources().getColor(android.R.color.white));
            btnFecha.setTextSize(18);
            btnFecha.setTypeface(null, android.graphics.Typeface.BOLD);
            btnFecha.setBackgroundResource(R.drawable.bg_evento_card);
            btnFecha.setPadding(16, 12, 16, 12);
            btnFecha.setMinWidth(60);
            btnFecha.setMinHeight(60);

            android.widget.LinearLayout.LayoutParams btnParams = new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
            btnParams.setMargins(0, 6, 0, 6);
            btnFecha.setLayoutParams(btnParams);

            // Mes (abajo)
            TextView tvMes = new TextView(requireContext());
            tvMes.setText(mes);
            tvMes.setTextColor(getResources().getColor(android.R.color.white));
            tvMes.setTextSize(10);
            tvMes.setGravity(android.view.Gravity.CENTER);
            tvMes.setAlpha(0.7f);

            contenedorFecha.addView(tvDia);
            contenedorFecha.addView(btnFecha);
            contenedorFecha.addView(tvMes);

            // Click listener en el botón
            btnFecha.setOnClickListener(v -> {
                selectedDay = fecha + " " + mes;
                cambiarColorDiaSeleccionado(btnFecha);
                tvResumenDia.setText(selectedDay);
                calcularPrecioDinamico();
                Toast.makeText(requireContext(), "Día seleccionado: " + selectedDay, Toast.LENGTH_SHORT).show();
            });

            containerDias.addView(contenedorFecha);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void cambiarColorDiaSeleccionado(android.widget.Button btnSeleccionado) {
        for (int i = 0; i < containerDias.getChildCount(); i++) {
            View view = containerDias.getChildAt(i);
            if (view instanceof android.widget.LinearLayout) {
                android.widget.LinearLayout contenedor = (android.widget.LinearLayout) view;
                for (int j = 0; j < contenedor.getChildCount(); j++) {
                    View child = contenedor.getChildAt(j);
                    if (child instanceof android.widget.Button) {
                        ((android.widget.Button) child).setBackgroundResource(R.drawable.bg_evento_card);
                    }
                }
            }
        }
        btnSeleccionado.setBackgroundColor(getResources().getColor(R.color.accent_orange));
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
                tvResumenHora.setText(selectedHour);
                calcularPrecioDinamico();
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
        tvResumenDuracion.setText(duracion + " min");
        calcularPrecioDinamico();
        Toast.makeText(requireContext(), "Duración: " + duracion + " min", Toast.LENGTH_SHORT).show();
    }

    private void calcularPrecioDinamico() {
        if (pistaSeleccionada == null || selectedHour.isEmpty() || selectedDuration.isEmpty()) {
            tvResumenPrecio.setText("A calcular según hora");
            return;
        }

        // Obtener la hora de inicio (ej: "08:00" -> 8)
        String[] partes = selectedHour.split(":");
        int hora = Integer.parseInt(partes[0]);
        int duracion = Integer.parseInt(selectedDuration);

        // Precio base de la pista
        double precioBase = pistaSeleccionada.getPrecio();

        // Aplicar multiplicador según hora (horas punta: 18-21, tarifa más alta)
        double multiplicador = 1.0;
        if (hora >= 18 && hora <= 21) {
            multiplicador = 1.5; // +50% en horas punta
        } else if (hora >= 12 && hora < 18) {
            multiplicador = 1.2; // +20% en tarde
        }

        // Calcular precio: (precioBase * multiplicador) * (duracion / 60)
        double precioTotal = (precioBase * multiplicador) * (duracion / 60.0);

        tvResumenPrecio.setText(String.format("€%.2f", precioTotal));
    }

    private void procesarReserva() {
        // Validar que todos los datos estén seleccionados
        if (clubSeleccionado == null) {
            Toast.makeText(requireContext(), "Selecciona un club", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pistaSeleccionada == null) {
            Toast.makeText(requireContext(), "Selecciona una pista", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDay.isEmpty()) {
            Toast.makeText(requireContext(), "Selecciona un día", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedHour.isEmpty()) {
            Toast.makeText(requireContext(), "Selecciona una hora", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ir a pasarela de pago
        Intent intent = new Intent(requireContext(), PaymentActivity.class);
        intent.putExtra("clubNombre", clubSeleccionado.getNombre());
        intent.putExtra("pistaNombre", pistaSeleccionada.getNombre());
        intent.putExtra("dia", selectedDay);
        intent.putExtra("hora", selectedHour);
        intent.putExtra("duracion", selectedDuration);
        intent.putExtra("precio", pistaSeleccionada.getPrecio());
        startActivity(intent);
    }

}



