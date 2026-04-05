package com.example.focus;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityLogin extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvCadastro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvCadastro = findViewById(R.id.tvCadastro);

        btnLogin.setOnClickListener(v -> login());

        tvCadastro.setOnClickListener(v -> {
            startActivity(new Intent(this, ActivityRegister.class));
        });
    }

    private void login() {

        String email = etEmail.getText().toString();
        String senha = etPassword.getText().toString();

        ApiService api = RetrofitClient.getClient().create(ApiService.class);

        Call<String> call = api.login(email, senha);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                if (response.isSuccessful()) {

                    String res = response.body();

                    if (res.equals("login_ok")) {
                        startActivity(new Intent(ActivityLogin.this, MainActivity.class));
                    } else {
                        Toast.makeText(ActivityLogin.this, res, Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(ActivityLogin.this, "Erro servidor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(ActivityLogin.this, "Erro conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }
}