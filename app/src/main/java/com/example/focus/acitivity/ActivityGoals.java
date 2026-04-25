package com.example.focus.acitivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.focus.AddTask.AddTaskBottomSheet;
import com.example.focus.AddTask.AddTaskHelper;
import com.example.focus.NavBar.NavHelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.example.focus.R;
import com.example.focus.responses.TaskResponse;
import com.example.focus.network.ApiService;
import com.example.focus.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityGoals extends AppCompatActivity {

    private TextView txtProgressPercent;
    private TextView txtGoalsTotal;
    private TextView txtGoalsCompleted;
    private View progressBarFill;
    private LinearLayout inProgressSection;
    private LinearLayout completedSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);

        // NavHelper cuida de TODA a navegação — não precisa de setupNav()
        NavHelper.setup(this, "goals");

        txtProgressPercent = findViewById(R.id.txtProgressPercent);
        txtGoalsTotal      = findViewById(R.id.txtGoalsTotal);
        txtGoalsCompleted  = findViewById(R.id.txtGoalsCompleted);
        progressBarFill    = findViewById(R.id.progressBarFill);
        inProgressSection  = findViewById(R.id.inProgressSection);
        completedSection   = findViewById(R.id.completedSection);

        // FAB — abre o bottom sheet de adicionar tarefa
        AppCompatImageButton btnAddGoal = findViewById(R.id.btnAddGoal);
        btnAddGoal.setOnClickListener(v -> {
            AddTaskBottomSheet sheet = new AddTaskBottomSheet();
            sheet.setOnTaskAddedListener((title, priority, tag) -> carregarTasks());
            sheet.show(getSupportFragmentManager(), "AddTask");
        });

        carregarTasks();
    }

    // ── Carrega tasks da API ──────────────────────────────────────────────────
    private void carregarTasks() {

        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        int profileId = prefs.getInt("profile_id", 0);

        if (profileId == 0) {
            Toast.makeText(this, "Sessão inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = RetrofitClient.getClient().create(ApiService.class);

        api.getTasks(profileId).enqueue(new Callback<TaskResponse>() {

            @Override
            public void onResponse(Call<TaskResponse> call, Response<TaskResponse> response) {

                if (!response.isSuccessful()
                        || response.body() == null
                        || !"ok".equals(response.body().status)) {
                    Toast.makeText(ActivityGoals.this,
                            "Erro ao carregar tarefas", Toast.LENGTH_SHORT).show();
                    return;
                }

                TaskResponse res = response.body();
                renderizarTasks(res.tasks, res.total, res.concluidas);
            }

            @Override
            public void onFailure(Call<TaskResponse> call, Throwable t) {
                Toast.makeText(ActivityGoals.this,
                        "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Renderiza os dados na tela ────────────────────────────────────────────
    private void renderizarTasks(List<TaskResponse.TaskItem> tasks, int total, int concluidas) {

        txtGoalsTotal.setText(String.valueOf(total));
        txtGoalsCompleted.setText(String.valueOf(concluidas));

        int percent = total > 0 ? (int) ((concluidas / (float) total) * 100) : 0;
        txtProgressPercent.setText(percent + "%");

        progressBarFill.post(() -> {
            int parentWidth = ((View) progressBarFill.getParent()).getWidth();
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)
                    progressBarFill.getLayoutParams();
            lp.width = (int) (parentWidth * (percent / 100f));
            progressBarFill.setLayoutParams(lp);
        });

        while (inProgressSection.getChildCount() > 1) inProgressSection.removeViewAt(1);
        while (completedSection.getChildCount() > 1)  completedSection.removeViewAt(1);

        for (TaskResponse.TaskItem task : tasks) {
            View card = criarCard(task);
            if (task.done) completedSection.addView(card);
            else           inProgressSection.addView(card);
        }

        if (inProgressSection.getChildCount() == 1)
            inProgressSection.addView(criarMensagemVazia("Nenhuma tarefa em andamento 🎉"));

        if (completedSection.getChildCount() == 1)
            completedSection.addView(criarMensagemVazia("Nenhuma tarefa concluída ainda"));
    }

    // ── Card de tarefa ────────────────────────────────────────────────────────
    private View criarCard(TaskResponse.TaskItem task) {

        int dp = (int) getResources().getDisplayMetrics().density;

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(20 * dp, 20 * dp, 20 * dp, 20 * dp);
        card.setBackgroundResource(R.drawable.bg_stat_card);
        card.setElevation(2 * dp);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 12 * dp);
        card.setLayoutParams(cardParams);

        LinearLayout rowTitle = new LinearLayout(this);
        rowTitle.setOrientation(LinearLayout.HORIZONTAL);
        rowTitle.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 0, 0, 8 * dp);
        rowTitle.setLayoutParams(rowParams);

        View dot = new View(this);
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(8 * dp, 8 * dp);
        dotParams.setMarginEnd(12 * dp);
        dot.setLayoutParams(dotParams);
        dot.setBackgroundColor(corDaPrioridade(task.priority));
        rowTitle.addView(dot);

        TextView tvTitle = new TextView(this);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tvTitle.setLayoutParams(titleParams);
        tvTitle.setText(task.title);
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setTextSize(15);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        rowTitle.addView(tvTitle);

        TextView tvBadge = new TextView(this);
        if (task.done) {
            tvBadge.setText("✅ Concluída");
            tvBadge.setTextColor(Color.parseColor("#4ADE80"));
        } else {
            tvBadge.setText(labelPrioridade(task.priority));
            tvBadge.setTextColor(corDaPrioridade(task.priority));
        }
        tvBadge.setTextSize(12);
        rowTitle.addView(tvBadge);

        card.addView(rowTitle);

        if (task.tag != null && !task.tag.isEmpty()) {
            TextView tvTag = new TextView(this);
            tvTag.setText("#" + task.tag);
            tvTag.setTextColor(Color.parseColor("#888888"));
            tvTag.setTextSize(12);
            card.addView(tvTag);
        }

        return card;
    }

    // ── Mensagem vazia ────────────────────────────────────────────────────────
    private View criarMensagemVazia(String msg) {
        int dp = (int) getResources().getDisplayMetrics().density;
        TextView tv = new TextView(this);
        tv.setText(msg);
        tv.setTextColor(Color.parseColor("#666666"));
        tv.setTextSize(13);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 12 * dp);
        tv.setLayoutParams(lp);
        return tv;
    }

    // ── Cores e labels ────────────────────────────────────────────────────────
    private int corDaPrioridade(String priority) {
        switch (priority) {
            case "high":   return Color.parseColor("#FF6B8A");
            case "medium": return Color.parseColor("#FFAA44");
            default:       return Color.parseColor("#4ADE80");
        }
    }

    private String labelPrioridade(String priority) {
        switch (priority) {
            case "high":   return "● Alta";
            case "medium": return "● Média";
            default:       return "● Baixa";
        }
    }
}