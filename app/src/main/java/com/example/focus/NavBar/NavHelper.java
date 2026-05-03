package com.example.focus.NavBar;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
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

        // Colore o ativo e anima entrada
        colorir(navHome,    lblHome,    activeScreen.equals("home"));
        colorir(navStats,   lblStats,   activeScreen.equals("stats"));
        colorir(navGoals,   lblGoals,   activeScreen.equals("goals"));
        colorir(navProfile, lblProfile, activeScreen.equals("profile"));

        // Anima o ícone ativo ao entrar na tela
        animarEntrada(activeScreen.equals("home")    ? navHome    : null);
        animarEntrada(activeScreen.equals("stats")   ? navStats   : null);
        animarEntrada(activeScreen.equals("goals")   ? navGoals   : null);
        animarEntrada(activeScreen.equals("profile") ? navProfile : null);

        // ── Listeners com animação de toque ──────────────────────────────────
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                animarToque(v);
                if (!activeScreen.equals("home")) {
                    Intent i = new Intent(activity, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    v.postDelayed(() -> {
                        activity.startActivity(i);
                        activity.overridePendingTransition(0, 0);
                    }, 150);
                }
            });
        }

        if (navStats != null) {
            navStats.setOnClickListener(v -> {
                animarToque(v);
                if (!activeScreen.equals("stats")) {
                    v.postDelayed(() -> {
                        activity.startActivity(new Intent(activity, ActivityStats.class));
                        activity.overridePendingTransition(0, 0);
                    }, 150);
                }
            });
        }

        if (navGoals != null) {
            navGoals.setOnClickListener(v -> {
                animarToque(v);
                if (!activeScreen.equals("goals")) {
                    v.postDelayed(() -> {
                        activity.startActivity(new Intent(activity, ActivityGoals.class));
                        activity.overridePendingTransition(0, 0);
                    }, 150);
                }
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                animarToque(v);
                if (!activeScreen.equals("profile")) {
                    v.postDelayed(() -> {
                        activity.startActivity(new Intent(activity, ActivityProfile.class));
                        activity.overridePendingTransition(0, 0);
                    }, 150);
                }
            });
        }
    }

    // ── Animação de entrada: ícone ativo salta para cima com bounce ───────────
    private static void animarEntrada(View icon) {
        if (icon == null) return;

        icon.post(() -> {
            // Sobe e volta com overshoot
            ObjectAnimator translY = ObjectAnimator.ofFloat(icon, "translationY", 0f, -14f, 0f);
            translY.setDuration(450);
            translY.setInterpolator(new OvershootInterpolator(2f));

            // Escala pulsa levemente
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(icon, "scaleX", 1f, 1.25f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(icon, "scaleY", 1f, 1.25f, 1f);
            scaleX.setDuration(450);
            scaleY.setDuration(450);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(translY, scaleX, scaleY);
            set.setStartDelay(80);
            set.start();
        });
    }

    // ── Animação de toque: pressiona para baixo e volta com bounce ────────────
    private static void animarToque(View icon) {
        if (icon == null) return;

        // Aperta
        ObjectAnimator scaleXDown = ObjectAnimator.ofFloat(icon, "scaleX", 1f, 0.75f);
        ObjectAnimator scaleYDown = ObjectAnimator.ofFloat(icon, "scaleY", 1f, 0.75f);
        scaleXDown.setDuration(100);
        scaleYDown.setDuration(100);

        // Solta com bounce
        ObjectAnimator scaleXUp = ObjectAnimator.ofFloat(icon, "scaleX", 0.75f, 1.2f, 1f);
        ObjectAnimator scaleYUp = ObjectAnimator.ofFloat(icon, "scaleY", 0.75f, 1.2f, 1f);
        scaleXUp.setDuration(250);
        scaleYUp.setDuration(250);
        scaleXUp.setInterpolator(new OvershootInterpolator(3f));
        scaleYUp.setInterpolator(new OvershootInterpolator(3f));

        AnimatorSet press = new AnimatorSet();
        press.playTogether(scaleXDown, scaleYDown);

        AnimatorSet release = new AnimatorSet();
        release.playTogether(scaleXUp, scaleYUp);
        release.setStartDelay(100);

        AnimatorSet full = new AnimatorSet();
        full.playSequentially(press, release);
        full.start();
    }

    // ── Cor do ícone e label ──────────────────────────────────────────────────
    private static void colorir(AppCompatImageButton btn, TextView lbl, boolean ativo) {
        int cor = ativo ? 0xFF06B6D4 : 0xFF555555;
        if (btn != null) btn.setColorFilter(cor);
        if (lbl != null) lbl.setTextColor(cor);
    }
}