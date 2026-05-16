package com.example.focus.acitivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.focus.AddTask.AddTaskHelper;
import com.example.focus.NavBar.NavHelper;
import com.example.focus.R;
import com.example.focus.notifications.FocusNotificationManager;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

public class ActivityNotifications extends AppCompatActivity {

    // ── Chaves de SharedPreferences ──────────────────────────────────────────
    private static final String PREFS_NOTIF        = "notif_prefs";
    private static final String KEY_MASTER         = "master_enabled";
    private static final String KEY_MORNING        = "morning_enabled";
    private static final String KEY_AFTERNOON      = "afternoon_enabled";
    private static final String KEY_NIGHT          = "night_enabled";
    private static final String KEY_HOUR_MORNING   = "hour_morning";
    private static final String KEY_MIN_MORNING    = "min_morning";
    private static final String KEY_HOUR_AFTERNOON = "hour_afternoon";
    private static final String KEY_MIN_AFTERNOON  = "min_afternoon";
    private static final String KEY_HOUR_NIGHT     = "hour_night";
    private static final String KEY_MIN_NIGHT      = "min_night";

    // ── Defaults ─────────────────────────────────────────────────────────────
    private static final int DEFAULT_HOUR_MORNING   = 9;
    private static final int DEFAULT_HOUR_AFTERNOON = 15;
    private static final int DEFAULT_HOUR_NIGHT     = 21;

    // ── Views ─────────────────────────────────────────────────────────────────
    private Switch  switchMaster, switchMorning, switchAfternoon, switchNight;
    private TextView badgeMorning, badgeAfternoon, badgeNight;
    private View    cardMorning, cardAfternoon, cardNight;

    // ── Estado local ──────────────────────────────────────────────────────────
    private int hourMorning, minMorning;
    private int hourAfternoon, minAfternoon;
    private int hourNight, minNight;

    private SharedPreferences prefs;

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notifications);

        NavHelper.setup(this, "profile");
        AddTaskHelper.setup(this);

        prefs = getSharedPreferences(PREFS_NOTIF, MODE_PRIVATE);

        initViews();
        carregarPreferencias();
        setupListeners();
    }

    // ── Inicializa views ──────────────────────────────────────────────────────
    private void initViews() {
        switchMaster    = findViewById(R.id.switchMaster);
        switchMorning   = findViewById(R.id.switchMorning);
        switchAfternoon = findViewById(R.id.switchAfternoon);
        switchNight     = findViewById(R.id.switchNight);

        badgeMorning    = findViewById(R.id.badgeMorning);
        badgeAfternoon  = findViewById(R.id.badgeAfternoon);
        badgeNight      = findViewById(R.id.badgeNight);

        cardMorning     = findViewById(R.id.cardMorning);
        cardAfternoon   = findViewById(R.id.cardAfternoon);
        cardNight       = findViewById(R.id.cardNight);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    // ── Carrega preferências salvas ───────────────────────────────────────────
    private void carregarPreferencias() {
        boolean master    = prefs.getBoolean(KEY_MASTER,    true);
        boolean morning   = prefs.getBoolean(KEY_MORNING,   true);
        boolean afternoon = prefs.getBoolean(KEY_AFTERNOON, true);
        boolean night     = prefs.getBoolean(KEY_NIGHT,     true);

        hourMorning   = prefs.getInt(KEY_HOUR_MORNING,   DEFAULT_HOUR_MORNING);
        minMorning    = prefs.getInt(KEY_MIN_MORNING,    0);
        hourAfternoon = prefs.getInt(KEY_HOUR_AFTERNOON, DEFAULT_HOUR_AFTERNOON);
        minAfternoon  = prefs.getInt(KEY_MIN_AFTERNOON,  0);
        hourNight     = prefs.getInt(KEY_HOUR_NIGHT,     DEFAULT_HOUR_NIGHT);
        minNight      = prefs.getInt(KEY_MIN_NIGHT,      0);

        // Aplica estado inicial sem disparar listeners
        switchMaster.setChecked(master);
        switchMorning.setChecked(morning);
        switchAfternoon.setChecked(afternoon);
        switchNight.setChecked(night);

        atualizarBadge(badgeMorning,   hourMorning,   minMorning);
        atualizarBadge(badgeAfternoon, hourAfternoon, minAfternoon);
        atualizarBadge(badgeNight,     hourNight,     minNight);

        atualizarOpacidadeCards(master);
        atualizarOpacidadeCard(cardMorning,    morning   && master);
        atualizarOpacidadeCard(cardAfternoon,  afternoon && master);
        atualizarOpacidadeCard(cardNight,      night     && master);
    }

    // ── Configura listeners ───────────────────────────────────────────────────
    private void setupListeners() {

        // Toggle master: ativa/desativa tudo
        switchMaster.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean(KEY_MASTER, isChecked).apply();
            atualizarOpacidadeCards(isChecked);

            if (isChecked) {
                // Reagenda somente os que estavam habilitados individualmente
                if (switchMorning.isChecked())   agendarMorning();
                if (switchAfternoon.isChecked()) agendarAfternoon();
                if (switchNight.isChecked())     agendarNight();
            } else {
                FocusNotificationManager.cancelarTodas(this);
            }

            Toast.makeText(this,
                    isChecked ? "Notificações ativadas" : "Notificações desativadas",
                    Toast.LENGTH_SHORT).show();
        });

        // Toggle manhã
        switchMorning.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean(KEY_MORNING, isChecked).apply();
            atualizarOpacidadeCard(cardMorning, isChecked && switchMaster.isChecked());
            if (isChecked && switchMaster.isChecked()) agendarMorning();
            else cancelarAlarm(1001);
        });

        // Toggle tarde
        switchAfternoon.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean(KEY_AFTERNOON, isChecked).apply();
            atualizarOpacidadeCard(cardAfternoon, isChecked && switchMaster.isChecked());
            if (isChecked && switchMaster.isChecked()) agendarAfternoon();
            else cancelarAlarm(1002);
        });

        // Toggle noite
        switchNight.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean(KEY_NIGHT, isChecked).apply();
            atualizarOpacidadeCard(cardNight, isChecked && switchMaster.isChecked());
            if (isChecked && switchMaster.isChecked()) agendarNight();
            else cancelarAlarm(1003);
        });

        // Badges abrem o TimePicker
        badgeMorning.setOnClickListener(v -> abrirTimePicker(
                "Horário da manhã", hourMorning, minMorning, (h, m) -> {
                    hourMorning = h;
                    minMorning  = m;
                    prefs.edit()
                            .putInt(KEY_HOUR_MORNING, h)
                            .putInt(KEY_MIN_MORNING,  m)
                            .apply();
                    atualizarBadge(badgeMorning, h, m);
                    if (switchMaster.isChecked() && switchMorning.isChecked()) agendarMorning();
                }));

        badgeAfternoon.setOnClickListener(v -> abrirTimePicker(
                "Horário da tarde", hourAfternoon, minAfternoon, (h, m) -> {
                    hourAfternoon = h;
                    minAfternoon  = m;
                    prefs.edit()
                            .putInt(KEY_HOUR_AFTERNOON, h)
                            .putInt(KEY_MIN_AFTERNOON,  m)
                            .apply();
                    atualizarBadge(badgeAfternoon, h, m);
                    if (switchMaster.isChecked() && switchAfternoon.isChecked()) agendarAfternoon();
                }));

        badgeNight.setOnClickListener(v -> abrirTimePicker(
                "Horário da noite", hourNight, minNight, (h, m) -> {
                    hourNight = h;
                    minNight  = m;
                    prefs.edit()
                            .putInt(KEY_HOUR_NIGHT, h)
                            .putInt(KEY_MIN_NIGHT,  m)
                            .apply();
                    atualizarBadge(badgeNight, h, m);
                    if (switchMaster.isChecked() && switchNight.isChecked()) agendarNight();
                }));
    }

    // ── TimePicker do Material Design ─────────────────────────────────────────
    private void abrirTimePicker(String titulo, int horaAtual, int minAtual, TimePickerCallback callback) {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(horaAtual)
                .setMinute(minAtual)
                .setTitleText(titulo)
                .build();

        picker.addOnPositiveButtonClickListener(v ->
                callback.onTimePicked(picker.getHour(), picker.getMinute()));

        picker.show(getSupportFragmentManager(), "timepicker_" + titulo);
    }

    // ── Alarmes ───────────────────────────────────────────────────────────────
    private void agendarMorning() {
        FocusNotificationManager.agendarAlarm(this, hourMorning, minMorning, 1001, "morning");
    }

    private void agendarAfternoon() {
        FocusNotificationManager.agendarAlarm(this, hourAfternoon, minAfternoon, 1002, "afternoon");
    }

    private void agendarNight() {
        FocusNotificationManager.agendarAlarm(this, hourNight, minNight, 1003, "night");
    }

    private void cancelarAlarm(int requestCode) {
        // Cancela o alarme específico via FocusNotificationManager
        FocusNotificationManager.cancelarAlarm(this, requestCode);
    }

    // ── Helpers de UI ─────────────────────────────────────────────────────────
    private void atualizarBadge(TextView badge, int hora, int min) {
        badge.setText(String.format("%02d:%02d", hora, min));
    }

    private void atualizarOpacidadeCards(boolean ativo) {
        float alpha = ativo ? 1f : 0.45f;
        cardMorning.setAlpha(alpha);
        cardAfternoon.setAlpha(alpha);
        cardNight.setAlpha(alpha);
        switchMorning.setEnabled(ativo);
        switchAfternoon.setEnabled(ativo);
        switchNight.setEnabled(ativo);
        badgeMorning.setEnabled(ativo);
        badgeAfternoon.setEnabled(ativo);
        badgeNight.setEnabled(ativo);
    }

    private void atualizarOpacidadeCard(View card, boolean ativo) {
        card.setAlpha(ativo ? 1f : 0.45f);
    }

    // ── Interface callback interna ────────────────────────────────────────────
    private interface TimePickerCallback {
        void onTimePicked(int hora, int min);
    }
}
