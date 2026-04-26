package com.example.focus.acitivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.focus.R;
import com.example.focus.network.ApiService;
import com.example.focus.network.RetrofitClient;
import com.example.focus.responses.UpdateProfileResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityEditProfile extends AppCompatActivity {

    private ImageView imgAvatar;
    private EditText etNome, etEmail, etSenha, etConfirmarSenha;
    private Button btnSalvar;

    private Uri fotoUri = null;         // URI local selecionada pelo user
    private String fotoAtualUrl = null; // URL da foto atual (do SharedPreferences)

    // Launcher para galeria
    private final ActivityResultLauncher<String> galeriaLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    fotoUri = uri;
                    // Mostra preview da foto selecionada
                    Glide.with(this).load(uri).circleCrop().into(imgAvatar);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        imgAvatar        = findViewById(R.id.imgAvatarEdit);
        etNome           = findViewById(R.id.etEditNome);
        etEmail          = findViewById(R.id.etEditEmail);
        etSenha          = findViewById(R.id.etEditSenha);
        etConfirmarSenha = findViewById(R.id.etEditConfirmarSenha);
        btnSalvar        = findViewById(R.id.btnSalvarPerfil);

        carregarDadosAtuais();

        // Clique na foto abre galeria
        imgAvatar.setOnClickListener(v -> galeriaLauncher.launch("image/*"));

        btnSalvar.setOnClickListener(v -> validarESalvar());

        // Botão voltar
        View btnVoltar = findViewById(R.id.btnVoltarEdit);
        if (btnVoltar != null) btnVoltar.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });
    }

    // ── Preenche campos com dados atuais ──────────────────────────────────────
    private void carregarDadosAtuais() {
        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);

        etNome.setText(prefs.getString("nome", ""));
        etEmail.setText(prefs.getString("email", ""));
        fotoAtualUrl = prefs.getString("foto_url", null);

        // Carrega foto atual ou default
        carregarFotoAvatar(fotoAtualUrl);
    }

    private void carregarFotoAvatar(String url) {
        if (url != null && !url.isEmpty()) {
            String urlCompleta = RetrofitClient.BASE_URL + url;
            Glide.with(this)
                    .load(urlCompleta)
                    .circleCrop()
                    .placeholder(R.drawable.ic_nav_profile)
                    .error(R.drawable.ic_nav_profile)
                    .into(imgAvatar);
        } else {
            imgAvatar.setImageResource(R.drawable.ic_nav_profile);
        }
    }

    // ── Valida e salva ────────────────────────────────────────────────────────
    private void validarESalvar() {

        String nome  = etNome.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String senha = etSenha.getText().toString().trim();
        String conf  = etConfirmarSenha.getText().toString().trim();

        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Nome e email são obrigatórios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(senha) && !senha.equals(conf)) {
            Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSalvar.setEnabled(false);
        btnSalvar.setText("Salvando...");

        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        int userId    = prefs.getInt("user_id", 0);
        int profileId = prefs.getInt("profile_id", 0);

        // Monta os campos de texto como RequestBody
        RequestBody rbUserId    = toBody(String.valueOf(userId));
        RequestBody rbProfileId = toBody(String.valueOf(profileId));
        RequestBody rbNome      = toBody(nome);
        RequestBody rbEmail     = toBody(email);
        RequestBody rbSenha     = toBody(senha); // vazio = não altera

        // Monta a parte da foto (nullable)
        MultipartBody.Part fotoPart = null;
        if (fotoUri != null) {
            File fotoFile = uriParaFile(fotoUri);
            if (fotoFile != null) {
                RequestBody rbFoto = RequestBody.create(
                        MediaType.parse("image/*"), fotoFile);
                fotoPart = MultipartBody.Part.createFormData("foto", fotoFile.getName(), rbFoto);
            }
        }

        ApiService api = RetrofitClient.getClient().create(ApiService.class);

        api.updateProfile(rbUserId, rbProfileId, rbNome, rbEmail, rbSenha, fotoPart)
                .enqueue(new Callback<UpdateProfileResponse>() {

                    @Override
                    public void onResponse(Call<UpdateProfileResponse> call,
                                           Response<UpdateProfileResponse> response) {

                        btnSalvar.setEnabled(true);
                        btnSalvar.setText("Salvar");

                        if (!response.isSuccessful()
                                || response.body() == null
                                || !"ok".equals(response.body().status)) {

                            String msg = "Erro ao salvar";
                            if (response.body() != null
                                    && "email_em_uso".equals(response.body().status)) {
                                msg = "Este email já está em uso";
                            }
                            Toast.makeText(ActivityEditProfile.this, msg, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Atualiza SharedPreferences com novos dados
                        SharedPreferences.Editor editor = prefs.edit()
                                .putString("nome", nome)
                                .putString("email", email);

                        String novaFoto = response.body().fotoUrl;
                        if (novaFoto != null) {
                            editor.putString("foto_url", novaFoto);
                        }
                        editor.apply();

                        Toast.makeText(ActivityEditProfile.this,
                                "Perfil atualizado!", Toast.LENGTH_SHORT).show();

                        // Volta para o perfil
                        setResult(RESULT_OK);
                        finish();
                        overridePendingTransition(0, 0);
                    }

                    @Override
                    public void onFailure(Call<UpdateProfileResponse> call, Throwable t) {
                        btnSalvar.setEnabled(true);
                        btnSalvar.setText("Salvar");
                        Toast.makeText(ActivityEditProfile.this,
                                "Erro de conexão", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private RequestBody toBody(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }

    // Copia URI para um File temporário (necessário para Retrofit)
    private File uriParaFile(Uri uri) {
        try {
            InputStream is  = getContentResolver().openInputStream(uri);
            File tmpFile    = File.createTempFile("foto_", ".jpg", getCacheDir());
            FileOutputStream fos = new FileOutputStream(tmpFile);
            byte[] buf = new byte[4096];
            int len;
            while ((len = is.read(buf)) != -1) fos.write(buf, 0, len);
            fos.close();
            is.close();
            return tmpFile;
        } catch (Exception e) {
            return null;
        }
    }
}