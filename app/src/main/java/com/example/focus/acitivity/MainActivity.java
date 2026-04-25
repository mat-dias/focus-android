package com.example.focus.acitivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.focus.AddTask.AddTaskHelper;
import com.example.focus.NavBar.NavHelper;
import com.example.focus.FocusMode.PomodoroController;
import com.example.focus.R;

public class MainActivity extends AppCompatActivity {

    private TextView txtWelcome, txtXP, txtStreak;
    private SharedPreferences prefs;

    private PomodoroController pomodoro; // 👈 controlador separado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("user", MODE_PRIVATE);

        // LOGIN
        if (!prefs.getBoolean("logado", false)) {
            startActivity(new Intent(this, ActivityLogin.class));
            finish();
            return;
        }

        initViews();
        carregarDados();

        NavHelper.setup(this, "home");

        AddTaskHelper.setup(this); // 👈 ISSO AQUI

        // 👇 INICIA O POMODORO SEPARADO
        pomodoro = new PomodoroController(this);
    }

    private void initViews() {
        txtWelcome = findViewById(R.id.txtWelcome);
        txtXP = findViewById(R.id.txtXP);
        txtStreak = findViewById(R.id.txtStreak);
    }

    private void carregarDados() {
        String nome = prefs.getString("nome", "Usuário");
        int xp = prefs.getInt("xp", 0);
        int streak = prefs.getInt("streak", 0);

        txtWelcome.setText("Olá, " + nome + "!");
        txtXP.setText(xp + " XP");
        txtStreak.setText(String.valueOf(streak));
    }
}