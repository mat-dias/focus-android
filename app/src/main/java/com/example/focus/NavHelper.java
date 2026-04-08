package com.example.focus;

import android.app.Activity;
import android.content.Intent;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatImageButton;

public class NavHelper {

    private static final int CYAN     = 0xFF06B6D4;
    private static final int INACTIVE = 0xFF555555;

    public static void setup(Activity activity, String activeScreen) {

        AppCompatImageButton navHome    = activity.findViewById(R.id.navHome);
        AppCompatImageButton navStats   = activity.findViewById(R.id.navStats);
        AppCompatImageButton navGoals   = activity.findViewById(R.id.navGoals);
        AppCompatImageButton navProfile = activity.findViewById(R.id.navProfile);

        TextView lblHome    = activity.findViewById(R.id.lblHome);
        TextView lblStats   = activity.findViewById(R.id.lblStats);
        TextView lblGoals   = activity.findViewById(R.id.lblGoals);
        TextView lblProfile = activity.findViewById(R.id.lblProfile);

        //  COLORIR o botão da pagina ativa
        colorir(navHome,    lblHome,    activeScreen.equals("home"));
        colorir(navStats,   lblStats,   activeScreen.equals("stats"));
        colorir(navGoals,   lblGoals,   activeScreen.equals("goals"));
        colorir(navProfile, lblProfile, activeScreen.equals("profile"));

        //  CLIQUE de cada botão
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                if (!activeScreen.equals("home"))
                    navegar(activity, MainActivity.class);
            });
        }

        if (navStats != null) {
            navStats.setOnClickListener(v -> {
                if (!activeScreen.equals("stats"))
                    navegar(activity, ActivityStats.class); // cria depois
            });
        }

        if (navGoals != null) {
            navGoals.setOnClickListener(v -> {
                if (!activeScreen.equals("goals"))
                    navegar(activity, ActivityGoals.class); // cria depois
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                if (!activeScreen.equals("profile"))
                    navegar(activity, ActivityProfile.class);
            });
        }
    }

    //colori botão da pagina inativa
    private static void colorir(AppCompatImageButton btn, TextView lbl, boolean ativo) {
        int cor = ativo ? CYAN : INACTIVE;
        if (btn != null) btn.setColorFilter(cor);
        if (lbl != null) lbl.setTextColor(cor);
    }

    //fecha a ultima pagina para melhorar desempenho
    private static void navegar(Activity de, Class<?> para) {
        Intent intent = new Intent(de, para);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        de.startActivity(intent);
        de.finish();
    }
}