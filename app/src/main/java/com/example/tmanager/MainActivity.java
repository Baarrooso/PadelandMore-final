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

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Determinar rol del usuario y ajustar comportamiento de navegación
        AuthUtil.isJugador(this, isJ -> isJugador = isJ);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (isJugador && item.getItemId() == R.id.btnComunidad) {
                android.widget.Toast.makeText(this, "Acceso restringido en modo jugador", android.widget.Toast.LENGTH_SHORT).show();
                return false;
            }
            return loadFragmentForItem(item.getItemId());
        });

        int initialItem = getInitialMenuItem();
        bottomNavigationView.setSelectedItemId(initialItem);
        loadFragmentForItem(initialItem);
    }

    private boolean loadFragmentForItem(int itemId) {
        Fragment fragment = null;

        if (itemId == R.id.btnInicio) {
            fragment = new ReservasFragment();
        } else if (itemId == R.id.btnComunidad) {
            fragment = new MatchNivelFragment();
        } else if (itemId == R.id.btnPerfil) {
            fragment = new MiPerfilFragment();
        }

        if (fragment == null) {
            return false;
        }

        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
        return true;
    }

    private int getInitialMenuItem() {
        String open = getIntent().getStringExtra("open");
        if ("perfil".equalsIgnoreCase(open)) {
            return R.id.btnPerfil;
        }
        if ("comunidad".equalsIgnoreCase(open)) {
            return R.id.btnComunidad;
        }
        return R.id.btnInicio;
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
