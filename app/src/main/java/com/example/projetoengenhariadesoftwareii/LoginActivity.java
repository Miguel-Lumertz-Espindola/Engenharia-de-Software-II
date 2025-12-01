package com.example.projetoengenhariadesoftwareii;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.example.projetoengenhariadesoftwareii.database.AppDatabase;
import com.example.projetoengenhariadesoftwareii.database.DAO.RefeicaoDAO;
import com.example.projetoengenhariadesoftwareii.database.DAO.UsuarioDao;
import com.example.projetoengenhariadesoftwareii.database.model.DietaPreProntaModel;
import com.example.projetoengenhariadesoftwareii.database.model.Refeicao;
import com.example.projetoengenhariadesoftwareii.database.model.UsuarioModel;
import com.example.projetoengenhariadesoftwareii.database.DAO.DietaDAO;

import java.util.List;


public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, etSenha;
    private Button loginButton;
    private Button cadastroButton;
    private UsuarioDao usuarioDao;
    private UsuarioModel usuarioLogado; // ← receber do login

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

            if (email.isEmpty()) { mostrarMensagemErro("Campo e-mail obrigatório!"); return; }
            if (senha.isEmpty()) { mostrarMensagemErro("Campo senha obrigatório!"); return; }

            new Thread(() -> {
                UsuarioModel usuario = usuarioDao.autenticarUsuario(email, senha);

                if (usuario != null) {
                    usuarioLogado = usuario;

                    SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                    String keyInicial = "dieta_inicial_user_" + usuarioLogado.getId();

                    if (!prefs.getBoolean(keyInicial, false)) {
                        DietaPreProntaModel escolhida = escolherMelhorDieta(usuarioLogado);
                        if (escolhida != null) {
                            for (int dia = 1; dia <= 30; dia++) {
                                adicionarDietaParaDia(dia, (int) usuarioLogado.getId(), escolhida);
                            }
                            prefs.edit().putBoolean(keyInicial, true).apply();
                        }
                    }

                    runOnUiThread(() -> {
                        Intent intent = new Intent(LoginActivity.this, todayActivity.class);
                        intent.putExtra("usuarioLogado", usuarioLogado);
                        startActivity(intent);
                        finish();
                    });

                } else {
                    runOnUiThread(() -> mostrarMensagemErro("E-mail ou senha incorretos."));
                }
            }).start();
        });





        // ✅ Botão para ir à tela de cadastro
        cadastroButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CadastroUsuarioActivity.class);
            startActivity(intent);
        });
    }

    // 🔹 INSERIR DIETAS PRÉ-PRONTAS APENAS UMA VEZ POR USUÁRIO
//    private void inserirDietasPadrao() {
//        AppDatabase db = AppDatabase.getInstance(this);
//
//        if (db.dietaPreProntaDAO().getTodas().isEmpty()) {
//            db.dietaPreProntaDAO().inserir(new DietaPreProntaModel(
//                    "Emagrecimento Prático",
//                    "Feita para quem quer perder peso de forma leve, sem muitas restrições e com alimentos simples, baratos e fáceis de preparar.",
//                    "Pão Francês (50g) + ovo mexido (100g) + mamão (170g)",
//                    "Arroz (165g) + Feijão (140g) + Peito de frango grelhado (100g) + Cenoura cozida (160g) + Salada de alface e pepino à vontade + 1 fio de azeite",
//                    "Iogurte natural desnatado (165g) + Granola (39g) + banana pequena (50g)",
//                    "Filé de frango grelhado (150g) + Legumes cozidos (165g) + Suco natural de limão sem açúcar (240ml)",
//                    1, 2, 3, 1
//            ));
//
//            db.dietaPreProntaDAO().inserir(new DietaPreProntaModel(
//                    "Ganho de Massa com Moderado Rigor",
//                    "Focada em fornecer proteínas e calorias suficientes para aumentar a massa muscular, com alimentos balanceados e quantidades um pouco maiores.",
//                    "Pão de forma integral (100g) + Pasta de amendoim (16g) + Iogurte grego (100g) + banana pequena (55g)",
//                    "Arroz integral (165g) + Lentilha (85g) + Peito de frango grelhado (120g) + Beterraba cozida (125g) + Salada de rúcula e pepino à vontade + azeite (8ml)",
//                    "Crepioca (100g) com frango desfiado (60g) + mamão papaia (170g)",
//                    "Carne moída refogada (75g)+ Macarrão cozido (140g) + Legumes à moda mediterrânea (150g) + Suco de abacaxi (165ml)",
//                    2, 4, 2, 2
//            ));
//
//            db.dietaPreProntaDAO().inserir(new DietaPreProntaModel(
//                    "Manutenção de Peso Saudável",
//                    "Equilíbrio entre energia e nutrientes. Ideal para quem busca manter o peso e a saúde com refeições variadas e flexíveis.",
//                    "Pão caseiro com requeijão (100g) + ovo mexido (100g) + fatia de melância (200g)",
//                    "Polenta (240g) + Feijão (140g) + Filé de peixe grelhado (120g) + Moranga (180g) + Salada de acelga com azeite",
//                    "Iogurte grego (100g) + Castanha-do-pará (16g) + pêra (110g)",
//                    "Frango desfiado (120g) + Aipim cozido (150g) + Suco natural de abacaxi (165ml)",
//                    3, 3, 2, 1
//            ));
//
//            db.dietaPreProntaDAO().inserir(new DietaPreProntaModel(
//                    "Melhorar Saúde em Geral, Alto Rigor e Orçamento",
//                    "Dieta inspirada no estilo mediterrâneo, com foco em qualidade nutricional, antioxidantes e gorduras boas. Inclui alimentos mais caros (salmão, oleaginosas, frutas variadas).",
//                    "Panqueca de banana com aveia (121g) + Iogurte natural (100g) + Mix de sementes (chia, girassol, abóbora) (45g)",
//                    "Arroz integral (165g) + Grão-de-bico cozido (83g) + Filé de peixe grelhado (120g) + Legumes variados (cenoura, chuchu, couve-flor) (180g) + Azeite extravirgem (8ml)",
//                    "Iogurte grego (100g) + Abacate (135g) com farelo de aveia (20g)",
//                    "Filé de frango grelhado (150g) + Salada de legumes cozidos (165g) + Suco de limão tahiti sem açucar (240ml)",
//                    4, 3, 1, 3
//            ));
//
//            db.dietaPreProntaDAO().inserir(new DietaPreProntaModel(
//                    "Emagrecimento Controlado",
//                    "Dieta mais trabalhosa (baixa praticidade), com preparações caseiras, alimentos frescos e controle alto de calorias. Foco forte em proteínas magras, fibras e quase nada de ultraprocessados.",
//                    "Panqueca de banana com aveia (121g) + Ovos mexidos (100g) + Mamão (170g) + Chia (15g)",
//                    "Arroz integral (165g) + Lentilha cozida (85g) + Peito de frango grelhado (120g) + Cenoura cozida (160g) + Salada de rúcula à vontade + azeite (8ml)",
//                    "Iogurte natural desnatado (165g) + Mix de castanhas (15g) + Morango (120g)",
//                    "Filé de frango grelhado (150g) + Moranga cozida (300g) + Salada de acelga limão à vontade",
//                    1, 3, 1, 3
//            ));
//
//            db.dietaPreProntaDAO().inserir(new DietaPreProntaModel(
//                    "Massa Magra com Baixo Custo",
//                    "Como a pessoa treina pouco, o ganho de massa vem da regularidade proteica, refeições simples, rápidas, baratas e com bastante carboidrato.",
//                    "Pão francês (50g) + Pasta de amendoim (16g) + Banana (55g) + Iogurte natural (100g)",
//                    "Arroz (165g) + Feijão (140g) + Peito de frango grelhado (120g) + Cenoura cozida (160g)",
//                    "Bolo de aveia (50g) + Iogurte natural desnatado (165g)",
//                    "Tapioca (200g) com frango desfiado (120g) + Suco natural de laranja (180ml)",
//                    2, 2, 3, 3
//            ));
//
//            db.dietaPreProntaDAO().inserir(new DietaPreProntaModel(
//                    "Emagrecimento Saudável Vegetariano",
//                    "Dieta vegetariana, rica em fibras e com menor densidade calórica. Foco total em emagrecimento saudável e controle calórico.",
//                    "Panqueca de banana com aveia (121g) + Iogurte grego (100g) + Mamão (170g) + Semente de chia (15g)",
//                    "Arroz integral (165g) + Lentilha (85g) + Ovo cozido (2un) + Couve-flor cozida (180g) + Salada de rúcula à vontade + fio de azeite",
//                    "Iogurte natural desnatado (165g) + Castanha-do-pará (12g) + Morango (120g)",
//                    "Legumes à moda mediterrânea (150g) + Batata inglesa cozida (300g) + Suco de limão (240ml)",
//                    1, 2, 2, 2
//            ));
//
//            db.dietaPreProntaDAO().inserir(new DietaPreProntaModel(
//                    "Ganho de Massa Vegetariano",
//                    "Com foco em proteínas vegetais, ovos, laticínios e carboidratos mais densos. Ideal para vegetarianos que querem aumentar a ingestão calórica e proteica.",
//                    "Pão integral (100g) + Queijo minas (40g) + Ovo mexido (100g) + Banana (55g)",
//                    "Macarrão cozido (160g) + Grão-de-bico cozido (83g) + Ovo cozido (2un) + Beterraba cozida (125g) + Salada de acelga à vontade + fio de azeite",
//                    "Crepioca (100g) + Pasta de amendoim (16g) + Kiwi (150g)",
//                    "Legumes cozidos (165g) + Aipim cozido (150g) + Iogurte natural (100g)",
//                    2, 3, 2, 3
//            ));
//        }
//    }
    // Escolhe melhor dieta baseada nas respostas do cadastro (4 perguntas)
    private DietaPreProntaModel escolherMelhorDieta(UsuarioModel usuario) {
        AppDatabase db = AppDatabase.getInstance(this);
        return db.dietaPreProntaDAO().buscarMaisCompatíveis(
                usuario.getObjetivoId(),
                usuario.getAtividadeId(),     // ⚠️ estava errado
                usuario.getPraticidadeId(),
                usuario.getRigorId()          // ⚠️ estava errado
        );
    }

    private void adicionarDietaParaDia(int dia, int idUsuario, DietaPreProntaModel dieta) {
        AppDatabase db = AppDatabase.getInstance(this);

        // 1) Insere a dieta no dia
        db.dietaDAO().inserirNoDia(dia, idUsuario, dieta.getId());

        // 2) Insere ou atualiza as refeições
        RefeicaoDAO rDao = db.RefeicaoDAO();
        upsertRefeicao(rDao, dia, idUsuario,"Café da Manhã", "08:00", dieta.getCafeManha());
        upsertRefeicao(rDao, dia, idUsuario,"Almoço", "12:00", dieta.getAlmoco());
        upsertRefeicao(rDao, dia, idUsuario,"Café da Tarde", "15:00", dieta.getCafeTarde());
        upsertRefeicao(rDao, dia, idUsuario,"Jantar", "19:00", dieta.getJantar());
    }

    private void upsertRefeicao(RefeicaoDAO rDao, int dia, int idUsuario, String tipo, String horario, String conteudo) {
        if (conteudo == null || conteudo.trim().isEmpty()) return; //------teste

        // BUSCA se já existe refeição nesse dia com este nome
        Refeicao existente = rDao.getRefeicaoPorDiaENome(dia, idUsuario, tipo);
        //int idUsuario = (int) usuarioLogado.getId();

        if (existente == null) {
            // NÃO EXISTE? INSERE NOVA
            Refeicao nova = new Refeicao(dia, idUsuario, tipo, horario, formatarRefeicao(conteudo));
            rDao.inserirRefeicao(nova);
        } else {
            // EXISTE? ATUALIZA
            existente.setHorario(horario);
            existente.setDescricao(formatarRefeicao(conteudo));
            rDao.atualizarRefeicao(existente);
        }
    }

    private String formatarRefeicao(String conteudo) {
        if (conteudo == null || conteudo.trim().isEmpty()) return "";

        String[] itens = conteudo.split("\\+");
        StringBuilder builder = new StringBuilder();

        for (String item : itens) {
            String nome = item.trim();
            if (!nome.isEmpty()) {
                // Aqui definimos o formato padrão
                builder.append(nome)
                        //.append(" - ")
                        .append("\n");
            }
        }
        return builder.toString().trim(); // remove última quebra de linha
    }

    private void mostrarMensagemErro(String mensagem) {
        new AlertDialog.Builder(this)
                .setTitle("Informação")
                .setMessage(mensagem)
                .setPositiveButton("Entendi", (dialog, which) -> dialog.dismiss())
                .show();
    }
}