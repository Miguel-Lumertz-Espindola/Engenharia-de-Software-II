package com.example.projetoengenhariadesoftwareii; // mesmo pacote do LoginActivity

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projetoengenhariadesoftwareii.database.AppDatabase;
import com.example.projetoengenhariadesoftwareii.database.DAO.UsuarioDao;
import com.example.projetoengenhariadesoftwareii.database.model.UsuarioModel;

public class CadastroUsuarioActivity extends AppCompatActivity {

    private EditText etNome, etIdade, etAltura, etPeso, etEmail, etSenha;
    private RadioGroup rgSexo;
    private RadioButton rbMasculino, rbFeminino;
    private TextView tvIMC;
    private Button btnCalcular, btnSalvar;

    private UsuarioDao usuarioDao;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrousuario);

        // Inicialização dos componentes e DAO
        etNome = findViewById(R.id.Nome);
        etIdade = findViewById(R.id.Idade);
        etAltura = findViewById(R.id.etAltura);
        etPeso = findViewById(R.id.etPeso);
        rgSexo = findViewById(R.id.rgSexo);
        rbMasculino = findViewById(R.id.rbMasculino);
        rbFeminino = findViewById(R.id.rbFeminino);
        etEmail = findViewById(R.id.Email);
        etSenha = findViewById(R.id.Senha);
        tvIMC = findViewById(R.id.tvIMC);
        btnCalcular = findViewById(R.id.btnCalcular);
        btnSalvar = findViewById(R.id.btnSalvar);

        usuarioDao = AppDatabase.getInstance(this).usuarioDao();

        btnCalcular.setOnClickListener(v -> calcularIMC());
        btnSalvar.setOnClickListener(v -> salvarUsuario());
    }

    private void calcularIMC() {
        String strPeso = etPeso.getText().toString().trim();
        String strAltura = etAltura.getText().toString().trim();

        if(strPeso.isEmpty() || strAltura.isEmpty()){
            Toast.makeText(this, "Preencha peso e altura corretamente!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double peso = Double.parseDouble(strPeso.replace(",", "."));
            double altura = Double.parseDouble(strAltura.replace(",", "."));

            // Ajusta altura: se > 10, assume cm
            if (altura > 10) {
                altura = altura / 100.0;
            }

            double imc = peso / (altura * altura);

            String classificacao;
            if (imc < 18.5) classificacao = "Abaixo do peso";
            else if (imc < 24.9) classificacao = "Peso normal";
            else if (imc < 29.9) classificacao = "Sobrepeso";
            else classificacao = "Obesidade";

            tvIMC.setText(String.format("IMC: %.2f (%s)", imc, classificacao));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Digite números válidos para peso e altura!", Toast.LENGTH_SHORT).show();
        }
    }



    private void salvarUsuario() {
        try {
            String nome = etNome.getText().toString();
            String email = etEmail.getText().toString();
            String senha = etSenha.getText().toString();
            int idade = Integer.parseInt(etIdade.getText().toString());
            double peso = Double.parseDouble(etPeso.getText().toString());
            double altura = Double.parseDouble(etAltura.getText().toString());
            String sexo = rbMasculino.isChecked() ? "Masculino" : rbFeminino.isChecked() ? "Feminino" : "";

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || sexo.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            UsuarioModel usuario = new UsuarioModel();
            usuario.setNome(nome);
            usuario.setEmail(email);
            usuario.setSenha(senha);
            usuario.setIdade(idade);
            usuario.setPeso(peso);
            usuario.setAltura(altura);
            usuario.setSexo(sexo);
            usuario.setImc(peso / (altura * altura));

            new Thread(() -> {
                usuarioDao.salvarUsuario(usuario);
                runOnUiThread(() ->
                        Toast.makeText(this, "Usuário salvo com sucesso!", Toast.LENGTH_SHORT).show());
                        Intent intent = new Intent(CadastroUsuarioActivity.this, LoginActivity.class);
                 finish();
            }).start();

        } catch (Exception e) {
            Toast.makeText(this, "Erro ao salvar: verifique os campos!", Toast.LENGTH_SHORT).show();
        }
    }
}
