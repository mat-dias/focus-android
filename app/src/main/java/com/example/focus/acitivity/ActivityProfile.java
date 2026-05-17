package com.example.focus.acitivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.focus.AddTask.AddTaskHelper;
import com.example.focus.NavBar.NavHelper;
import com.example.focus.R;
import com.example.focus.network.RetrofitClient;

public class ActivityProfile extends AppCompatActivity {

    private TextView txtNome, txtEmail, txtXP, txtStreak;
    private ImageView imgAvatar;
    private LinearLayout btnLogout, btnEditProfile, btnNotifications, btnAbout;

    private final ActivityResultLauncher<Intent> editLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            carregarDados();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        NavHelper.setup(this, "profile");
        AddTaskHelper.setup(this);

        initViews();
        carregarDados();
        setupBotoes();
    }

    private void initViews() {
        txtNome          = findViewById(R.id.txtProfileName);
        txtEmail         = findViewById(R.id.txtProfileEmail);
        txtXP            = findViewById(R.id.txtProfileXP);
        txtStreak        = findViewById(R.id.txtProfileStreak);
        imgAvatar        = findViewById(R.id.imgProfileAvatar);
        btnLogout        = findViewById(R.id.btnLogout);
        btnEditProfile   = findViewById(R.id.btnEditProfile);
        btnNotifications = findViewById(R.id.btnNotifications);
        btnAbout         = findViewById(R.id.btnAbout);
    }

    private void carregarDados() {
        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);

        txtNome.setText(prefs.getString("nome", "Usuário"));
        txtEmail.setText(prefs.getString("email", "email@exemplo.com"));
        txtXP.setText(prefs.getInt("xp", 0) + " XP");
        txtStreak.setText(String.valueOf(prefs.getInt("streak", 0)));

        String foto = prefs.getString("foto_url", null);
        carregarFotoAvatar(foto);
    }

    private void carregarFotoAvatar(String foto) {
        if (imgAvatar == null) return;

        if (foto == null || foto.isEmpty()) {
            imgAvatar.setImageResource(R.drawable.ic_nav_profile);
            return;
        }

        // Base64
        if (!foto.startsWith("http") && !foto.startsWith("uploads/")) {
            try {
                byte[] decoded = Base64.decode(foto, Base64.NO_WRAP);
                Bitmap bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                Glide.with(this)
                        .load(bmp)
                        .circleCrop()
                        .placeholder(R.drawable.ic_nav_profile)
                        .error(R.drawable.ic_nav_profile)
                        .into(imgAvatar);
            } catch (Exception e) {
                imgAvatar.setImageResource(R.drawable.ic_nav_profile);
            }
        } else {
            // URL normal (compatibilidade com fotos antigas)
            Glide.with(this)
                    .load(RetrofitClient.BASE_URL + foto)
                    .circleCrop()
                    .placeholder(R.drawable.ic_nav_profile)
                    .error(R.drawable.ic_nav_profile)
                    .into(imgAvatar);
        }
    }

    private void setupBotoes() {

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ActivityEditProfile.class);
            editLauncher.launch(intent);
            overridePendingTransition(0, 0);
        });

        btnNotifications.setOnClickListener(v -> {
            startActivity(new Intent(this, ActivityNotifications.class));
            overridePendingTransition(0, 0);
        });

        btnAbout.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Focus App")
                        .setMessage("Versão 1.0\n\nGerencie suas tarefas, acompanhe seu progresso e mantenha seu streak em dia!")
                        .setPositiveButton("Fechar", null)
                        .show()
        );

        btnLogout.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Sair da conta")
                        .setMessage("Tem certeza que deseja sair?")
                        .setPositiveButton("Sair", (dialog, which) -> {
                            getSharedPreferences("user", MODE_PRIVATE).edit().clear().apply();
                            Toast.makeText(this, "Logout realizado", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, ActivityLogin.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("Cancelar", null)
                        .show()
        );
    }
}