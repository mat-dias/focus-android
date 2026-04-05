package com.example.focus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView txtWelcome, txtXP, txtStreak;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavHelper.setup(this, "home");

        // 🔥 VERIFICA LOGIN
        prefs = getSharedPreferences("user", MODE_PRIVATE);
        if (!prefs.getBoolean("logado", false)) {
            startActivity(new Intent(this, ActivityLogin.class));
            finish();
            return;
        }

        initViews();
        carregarDados();
        NavHelper.setup(this, "home");
    }

    private void initViews() {
        txtWelcome = findViewById(R.id.txtWelcome);
        // txtXP = findViewById(R.id.txtXP);
        // txtStreak = findViewById(R.id.txtStreak);
    }

    private void carregarDados() {
        String nome = prefs.getString("nome", "Usuário");
        int xp = prefs.getInt("xp", 0);
        int streak = prefs.getInt("streak", 0);

        txtWelcome.setText("Olá, " + nome + "!");
        // txtXP.setText(xp + " XP");
        // txtStreak.setText(streak + " dias 🔥");
    }

    // 🔥 LOGOUT (chame no botão ou menu)
    public void fazerLogout() {
        prefs.edit().clear().apply();
        startActivity(new Intent(this, ActivityLogin.class));
        finish();
    }
}


