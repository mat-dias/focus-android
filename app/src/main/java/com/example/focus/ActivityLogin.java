package com.example.focus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import org.json.JSONObject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActivityLogin extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvCadastro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        if(prefs.getBoolean("logado", false)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvCadastro = findViewById(R.id.tvCadastro);

        btnLogin.setOnClickListener(v -> fazerLogin());
        tvCadastro.setOnClickListener(v -> startActivity(new Intent(this, ActivityRegister.class)));
    }

    private void fazerLogin() {
        String email = etEmail.getText().toString().trim();
        String senha = etPassword.getText().toString().trim();

        if(email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha email e senha", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setText("⏳");
        btnLogin.setEnabled(false);

        RetrofitClient.getClient().create(ApiService.class)
                .login(email, senha)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        btnLogin.setText("Entrar");
                        btnLogin.setEnabled(true);

                        if(response.body() == null) return;

                        String rawResponse = response.body();

                        try {
                            // 🔥 MÉTODO 1: Tenta JSON direto
                            JSONObject json = new JSONObject(rawResponse);
                            processarResposta(json);
                            return;
                        } catch(Exception e1) {
                            try {
                                // 🔥 MÉTODO 2: Limpa HTML e tenta JSON
                                String cleaned = limparHTML(rawResponse);
                                JSONObject json = new JSONObject(cleaned);
                                processarResposta(json);
                                return;
                            } catch(Exception e2) {
                                try {
                                    // 🔥 MÉTODO 3: Extrai JSON com REGEX
                                    String jsonStr = extrairJSON(rawResponse);
                                    if(jsonStr != null) {
                                        JSONObject json = new JSONObject(jsonStr);
                                        processarResposta(json);
                                        return;
                                    }
                                } catch(Exception e3) {}
                            }
                        }

                        Toast.makeText(ActivityLogin.this, "Erro resposta: " + rawResponse.substring(0, Math.min(100, rawResponse.length())), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        btnLogin.setText("Entrar");
                        btnLogin.setEnabled(true);
                        Toast.makeText(ActivityLogin.this, "Sem internet", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String limparHTML(String html) {
        return html
                .replaceAll("<[^>]*>", "")
                .replaceAll("[\\n\\r\\t ]+", "")
                .replaceAll("\\\\\"", "\"")
                .trim();
    }

    private String extrairJSON(String text) {
        // 🔥 REGEX PARA ENCONTRAR JSON NA BAGUNÇA
        Pattern pattern = Pattern.compile("\\{[^}]*\"status\"[^}]*\\}");
        Matcher matcher = pattern.matcher(text);
        if(matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private void processarResposta(JSONObject json) {
        try {
            if("ok".equals(json.getString("status"))) {
                SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
                prefs.edit()
                        .putString("nome", json.getString("nome"))
                        .putInt("xp", json.optInt("xp", 0))
                        .putInt("streak", json.optInt("streak", 0))
                        .putBoolean("logado", true)
                        .apply();

                Toast.makeText(this, "Login OK - " + json.getString("nome"), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, json.optString("msg", "Erro desconhecido"), Toast.LENGTH_LONG).show();
            }
        } catch(Exception e) {
            Toast.makeText(this, "Erro processar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}