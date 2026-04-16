package com.example.focus;

import android.app.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

public class AddTaskHelper {

    public static void setup(Activity activity) {

        if (!(activity instanceof AppCompatActivity)) return;

        AppCompatImageButton btnAdd = activity.findViewById(R.id.btnAdd);
        if (btnAdd == null) return;

        btnAdd.setOnClickListener(v -> {

            AddTaskBottomSheet sheet = new AddTaskBottomSheet();

            if (activity instanceof TaskListener) {
                sheet.setOnTaskAddedListener((TaskListener) activity);
            }

            sheet.show(
                    ((AppCompatActivity) activity)
                            .getSupportFragmentManager(),
                    "AddTask"
            );
        });
    }
}