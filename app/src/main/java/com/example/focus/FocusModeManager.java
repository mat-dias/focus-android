package com.example.focus;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;

public class FocusModeManager {

    private AppCompatActivity activity;
    private NotificationManager notificationManager;

    public interface PermissaoCallback {
        void onPermissaoAceita();
        void onPermissaoNegada();
    }

    public FocusModeManager(AppCompatActivity activity) {
        this.activity = activity;
        notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public boolean temPermissao() {
        return notificationManager.isNotificationPolicyAccessGranted();
    }

    public void solicitarPermissao(PermissaoCallback callback) {

        if (temPermissao()) {
            callback.onPermissaoAceita();
            return;
        }

        new AlertDialog.Builder(activity)
                .setTitle("Ativar modo foco")
                .setMessage("Para melhorar sua concentração, precisamos ativar o 'Não Perturbe'. Deseja ativar agora?")

                .setPositiveButton("Ativar agora", (d, w) -> {
                    Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                    activity.startActivity(intent);
                    callback.onPermissaoNegada(); // ainda não ativou
                })

                .setNegativeButton("Agora não", (d, w) -> {
                    callback.onPermissaoNegada();
                })

                .setCancelable(false)
                .show();
    }

    public void ativarModoFoco() {
        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
    }

    public void desativarModoFoco() {
        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
    }
}