package com.example.focus.editTask;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.focus.R;
import com.example.focus.network.ApiService;
import com.example.focus.network.RetrofitClient;
import com.example.focus.responses.BasicResponse;
import com.example.focus.responses.TaskResponse;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditTaskBottomSheet extends BottomSheetDialogFragment {

    private final TaskResponse.TaskItem task;
    private final int profileId;
    private Runnable onSaved;

    private String selectedPriority;

    public EditTaskBottomSheet(TaskResponse.TaskItem task, int profileId) {
        this.task      = task;
        this.profileId = profileId;
        this.selectedPriority = task.priority;
    }

    public void setOnSavedListener(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Reutiliza o mesmo layout do AddTask
        return inflater.inflate(R.layout.fragment_add_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Muda título do sheet
        TextView tvTitulo = view.findViewById(R.id.tvSheetTitle);
        if (tvTitulo != null) tvTitulo.setText("Editar tarefa");

        EditText etTitle    = view.findViewById(R.id.etTaskTitle);
        EditText etTag      = view.findViewById(R.id.etTaskTag);
        Button   btnConfirm = view.findViewById(R.id.btnConfirmTask);
        TextView pillHigh   = view.findViewById(R.id.pillHigh);
        TextView pillMedium = view.findViewById(R.id.pillMedium);
        TextView pillLow    = view.findViewById(R.id.pillLow);

        // Preenche com dados atuais
        etTitle.setText(task.title);
        etTag.setText(task.tag != null ? task.tag : "");
        btnConfirm.setText("Salvar alterações");

        // Ativa pill da prioridade atual
        ativarPillInicial(pillHigh, pillMedium, pillLow, task.priority);

        pillHigh.setOnClickListener(v -> {
            selectedPriority = "high";
            activatePill(pillHigh, "high");
            deactivatePill(pillMedium);
            deactivatePill(pillLow);
        });
        pillMedium.setOnClickListener(v -> {
            selectedPriority = "medium";
            activatePill(pillMedium, "medium");
            deactivatePill(pillHigh);
            deactivatePill(pillLow);
        });
        pillLow.setOnClickListener(v -> {
            selectedPriority = "low";
            activatePill(pillLow, "low");
            deactivatePill(pillHigh);
            deactivatePill(pillMedium);
        });

        btnConfirm.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String tag   = etTag.getText().toString().trim();

            if (TextUtils.isEmpty(title)) {
                etTitle.setError("Informe um título");
                return;
            }

            btnConfirm.setEnabled(false);
            btnConfirm.setText("Salvando...");

            ApiService api = RetrofitClient.getClient().create(ApiService.class);
            api.editTask(task.taskId, profileId, title, selectedPriority, tag.isEmpty() ? null : tag)
               .enqueue(new Callback<BasicResponse>() {
                   @Override
                   public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                       if (response.body() != null && "ok".equals(response.body().status)) {
                           if (onSaved != null) onSaved.run();
                           dismiss();
                       } else {
                           Toast.makeText(requireContext(), "Erro ao salvar", Toast.LENGTH_SHORT).show();
                           btnConfirm.setEnabled(true);
                           btnConfirm.setText("Salvar alterações");
                       }
                   }
                   @Override
                   public void onFailure(Call<BasicResponse> call, Throwable t) {
                       Toast.makeText(requireContext(), "Erro de conexão", Toast.LENGTH_SHORT).show();
                       btnConfirm.setEnabled(true);
                       btnConfirm.setText("Salvar alterações");
                   }
               });
        });
    }

    private void ativarPillInicial(TextView high, TextView medium, TextView low, String priority) {
        deactivatePill(high);
        deactivatePill(medium);
        deactivatePill(low);
        switch (priority) {
            case "high":   activatePill(high, "high");     break;
            case "medium": activatePill(medium, "medium"); break;
            default:       activatePill(low, "low");       break;
        }
    }

    private void activatePill(TextView pill, String priority) {
        switch (priority) {
            case "high":
                pill.setBackgroundResource(R.drawable.pill_priority_high_active);
                pill.setTextColor(0xFFFF6B8A);
                break;
            case "medium":
                pill.setBackgroundResource(R.drawable.pill_priority_medium_active);
                pill.setTextColor(0xFFFFAA44);
                break;
            case "low":
                pill.setBackgroundResource(R.drawable.pill_priority_low_active);
                pill.setTextColor(0xFF4BE09A);
                break;
        }
    }

    private void deactivatePill(TextView pill) {
        pill.setBackgroundResource(R.drawable.pill_priority_inactive);
        pill.setTextColor(0xFF666666);
    }
}
