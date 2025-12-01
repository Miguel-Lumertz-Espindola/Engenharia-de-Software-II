package com.example.projetoengenhariadesoftwareii;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projetoengenhariadesoftwareii.database.AppDatabase;
import com.example.projetoengenhariadesoftwareii.database.DAO.RefeicaoDAO;
import com.example.projetoengenhariadesoftwareii.database.model.DietaPreProntaModel;
import com.example.projetoengenhariadesoftwareii.database.model.UsuarioModel;
import com.example.projetoengenhariadesoftwareii.database.DAO.UsuarioDao;
import com.example.projetoengenhariadesoftwareii.database.model.Dieta;
import com.example.projetoengenhariadesoftwareii.database.DAO.DietaDAO;
import com.example.projetoengenhariadesoftwareii.database.model.Refeicao;
import com.example.projetoengenhariadesoftwareii.DiaItem;

import java.util.*;

public class DietasPreProntasActivity extends AppCompatActivity implements DietaPreProntaAdapter.OnDietaClickListener {

    private RecyclerView recyclerDietaspreprontas, recyclerDias;
    ImageButton buttonMenu, buttonLogo;
    private AppDatabase db;
    private DietaDAO dietaDAO;
    private RefeicaoDAO refeicaoDao;
    private UsuarioModel usuarioLogado; // ← receber do login

    // 🔹 Mini agenda
    private Calendar calendario;
    private List<DiaItem> diasDoMes = new ArrayList<>();
    private Set<Integer> diasSelecionados = new HashSet<>();
    private DiaAdapter diaAdapter;
    private int mesAtual, anoAtual, diaHoje, semanaAtual = 1;
    private TextView textoMesSemana;
    private final String[] nomesDias = {"Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dietas_pre_prontas);

        // ⚠️ 1. RECEBER USUÁRIO LOGADO
        usuarioLogado = getIntent().getParcelableExtra("usuarioLogado");
        if (usuarioLogado == null) {
            Toast.makeText(this, "Erro ao receber usuário", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //escolherMelhorDieta();

        recyclerDietaspreprontas = findViewById(R.id.recyclerDietaspreprontas);
        recyclerDias = findViewById(R.id.recyclerDias);
        textoMesSemana = findViewById(R.id.textoMesSemana);
        buttonMenu = findViewById(R.id.buttonMenu);
        buttonLogo = findViewById(R.id.buttonlogo);

        recyclerDietaspreprontas.setLayoutManager(new LinearLayoutManager(this));
        recyclerDias.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        db = AppDatabase.getInstance(this);
        dietaDAO = db.dietaDAO();

        //db.dietaPreProntaDAO().excluirTodas();

        refeicaoDao = db.RefeicaoDAO(); // <-- ESSA LINHA FALTAVA!

        // 🔹 Inicializa agenda
        calendario = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));
        mesAtual = calendario.get(Calendar.MONTH);
        anoAtual = calendario.get(Calendar.YEAR);
        diaHoje = calendario.get(Calendar.DAY_OF_MONTH);

        diasDoMes = gerarDiasDoMes(mesAtual, anoAtual);
        List<DiaItem> primeiraSemana = getSubListaSemana(1);
        diaAdapter = new DiaAdapter(primeiraSemana, diasSelecionados, dia -> {
            if (!diasSelecionados.contains(dia)) diasSelecionados.add(dia);
            else diasSelecionados.remove(dia);
            diaAdapter.notifyDataSetChanged();
        });
        recyclerDias.setAdapter(diaAdapter);
        atualizarTextoMesSemana();

        findViewById(R.id.btnSemanaAnterior).setOnClickListener(v -> {
            if (semanaAtual > 1) {
                semanaAtual--;
                irParaSemana(semanaAtual);
            }
        });

        findViewById(R.id.btnProximaSemana).setOnClickListener(v -> {
            int totalSemanas = (int) Math.ceil(diasDoMes.size() / 7.0);
            if (semanaAtual < totalSemanas) {
                semanaAtual++;
                irParaSemana(semanaAtual);
            }
        });

        // 🔹 Insere dietas pré-prontas só se o banco estiver vazio

        //Obj: (1 - emagrecer, 2 - ganho de massa, 3 - manter peso, 4 - melhorar saúde
        //Freq: (1 - sedentário, 2 - leve, 3 - moderado, 4 - intenso
        //Prat: (1 - baixa, 2 - média, 3 - alta
        //Rigor: (1 - leve, 2 - moderado, 3 - rigoroso

        if (db.dietaPreProntaDAO().getTodas().isEmpty()) {
            db.dietaPreProntaDAO().inserir(new DietaPreProntaModel(
                    "Emagrecimento Prático",
                    "Feita para quem quer perder peso de forma leve, sem muitas restrições e com alimentos simples, baratos e fáceis de preparar.",
                    "Pão Francês (50g) + ovo mexido (100g) + mamão (170g)",
                    "Arroz (165g) + Feijão (140g) + Peito de frango grelhado (100g) + Cenoura cozida (160g) + Salada de alface e pepino à vontade + 1 fio de azeite",
                    "Iogurte natural desnatado (165g) + Granola (39g) + banana pequena (50g)",
                    "Filé de frango grelhado (150g) + Legumes cozidos (165g) + Suco natural de limão sem açúcar (240ml)",
                    1, 2, 3, 1
            ));

            db.dietaPreProntaDAO().inserir(new DietaPreProntaModel(
                    "Ganho de Massa com Moderado Rigor",
                    "Focada em fornecer proteínas e calorias suficientes para aumentar a massa muscular, com alimentos balanceados e quantidades um pouco maiores.",
                    "Pão de forma integral (100g) + Pasta de amendoim (16g) + Iogurte grego (100g) + banana pequena (55g)",
                    "Arroz integral (165g) + Lentilha (85g) + Peito de frango grelhado (120g) + Beterraba cozida (125g) + Salada de rúcula e pepino à vontade + azeite (8ml)",
                    "Crepioca (100g) com frango desfiado (60g) + mamão papaia (170g)",
                    "Carne moída refogada (75g)+ Macarrão cozido (140g) + Legumes à moda mediterrânea (150g) + Suco de abacaxi (165ml)",
                    2, 4, 2, 2
            ));

            db.dietaPreProntaDAO().inserir(new DietaPreProntaModel(
                    "Manutenção de Peso Saudável",
                    "Equilíbrio entre energia e nutrientes. Ideal para quem busca manter o peso e a saúde com refeições variadas e flexíveis.",
                    "Pão caseiro com requeijão (100g) + ovo mexido (100g) + fatia de melância (200g)",
                    "Polenta (240g) + Feijão (140g) + Filé de peixe grelhado (120g) + Moranga (180g) + Salada de acelga com azeite",
                    "Iogurte grego (100g) + Castanha-do-pará (16g) + pêra (110g)",
                    "Frango desfiado (120g) + Aipim cozido (150g) + Suco natural de abacaxi (165ml)",
                    3, 3, 2, 1
            ));

            db.dietaPreProntaDAO().inserir(new DietaPreProntaModel(
                    "Melhorar Saúde em Geral, Alto Rigor e Orçamento",
                    "Dieta inspirada no estilo mediterrâneo, com foco em qualidade nutricional, antioxidantes e gorduras boas. Inclui alimentos mais caros (salmão, oleaginosas, frutas variadas).",
                    "Panqueca de banana com aveia (121g) + Iogurte natural (100g) + Mix de sementes (chia, girassol, abóbora) (45g)",
                    "Arroz integral (165g) + Grão-de-bico cozido (83g) + Filé de peixe grelhado (120g) + Legumes variados (cenoura, chuchu, couve-flor) (180g) + Azeite extravirgem (8ml)",
                    "Iogurte grego (100g) + Abacate (135g) com farelo de aveia (20g)",
                    "Filé de frango grelhado (150g) + Salada de legumes cozidos (165g) + Suco de limão tahiti sem açucar (240ml)",
                    4, 3, 1, 3
            ));

            db.dietaPreProntaDAO().inserir(new DietaPreProntaModel(
                    "Emagrecimento Controlado",
                    "Dieta mais trabalhosa (baixa praticidade), com preparações caseiras, alimentos frescos e controle alto de calorias. Foco forte em proteínas magras, fibras e quase nada de ultraprocessados.",
                    "Panqueca de banana com aveia (121g) + Ovos mexidos (100g) + Mamão (170g) + Chia (15g)",
                    "Arroz integral (165g) + Lentilha cozida (85g) + Peito de frango grelhado (120g) + Cenoura cozida (160g) + Salada de rúcula à vontade + azeite (8ml)",
                    "Iogurte natural desnatado (165g) + Mix de castanhas (15g) + Morango (120g)",
                    "Filé de frango grelhado (150g) + Moranga cozida (300g) + Salada de acelga limão à vontade",
                    1, 3, 1, 3
            ));

            db.dietaPreProntaDAO().inserir(new DietaPreProntaModel(
                    "Massa Magra com Baixo Custo",
                    "Como a pessoa treina pouco, o ganho de massa vem da regularidade proteica, refeições simples, rápidas, baratas e com bastante carboidrato.",
                    "Pão francês (50g) + Pasta de amendoim (16g) + Banana (55g) + Iogurte natural (100g)",
                    "Arroz (165g) + Feijão (140g) + Peito de frango grelhado (120g) + Cenoura cozida (160g)",
                    "Bolo de aveia (50g) + Iogurte natural desnatado (165g)",
                    "Tapioca (200g) com frango desfiado (120g) + Suco natural de laranja (180ml)",
                    2, 2, 3, 3
            ));

            db.dietaPreProntaDAO().inserir(new DietaPreProntaModel(
                    "Emagrecimento Saudável Vegetariano",
                    "Dieta vegetariana, rica em fibras e com menor densidade calórica. Foco total em emagrecimento saudável e controle calórico.",
                    "Panqueca de banana com aveia (121g) + Iogurte grego (100g) + Mamão (170g) + Semente de chia (15g)",
                    "Arroz integral (165g) + Lentilha (85g) + Ovo cozido (2un) + Couve-flor cozida (180g) + Salada de rúcula à vontade + fio de azeite",
                    "Iogurte natural desnatado (165g) + Castanha-do-pará (12g) + Morango (120g)",
                    "Legumes à moda mediterrânea (150g) + Batata inglesa cozida (300g) + Suco de limão (240ml)",
                    1, 2, 2, 2
            ));

            db.dietaPreProntaDAO().inserir(new DietaPreProntaModel(
                    "Ganho de Massa Vegetariano",
                    "Com foco em proteínas vegetais, ovos, laticínios e carboidratos mais densos. Ideal para vegetarianos que querem aumentar a ingestão calórica e proteica.",
                    "Pão integral (100g) + Queijo minas (40g) + Ovo mexido (100g) + Banana (55g)",
                    "Macarrão cozido (160g) + Grão-de-bico cozido (83g) + Ovo cozido (2un) + Beterraba cozida (125g) + Salada de acelga à vontade + fio de azeite",
                    "Crepioca (100g) + Pasta de amendoim (16g) + Kiwi (150g)",
                    "Legumes cozidos (165g) + Aipim cozido (150g) + Iogurte natural (100g)",
                    2, 3, 2, 3
            ));
        }

        List<DietaPreProntaModel> listaDietas = db.dietaPreProntaDAO().getTodas();

//        DietaPreProntaAdapter adapter = new DietaPreProntaAdapter(listaDietas, new DietaPreProntaAdapter.OnDietaClickListener() {
//            @Override
//            public void onVisualizar(DietaPreProntaModel dieta) {
//                abrirDialogVisualizar(DietasPreProntasActivity.this, dieta);
//            }
//
//            @Override
//            public void onAdicionar(DietaPreProntaModel dieta) {
//                adicionarDietaNosDiasSelecionados(dieta);
//            }
//        });
        DietaPreProntaAdapter adapter = new DietaPreProntaAdapter(listaDietas, this);

        DietaPreProntaModel escolhida = escolherMelhorDieta(usuarioLogado);

        if (escolhida != null) {
            for (DiaItem diaItem : diasDoMes) {
                adicionarDietaParaDia(diaItem.getDia(), escolhida);
            }
        }

        recyclerDietaspreprontas.setAdapter(adapter);

        configurarMenu();
    }

    @Override
    public void onVisualizar(DietaPreProntaModel dieta) {
        abrirDialogVisualizar(this, dieta);
    }

    @Override
    public void onAdicionar(DietaPreProntaModel dieta) {
        // quando o usuário clicar em "Adicionar" na lista, usamos os dias selecionados
        if (diasSelecionados.isEmpty()) {
            Toast.makeText(this, "Selecione ao menos um dia na mini agenda!", Toast.LENGTH_SHORT).show();
            return;
        }
        for (int dia : diasSelecionados) {
            adicionarDietaParaDia(dia, dieta);
        }
        Toast.makeText(this, "Dieta adicionada com sucesso!", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }


    // 🔹 ADICIONAR DIETA PARA UM DIA ESPECÍFICO (SEM USAR `diasSelecionados`)
    private void adicionarDietaParaDia(int dia, DietaPreProntaModel dieta) {
        //UsuarioModel usuarioLogado = Sessao.getUsuarioLogado(); // ou outro método que você usa
        int idUsuario = (int) usuarioLogado.getId(); // agora sim temos o ID
        Dieta dietaExistente = dietaDAO.getDietaPorDia(dia, idUsuario);
        Log.d("DEBUG_DIETA", "Dia recebido: " + dia + " | Usuário: " + usuarioLogado.getId());

        if (dietaExistente == null) {
            // não existe → cria uma nova dieta
            Dieta nova = new Dieta(
                    dia,
                    idUsuario,
                    0,
                    dieta.getCafeManha(),
                    dieta.getAlmoco(),
                    dieta.getCafeTarde(),
                    dieta.getJantar()
            );
            dietaDAO.inserirDieta(nova);
        } else {
            // já existe → apenas atualiza campos vazios
            if (dietaExistente.getCafeManha() == null || dietaExistente.getCafeManha().isEmpty()) {
                dietaExistente.setCafeManha(dieta.getCafeManha());
            }
            if (dietaExistente.getAlmoco() == null || dietaExistente.getAlmoco().isEmpty()) {
                dietaExistente.setAlmoco(dieta.getAlmoco());
            }
            if (dietaExistente.getCafeTarde() == null || dietaExistente.getCafeTarde().isEmpty()) {
                dietaExistente.setCafeTarde(dieta.getCafeTarde());
            }
            if (dietaExistente.getJantar() == null || dietaExistente.getJantar().isEmpty()) {
                dietaExistente.setJantar(dieta.getJantar());
            }
            dietaDAO.atualizarDieta(dietaExistente);
        }

        // SEMPRE CRIAR AS REFEIÇÕES PADRÃO
        String[][] refeicoes = {
                {"Café da Manhã", "08:00", dieta.getCafeManha()},
                {"Almoço", "12:00", dieta.getAlmoco()},
                {"Café da Tarde", "15:00", dieta.getCafeTarde()},
                {"Jantar", "19:00", dieta.getJantar()}
        };

        for (String[] r : refeicoes) {
            if (refeicaoDao.getRefeicaoPorDiaENome(dia, idUsuario, r[0]) == null) {
                refeicaoDao.inserirRefeicao(new Refeicao(dia, idUsuario, r[0], r[1], r[2]));
            }
        }
        Log.d("DEBUG_DIETA", "Dieta criada ou atualizada para o dia: " + dia);
    }


    // 🔹 ESCOLHE AS DIETAS MAIS COMPATÍVEIS COM O USUÁRIO
    // 📌 PEGAR O USUÁRIO LOGADO

    // 📌 CALCULAR COMPATIBILIDADE
    private int calcularCompatibilidade(DietaPreProntaModel dieta, UsuarioModel usuario) {
        int score = 0;

        if (dieta.getObjetivoId() == usuario.getObjetivoId()) score++;
        if (dieta.getAtividadeId() == usuario.getAtividadeId()) score++;
        if (dieta.getPraticidadeId() == usuario.getPraticidadeId()) score++;
        if (dieta.getRigorId() == usuario.getRigorId()) score++;

        return score;
    }

    private DietaPreProntaModel escolherMelhorDieta(UsuarioModel usuario) {
        List<DietaPreProntaModel> todas = db.dietaPreProntaDAO().getTodas();

        DietaPreProntaModel melhor = null;
        int maiorScore = -1;

        for (DietaPreProntaModel d : todas) {
            int score = calcularCompatibilidade(d, usuario);

            if (score > maiorScore) {
                maiorScore = score;
                melhor = d;   // ← ESCOLHE APENAS UMA DIETA
            }
        }
        return melhor;
    }

//    private List<DietaPreProntaModel> escolherMelhorDieta(UsuarioModel user) {
//        if (usuarioLogado == null) {
//            Log.e("ERRO_DIETA", "Usuário está NULL!");
//            return new ArrayList<>();
//        }
//        List<DietaPreProntaModel> todas = db.dietaPreProntaDAO().getTodas();
//
//        int melhorPontuacao = -1;
//        List<DietaPreProntaModel> melhores = new ArrayList<>();
//
//        for (DietaPreProntaModel d : todas) {
//            int pontos = 0;
//            if (d.getObjetivoId() == user.getObjetivoId()) pontos++;
//            if (d.getAtividadeId() == user.getAtividadeId()) pontos++;
//            if (d.getPraticidadeId() == user.getPraticidadeId()) pontos++;
//            if (d.getRigorId() == user.getRigorId()) pontos++;
//
//            if (pontos > melhorPontuacao) {
//                melhorPontuacao = pontos;
//                melhores.clear();
//                melhores.add(d);
//            } else if (pontos == melhorPontuacao) {
//                melhores.add(d);
//            }
//        }
//        return melhores; // 1 ou várias dietas
//    }

    // 🔹 Salva a dieta pré-pronta nos dias marcados (com ingredientes formatados)
    private void adicionarDietaSelecionada(int dia, int idUsuario, DietaPreProntaModel escolhida) {
        AppDatabase db = AppDatabase.getInstance(this);

        // Salva a dieta no dia
        db.dietaDAO().inserirNoDia(dia, idUsuario, escolhida.getId());

        // Insere ou atualiza as refeições
        RefeicaoDAO rDao = db.RefeicaoDAO();
        upsertRefeicao(rDao, dia, idUsuario,"Café da Manhã", "08:00", escolhida.getCafeManha());
        upsertRefeicao(rDao, dia, idUsuario,"Almoço", "12:00", escolhida.getAlmoco());
        upsertRefeicao(rDao, dia, idUsuario,"Café da Tarde", "15:00", escolhida.getCafeTarde());
        upsertRefeicao(rDao, dia, idUsuario,"Jantar", "19:00", escolhida.getJantar());
    }

    private void upsertRefeicao(RefeicaoDAO rDao, int dia, int idUsuario, String tipo, String horario, String conteudo) {
        if (conteudo == null || conteudo.trim().isEmpty()) return;

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

//    private void adicionarDietaNosDiasSelecionados(DietaPreProntaModel dieta) {
//        if (diasSelecionados.isEmpty()) {
//            Toast.makeText(this, "Selecione ao menos um dia na mini agenda!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // 🔸 Formatar refeições
//        String cafe = formatarRefeicao(dieta.getCafeManha());
//        String almoco = formatarRefeicao(dieta.getAlmoco());
//        String cafeTarde = formatarRefeicao(dieta.getCafeTarde());
//        String jantar = formatarRefeicao(dieta.getJantar());
//
//        int idDietaPrePronta = 0;  // ou algum valor recebido de Intent ou Spinner
//
//        // 🔹 Salvar para cada dia selecionado
//        for (int dia : diasSelecionados) {
//            int idUsuario = (int) usuarioLogado.getId();
//
//            // Salvar dieta na tabela DIETAS
//            Dieta nova = new Dieta(dia, idUsuario, idDietaPrePronta, cafe, almoco, cafeTarde, jantar);
//            dietaDAO.salvarDieta(nova);
//
//            // 🔄 SE EXISTIR REFEICOES NO DIA → NÃO APAGA! SOMENTE PREENCHE OS CAMPOS VAZIOS
//            List<Refeicao> existentes = refeicaoDao.getRefeicoesPorDia(dia);
//
//            if (existentes != null && !existentes.isEmpty()) {
//                // Só atualiza os campos vazios
//                for (Refeicao r : existentes) {
//                    if (r.getDescricao() == null || r.getDescricao().trim().isEmpty()) {
//                        if (r.getNome().contains("Café da Manhâ")) r.setDescricao(cafe);
//                        else if (r.getNome().contains("Almoço")) r.setDescricao(almoco);
//                        else if (r.getNome().contains("Café da Tarde")) r.setDescricao(cafeTarde);
//                        else if (r.getNome().contains("Jantar")) r.setDescricao(jantar);
//                        refeicaoDao.atualizarRefeicao(r);
//                    }
//                }
//            } else {
//                // NÃO EXISTE NADA → cria padrão
//                refeicaoDao.inserirRefeicao(new Refeicao(dia, "Café da Manhã", "08:00", cafe));
//                refeicaoDao.inserirRefeicao(new Refeicao(dia, "Almoço", "12:00", almoco));
//                refeicaoDao.inserirRefeicao(new Refeicao(dia, "Café da Tarde", "15:00", cafe));
//                refeicaoDao.inserirRefeicao(new Refeicao(dia, "Jantar", "19:00", jantar));
//            }
//        }
//
//        Toast.makeText(this,
//                "Dieta \"" + dieta.getNomeDieta() + "\" adicionada nos dias selecionados!",
//                Toast.LENGTH_SHORT).show();
//    }

    // 🔹 Função auxiliar para transformar "A + B + C" → em linhas "A - ? unidade"
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

    // 🔹 Métodos da mini agenda
    private List<DiaItem> gerarDiasDoMes(int mes, int ano) {
        List<DiaItem> dias = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, ano);
        cal.set(Calendar.MONTH, mes);
        int totalDias = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 1; i <= totalDias; i++) {
            cal.set(Calendar.DAY_OF_MONTH, i);
            int diaSemana = cal.get(Calendar.DAY_OF_WEEK);
            boolean isHoje = (i == diaHoje && mes == calendario.get(Calendar.MONTH) && ano == calendario.get(Calendar.YEAR));
            dias.add(new DiaItem(i, nomesDias[diaSemana - 1], isHoje));
        }
        return dias;
    }

    private List<DiaItem> getSubListaSemana(int semana) {
        int inicio = (semana - 1) * 7;
        int fim = Math.min(inicio + 7, diasDoMes.size());
        return new ArrayList<>(diasDoMes.subList(inicio, fim));
    }

    private void irParaSemana(int semana) {
        List<DiaItem> subLista = getSubListaSemana(semana);
        diaAdapter.atualizarLista(subLista, diasSelecionados);
        atualizarTextoMesSemana();
    }

    private void atualizarTextoMesSemana() {
        textoMesSemana.setText(getNomeMes(mesAtual) + " - Semana " + semanaAtual);
    }

    private String getNomeMes(int mes) {
        String[] nomes = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};
        return nomes[mes];
    }

    // Exibe popup de visualização
    private void abrirDialogVisualizar(Context context, DietaPreProntaModel dieta) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_visualizar_dieta_pre_prontas, null);

        TextView titulo = dialogView.findViewById(R.id.tituloDieta);
        TextView descricao = dialogView.findViewById(R.id.textDescricao);
        LinearLayout layoutRefeicoes = dialogView.findViewById(R.id.layoutRefeicoes);


        titulo.setText(dieta.getNomeDieta());
        descricao.setText(dieta.getDescricao());

        adicionarRefeicao(layoutRefeicoes, "Café da Manhã", dieta.getCafeManha());
        adicionarRefeicao(layoutRefeicoes, "Almoço", dieta.getAlmoco());
        adicionarRefeicao(layoutRefeicoes, "Café da Tarde", dieta.getCafeTarde());
        adicionarRefeicao(layoutRefeicoes, "Jantar", dieta.getJantar());

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setPositiveButton("Fechar", null)
                .create();
        dialog.show();

        // 👇 Define o background do diálogo inteiro
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
    }

    private void adicionarRefeicao(LinearLayout container, String titulo, String conteudo) {
        TextView tvTitulo = new TextView(container.getContext());
        tvTitulo.setText(titulo);
        tvTitulo.setTextColor(container.getResources().getColor(R.color.azulpadrao));
        tvTitulo.setTextSize(16);
        tvTitulo.setTypeface(null, android.graphics.Typeface.BOLD);
        container.addView(tvTitulo);

        TextView tvConteudo = new TextView(container.getContext());
        tvConteudo.setText(conteudo == null || conteudo.isEmpty() ? "Nenhum item" : conteudo);
        tvConteudo.setTextColor(container.getResources().getColor(R.color.black));
        tvConteudo.setTextSize(14);
        tvConteudo.setPadding(16, 4, 0, 12);
        container.addView(tvConteudo);
    }

    private void configurarMenu() {
        buttonLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DietasPreProntasActivity.this, todayActivity.class);
                startActivity(intent);
            }
        });

        buttonMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(DietasPreProntasActivity.this, buttonMenu);
            popupMenu.getMenuInflater().inflate(R.menu.activity_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.opcao1) {
                    startActivity(new Intent(this, todayActivity.class));
                    return true;
                } else if (id == R.id.opcao2) {
                    startActivity(new Intent(this, ComprasMesActivity.class));
                    return true;
                } else if (id == R.id.opcao3) {
                    startActivity(new Intent(this, RelatoriosActivity.class));
                    return true;
                } else if (id == R.id.opcao4) {
                    startActivity(new Intent(this, SobreActivity.class));
                    return true;
                } else if (id == R.id.opcao5) {
                    startActivity(new Intent(this, LoginActivity.class));
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });
    }
}