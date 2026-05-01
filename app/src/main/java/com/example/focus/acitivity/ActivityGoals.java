package com.example.focus.acitivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.focus.AddTask.AddTaskBottomSheet;
import com.example.focus.NavBar.NavHelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.example.focus.R;
import com.example.focus.responses.BasicResponse;
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
    private int profileId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);

        NavHelper.setup(this, "goals");

        txtProgressPercent = findViewById(R.id.txtProgressPercent);
        txtGoalsTotal      = findViewById(R.id.txtGoalsTotal);
        txtGoalsCompleted  = findViewById(R.id.txtGoalsCompleted);
        progressBarFill    = findViewById(R.id.progressBarFill);
        inProgressSection  = findViewById(R.id.inProgressSection);
        completedSection   = findViewById(R.id.completedSection);

        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        profileId = prefs.getInt("profile_id", 0);

        AppCompatImageButton btnAddGoal = findViewById(R.id.btnAddGoal);
        btnAddGoal.setOnClickListener(v -> {
            AddTaskBottomSheet sheet = new AddTaskBottomSheet();
            sheet.setOnTaskAddedListener((title, priority, tag) -> carregarTasks());
            sheet.show(getSupportFragmentManager(), "AddTask");
        });

        carregarTasks();
    }

    // ── Carrega tasks ─────────────────────────────────────────────────────────
    private void carregarTasks() {
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
                    Toast.makeText(ActivityGoals.this, "Erro ao carregar tarefas", Toast.LENGTH_SHORT).show();
                    return;
                }
                TaskResponse res = response.body();
                renderizarTasks(res.tasks, res.total, res.concluidas);
            }

            @Override
            public void onFailure(Call<TaskResponse> call, Throwable t) {
                Toast.makeText(ActivityGoals.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Renderiza ─────────────────────────────────────────────────────────────
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
            if (task.done) completedSection.addView(criarCard(task));
            else           inProgressSection.addView(criarCard(task));
        }

        if (inProgressSection.getChildCount() == 1)
            inProgressSection.addView(criarMensagemVazia("Nenhuma tarefa em andamento 🎉"));

        if (completedSection.getChildCount() == 1)
            completedSection.addView(criarMensagemVazia("Nenhuma tarefa concluída ainda"));
    }

    // ── Card ──────────────────────────────────────────────────────────────────
    private View criarCard(TaskResponse.TaskItem task) {
        int dp = (int) getResources().getDisplayMetrics().density;

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(20 * dp, 16 * dp, 12 * dp, 16 * dp);
        card.setBackgroundResource(R.drawable.bg_stat_card);
        card.setElevation(2 * dp);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 12 * dp);
        card.setLayoutParams(cardParams);

        LinearLayout rowMain = new LinearLayout(this);
        rowMain.setOrientation(LinearLayout.HORIZONTAL);
        rowMain.setGravity(Gravity.CENTER_VERTICAL);
        rowMain.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // ── Botão CHECK ───────────────────────────────────────────────────────
        ImageButton btnCheck = new ImageButton(this);
        btnCheck.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout.LayoutParams checkParams = new LinearLayout.LayoutParams(40 * dp, 40 * dp);
        checkParams.setMarginEnd(10 * dp);
        btnCheck.setLayoutParams(checkParams);
        atualizarBotaoCheck(btnCheck, task.done);

        btnCheck.setOnClickListener(v -> {
            boolean novoDone = !task.done;
            task.done = novoDone;
            atualizarBotaoCheck(btnCheck, novoDone);

            ApiService api = RetrofitClient.getClient().create(ApiService.class);
            api.updateTaskDone(
                    task.taskId,
                    profileId,
                    novoDone ? 1 : 0,
                    task.schedulingId != null ? task.schedulingId : 0,
                    task.scheduleId   != null ? task.scheduleId   : 0
            ).enqueue(new Callback<BasicResponse>() {
                @Override
                public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                    if (response.body() != null && "ok".equals(response.body().status)) {
                        carregarTasks();
                    } else {
                        task.done = !novoDone;
                        atualizarBotaoCheck(btnCheck, task.done);
                        Toast.makeText(ActivityGoals.this, "Erro ao atualizar", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<BasicResponse> call, Throwable t) {
                    task.done = !novoDone;
                    atualizarBotaoCheck(btnCheck, task.done);
                    Toast.makeText(ActivityGoals.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
                }
            });
        });

        rowMain.addView(btnCheck);

        // ── Coluna central ────────────────────────────────────────────────────
        LinearLayout colCenter = new LinearLayout(this);
        colCenter.setOrientation(LinearLayout.VERTICAL);
        colCenter.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        LinearLayout rowTitulo = new LinearLayout(this);
        rowTitulo.setOrientation(LinearLayout.HORIZONTAL);
        rowTitulo.setGravity(Gravity.CENTER_VERTICAL);

        View dot = new View(this);
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(8 * dp, 8 * dp);
        dotParams.setMarginEnd(8 * dp);
        dot.setLayoutParams(dotParams);
        dot.setBackgroundColor(corDaPrioridade(task.priority));
        rowTitulo.addView(dot);

        TextView tvTitle = new TextView(this);
        tvTitle.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        tvTitle.setText(task.title);
        tvTitle.setTextColor(task.done ? Color.parseColor("#888888") : Color.WHITE);
        tvTitle.setTextSize(15);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        rowTitulo.addView(tvTitle);
        colCenter.addView(rowTitulo);

        if (task.tag != null && !task.tag.isEmpty()) {
            TextView tvTag = new TextView(this);
            LinearLayout.LayoutParams tagParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tagParams.setMargins(16 * dp, 4 * dp, 0, 0);
            tvTag.setLayoutParams(tagParams);
            tvTag.setText("#" + task.tag);
            tvTag.setTextColor(Color.parseColor("#666666"));
            tvTag.setTextSize(12);
            colCenter.addView(tvTag);
        }

        rowMain.addView(colCenter);

        // ── Botão 3 pontinhos ─────────────────────────────────────────────────
        ImageButton btnMenu = new ImageButton(this);
        btnMenu.setBackgroundColor(Color.TRANSPARENT);
        btnMenu.setLayoutParams(new LinearLayout.LayoutParams(40 * dp, 40 * dp));
        btnMenu.setImageResource(android.R.drawable.ic_menu_more);
        btnMenu.setColorFilter(Color.parseColor("#888888"));

        btnMenu.setOnClickListener(v -> {
            androidx.appcompat.widget.PopupMenu popup =
                    new androidx.appcompat.widget.PopupMenu(this, btnMenu);
            popup.getMenu().add(0, 1, 0, "🗑️  Deletar tarefa");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) { confirmarDelecao(task.taskId); return true; }
                return false;
            });
            popup.show();
        });

        rowMain.addView(btnMenu);
        card.addView(rowMain);
        return card;
    }

    private void atualizarBotaoCheck(ImageButton btn, boolean done) {
        if (done) {
            btn.setImageResource(android.R.drawable.checkbox_on_background);
            btn.setColorFilter(Color.parseColor("#4ADE80"));
        } else {
            btn.setImageResource(android.R.drawable.checkbox_off_background);
            btn.setColorFilter(Color.parseColor("#555555"));
        }
    }

    private void confirmarDelecao(int taskId) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Deletar tarefa")
                .setMessage("Tem certeza que deseja deletar esta tarefa?")
                .setPositiveButton("Deletar", (dialog, which) -> deletarTask(taskId))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deletarTask(int taskId) {
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.deleteTask(taskId, profileId).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.body() != null && "ok".equals(response.body().status)) {
                    Toast.makeText(ActivityGoals.this, "Tarefa deletada", Toast.LENGTH_SHORT).show();
                    carregarTasks();
                } else {
                    Toast.makeText(ActivityGoals.this, "Erro ao deletar", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(ActivityGoals.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private View criarMensagemVazia(String msg) {
        int dp = (int) getResources().getDisplayMetrics().density;
        TextView tv = new TextView(this);
        tv.setText(msg);
        tv.setTextColor(Color.parseColor("#666666"));
        tv.setTextSize(13);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 12 * dp);
        tv.setLayoutParams(lp);
        return tv;
    }

    private int corDaPrioridade(String priority) {
        switch (priority) {
            case "high":   return Color.parseColor("#FF6B8A");
            case "medium": return Color.parseColor("#FFAA44");
            default:       return Color.parseColor("#4ADE80");
        }
    }
}