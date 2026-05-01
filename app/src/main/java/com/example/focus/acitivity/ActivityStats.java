package com.example.focus.acitivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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
import com.example.focus.views.PieChartView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityStats extends AppCompatActivity {

    // Views
    private TextView txtStatsStreak, txtStatsXP, txtNivel, txtXpProgresso;
    private TextView txtTodayTasks, txtTodayCompleted, txtTodayXP, txtTaxaHoje;
    private TextView txtMelhorStreak, txtDiasAtivos, txtDiasSemana;
    private TextView txtTaxaConclusao, txtConcluidasPeriodo;
    private TextView txtPrioHigh, txtPrioMedium, txtPrioLow;
    private View barXpFill, barTaxaFill, barTaxaHojeFill;
    private View barHighFill, barMediumFill, barLowFill;
    private LinearLayout chartWeeklyBars, chartMonthlyContainer;
    private PieChartView pieChart;
    private LinearLayout btnPeriodoSemana, btnPeriodoMes, btnPeriodoTotal;

    private int profileId;
    private String periodoAtual = "semana";

    private final int[] CORES = {
            Color.parseColor("#4ADE80"), Color.parseColor("#EC4899"),
            Color.parseColor("#FFAA44"), Color.parseColor("#06B6D4"),
            Color.parseColor("#A78BFA")
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        AddTaskHelper.setup(this);
        NavHelper.setup(this, "stats");

        bindViews();

        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        profileId = prefs.getInt("profile_id", 0);

        setupFiltroPeriodo();
        carregarStats("semana");
    }

    // ── Bind ──────────────────────────────────────────────────────────────────
    private void bindViews() {
        txtStatsStreak       = findViewById(R.id.txtStatsStreak);
        txtStatsXP           = findViewById(R.id.txtStatsXP);
        txtNivel             = findViewById(R.id.txtNivel);
        txtXpProgresso       = findViewById(R.id.txtXpProgresso);
        barXpFill            = findViewById(R.id.barXpFill);
        txtTodayTasks        = findViewById(R.id.txtTodayTasks);
        txtTodayCompleted    = findViewById(R.id.txtTodayCompleted);
        txtTodayXP           = findViewById(R.id.txtTodayXP);
        txtTaxaHoje          = findViewById(R.id.txtTaxaHoje);
        barTaxaHojeFill      = findViewById(R.id.barTaxaHojeFill);
        txtMelhorStreak      = findViewById(R.id.txtMelhorStreak);
        txtDiasAtivos        = findViewById(R.id.txtDiasAtivos);
        txtDiasSemana        = findViewById(R.id.txtDiasSemana);
        txtTaxaConclusao     = findViewById(R.id.txtTaxaConclusao);
        txtConcluidasPeriodo = findViewById(R.id.txtConcluidasPeriodo);
        barTaxaFill          = findViewById(R.id.barTaxaFill);
        txtPrioHigh          = findViewById(R.id.txtPrioHigh);
        txtPrioMedium        = findViewById(R.id.txtPrioMedium);
        txtPrioLow           = findViewById(R.id.txtPrioLow);
        barHighFill          = findViewById(R.id.barHighFill);
        barMediumFill        = findViewById(R.id.barMediumFill);
        barLowFill           = findViewById(R.id.barLowFill);
        chartWeeklyBars      = findViewById(R.id.chartWeeklyBars);
        chartMonthlyContainer = findViewById(R.id.chartMonthlyContainer);
        pieChart             = findViewById(R.id.pieChart);
        btnPeriodoSemana     = findViewById(R.id.btnPeriodoSemana);
        btnPeriodoMes        = findViewById(R.id.btnPeriodoMes);
        btnPeriodoTotal      = findViewById(R.id.btnPeriodoTotal);
    }

    // ── Filtro de período ─────────────────────────────────────────────────────
    private void setupFiltroPeriodo() {
        btnPeriodoSemana.setOnClickListener(v -> selecionarPeriodo("semana"));
        btnPeriodoMes.setOnClickListener(v    -> selecionarPeriodo("mes"));
        btnPeriodoTotal.setOnClickListener(v  -> selecionarPeriodo("total"));
        selecionarPeriodo("semana");
    }

    private void selecionarPeriodo(String periodo) {
        periodoAtual = periodo;
        int ativo   = Color.parseColor("#06B6D4");
        int inativo = Color.parseColor("#1E1E1E");
        int txtAtivo   = Color.WHITE;
        int txtInativo = Color.parseColor("#888888");

        resetarPillFiltro(btnPeriodoSemana, inativo, txtInativo);
        resetarPillFiltro(btnPeriodoMes,    inativo, txtInativo);
        resetarPillFiltro(btnPeriodoTotal,  inativo, txtInativo);

        LinearLayout selecionado = periodo.equals("semana") ? btnPeriodoSemana
                : periodo.equals("mes") ? btnPeriodoMes : btnPeriodoTotal;
        resetarPillFiltro(selecionado, ativo, txtAtivo);

        carregarStats(periodo);
    }

    private void resetarPillFiltro(LinearLayout pill, int bgColor, int txtColor) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(bgColor);
        bg.setCornerRadius(20 * getResources().getDisplayMetrics().density);
        pill.setBackground(bg);
        for (int i = 0; i < pill.getChildCount(); i++) {
            if (pill.getChildAt(i) instanceof TextView)
                ((TextView) pill.getChildAt(i)).setTextColor(txtColor);
        }
    }

    // ── Carrega ───────────────────────────────────────────────────────────────
    private void carregarStats(String periodo) {
        if (profileId == 0) return;
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.getStats(profileId, periodo).enqueue(new Callback<StatsResponse>() {
            @Override
            public void onResponse(Call<StatsResponse> call, Response<StatsResponse> response) {
                if (!response.isSuccessful() || response.body() == null || !"ok".equals(response.body().status)) {
                    Toast.makeText(ActivityStats.this, "Erro ao carregar", Toast.LENGTH_SHORT).show();
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

    // ── Renderiza ─────────────────────────────────────────────────────────────
    private void renderizar(StatsResponse s) {
        int dp = (int) getResources().getDisplayMetrics().density;

        // Cards topo
        txtStatsStreak.setText(String.valueOf(s.streak));
        txtStatsXP.setText(s.xp + " XP");
        txtNivel.setText("Nível " + s.nivel);
        txtXpProgresso.setText(s.xpProgresso + "% para Nível " + (s.nivel + 1));
        animarBarra(barXpFill, s.xpProgresso);

        // Hoje
        txtTodayTasks.setText(String.valueOf(s.hojeTotal));
        txtTodayCompleted.setText(String.valueOf(s.hojeConcluidas));
        txtTodayXP.setText("+" + (s.hojeConcluidas * 67));
        txtTaxaHoje.setText(s.taxaHoje + "%");
        animarBarra(barTaxaHojeFill, s.taxaHoje);

        // Histórico
        txtMelhorStreak.setText(String.valueOf(s.melhorStreak));
        txtDiasAtivos.setText(String.valueOf(s.diasAtivos));
        txtDiasSemana.setText(s.diasSemana + "/7 dias");

        // Taxa período
        txtTaxaConclusao.setText(s.taxaConclusao + "%");
        txtConcluidasPeriodo.setText(s.concluidasPeriodo + " de " + s.agendadasPeriodo);
        animarBarra(barTaxaFill, s.taxaConclusao);

        // Prioridades
        int totalPrio = (s.prioridades != null)
                ? (s.prioridades.getOrDefault("high", 0)
                + s.prioridades.getOrDefault("medium", 0)
                + s.prioridades.getOrDefault("low", 0))
                : 0;
        int high   = s.prioridades != null ? s.prioridades.getOrDefault("high", 0)   : 0;
        int medium = s.prioridades != null ? s.prioridades.getOrDefault("medium", 0) : 0;
        int low    = s.prioridades != null ? s.prioridades.getOrDefault("low", 0)    : 0;
        txtPrioHigh.setText(high + " Alta");
        txtPrioMedium.setText(medium + " Média");
        txtPrioLow.setText(low + " Baixa");
        animarBarra(barHighFill,   totalPrio > 0 ? (high   * 100 / totalPrio) : 0);
        animarBarra(barMediumFill, totalPrio > 0 ? (medium * 100 / totalPrio) : 0);
        animarBarra(barLowFill,    totalPrio > 0 ? (low    * 100 / totalPrio) : 0);

        // Gráfico barras
        renderizarBarras(s.barras);

        // Pizza
        renderizarPizza(s.tags);
    }

    private void animarBarra(View barra, int percent) {
        if (barra == null) return;
        barra.post(() -> {
            View parent = (View) barra.getParent();
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) barra.getLayoutParams();
            lp.width = (int) (parent.getWidth() * (percent / 100f));
            barra.setLayoutParams(lp);
        });
    }

    // ── Barras semanais ───────────────────────────────────────────────────────
    private void renderizarBarras(List<StatsResponse.BarraItem> barras) {
        chartWeeklyBars.removeAllViews();
        if (barras == null || barras.isEmpty()) return;

        int dp  = (int) getResources().getDisplayMetrics().density;
        int max = 1;
        for (StatsResponse.BarraItem b : barras) if (b.qtd > max) max = b.qtd;
        int maxH = 110 * dp;

        String[] diasPt = {"Dom","Seg","Ter","Qua","Qui","Sex","Sáb"};

        for (StatsResponse.BarraItem b : barras) {
            // Converte label inglês para PT
            String labelPt = b.label;
            try {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
                cal.setTime(sdf.parse(b.data));
                labelPt = diasPt[cal.get(java.util.Calendar.DAY_OF_WEEK) - 1];
            } catch (Exception ignored) {}

            LinearLayout coluna = new LinearLayout(this);
            coluna.setOrientation(LinearLayout.VERTICAL);
            coluna.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            coluna.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));

            // Número
            TextView tvNum = new TextView(this);
            tvNum.setText(b.qtd > 0 ? String.valueOf(b.qtd) : "");
            tvNum.setTextColor(Color.parseColor("#888888"));
            tvNum.setTextSize(9);
            tvNum.setGravity(Gravity.CENTER);
            coluna.addView(tvNum);

            // Barra
            View bar = new View(this);
            int altura = b.qtd == 0 ? 4 * dp : (int) ((b.qtd / (float) max) * maxH);
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(22 * dp, altura);
            bp.setMargins(3 * dp, 4 * dp, 3 * dp, 0);
            bar.setLayoutParams(bp);
            GradientDrawable shape = new GradientDrawable();
            shape.setColor(b.qtd > 0 ? Color.parseColor("#06B6D4") : Color.parseColor("#2A2A2A"));
            shape.setCornerRadii(new float[]{6*dp,6*dp,6*dp,6*dp,0,0,0,0});
            bar.setBackground(shape);
            coluna.addView(bar);

            // Label dia
            TextView tvLabel = new TextView(this);
            tvLabel.setText(labelPt);
            tvLabel.setTextColor(Color.parseColor("#888888"));
            tvLabel.setTextSize(9);
            tvLabel.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 4*dp, 0, 0);
            tvLabel.setLayoutParams(lp);
            coluna.addView(tvLabel);

            chartWeeklyBars.addView(coluna);
        }
    }

    // ── Pizza ─────────────────────────────────────────────────────────────────
    private void renderizarPizza(List<StatsResponse.TagItem> tags) {
        while (chartMonthlyContainer.getChildCount() > 2)
            chartMonthlyContainer.removeViewAt(2);

        int dp = (int) getResources().getDisplayMetrics().density;

        if (tags == null || tags.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            TextView tv = new TextView(this);
            tv.setText("Nenhuma etiqueta concluída no período");
            tv.setTextColor(Color.parseColor("#666666"));
            tv.setTextSize(13);
            chartMonthlyContainer.addView(tv);
            return;
        }

        pieChart.setVisibility(View.VISIBLE);
        List<PieChartView.Fatia> fatias = new ArrayList<>();
        for (int i = 0; i < tags.size(); i++)
            fatias.add(new PieChartView.Fatia(tags.get(i).qtd, CORES[i % CORES.length], tags.get(i).tag));
        pieChart.setFatias(fatias);

        for (int i = 0; i < tags.size(); i++) {
            StatsResponse.TagItem item = tags.get(i);
            int cor = CORES[i % CORES.length];

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rp.setMargins(0, 0, 0, 10*dp);
            row.setLayoutParams(rp);

            View dot = new View(this);
            LinearLayout.LayoutParams dp2 = new LinearLayout.LayoutParams(12*dp, 12*dp);
            dot.setLayoutParams(dp2);
            dot.setBackgroundColor(cor);
            row.addView(dot);

            TextView tvTag = new TextView(this);
            LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tp.setMargins(12*dp, 0, 0, 0);
            tvTag.setLayoutParams(tp);
            tvTag.setText("#" + item.tag);
            tvTag.setTextColor(Color.WHITE);
            tvTag.setTextSize(13);
            row.addView(tvTag);

            TextView tvPct = new TextView(this);
            tvPct.setText(item.percent + "%");
            tvPct.setTextColor(cor);
            tvPct.setTextSize(13);
            tvPct.setTypeface(null, Typeface.BOLD);
            row.addView(tvPct);

            chartMonthlyContainer.addView(row);
        }
    }
}