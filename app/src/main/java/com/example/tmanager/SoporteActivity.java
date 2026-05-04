package com.example.tmanager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SoporteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soporte);

        ImageButton btnRetroceso = findViewById(R.id.btnRetroceso);
        btnRetroceso.setOnClickListener(v -> finish());

        TextView txtEmail = findViewById(R.id.txtSoporteEmail);
        Button btnCorreo = findViewById(R.id.btnContactarSoporte);

        btnCorreo.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + txtEmail.getText().toString().trim()));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Soporte TManager");
            startActivity(Intent.createChooser(intent, "Contactar soporte"));
        });
    }
}

