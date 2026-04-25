package com.example.focus.acitivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.focus.network.ApiService;
import com.example.focus.responses.BasicResponse;
import com.example.focus.R;
import com.example.focus.network.RetrofitClient;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityRegister extends AppCompatActivity {

    EditText etNome, etEmail, etPassword;
    TextView tvDataNasc, tvJaTenho;
    Button btnRegister;
    RadioGroup rgSexo;
    LinearLayout layoutDataNasc;

    String dataSelecionada = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // inputs
        etNome = findViewById(R.id.etNome);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvDataNasc = findViewById(R.id.tvDataNasc);
        btnRegister = findViewById(R.id.btnRegister);
        rgSexo = findViewById(R.id.rgSexo);
        layoutDataNasc = findViewById(R.id.layoutDataNasc);
        tvJaTenho = findViewById(R.id.tvJaTenho);

        // abrir date picker
        layoutDataNasc.setOnClickListener(v -> abrirCalendario());

        // botão cadastrar
        btnRegister.setOnClickListener(v -> registrar());

        // voltar pro login
        tvJaTenho.setOnClickListener(v -> {
            startActivity(new Intent(this, ActivityLogin.class));
            finish();
        });
    }

    private void abrirCalendario() {
        Calendar cal = Calendar.getInstance();

        int ano = cal.get(Calendar.YEAR);
        int mes = cal.get(Calendar.MONTH);
        int dia = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            month = month + 1;

            dataSelecionada = dayOfMonth + "/" + month + "/" + year;
            tvDataNasc.setText(dataSelecionada);
            tvDataNasc.setTextColor(getResources().getColor(android.R.color.white));

        }, ano, mes, dia);

        dialog.show();
    }

    private void registrar() {

        String nome = etNome.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String senha = etPassword.getText().toString().trim();

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha tudo!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setText("Carregando...");
        btnRegister.setEnabled(false);

        ApiService api = RetrofitClient.getClient().create(ApiService.class);

        api.register(nome, email, senha).enqueue(new Callback<BasicResponse>() {

            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {

                btnRegister.setText("Cadastrar");
                btnRegister.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {

                    BasicResponse res = response.body();

                    if ("ok".equals(res.status)) {

                        Toast.makeText(ActivityRegister.this,
                                "Cadastro realizado!",
                                Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(ActivityRegister.this, ActivityLogin.class));
                        finish();

                    } else {
                        Toast.makeText(ActivityRegister.this,
                                res.msg,
                                Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(ActivityRegister.this,
                            "Erro no servidor",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {

                btnRegister.setText("Cadastrar");
                btnRegister.setEnabled(true);

                Toast.makeText(ActivityRegister.this,
                        "Erro de conexão",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}