package com.example.tmanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.AdapterView;
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

public class MiPerfilFragment extends Fragment {

    private EditText edtEdad;
    private Spinner spinnerNivel, spinnerMano;
    private Button btnGuardarPerfil;
    private ListView listResultados, listTorneosApuntados, listMisReservas;
    private ArrayAdapter<String> adapterResultados, adapterTorneos, adapterReservas;
    private List<String> resultados, torneos, reservas;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private static final int NIVEL_MAXIMO = 5;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_mi_perfil, container, false);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Inicializar vistas
        edtEdad = v.findViewById(R.id.edtEdad);
        spinnerNivel = v.findViewById(R.id.spinnerNivel);
        spinnerMano = v.findViewById(R.id.spinnerMano);
        btnGuardarPerfil = v.findViewById(R.id.btnGuardarPerfil);
        listResultados = v.findViewById(R.id.listResultados);
        listTorneosApuntados = v.findViewById(R.id.listTorneosApuntados);
        listMisReservas = v.findViewById(R.id.listMisReservas);

        // Inicializar listas
        resultados = new ArrayList<>();
        torneos = new ArrayList<>();
        reservas = new ArrayList<>();

        // Configurar adapters
        adapterResultados = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, resultados);
        adapterTorneos = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, torneos);
        adapterReservas = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, reservas);

        listResultados.setAdapter(adapterResultados);
        listTorneosApuntados.setAdapter(adapterTorneos);
        listMisReservas.setAdapter(adapterReservas);

        // Cargar datos del usuario
        cargarDatosUsuario();

        // Agregar listener al spinner de nivel para validar nivel máximo
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

        // Botón guardar
        btnGuardarPerfil.setOnClickListener(v1 -> guardarCambios());

        // Cargar resultados, torneos y reservas
        cargarResultados();
        cargarTorneos();
        cargarReservas();

        return v;
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
                        String edad = doc.getString("edad");
                        String nivel = doc.getString("nivel");
                        String mano = doc.getString("mano");

                        if (edad != null) {
                            edtEdad.setText(edad);
                        }

                        if (nivel != null) {
                            int posicion = getIndexInArray(R.array.niveles_padel, nivel);
                            spinnerNivel.setSelection(posicion);
                        }

                        if (mano != null) {
                            int posicion = getIndexInArray(R.array.mano_juego, mano);
                            spinnerMano.setSelection(posicion);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Error cargando perfil", Toast.LENGTH_SHORT).show()
                );
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

    private void guardarCambios() {
        if (user == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        String edad = edtEdad.getText().toString().trim();
        String nivel = spinnerNivel.getSelectedItem().toString();
        String mano = spinnerMano.getSelectedItem().toString();

        if (edad.isEmpty()) {
            Toast.makeText(requireContext(), "Introduce tu edad", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("edad", edad);
        data.put("nivel", nivel);
        data.put("mano", mano);

        db.collection("usuarios")
                .document(user.getUid())
                .update(data)
                .addOnSuccessListener(v ->
                        Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void cargarResultados() {
        if (user == null) return;

        db.collection("resultados")
                .whereEqualTo("userUid", user.getUid())
                .limit(5)
                .get()
                .addOnSuccessListener(snapshot -> {
                    resultados.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String equipo1 = doc.getString("equipo1");
                        String equipo2 = doc.getString("equipo2");
                        String marcador = doc.getString("marcador");
                        String resultado = equipo1 + " vs " + equipo2 + " - " + marcador;
                        resultados.add(resultado);
                    }

                    if (resultados.isEmpty()) {
                        resultados.add("Sin resultados aún");
                    }

                    adapterResultados.notifyDataSetChanged();
                });
    }

    private void cargarTorneos() {
        if (user == null) return;

        db.collection("torneos_inscritos")
                .whereEqualTo("userUid", user.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    torneos.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String nombreTorneo = doc.getString("nombre");
                        String fecha = doc.getString("fecha");
                        String torneo = nombreTorneo + " - " + fecha;
                        torneos.add(torneo);
                    }

                    if (torneos.isEmpty()) {
                        torneos.add("No estás inscrito en torneos");
                    }

                    adapterTorneos.notifyDataSetChanged();
                });
    }

    private void cargarReservas() {
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
                        String reserva = pista + " - " + fecha + " " + hora;
                        reservas.add(reserva);
                    }

                    if (reservas.isEmpty()) {
                        reservas.add("No tienes reservas");
                    }

                    adapterReservas.notifyDataSetChanged();
                });
    }
}
