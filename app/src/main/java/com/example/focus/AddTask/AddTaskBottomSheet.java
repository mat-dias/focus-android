package com.example.focus.AddTask;

import android.content.SharedPreferences;
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

import com.example.focus.responses.BasicResponse;
import com.example.focus.R;
import com.example.focus.network.ApiService;
import com.example.focus.network.RetrofitClient;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class AddTaskBottomSheet extends BottomSheetDialogFragment {

    private String selectedPriority = "high";
    private TaskListener listener;

    public void setOnTaskAddedListener(TaskListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText etTitle    = view.findViewById(R.id.etTaskTitle);
        EditText etTag      = view.findViewById(R.id.etTaskTag);
        Button   btnConfirm = view.findViewById(R.id.btnConfirmTask);
        TextView pillHigh   = view.findViewById(R.id.pillHigh);
        TextView pillMedium = view.findViewById(R.id.pillMedium);
        TextView pillLow    = view.findViewById(R.id.pillLow);

        activatePill(pillHigh, "high");

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

            // Pega o profile_id salvo no login
            SharedPreferences prefs = requireContext()
                    .getSharedPreferences("user", MODE_PRIVATE);
            int profileId = prefs.getInt("profile_id", 0);

            if (profileId == 0) {
                Toast.makeText(requireContext(),
                        "Sessão inválida, faça login novamente",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            btnConfirm.setEnabled(false);
            btnConfirm.setText("Salvando...");

            ApiService api = RetrofitClient.getClient().create(ApiService.class);

            api.addTask(profileId, title, selectedPriority, tag.isEmpty() ? null : tag)
                    .enqueue(new Callback<BasicResponse>() {

                        @Override
                        public void onResponse(Call<BasicResponse> call,
                                               Response<BasicResponse> response) {

                            if (response.isSuccessful()
                                    && response.body() != null
                                    && "ok".equals(response.body().status)) {

                                if (listener != null) {
                                    listener.onTaskAdded(title, selectedPriority,
                                            tag.isEmpty() ? null : tag);
                                }
                                dismiss();

                            } else {
                                Toast.makeText(requireContext(),
                                        "Erro ao salvar tarefa",
                                        Toast.LENGTH_SHORT).show();
                                btnConfirm.setEnabled(true);
                                btnConfirm.setText("Adicionar tarefa");
                            }
                        }

                        @Override
                        public void onFailure(Call<BasicResponse> call, Throwable t) {
                            Toast.makeText(requireContext(),
                                    "Erro de conexão",
                                    Toast.LENGTH_SHORT).show();
                            btnConfirm.setEnabled(true);
                            btnConfirm.setText("Adicionar tarefa");
                        }
                    });
        });
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