package com.example.focus.notifications;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

public class FocusNotificationManager {

    public static final String CHANNEL_ID      = "focus_channel";
    public static final String CHANNEL_TAREFAS = "focus_tarefas";

    // ── Cria os canais de notificação (chamar no onCreate do app) ─────────────
    public static void criarCanais(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = ctx.getSystemService(NotificationManager.class);

            // Canal geral
            NotificationChannel canal1 = new NotificationChannel(
                    CHANNEL_ID, "Focus App", NotificationManager.IMPORTANCE_DEFAULT);
            canal1.setDescription("Notificações gerais do Focus");
            nm.createNotificationChannel(canal1);

            // Canal de tarefas pendentes
            NotificationChannel canal2 = new NotificationChannel(
                    CHANNEL_TAREFAS, "Tarefas Pendentes", NotificationManager.IMPORTANCE_HIGH);
            canal2.setDescription("Avisos de tarefas não concluídas");
            nm.createNotificationChannel(canal2);
        }
    }

    // ── Agenda notificação diária às 09h ──────────────────────────────────────
    public static void agendarNotificacaoManha(Context ctx) {
        agendarAlarm(ctx, 9, 0, 1001, "morning");
    }

    // ── Agenda lembrete da tarde às 15h ───────────────────────────────────────
    public static void agendarNotificacaoTarde(Context ctx) {
        agendarAlarm(ctx, 15, 0, 1002, "afternoon");
    }

    // ── Agenda lembrete noturno às 21h ────────────────────────────────────────
    public static void agendarNotificacaoNoite(Context ctx) {
        agendarAlarm(ctx, 21, 0, 1003, "night");
    }

    private static void agendarAlarm(Context ctx, int hora, int min, int requestCode, String tipo) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(ctx, NotificationReceiver.class);
        intent.putExtra("tipo", tipo);

        PendingIntent pi = PendingIntent.getBroadcast(
                ctx, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hora);
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.SECOND, 0);

        // Se já passou o horário hoje, agenda para amanhã
        if (cal.getTimeInMillis() < System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (am != null) {
            am.setRepeating(AlarmManager.RTC_WAKEUP,
                    cal.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pi);
        }
    }

    // ── Cancela todas as notificações agendadas ────────────────────────────────
    public static void cancelarTodas(Context ctx) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        for (int code : new int[]{1001, 1002, 1003}) {
            Intent intent = new Intent(ctx, NotificationReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(
                    ctx, code, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            if (am != null) am.cancel(pi);
        }
    }
}
