package com.example.projetoengenhariadesoftwareii;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.example.projetoengenhariadesoftwareii.database.AppDatabase;
import com.example.projetoengenhariadesoftwareii.database.DAO.UsuarioDao;
import com.example.projetoengenhariadesoftwareii.database.model.UsuarioModel;


public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, etSenha;
    private Button loginButton;
    private Button cadastroButton;
    private UsuarioDao usuarioDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializa os componentes da tela
        emailEditText = findViewById(R.id.email);
        etSenha = findViewById(R.id.Senha);
        loginButton = findViewById(R.id.button);
        cadastroButton = findViewById(R.id.buttoncadastro);

        // Inicializa o DAO
        usuarioDao = AppDatabase.getInstance(this).usuarioDao();

        // Botão de login
        loginButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString().trim();
            String senha = etSenha.getText().toString().trim();

            if (email.isEmpty()) {
                mostrarMensagemErro("Campo e-mail obrigatório!");
                return;
            }
            if (senha.isEmpty()) {
                mostrarMensagemErro("Campo senha obrigatório!");
                return;
            }

            new Thread(() -> {
                UsuarioModel usuario = usuarioDao.autenticarUsuario(email, senha);

                runOnUiThread(() -> {
                    if (usuario != null) {
                        Toast.makeText(this,
                                "Login bem-sucedido! Bem-vindo, " + usuario.getNome(),
                                Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, todayActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        mostrarMensagemErro("E-mail ou senha incorretos. Tente novamente.");
                    }
                });
            }).start();
        });

        // ✅ Botão para ir à tela de cadastro
        cadastroButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CadastroUsuarioActivity.class);
            startActivity(intent);
        });
    }

    private void mostrarMensagemErro(String mensagem) {
        new AlertDialog.Builder(this)
                .setTitle("Informação")
                .setMessage(mensagem)
                .setPositiveButton("Entendi", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
