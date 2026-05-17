package com.example.focus.acitivity;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.focus.R;
import com.example.focus.network.ApiService;
import com.example.focus.network.RetrofitClient;
import com.example.focus.responses.UpdateProfileResponse;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityEditProfile extends AppCompatActivity {

    private static final String TAG = "EDIT_PROFILE";
    private static final int PERMISSION_REQUEST_CODE = 100;

    private ImageView imgAvatar;
    private EditText etNome, etEmail, etSenha, etConfirmarSenha;
    private Button btnSalvar;
    private SharedPreferences prefs;

    private Uri fotoUri = null;

    private final ActivityResultLauncher<String> galeriaLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    fotoUri = uri;
                    Log.d(TAG, "Foto selecionada: " + uri.toString());
                    imgAvatar.setPadding(0, 0, 0, 0);
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

        prefs = getSharedPreferences("user", MODE_PRIVATE);

        carregarDadosAtuais();

        imgAvatar.setOnClickListener(v -> abrirGaleria());
        btnSalvar.setOnClickListener(v -> validarESalvar());

        View btnVoltar = findViewById(R.id.btnVoltarEdit);
        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> {
                finish();
                overridePendingTransition(0, 0);
            });
        }
    }

    // ── Galeria ───────────────────────────────────────────────────────────────
    private void abrirGaleria() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                galeriaLauncher.launch("image/*");
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_CODE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                galeriaLauncher.launch("image/*");
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                galeriaLauncher.launch("image/*");
            } else {
                Toast.makeText(this, "Permissão negada para acessar a galeria", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ── Carrega dados atuais ──────────────────────────────────────────────────
    private void carregarDadosAtuais() {
        etNome.setText(prefs.getString("nome", ""));
        etEmail.setText(prefs.getString("email", ""));
        String foto = prefs.getString("foto_url", null);
        carregarFotoAvatar(foto);
    }

    private void carregarFotoAvatar(String foto) {
        if (foto == null || foto.isEmpty()) {
            int pad = (int) (20 * getResources().getDisplayMetrics().density);
            imgAvatar.setPadding(pad, pad, pad, pad);
            imgAvatar.setImageResource(R.drawable.ic_nav_profile);
            return;
        }

        imgAvatar.setPadding(0, 0, 0, 0);

        // Se for Base64, decodifica direto
        if (!foto.startsWith("http") && !foto.startsWith("uploads/")) {
            try {
                byte[] decoded = Base64.decode(foto, Base64.NO_WRAP);
                Bitmap bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                Glide.with(this).load(bmp).circleCrop().into(imgAvatar);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao decodificar Base64", e);
                imgAvatar.setImageResource(R.drawable.ic_nav_profile);
            }
        } else {
            // URL normal
            Glide.with(this)
                    .load(RetrofitClient.BASE_URL + foto)
                    .circleCrop()
                    .placeholder(R.drawable.ic_nav_profile)
                    .error(R.drawable.ic_nav_profile)
                    .into(imgAvatar);
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

        int userId    = prefs.getInt("user_id", 0);
        int profileId = prefs.getInt("profile_id", 0);

        Log.d(TAG, "Salvando — user_id=" + userId + " profile_id=" + profileId);

        // Converte foto para Base64 (null se não selecionou)
        String fotoBase64 = null;
        if (fotoUri != null) {
            Log.d(TAG, "Convertendo foto para Base64...");
            fotoBase64 = uriParaBase64(fotoUri);
            if (fotoBase64 == null) {
                Toast.makeText(this, "Não foi possível carregar a imagem", Toast.LENGTH_SHORT).show();
                btnSalvar.setEnabled(true);
                btnSalvar.setText("Salvar alterações");
                return;
            }
            Log.d(TAG, "Base64 gerado, tamanho: " + fotoBase64.length() + " chars");
        }

        ApiService api = RetrofitClient.getClient().create(ApiService.class);

        api.updateProfile(
                String.valueOf(userId),
                String.valueOf(profileId),
                nome,
                email,
                senha,
                fotoBase64
        ).enqueue(new Callback<UpdateProfileResponse>() {

            @Override
            public void onResponse(Call<UpdateProfileResponse> call,
                                   Response<UpdateProfileResponse> response) {

                Log.d(TAG, "HTTP: " + response.code());
                Log.d(TAG, "body null? " + (response.body() == null));
                if (response.body() != null) {
                    Log.d(TAG, "status: "   + response.body().status);
                    Log.d(TAG, "msg: "      + response.body().msg);
                    Log.d(TAG, "foto_url tamanho: " + (response.body().fotoUrl != null ? response.body().fotoUrl.length() : "null"));
                }

                btnSalvar.setEnabled(true);
                btnSalvar.setText("Salvar alterações");

                if (response.body() == null) {
                    Toast.makeText(ActivityEditProfile.this,
                            "Erro ao salvar. Tente novamente.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String status = response.body().status;

                if ("email_em_uso".equals(status)) {
                    Toast.makeText(ActivityEditProfile.this,
                            "Este email já está em uso", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!"ok".equals(status)) {
                    Toast.makeText(ActivityEditProfile.this,
                            "Erro: " + response.body().msg, Toast.LENGTH_LONG).show();
                    return;
                }

                // Atualiza SharedPreferences
                SharedPreferences.Editor editor = prefs.edit()
                        .putString("nome", nome)
                        .putString("email", email);

                String novaFoto = response.body().fotoUrl;
                if (novaFoto != null && !novaFoto.isEmpty()) {
                    editor.putString("foto_url", novaFoto);
                }
                editor.apply();

                Log.d(TAG, "Perfil salvo com sucesso!");
                Toast.makeText(ActivityEditProfile.this,
                        "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show();

                setResult(RESULT_OK);
                finish();
                overridePendingTransition(0, 0);
            }

            @Override
            public void onFailure(Call<UpdateProfileResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage(), t);
                btnSalvar.setEnabled(true);
                btnSalvar.setText("Salvar alterações");
                Toast.makeText(ActivityEditProfile.this,
                        "Erro de conexão.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Converte URI para Base64 ──────────────────────────────────────────────
    private String uriParaBase64(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            if (is == null) return null;

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int len;
            while ((len = is.read(chunk)) != -1) {
                buffer.write(chunk, 0, len);
            }
            is.close();

            return Base64.encodeToString(buffer.toByteArray(), Base64.NO_WRAP);

        } catch (Exception e) {
            Log.e(TAG, "Erro em uriParaBase64", e);
            return null;
        }
    }
}