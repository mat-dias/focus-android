package com.example.focus.NavBar;

import android.app.Activity;
import android.content.Intent;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;

import com.example.focus.R;
import com.example.focus.acitivity.ActivityGoals;
import com.example.focus.acitivity.ActivityProfile;
import com.example.focus.acitivity.ActivityStats;
import com.example.focus.acitivity.MainActivity;

public class NavHelper {

    public static void setup(Activity activity, String activeScreen) {

        AppCompatImageButton navHome    = activity.findViewById(R.id.navHome);
        AppCompatImageButton navStats   = activity.findViewById(R.id.navStats);
        AppCompatImageButton navGoals   = activity.findViewById(R.id.navGoals);
        AppCompatImageButton navProfile = activity.findViewById(R.id.navProfile);

        TextView lblHome    = activity.findViewById(R.id.lblHome);
        TextView lblStats   = activity.findViewById(R.id.lblStats);
        TextView lblGoals   = activity.findViewById(R.id.lblGoals);
        TextView lblProfile = activity.findViewById(R.id.lblProfile);

        // HOME sempre ativo quando estiver nele
        colorir(navHome, lblHome, activeScreen.equals("home"));
        colorir(navStats, lblStats, activeScreen.equals("stats"));
        colorir(navGoals, lblGoals, activeScreen.equals("goals"));
        colorir(navProfile, lblProfile, activeScreen.equals("profile"));

        // 🔥 HOME (sempre limpa pilha e volta pro home)
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent i = new Intent(activity, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(i);
                activity.overridePendingTransition(0, 0);
            });
        }

        if (navStats != null) {
            navStats.setOnClickListener(v -> {
                if (!activeScreen.equals("stats")) {
                    activity.startActivity(new Intent(activity, ActivityStats.class));
                    activity.overridePendingTransition(0, 0);
                }
            });
        }

        if (navGoals != null) {
            navGoals.setOnClickListener(v -> {
                if (!activeScreen.equals("goals")) {
                    activity.startActivity(new Intent(activity, ActivityGoals.class));
                    activity.overridePendingTransition(0, 0);
                }
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                if (!activeScreen.equals("profile")) {
                    activity.startActivity(new Intent(activity, ActivityProfile.class));
                    activity.overridePendingTransition(0, 0);
                }
            });
        }
    }

    private static void colorir(AppCompatImageButton btn, TextView lbl, boolean ativo) {
        int cor = ativo ? 0xFF06B6D4 : 0xFF555555;

        if (btn != null) btn.setColorFilter(cor);
        if (lbl != null) lbl.setTextColor(cor);
    }
}