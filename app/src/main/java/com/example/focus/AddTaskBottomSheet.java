package com.example.focus;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

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

        EditText etTitle = view.findViewById(R.id.etTaskTitle);
        EditText etTag = view.findViewById(R.id.etTaskTag);
        Button btnConfirm = view.findViewById(R.id.btnConfirmTask);

        TextView pillHigh = view.findViewById(R.id.pillHigh);
        TextView pillMedium = view.findViewById(R.id.pillMedium);
        TextView pillLow = view.findViewById(R.id.pillLow);

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
            String tag = etTag.getText().toString().trim();

            if (TextUtils.isEmpty(title)) {
                etTitle.setError("Informe um título");
                return;
            }

            if (listener != null) {
                listener.onTaskAdded(
                        title,
                        selectedPriority,
                        tag.isEmpty() ? null : tag
                );
            }

            dismiss();
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