package com.example.focus.acitivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.focus.network.ApiService;
import com.example.focus.responses.LoginResponse;
import com.example.focus.R;
import com.example.focus.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityLogin extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvCadastro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        if (prefs.getBoolean("logado", false)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);
        tvCadastro = findViewById(R.id.tvCadastro);

        btnLogin.setOnClickListener(v -> fazerLogin());
        tvCadastro.setOnClickListener(v ->
                startActivity(new Intent(this, ActivityRegister.class))
        );
    }

    private void fazerLogin() {

        String email = etEmail.getText().toString().trim();
        String senha = etPassword.getText().toString().trim();

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setText("Carregando...");
        btnLogin.setEnabled(false);

        ApiService api = RetrofitClient.getClient().create(ApiService.class);

        api.login(email, senha).enqueue(new Callback<LoginResponse>() {

            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                btnLogin.setText("Entrar");
                btnLogin.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {

                    LoginResponse res = response.body();

                    if ("ok".equals(res.status)) {

                        // Salva tudo no SharedPreferences — incluindo profile_id
                        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
                        prefs.edit()
                                .putBoolean("logado", true)
                                .putInt("user_id", res.userId)
                                .putInt("profile_id", res.profileId)
                                .putString("nome", res.nome)
                                .putString("email", res.email)
                                .putInt("xp", res.xp)
                                .putInt("streak", res.streak)
                                .apply();

                        Toast.makeText(ActivityLogin.this,
                                "Bem-vindo " + res.nome,
                                Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(ActivityLogin.this, MainActivity.class));
                        finish();

                    } else {
                        Toast.makeText(ActivityLogin.this,
                                "Email ou senha inválidos",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnLogin.setText("Entrar");
                btnLogin.setEnabled(true);
                Toast.makeText(ActivityLogin.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }
}