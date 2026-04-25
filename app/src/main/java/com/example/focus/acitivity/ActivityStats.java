package com.example.focus.acitivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.focus.AddTask.AddTaskHelper;
import com.example.focus.NavBar.NavHelper;
import com.example.focus.R;
import com.example.focus.network.ApiService;
import com.example.focus.network.RetrofitClient;
import com.example.focus.responses.StatsResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityStats extends AppCompatActivity {

    private TextView txtStatsStreak, txtStatsXP;
    private TextView txtTodayTasks, txtTodayCompleted, txtTodayXP;
    private LinearLayout chartWeeklyBars, chartMonthlyContainer;
    private int profileId;

    // Cores para tags do gráfico mensal
    private final int[] CORES = {
            Color.parseColor("#4ADE80"),
            Color.parseColor("#EC4899"),
            Color.parseColor("#FFAA44"),
            Color.parseColor("#06B6D4"),
            Color.parseColor("#A78BFA")
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        AddTaskHelper.setup(this);
        NavHelper.setup(this, "stats");

        txtStatsStreak      = findViewById(R.id.txtStatsStreak);
        txtStatsXP          = findViewById(R.id.txtStatsXP);
        txtTodayTasks       = findViewById(R.id.txtTodayTasks);
        txtTodayCompleted   = findViewById(R.id.txtTodayCompleted);
        txtTodayXP          = findViewById(R.id.txtTodayXP);
        chartWeeklyBars     = findViewById(R.id.chartWeeklyBars);
        chartMonthlyContainer = findViewById(R.id.chartMonthlyContainer);

        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        profileId = prefs.getInt("profile_id", 0);

        carregarStats();
    }

    // ── Carrega stats da API ──────────────────────────────────────────────────
    private void carregarStats() {

        if (profileId == 0) {
            Toast.makeText(this, "Sessão inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = RetrofitClient.getClient().create(ApiService.class);

        api.getStats(profileId).enqueue(new Callback<StatsResponse>() {

            @Override
            public void onResponse(Call<StatsResponse> call, Response<StatsResponse> response) {
                if (!response.isSuccessful()
                        || response.body() == null
                        || !"ok".equals(response.body().status)) {
                    Toast.makeText(ActivityStats.this,
                            "Erro ao carregar estatísticas", Toast.LENGTH_SHORT).show();
                    return;
                }
                renderizar(response.body());
            }

            @Override
            public void onFailure(Call<StatsResponse> call, Throwable t) {
                Toast.makeText(ActivityStats.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Renderiza tudo ────────────────────────────────────────────────────────
    private void renderizar(StatsResponse s) {

        // Cards de streak e XP
        txtStatsStreak.setText(String.valueOf(s.streak));
        txtStatsXP.setText(s.xp + " XP");

        // Status hoje
        txtTodayTasks.setText(String.valueOf(s.hojeTotal));
        txtTodayCompleted.setText(String.valueOf(s.hojeConcluidas));

        // XP hoje = concluídas hoje × 10 (regra simples)
        txtTodayXP.setText("+" + (s.hojeConcluidas * 10));

        // Gráfico semanal de barras
        renderizarBarras(s.semana);

        // Gráfico mensal de tags
        renderizarTags(s.tags);
    }

    // ── Gráfico de barras semanal ─────────────────────────────────────────────
    private void renderizarBarras(List<Integer> semana) {

        chartWeeklyBars.removeAllViews();

        if (semana == null || semana.isEmpty()) return;

        int dp   = (int) getResources().getDisplayMetrics().density;
        int max  = 1;
        for (int v : semana) if (v > max) max = v;

        int maxAlturaBar = 120 * dp; // altura máxima da barra em px

        for (int i = 0; i < semana.size(); i++) {
            int valor = semana.get(i);

            LinearLayout coluna = new LinearLayout(this);
            coluna.setOrientation(LinearLayout.VERTICAL);
            coluna.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams colParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            coluna.setLayoutParams(colParams);

            // Número acima da barra
            TextView tvNum = new TextView(this);
            tvNum.setText(valor > 0 ? String.valueOf(valor) : "");
            tvNum.setTextColor(Color.parseColor("#888888"));
            tvNum.setTextSize(10);
            tvNum.setGravity(Gravity.CENTER);
            coluna.addView(tvNum);

            // Barra
            View bar = new View(this);
            int alturaBar = valor == 0 ? 4 * dp : (int) ((valor / (float) max) * maxAlturaBar);
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                    20 * dp, alturaBar);
            barParams.setMargins(4 * dp, 4 * dp, 4 * dp, 0);
            bar.setLayoutParams(barParams);
            bar.setBackgroundColor(valor > 0
                    ? Color.parseColor("#06B6D4")
                    : Color.parseColor("#333333"));

            // Arredonda cantos via background programático
            android.graphics.drawable.GradientDrawable shape =
                    new android.graphics.drawable.GradientDrawable();
            shape.setColor(valor > 0
                    ? Color.parseColor("#06B6D4")
                    : Color.parseColor("#333333"));
            shape.setCornerRadii(new float[]{6*dp, 6*dp, 6*dp, 6*dp, 0, 0, 0, 0});
            bar.setBackground(shape);

            coluna.addView(bar);
            chartWeeklyBars.addView(coluna);
        }
    }

    // ── Legenda de tags mensal ────────────────────────────────────────────────
    private void renderizarTags(List<StatsResponse.TagItem> tags) {

        // Remove apenas as views de legenda (mantém título e círculo)
        // O container tem: título(0), círculo(1), legenda(2+)
        while (chartMonthlyContainer.getChildCount() > 2) {
            chartMonthlyContainer.removeViewAt(2);
        }

        int dp = (int) getResources().getDisplayMetrics().density;

        if (tags == null || tags.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("Nenhuma tag registrada este mês");
            tv.setTextColor(Color.parseColor("#666666"));
            tv.setTextSize(13);
            chartMonthlyContainer.addView(tv);
            return;
        }

        for (int i = 0; i < tags.size(); i++) {
            StatsResponse.TagItem item = tags.get(i);
            int cor = CORES[i % CORES.length];

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 0, 0, 10 * dp);
            row.setLayoutParams(rowParams);

            // Quadrado colorido
            View dot = new View(this);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(12 * dp, 12 * dp);
            dot.setLayoutParams(dotParams);
            dot.setBackgroundColor(cor);
            row.addView(dot);

            // Nome da tag
            TextView tvTag = new TextView(this);
            LinearLayout.LayoutParams tagParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tagParams.setMargins(12 * dp, 0, 0, 0);
            tvTag.setLayoutParams(tagParams);
            tvTag.setText("#" + item.tag);
            tvTag.setTextColor(Color.WHITE);
            tvTag.setTextSize(13);
            row.addView(tvTag);

            // Percentual
            TextView tvPercent = new TextView(this);
            tvPercent.setText(item.percent + "%");
            tvPercent.setTextColor(cor);
            tvPercent.setTextSize(13);
            tvPercent.setTypeface(null, android.graphics.Typeface.BOLD);
            row.addView(tvPercent);

            chartMonthlyContainer.addView(row);
        }
    }
}