package com.example.tmanager;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.widget.PopupMenu;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    FragmentManager fragmentManager;
    private boolean isJugador = false;
    private ImageButton btnNotificacionesTop;
    private ImageButton btnMenuTop;

    private static final int REQ_NOTIF = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pedirPermisoNotificaciones();

        // Guardar token FCM para notificaciones.
        FCMUtil.guardarToken();

        fragmentManager = getSupportFragmentManager();

        btnNotificacionesTop = findViewById(R.id.btnNotificacionesTop);
        btnMenuTop = findViewById(R.id.btnMenuTop);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Determinar rol del usuario y ajustar comportamiento de navegación
        AuthUtil.isJugador(this, isJ -> isJugador = isJ);

        btnNotificacionesTop.setOnClickListener(v ->
                startActivity(new Intent(this, NotificacionesActivity.class)));

        btnMenuTop.setOnClickListener(v -> mostrarMenuSuperior(v));

        bottomNavigationView.setOnItemSelectedListener(item -> {
            boolean loaded = loadFragmentForItem(item.getItemId());
            return loaded;
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


    private void mostrarMenuSuperior(android.view.View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.menu_superior, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(this::onMenuSuperiorClick);
        popupMenu.show();
    }

    private boolean onMenuSuperiorClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_editar_perfil) {
            startActivity(new Intent(this, MiInformacionActivity.class));
            return true;
        } else if (id == R.id.menu_cambiar_idioma) {
            mostrarSelectorIdioma();
            return true;
        } else if (id == R.id.menu_ajustes_basicos) {
            startActivity(new Intent(this, AjustesBasicosActivity.class));
            return true;
        } else if (id == R.id.menu_soporte) {
            startActivity(new Intent(this, SoporteActivity.class));
            return true;
        } else if (id == R.id.menu_cerrar_sesion) {
            com.example.tmanager.network.Backend.logout(this);
            SessionNavigator.goToLoginClean(this);
            return true;
        }
        return false;
    }

    private void mostrarSelectorIdioma() {
        String[] opciones = {"Español", "English"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cambiar idioma")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es"));
                    } else {
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"));
                    }
                    Toast.makeText(this, "Idioma actualizado", Toast.LENGTH_SHORT).show();
                })
                .show();
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
