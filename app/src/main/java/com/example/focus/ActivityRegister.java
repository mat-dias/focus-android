package com.example.focus;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

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

        String nome = etNome.getText().toString();
        String email = etEmail.getText().toString();
        String senha = etPassword.getText().toString();

        int selectedId = rgSexo.getCheckedRadioButtonId();
        String sexo = "";

        if (selectedId != -1) {
            RadioButton rb = findViewById(selectedId);
            sexo = rb.getText().toString();
        }

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || dataSelecionada.isEmpty() || sexo.isEmpty()) {
            Toast.makeText(this, "Preencha tudo!", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = RetrofitClient.getClient().create(ApiService.class);

        Call<String> call = api.register(nome, email, senha, dataSelecionada, sexo);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                if (response.isSuccessful()) {

                    String res = response.body();

                    Toast.makeText(ActivityRegister.this, res, Toast.LENGTH_SHORT).show();

                    if (res.equals("cadastro_ok")) {
                        startActivity(new Intent(ActivityRegister.this, ActivityLogin.class));
                        finish();
                    }

                } else {
                    Toast.makeText(ActivityRegister.this, "Erro servidor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(ActivityRegister.this, "Erro conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }
}