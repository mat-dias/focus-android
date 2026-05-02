package com.example.focus.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;

import com.example.focus.R;
import com.example.focus.acitivity.MainActivity;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent intent) {

        String tipo = intent.getStringExtra("tipo");
        if (tipo == null) tipo = "morning";

        SharedPreferences prefs = ctx.getSharedPreferences("user", Context.MODE_PRIVATE);
        String nome = prefs.getString("nome", "usuário");
        int streak  = prefs.getInt("streak", 0);

        String titulo, mensagem;

        switch (tipo) {
            case "afternoon":
                titulo   = "☀️ Tarde produtiva!";
                mensagem = "Como estão suas tarefas hoje, " + nome + "? Não deixe para depois!";
                break;
            case "night":
                titulo   = "🌙 Hora de revisar o dia";
                mensagem = streak > 0
                        ? "Você tem " + streak + " dias de streak! Não perca agora 🔥"
                        : "Conclua pelo menos uma tarefa para começar seu streak! 🎯";
                break;
            default: // morning
                titulo   = "🌅 Bom dia, " + nome + "!";
                mensagem = "Suas tarefas de hoje te esperam. Vamos focar? 💪";
                break;
        }

        Intent mainIntent = new Intent(ctx, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pi = PendingIntent.getActivity(
                ctx, 0, mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                ctx, FocusNotificationManager.CHANNEL_TAREFAS)
                .setSmallIcon(R.drawable.ic_nav_home)
                .setContentTitle(titulo)
                .setContentText(mensagem)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mensagem))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pi);

        NotificationManager nm = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify((int) System.currentTimeMillis(), builder.build());
    }
}
