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

    // Lista de avisos que vão rotacionar no card
    private final List<String[]> avisos = new ArrayList<>();
    // cada item: [mensagem principal, sub-mensagem (pode ser "")]
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

        carregarDados();

        NavHelper.setup(this, "home");
        AddTaskHelper.setup(this);
        pomodoro = new PomodoroController(this);

        FocusNotificationManager.criarCanais(this);
        solicitarPermissaoNotificacao();

        // Carrega tarefas e monta os avisos
        verificarTarefas();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarDados();
        verificarTarefas();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
    }

    // ── Dados do SharedPreferences ────────────────────────────────────────────
    private void carregarDados() {
        txtWelcome.setText("Olá, " + prefs.getString("nome", "Usuário") + "!");
        txtXP.setText(prefs.getInt("xp", 0) + " XP");
        txtStreak.setText(String.valueOf(prefs.getInt("streak", 0)));
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

        // Aviso de tarefas pendentes ou conclusão total
        if (pendentes > 0) {
            avisos.add(new String[]{
                    "⚠️ " + pendentes + " tarefa" + (pendentes != 1 ? "s" : "") + " pendente" + (pendentes != 1 ? "s" : "") + " hoje",
                    concluidas + " concluída" + (concluidas != 1 ? "s" : "") + " até agora"
            });
        } else if (concluidas > 0) {
            avisos.add(new String[]{
                    "  Todas as tarefas concluídas!",
                    "Incrível! Você completou " + concluidas + " tarefa" + (concluidas != 1 ? "s" : "") + " hoje"
            });
        } else {
            avisos.add(new String[]{
                    "📋 Nenhuma tarefa para hoje ainda",
                    "Adicione tarefas tocando no botão +"
            });
        }

        // Aviso de streak
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

        // Dicas motivacionais fixas
        avisos.add(new String[]{"💪 Cada tarefa concluída = +67 XP", ""});
        avisos.add(new String[]{"⏱️ Use o timer Pomodoro para focar melhor", "25 minutos de foco, 5 de descanso"});
        avisos.add(new String[]{"📊 Veja suas estatísticas em Stats", "Acompanhe sua evolução semanal"});

        // Inicia rotação
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

            // Cor dinâmica baseada no conteúdo
            if (aviso[0].startsWith("⚠️"))       txtStatus.setTextColor(Color.parseColor("#FFAA44"));
            else if (aviso[0].startsWith("🎉"))   txtStatus.setTextColor(Color.parseColor("#4ADE80"));
            else                                   txtStatus.setTextColor(Color.parseColor("#AAAAAA"));

            // Sub-mensagem
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
        if (code == PERM_NOTIF && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED)
            agendarNotificacoes();
    }

    private void agendarNotificacoes() {
        FocusNotificationManager.agendarNotificacaoManha(this);
        FocusNotificationManager.agendarNotificacaoTarde(this);
        FocusNotificationManager.agendarNotificacaoNoite(this);
    }
}