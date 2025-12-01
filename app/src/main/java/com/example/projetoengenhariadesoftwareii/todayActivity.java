package com.example.projetoengenhariadesoftwareii;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.example.projetoengenhariadesoftwareii.database.AppDatabase;
import com.example.projetoengenhariadesoftwareii.database.DAO.IngredienteDAO;
import com.example.projetoengenhariadesoftwareii.database.DAO.RefeicaoDAO;
import com.example.projetoengenhariadesoftwareii.database.model.Dieta;
import com.example.projetoengenhariadesoftwareii.database.model.DietaPreProntaModel;
import com.example.projetoengenhariadesoftwareii.database.model.Ingrediente;
import com.example.projetoengenhariadesoftwareii.database.model.Refeicao;
import com.example.projetoengenhariadesoftwareii.database.model.UsuarioModel;

import java.text.DateFormatSymbols;
import java.util.*;

public class todayActivity extends AppCompatActivity {

    ImageButton buttonMenu, buttonLogo, btnSemanaAnterior, btnProximaSemana;
    TextView textoMesSemana;
    RecyclerView recyclerDias;
    Button btnEditarDieta;

    int semanaAtual = 1;
    Calendar calendario = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));

    int mesAtual = calendario.get(Calendar.MONTH);
    int anoAtual = calendario.get(Calendar.YEAR);
    int diaHoje; // atualizado com fuso horário correto

    DiaAdapter diaAdapter;
    List<DiaItem> listaDiasExibida = new ArrayList<>(); // lista que o adapter usa
    List<DiaItem> listaTodosDias = new ArrayList<>();   // lista completa do mês
    Set<Integer> diasSelecionados = new HashSet<>();
    int diaSelecionadoAtual = -1; // guarda o último dia clicado
    private final String[] nomesDias = {"Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"};
    private UsuarioModel usuarioLogado; // ← receber do login


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_today);

            // ---------- 1) RECEBER USUÁRIO (INTENT ou Session) ----------
            // tenta primeiro pela intent (como você vinha usando)
            usuarioLogado = getIntent().getParcelableExtra("usuarioLogado");
            if (usuarioLogado == null) {
                Log.e("ERRO_USER", "Usuário NÃO recebido! Saindo da activity.");
                Toast.makeText(this, "Erro: usuário não autenticado.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            if (usuarioLogado == null) {
                Log.e("ERRO_USER", "Usuário NÃO recebido! Saindo da activity.");
                Toast.makeText(this, "Erro: usuário não autenticado.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // ---------- 2) inicializações básicas ----------
            if (getSupportActionBar() != null) getSupportActionBar().hide();

            Calendar calBR = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));
            diaHoje = calBR.get(Calendar.DAY_OF_MONTH);

            buttonMenu = findViewById(R.id.buttonMenu);
            buttonLogo = findViewById(R.id.buttonlogo);
            textoMesSemana = findViewById(R.id.textoMesSemana);
            btnSemanaAnterior = findViewById(R.id.btnSemanaAnterior);
            btnProximaSemana = findViewById(R.id.btnProximaSemana);
            recyclerDias = findViewById(R.id.recyclerDias);
            btnEditarDieta = findViewById(R.id.btnEditarDieta);

            recyclerDias.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

            gerarTodosDiasDoMes();
            atualizarParaSemana(1);

            btnSemanaAnterior.setOnClickListener(v -> {
                if (semanaAtual > 1) {
                    semanaAtual--;
                    atualizarParaSemana(semanaAtual);
                }
            });

            btnProximaSemana.setOnClickListener(v -> {
                int totalSemanas = (int) Math.ceil(listaTodosDias.size() / 7.0);
                if (semanaAtual < totalSemanas) {
                    semanaAtual++;
                    atualizarParaSemana(semanaAtual);
                }
            });

            btnEditarDieta.setOnClickListener(v -> abrirTelaEdicaoDieta());
            configurarMenu();

            // ---------- 3) Inserir dieta compatível por USUÁRIO (apenas 1x por mês/usuário ou por dia conforme preferir) ----------
            // essa operação acessa DB → roda em background
            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(this);
                RefeicaoDAO refeicaoDAO = db.RefeicaoDAO();
                DietaPreProntaModel escolhida = escolherMelhorDieta(usuarioLogado);
                if (escolhida == null) {
                    Log.d("DIETA_INIT", "Nenhuma dieta pré pronta encontrada.");
                    return;
                }

                // opcional: inserir para todo o mês (como você queria). Use chave por usuário para não repetir.
                SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                String keyInicial = "dieta_inserida_mes_user_" + usuarioLogado.getId() + "_" + calBR.get(Calendar.MONTH) + "_" + calBR.get(Calendar.YEAR);

                if (!prefs.getBoolean(keyInicial, false)) {
                    int totalDiasMes = calBR.getActualMaximum(Calendar.DAY_OF_MONTH);
                    for (int dia = 1; dia <= totalDiasMes; dia++) {
                        // só insere se não tiver refeições nem dieta para ESTE dia/usuário
                        List<Refeicao> existentes = refeicaoDAO.buscarPorDiaEUsuario(dia, (int) usuarioLogado.getId());
                        if (existentes == null || existentes.isEmpty()) {
                            adicionarDietaParaDia(dia, (int) usuarioLogado.getId(), escolhida);
                        }
                    }
                    prefs.edit().putBoolean(keyInicial, true).apply();
                    Log.d("DIETA_INIT", "Dieta compatível inserida para o mês do usuário: " + usuarioLogado.getId());
                }

                // depois de inserir, atualizar a UI
                //runOnUiThread(this::atualizarRefeicoesDoDia);
                runOnUiThread(() -> {
                    gerarTodosDiasDoMes();      // ⚠️ REFAZ a lista de dias
                    atualizarParaSemana(semanaAtual); // ⚠️ RECARREGA o adapter
                });
            }).start();

    }

    private void gerarTodosDiasDoMes() {
        int totalDiasMes = calendario.getActualMaximum(Calendar.DAY_OF_MONTH);
        listaTodosDias.clear();
        for (int dia = 1; dia <= totalDiasMes; dia++) {
            boolean isHoje = (dia == diaHoje);
            listaTodosDias.add(new DiaItem(dia, getDiaSemana(dia), isHoje));
        }
    }

    // Atualiza o adapter para mostrar apenas os dias da semana X
    private void atualizarParaSemana(int semana) {
        int totalDiasMes = listaTodosDias.size();
        int primeiroDia = (semana - 1) * 7; // índice 0-based
        int ultimoIndice = Math.min(primeiroDia + 7, totalDiasMes);

        List<DiaItem> sub = new ArrayList<>(listaTodosDias.subList(primeiroDia, ultimoIndice));

        // se adapter ainda não criado, cria com sublista mutável
        if (diaAdapter == null) {
            listaDiasExibida.clear();
            listaDiasExibida.addAll(sub);
            diaAdapter = new DiaAdapter(listaDiasExibida, diasSelecionados, dia -> {
                if (!diasSelecionados.contains(dia)) {
                    diasSelecionados.clear();
                    diasSelecionados.add(dia);
                } else {
                    diasSelecionados.remove(dia);
                }
                diaSelecionadoAtual = dia;
                mostrarRefeicoesDoDia(dia);
                diaAdapter.notifyDataSetChanged();
            });
            recyclerDias.setAdapter(diaAdapter);
        } else {
            diaAdapter.atualizarLista(sub, diasSelecionados);
        }



        String nomeMes = new DateFormatSymbols().getMonths()[mesAtual];
        textoMesSemana.setText(getNomeMes(mesAtual) + " - Semana " + semanaAtual);
        // garante que as refeições do dia atual/selecionado apareçam
        mostrarRefeicoesDoDia(diaSelecionadoAtual == -1 ? diaHoje : diaSelecionadoAtual);
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

    private String getNomeMes(int mes) {
        String[] nomes = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};
        return nomes[mes];
    }

    private String getDiaSemana(int diaDoMes) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, diaDoMes);
        return nomesDias[c.get(Calendar.DAY_OF_WEEK) - 1];
    }

    private void mostrarRefeicoesDoDia(int diaSelecionado) {
        findViewById(R.id.layoutRefeicoes).post(() -> {
            RefeicaoHelper.mostrarRefeicoesSimples(
                    todayActivity.this,
                    findViewById(R.id.layoutRefeicoes),
                    diaSelecionado,
                    (int) usuarioLogado.getId()
            );
        });
    }

    private DietaPreProntaModel escolherMelhorDieta(UsuarioModel usuario) {
        AppDatabase db = AppDatabase.getInstance(this);
        return db.dietaPreProntaDAO().buscarMaisCompatíveis(
                usuario.getObjetivoId(),
                usuario.getAtividadeId(),     // ⚠️ estava errado
                usuario.getPraticidadeId(),
                usuario.getRigorId()          // ⚠️ estava errado
        );
    }

    private void abrirTelaEdicaoDieta() {
        int diaParaEditar = (diaSelecionadoAtual != -1) ? diaSelecionadoAtual : diaHoje;
        Intent intent = new Intent(this, EditarDietaActivity.class);
        intent.putExtra("diaSelecionado", diaParaEditar);
        intent.putExtra("usuarioLogado", usuarioLogado);  // ✔️ CORRETO!

        Log.d("DEBUG_USER", "Recebido em <Activity>: " + (usuarioLogado==null? "NULL": usuarioLogado.getNome()));
        Log.d("TESTE_USUARIO", "Recebido usuário!");
        startActivityForResult(intent, 2001); // <-- permite detectar quando voltar
        //startActivity(intent);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2001 && resultCode == RESULT_OK) {
            atualizarRefeicoesDoDia();
        }
    }

    private void atualizarRefeicoesDoDia() {
        if (diaSelecionadoAtual == -1) {
            diaSelecionadoAtual = diaHoje;
        }
        mostrarRefeicoesDoDia(diaSelecionadoAtual);
    }

    private void configurarMenu() {
        buttonLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(todayActivity.this, todayActivity.class);
                startActivity(intent);
            }
        });

        buttonMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(todayActivity.this, buttonMenu);
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

    @Override
    protected void onResume() {
        super.onResume();
        atualizarRefeicoesDoDia(); // <-- garante atualização sempre que voltar para esta tela
        if (diaSelecionadoAtual != -1) {
            mostrarRefeicoesDoDia(diaSelecionadoAtual);
        }
    }
}