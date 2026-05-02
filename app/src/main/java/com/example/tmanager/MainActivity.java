package com.example.tmanager;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    FragmentManager fragmentManager;
    private boolean isJugador = false;

    private static final int REQ_NOTIF = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pedirPermisoNotificaciones();

        // Guardar token FCM para notificaciones.
        FCMUtil.guardarToken();

        fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction()
                .replace(R.id.fragmentSuperior, new ReservasFragment())
                .commit();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Determinar rol del usuario y ajustar comportamiento de navegación
        AuthUtil.isJugador(this, isJ -> isJugador = isJ);

        bottomNavigationView.setSelectedItemId(R.id.btnInicio);
        loadFragment(new ReservasFragment());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            // Si es jugador, restringir acceso a pantallas ajenas a reservas/torneos/sorteos
            if (isJugador && item.getItemId() == R.id.btnComunidad) {
                android.widget.Toast.makeText(this, "Acceso restringido en modo jugador", android.widget.Toast.LENGTH_SHORT).show();
                return false;
            }
            Fragment fragment = null;

            if (item.getItemId() == R.id.btnInicio) {
                fragment = new ReservasFragment();
            } else if (item.getItemId() == R.id.btnComunidad) {
                fragment = new MatchNivelFragment();
            } else if (item.getItemId() == R.id.btnPerfil) {
                fragment = new MiPerfilFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void pedirPermisoNotificaciones() {

        if (Build.VERSION.SDK_INT >= 33) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        REQ_NOTIF
                );
            }
        }
    }
}
