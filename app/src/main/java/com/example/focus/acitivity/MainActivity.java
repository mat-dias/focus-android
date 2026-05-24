package com.example.focus.acitivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.focus.AddTask.AddTaskHelper;
import com.example.focus.FocusMode.PomodoroController;
import com.example.focus.NavBar.NavHelper;
import com.example.focus.R;
import com.example.focus.network.ApiService;
import com.example.focus.network.RetrofitClient;
import com.example.focus.notifications.FocusNotificationManager;
import com.example.focus.responses.StatsResponse;
import com.example.focus.responses.TaskResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int PERM_NOTIF = 100;

    private TextView txtWelcome, txtXP, txtStreak;
    private TextView txtStatus, txtStatusSub, txtAvisoIndicador;
    private SharedPreferences prefs;
    private PomodoroController pomodoro;

    private final List<String[]> avisos = new ArrayList<>();
    private int avisoIdx = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("user", MODE_PRIVATE);

        if (!prefs.getBoolean("logado", false)) {
            startActivity(new Intent(this, ActivityLogin.class));
            finish();
            return;
        }

        txtWelcome        = findViewById(R.id.txtWelcome);
        txtXP             = findViewById(R.id.txtXP);
        txtStreak         = findViewById(R.id.txtStreak);
        txtStatus         = findViewById(R.id.txtStatus);
        txtStatusSub      = findViewById(R.id.txtStatusSub);
        txtAvisoIndicador = findViewById(R.id.txtAvisoIndicador);

        // Mostra nome imediatamente do cache enquanto busca do banco
        txtWelcome.setText("Olá, " + prefs.getString("nome", "Usuário") + "!");
        txtXP.setText(prefs.getInt("xp", 0) + " XP");
        txtStreak.setText(String.valueOf(prefs.getInt("streak", 0)));

        NavHelper.setup(this, "home");
        AddTaskHelper.setup(this);
        pomodoro = new PomodoroController(this);

        FocusNotificationManager.criarCanais(this);
        solicitarPermissaoNotificacao();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Sempre que volta pra tela, busca dados frescos do banco
        buscarXpEStreak();
        verificarTarefas();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
    }

    // ── Busca XP e streak do banco ────────────────────────────────────────────
    private void buscarXpEStreak() {
        int profileId = prefs.getInt("profile_id", 0);
        if (profileId == 0) return;

        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.getStats(profileId, "semana").enqueue(new Callback<StatsResponse>() {
            @Override
            public void onResponse(Call<StatsResponse> call, Response<StatsResponse> response) {
                if (!response.isSuccessful()
                        || response.body() == null
                        || !"ok".equals(response.body().status)) return;

                StatsResponse s = response.body();

                // Atualiza SharedPreferences com valores do banco
                prefs.edit()
                        .putInt("xp", s.xp)
                        .putInt("streak", s.streak)
                        .apply();

                // Atualiza UI
                runOnUiThread(() -> {
                    txtXP.setText(s.xp + " XP");
                    txtStreak.setText(String.valueOf(s.streak));
                });
            }

            @Override
            public void onFailure(Call<StatsResponse> call, Throwable t) {
                // Falha silenciosa — mantém valores do cache
            }
        });
    }

    // ── Busca tarefas e monta lista de avisos ─────────────────────────────────
    private void verificarTarefas() {
        int profileId = prefs.getInt("profile_id", 0);
        if (profileId == 0) {
            montarAvisosPadrao(0, 0);
            return;
        }

        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.getTasks(profileId).enqueue(new Callback<TaskResponse>() {
            @Override
            public void onResponse(Call<TaskResponse> call, Response<TaskResponse> response) {
                int pendentes  = 0;
                int concluidas = 0;

                if (response.isSuccessful()
                        && response.body() != null
                        && "ok".equals(response.body().status)
                        && response.body().tasks != null) {

                    for (TaskResponse.TaskItem t : response.body().tasks) {
                        if (t.done) concluidas++;
                        else        pendentes++;
                    }
                }

                final int p = pendentes, c = concluidas;
                runOnUiThread(() -> montarAvisosPadrao(p, c));
            }

            @Override
            public void onFailure(Call<TaskResponse> call, Throwable t) {
                runOnUiThread(() -> montarAvisosPadrao(0, 0));
            }
        });
    }

    // ── Monta a lista de avisos e inicia rotação ──────────────────────────────
    private void montarAvisosPadrao(int pendentes, int concluidas) {
        avisos.clear();
        int streak = prefs.getInt("streak", 0);
        int xp     = prefs.getInt("xp", 0);

        // Tarefas
        if (pendentes > 0) {
            avisos.add(new String[]{
                    "⚠️ " + pendentes + " tarefa" + (pendentes != 1 ? "s" : "") + " pendente" + (pendentes != 1 ? "s" : "") + " hoje",
                    concluidas + " concluída" + (concluidas != 1 ? "s" : "") + " até agora"
            });
        } else if (concluidas > 0) {
            avisos.add(new String[]{
                    "🎉 Todas as tarefas concluídas!",
                    "Incrível! Você completou " + concluidas + " tarefa" + (concluidas != 1 ? "s" : "") + " hoje"
            });
        } else {
            avisos.add(new String[]{
                    "📋 Nenhuma tarefa para hoje ainda",
                    "Adicione tarefas tocando no botão +"
            });
        }

        // Streak
        if (streak > 0) {
            avisos.add(new String[]{
                    "🔥 Streak de " + streak + " dia" + (streak != 1 ? "s" : "") + "!",
                    "Continue assim para não perder sua sequência"
            });
        } else {
            avisos.add(new String[]{
                    "🎯 Comece seu streak hoje!",
                    "Conclua pelo menos uma tarefa para iniciar"
            });
        }

        // XP atual
        avisos.add(new String[]{
                "⭐ Você tem " + xp + " XP no total",
                "Tarefas fáceis +10 · médias +25 · difíceis +50"
        });

        // Dicas fixas
        avisos.add(new String[]{"⏱️ Use o timer Pomodoro para focar melhor", "25 minutos de foco, 5 de descanso"});
        avisos.add(new String[]{"📊 Veja suas estatísticas em Stats", "Acompanhe sua evolução semanal"});

        handler.removeCallbacksAndMessages(null);
        avisoIdx = 0;
        exibirAviso(avisoIdx);
        iniciarRotacao();
    }

    // ── Exibe o aviso atual com fade ──────────────────────────────────────────
    private void exibirAviso(int idx) {
        if (avisos.isEmpty() || txtStatus == null) return;

        String[] aviso = avisos.get(idx);
        txtAvisoIndicador.setText((idx + 1) + " / " + avisos.size());

        txtStatus.animate().alpha(0f).setDuration(250).withEndAction(() -> {
            txtStatus.setText(aviso[0]);

            if (aviso[0].startsWith("⚠️"))     txtStatus.setTextColor(Color.parseColor("#FFAA44"));
            else if (aviso[0].startsWith("🎉")) txtStatus.setTextColor(Color.parseColor("#4ADE80"));
            else                               txtStatus.setTextColor(Color.parseColor("#AAAAAA"));

            if (aviso[1] != null && !aviso[1].isEmpty()) {
                txtStatusSub.setText(aviso[1]);
                txtStatusSub.setVisibility(View.VISIBLE);
            } else {
                txtStatusSub.setVisibility(View.GONE);
            }

            txtStatus.animate().alpha(1f).setDuration(250).start();
        }).start();
    }

    private void iniciarRotacao() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (avisos.isEmpty()) return;
                avisoIdx = (avisoIdx + 1) % avisos.size();
                exibirAviso(avisoIdx);
                handler.postDelayed(this, 4500);
            }
        }, 4500);
    }

    // ── Permissão notificação (Android 13+) ───────────────────────────────────
    private void solicitarPermissaoNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERM_NOTIF);
                return;
            }
        }
        agendarNotificacoes();
    }

    @Override
    public void onRequestPermissionsResult(int code, @NonNull String[] perms, @NonNull int[] results) {
        super.onRequestPermissionsResult(code, perms, results);
        if (code == PERM_NOTIF && results.length > 0
                && results[0] == PackageManager.PERMISSION_GRANTED)
            agendarNotificacoes();
    }

    private void agendarNotificacoes() {
        FocusNotificationManager.agendarNotificacaoManha(this);
        FocusNotificationManager.agendarNotificacaoTarde(this);
        FocusNotificationManager.agendarNotificacaoNoite(this);
    }
}