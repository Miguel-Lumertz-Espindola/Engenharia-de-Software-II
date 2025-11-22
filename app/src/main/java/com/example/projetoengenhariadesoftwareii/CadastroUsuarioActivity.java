package com.example.projetoengenhariadesoftwareii; // mesmo pacote do LoginActivity

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projetoengenhariadesoftwareii.database.AppDatabase;
import com.example.projetoengenhariadesoftwareii.database.DAO.DietaDAO;
import com.example.projetoengenhariadesoftwareii.database.DAO.RefeicaoDAO;
import com.example.projetoengenhariadesoftwareii.database.DAO.UsuarioDao;
import com.example.projetoengenhariadesoftwareii.database.model.Dieta;
import com.example.projetoengenhariadesoftwareii.database.model.DietaPreProntaModel;
import com.example.projetoengenhariadesoftwareii.database.model.Refeicao;
import com.example.projetoengenhariadesoftwareii.database.model.UsuarioModel;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class CadastroUsuarioActivity extends AppCompatActivity {

    private EditText etNome, etIdade, etAltura, etPeso, etEmail, etSenha;
    private RadioGroup rgSexo;
    private RadioButton rbMasculino, rbFeminino;
    private TextView tvIMC;
    private Button btnCalcular, btnSalvar;
    private Spinner spObjetivo, spAtividade, spPraticidade, spRigor;
    private Spinner spOrcamento;
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
        spObjetivo = findViewById(R.id.spObjetivo);
        spAtividade = findViewById(R.id.spAtividade);
        spPraticidade = findViewById(R.id.spPraticidade);
        spRigor = findViewById(R.id.spRigor);
        spOrcamento = findViewById(R.id.spOrcamento);



        usuarioDao = AppDatabase.getInstance(this).usuarioDao();

        btnCalcular.setOnClickListener(v -> calcularIMC());
        btnSalvar.setOnClickListener(v -> salvarUsuario());

        ArrayAdapter<CharSequence> adapterOrcamento = ArrayAdapter.createFromResource(
                this,
                R.array.opcoes_orcamento,
                android.R.layout.simple_spinner_item
        );

        adapterOrcamento.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spOrcamento.setAdapter(adapterOrcamento);


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
            String objetivo = spObjetivo.getSelectedItem().toString();
            String atividade = spAtividade.getSelectedItem().toString();
            String praticidade = spPraticidade.getSelectedItem().toString();
            String rigor = spRigor.getSelectedItem().toString();
            String textoOrcamento = spOrcamento.getSelectedItem().toString();
            textoOrcamento = textoOrcamento.replace("R$", "").trim();
            double orcamento = Double.parseDouble(textoOrcamento);
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
            usuario.setObjetivo(objetivo);
            usuario.setAtividade(atividade);
            usuario.setPraticidade(praticidade);
            usuario.setRigor(rigor);
            usuario.setOrcamento(orcamento);
            usuario.setRestricoes("");



            new Thread(() -> {
                long idGerado = usuarioDao.salvarUsuario(usuario);
                usuario.setId(idGerado);

                aplicarDietaAposCadastro(usuario);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Usuário salvo com sucesso!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CadastroUsuarioActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                });

            }).start();


        } catch (Exception e) {
            Toast.makeText(this, "Erro ao salvar: verifique os campos!", Toast.LENGTH_SHORT).show();
        }
    }

    private void aplicarDietaAposCadastro(UsuarioModel usuario) {

        AppDatabase db = AppDatabase.getInstance(this);

        // 1 — Buscar dieta ideal com base nas respostas do cadastro
        DietaPreProntaModel dietaEscolhida = selecionarDietaComBaseNoUsuario(usuario);

        if (dietaEscolhida == null) return;

        // 2 — Preencher todos os dias do mês atual automaticamente
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));
        int totalDias = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        DietaDAO dietaDAO = db.dietaDAO();
        RefeicaoDAO refeicaoDAO = db.RefeicaoDAO();

        for (int dia = 1; dia <= totalDias; dia++) {

            String cafe = formatar(dietaEscolhida.getCafeManha());
            String almoco = formatar(dietaEscolhida.getAlmoco());
            String cafeTarde = formatar(dietaEscolhida.getCafeTarde());
            String jantar = formatar(dietaEscolhida.getJantar());

            dietaDAO.salvarDieta(new Dieta(dia, cafe, almoco, cafeTarde, jantar));

            // Cria refeições do dia
            refeicaoDAO.inserirRefeicao(new Refeicao(dia, "Café da Manhã", "08:00", cafe));
            refeicaoDAO.inserirRefeicao(new Refeicao(dia, "Almoço", "12:00", almoco));
            refeicaoDAO.inserirRefeicao(new Refeicao(dia, "Café da Tarde", "15:00", cafeTarde));
            refeicaoDAO.inserirRefeicao(new Refeicao(dia, "Jantar", "19:00", jantar));
        }
    }

    private DietaPreProntaModel selecionarDietaComBaseNoUsuario(UsuarioModel user) {
        List<DietaPreProntaModel> todas = AppDatabase.getInstance(this)
                .dietaPreProntaDAO()
                .getTodas();

        // EXEMPLOS (você ajusta conforme seus campos reais):
        if (user.getObjetivo().equals("Emagrecer") && user.getPraticidade().equals("Alta")) {
            return buscar("Emagrecimento Prático", todas);
        }

        if (user.getObjetivo().equals("Ganhar Massa") && user.getRigor().equals("Moderado")) {
            return buscar("Ganho de Massa com Moderado Rigor", todas);
        }

        if (user.getRestricoes().equals("Vegetariano") && user.getObjetivo().equals("Emagrecer")) {
            return buscar("Emagrecimento Saudável Vegetariano", todas);
        }

        // fallback
        return buscar("Manutenção de Peso Saudável", todas);
    }

    private DietaPreProntaModel buscar(String nome, List<DietaPreProntaModel> lista) {
        for (DietaPreProntaModel d : lista) {
            if (d.getNomeDieta().equalsIgnoreCase(nome)) return d;
        }
        return null;
    }


    private String formatar(String conteudo) {
        if (conteudo == null) return "";
        String[] itens = conteudo.split("\\+");
        StringBuilder b = new StringBuilder();
        for (String i : itens) b.append(i.trim()).append("\n");
        return b.toString().trim();
    }


}