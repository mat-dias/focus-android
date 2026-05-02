package com.example.focus.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

// Reagenda as notificações após o celular reiniciar
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences prefs = ctx.getSharedPreferences("user", Context.MODE_PRIVATE);
            if (prefs.getBoolean("logado", false)) {
                FocusNotificationManager.agendarNotificacaoManha(ctx);
                FocusNotificationManager.agendarNotificacaoTarde(ctx);
                FocusNotificationManager.agendarNotificacaoNoite(ctx);
            }
        }
    }
}
