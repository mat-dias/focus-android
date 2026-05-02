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
import android.widget.LinearLayout;
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

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int PERM_NOTIF = 100;

    private TextView txtWelcome, txtXP, txtStreak;
    private TextView txtStatus, txtPendentes, txtConcluidasHoje;
    private LinearLayout bannerPendente;
    private SharedPreferences prefs;
    private PomodoroController pomodoro;

    // Handler para rotacionar mensagens de status
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int statusIdx = 0;
    private final String[] statusMsgs = {
            "Pronto para focar! 🎯",
            "Cada tarefa concluída = +67 XP ⭐",
            "Mantenha seu streak em dia 🔥",
            "Pequenos passos, grandes resultados 💪"
    };

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

        initViews();
        carregarDados();

        NavHelper.setup(this, "home");
        AddTaskHelper.setup(this);

        pomodoro = new PomodoroController(this);

        // Canais de notificação
        FocusNotificationManager.criarCanais(this);
        solicitarPermissaoNotificacao();

        // Rotação de mensagens de status
        iniciarRotacaoStatus();

        // Carrega tarefas pendentes para exibir banner
        verificarTarefasPendentes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Atualiza XP e streak ao voltar para a tela
        carregarDados();
        verificarTarefasPendentes();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    // ── Views ─────────────────────────────────────────────────────────────────
    private void initViews() {
        txtWelcome       = findViewById(R.id.txtWelcome);
        txtXP            = findViewById(R.id.txtXP);
        txtStreak        = findViewById(R.id.txtStreak);
        txtStatus        = findViewById(R.id.txtStatus);
        txtPendentes     = findViewById(R.id.txtPendentes);
        txtConcluidasHoje = findViewById(R.id.txtConcluidasHoje);
        bannerPendente   = findViewById(R.id.bannerPendente);
    }

    // ── Dados do SharedPreferences ────────────────────────────────────────────
    private void carregarDados() {
        String nome = prefs.getString("nome", "Usuário");
        int xp      = prefs.getInt("xp", 0);
        int streak  = prefs.getInt("streak", 0);

        txtWelcome.setText("Olá, " + nome + "!");
        txtXP.setText(xp + " XP");
        txtStreak.setText(String.valueOf(streak));
    }

    // ── Rotação de mensagens de status ────────────────────────────────────────
    private void iniciarRotacaoStatus() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (txtStatus != null) {
                    statusIdx = (statusIdx + 1) % statusMsgs.length;
                    txtStatus.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                        txtStatus.setText(statusMsgs[statusIdx]);
                        txtStatus.animate().alpha(1f).setDuration(300).start();
                    }).start();
                }
                handler.postDelayed(this, 4000);
            }
        }, 4000);
    }

    // ── Verifica tarefas pendentes via API ────────────────────────────────────
    private void verificarTarefasPendentes() {
        int profileId = prefs.getInt("profile_id", 0);
        if (profileId == 0 || bannerPendente == null) return;

        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.getTasks(profileId).enqueue(new Callback<TaskResponse>() {

            @Override
            public void onResponse(Call<TaskResponse> call, Response<TaskResponse> response) {
                if (!response.isSuccessful()
                        || response.body() == null
                        || !"ok".equals(response.body().status)) return;

                List<TaskResponse.TaskItem> tasks = response.body().tasks;
                if (tasks == null) return;

                int pendentes  = 0;
                int concluidas = 0;
                for (TaskResponse.TaskItem t : tasks) {
                    if (t.done) concluidas++;
                    else        pendentes++;
                }

                final int p = pendentes;
                final int c = concluidas;

                runOnUiThread(() -> atualizarBanner(p, c));
            }

            @Override
            public void onFailure(Call<TaskResponse> call, Throwable t) { /* silencioso */ }
        });
    }

    private void atualizarBanner(int pendentes, int concluidas) {
        if (bannerPendente == null) return;

        if (txtConcluidasHoje != null)
            txtConcluidasHoje.setText(concluidas + " concluída" + (concluidas != 1 ? "s" : "") + " hoje");

        if (pendentes == 0) {
            // Todas feitas!
            bannerPendente.setVisibility(View.VISIBLE);
            bannerPendente.setBackgroundColor(Color.parseColor("#1A2E1A"));
            if (txtPendentes != null) {
                txtPendentes.setText("🎉 Todas as tarefas concluídas!");
                txtPendentes.setTextColor(Color.parseColor("#4ADE80"));
            }
        } else {
            // Ainda há pendências
            bannerPendente.setVisibility(View.VISIBLE);
            bannerPendente.setBackgroundColor(Color.parseColor("#2A1A1A"));
            if (txtPendentes != null) {
                txtPendentes.setText("⚠️ " + pendentes + " tarefa" + (pendentes != 1 ? "s" : "") + " pendente" + (pendentes != 1 ? "s" : "") + " hoje");
                txtPendentes.setTextColor(Color.parseColor("#FFAA44"));
            }
        }
    }

    // ── Permissão de notificação (Android 13+) ────────────────────────────────
    private void solicitarPermissaoNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERM_NOTIF);
            } else {
                agendarNotificacoes();
            }
        } else {
            agendarNotificacoes();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERM_NOTIF
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            agendarNotificacoes();
        }
    }

    private void agendarNotificacoes() {
        FocusNotificationManager.agendarNotificacaoManha(this);
        FocusNotificationManager.agendarNotificacaoTarde(this);
        FocusNotificationManager.agendarNotificacaoNoite(this);
    }
}