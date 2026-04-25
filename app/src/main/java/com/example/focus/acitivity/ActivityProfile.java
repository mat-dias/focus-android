package com.example.focus.acitivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.focus.AddTask.AddTaskHelper;
import com.example.focus.NavBar.NavHelper;
import com.example.focus.R;

public class ActivityProfile extends AppCompatActivity {

    private TextView txtNome, txtEmail, txtXP, txtStreak;
    private LinearLayout btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        NavHelper.setup(this, "profile");

        AddTaskHelper.setup(this);


        initViews();
        carregarDados();
        setupLogout();
    }

    private void initViews() {
        txtNome = findViewById(R.id.txtProfileName);
        txtEmail = findViewById(R.id.txtProfileEmail);
        txtXP = findViewById(R.id.txtProfileXP);
        txtStreak = findViewById(R.id.txtProfileStreak);

        btnLogout = findViewById(R.id.btnLogout);
    }

    private void carregarDados() {
        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);

        String nome = prefs.getString("nome", "Usuário");
        String email = prefs.getString("email", "email@exemplo.com");
        int xp = prefs.getInt("xp", 0);
        int streak = prefs.getInt("streak", 0);

        txtNome.setText(nome);
        txtEmail.setText(email);
        txtXP.setText(xp + " XP");
        txtStreak.setText(String.valueOf(streak));
    }

    private void setupLogout() {

        btnLogout.setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle("Sair da conta")
                    .setMessage("Tem certeza que deseja sair?")
                    .setPositiveButton("Sair", (dialog, which) -> {

                        // 🔥 limpa sessão
                        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
                        prefs.edit().clear().apply();

                        Toast.makeText(this, "Logout realizado", Toast.LENGTH_SHORT).show();

                        // 🔥 volta pro login e limpa histórico
                        Intent intent = new Intent(this, ActivityLogin.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }
}