package com.example.focus;

import android.app.AlertDialog;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

public class PomodoroController {

    private FocusModeManager focusMode;

    private TextView txtTimer, txtStatus;
    private Button btnStart, btnPause, btnReset;
    private ImageButton btnPlus, btnMenos;
    private LinearLayout controlButtons;

    private long tempoInicial = 25 * 60 * 1000;
    private long tempoRestante = tempoInicial;

    private CountDownTimer timer;
    private boolean rodando = false;

    private AppCompatActivity activity;

    public PomodoroController(AppCompatActivity activity) {
        this.activity = activity;

        focusMode = new FocusModeManager(activity);

        initViews();
        setupListeners();
        atualizarTimer();
    }

    private void initViews() {
        txtTimer = activity.findViewById(R.id.txtTimer);
        txtStatus = activity.findViewById(R.id.txtStatus);

        btnStart = activity.findViewById(R.id.btnStart);
        btnPause = activity.findViewById(R.id.btnPause);
        btnReset = activity.findViewById(R.id.btnReset);

        btnPlus = activity.findViewById(R.id.btnPlus);
        btnMenos = activity.findViewById(R.id.btnMenos);

        controlButtons = activity.findViewById(R.id.controlButtons);
    }

    private void setupListeners() {

        btnStart.setOnClickListener(v -> {

            focusMode.solicitarPermissao(new FocusModeManager.PermissaoCallback() {

                @Override
                public void onPermissaoAceita() {
                    iniciarTimer();
                }

                @Override
                public void onPermissaoNegada() {
                    txtStatus.setText("Modo foco não ativado ⚠️");
                }
            });

        });

        btnPause.setOnClickListener(v -> {
            if (rodando) {
                pausarTimer();
                btnPause.setText("Continuar");
            } else {
                iniciarTimer();
                btnPause.setText("Pausar");
            }
        });

        btnReset.setOnClickListener(v -> resetarTimer());

        btnPlus.setOnClickListener(v -> {
            tempoInicial += 60000;
            tempoRestante = tempoInicial;
            atualizarTimer();
        });

        btnMenos.setOnClickListener(v -> {
            if (tempoInicial > 60000) {
                tempoInicial -= 60000;
                tempoRestante = tempoInicial;
                atualizarTimer();
            }
        });

        txtTimer.setOnClickListener(v -> mostrarDialogoTempo());
    }

    private void iniciarTimer() {

        focusMode.ativarModoFoco();

        if (timer != null) {
            timer.cancel();
        }

        timer = new CountDownTimer(tempoRestante, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tempoRestante = millisUntilFinished;
                atualizarTimer();
            }

            @Override
            public void onFinish() {
                txtStatus.setText("Tempo finalizado! 🎉");
                rodando = false;

                focusMode.desativarModoFoco();
                timer = null;
            }
        }.start();

        rodando = true;

        btnStart.setVisibility(View.GONE);
        controlButtons.setVisibility(View.VISIBLE);

        txtStatus.setText("Foco em andamento...");
    }

    private void pausarTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        rodando = false;
        focusMode.desativarModoFoco();

        txtStatus.setText("Pausado...");
    }

    private void resetarTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        tempoRestante = tempoInicial;
        atualizarTimer();

        rodando = false;
        focusMode.desativarModoFoco();

        btnStart.setVisibility(View.VISIBLE);
        controlButtons.setVisibility(View.GONE);
        btnPause.setText("Pausar");

        txtStatus.setText("Pronto para focar!");
    }

    private void atualizarTimer() {
        int minutos = (int) (tempoRestante / 1000) / 60;
        int segundos = (int) (tempoRestante / 1000) % 60;

        txtTimer.setText(String.format("%02d:%02d", minutos, segundos));
    }

    private void mostrarDialogoTempo() {
        EditText input = new EditText(activity);
        input.setHint("Minutos");

        new AlertDialog.Builder(activity)
                .setTitle("Definir tempo")
                .setView(input)
                .setPositiveButton("OK", (d, w) -> {
                    String valor = input.getText().toString();
                    if (!valor.isEmpty()) {
                        int minutos = Integer.parseInt(valor);
                        tempoInicial = minutos * 60 * 1000L;
                        tempoRestante = tempoInicial;
                        atualizarTimer();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // 🔥 chama no onResume da Activity
    public void verificarPermissaoRetorno() {
        if (focusMode.temPermissao() && !rodando) {
            iniciarTimer();
        }
    }
}