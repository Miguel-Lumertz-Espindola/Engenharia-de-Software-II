package com.example.projetoengenhariadesoftwareii;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projetoengenhariadesoftwareii.database.AppDatabase;
import com.example.projetoengenhariadesoftwareii.database.DAO.HorarioRefeicaoDAO;
import com.example.projetoengenhariadesoftwareii.database.DAO.RefeicaoDAO;
import com.example.projetoengenhariadesoftwareii.database.DAO.RefeicaoIngredienteDAO;
import com.example.projetoengenhariadesoftwareii.database.DAO.DietaDAO;
import com.example.projetoengenhariadesoftwareii.database.model.HorarioRefeicao;
import com.example.projetoengenhariadesoftwareii.database.model.Refeicao;
import com.example.projetoengenhariadesoftwareii.database.model.RefeicaoIngrediente;
import com.example.projetoengenhariadesoftwareii.database.DietaMigrator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EditarDietaActivity extends AppCompatActivity {

    ImageButton buttonMenu, buttonLogo;
    private EditText inputCafe, inputAlmoco, inputJantar;
    private TextView textoDia;
    private int diaSelecionado;
    private DietaDAO dietaDao;

    private AppDatabase db;
    private RefeicaoDAO refeicaoDAO;
    private RefeicaoIngredienteDAO riDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_dieta);

        buttonMenu = findViewById(R.id.buttonMenu);
        buttonLogo = findViewById(R.id.buttonlogo);

        // --- inicialização (manter campos antigos para compatibilidade) ---
        inputCafe = findViewById(R.id.inputCafeManha);
        inputAlmoco = findViewById(R.id.inputAlmoco);
        inputJantar = findViewById(R.id.inputJantar);
        textoDia = findViewById(R.id.textoDiaSelecionado);

        findViewById(R.id.btnHorarioCafe).setOnClickListener(v -> abrirDialogHorario("cafe"));
        findViewById(R.id.btnHorarioAlmoco).setOnClickListener(v -> abrirDialogHorario("almoco"));
        findViewById(R.id.btnHorarioJantar).setOnClickListener(v -> abrirDialogHorario("jantar"));


        db = AppDatabase.getInstance(this);
        dietaDao = db.dietaDAO();
        refeicaoDAO = db.refeicaoDAO();
        riDAO = db.refeicaoIngredienteDAO();

        // --- recupera o dia vindo da tela anterior ---
        diaSelecionado = getIntent().getIntExtra("diaSelecionado", -1);
        textoDia.setText("Dia selecionado: " + diaSelecionado);

        // MIGRATE if needed: cria Refeicao entries a partir da Dieta antiga caso não exista
        DietaMigrator.migrateIfNeeded(this, diaSelecionado);

        // Carrega Refeições atualizadas (se quiser mostrar no layout padrão, veja abaixo)
        // Se preferir manter os EditTexts e salvar como antes, esse código continua compatível.

        // --- botão salvar (mantém comportamento antigo) ---
        findViewById(R.id.btnSalvar).setOnClickListener(v -> {
            salvarDietaCompat();
        });
        findViewById(R.id.btnExcluir).setOnClickListener(v -> excluirDietaCompat());
        findViewById(R.id.btnInicio).setOnClickListener(v -> finish());

        // Adiciona botão dinâmico "Editar Refeições" no layout principal (layoutConteudoEditar existe no XML)
        LinearLayout layoutConteudo = findViewById(R.id.layoutConteudoEditar);
        Button btnEditarRefeicoes = new Button(this);
        btnEditarRefeicoes.setText("Editar Refeições");
        btnEditarRefeicoes.setAllCaps(false);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 12, 0, 12);
        btnEditarRefeicoes.setLayoutParams(params);
        layoutConteudo.addView(btnEditarRefeicoes, 0); // adiciona acima dos EditTexts
        btnEditarRefeicoes.setOnClickListener(v -> abrirDialogGerenciarRefeicoes(diaSelecionado));

        // Mantém navegações existentes no menu
        findViewById(R.id.btnCriarDieta).setOnClickListener(v -> {
            Intent intent = new Intent(EditarDietaActivity.this, CriarDietaActivity.class);
            startActivityForResult(intent, 1);
        });
        findViewById(R.id.btnDietaPronta).setOnClickListener(v -> {
            Intent intent = new Intent(EditarDietaActivity.this, DietasPreProntasActivity.class);
            startActivity(intent);
        });

        configurarMenu();
    }

    private void salvarDietaCompat() {
        // compat: salva os textos antigos na tabela Dieta (mantém compatibilidade)
        String cafe = inputCafe.getText().toString();
        String almoco = inputAlmoco.getText().toString();
        String jantar = inputJantar.getText().toString();

        com.example.projetoengenhariadesoftwareii.database.model.Dieta dieta = new com.example.projetoengenhariadesoftwareii.database.model.Dieta(diaSelecionado, cafe, almoco, jantar);
        dietaDao.salvarDieta(dieta);

        Toast.makeText(this, "Dieta salva com sucesso (compat)!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void excluirDietaCompat() {
        com.example.projetoengenhariadesoftwareii.database.model.Dieta dietaAtual = dietaDao.getDietaPorDia(diaSelecionado);
        if (dietaAtual != null) {
            dietaDao.excluirDieta(dietaAtual);
            // também deleta as refeições estruturadas daquele dia
            refeicaoDAO.deleteByDia(diaSelecionado);
            Toast.makeText(this, "Dieta excluída!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Nenhuma dieta para excluir!", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    // ---------- DIALOG: Gerenciar Refeições do dia ----------
    private void abrirDialogGerenciarRefeicoes(int dia) {
        Context ctx = this;
        List<Refeicao> refeicoes = refeicaoDAO.getByDia(dia);

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        ScrollView sv = new ScrollView(ctx);
        LinearLayout container = new LinearLayout(ctx);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(24,24,24,24);
        sv.addView(container);

        TextView header = new TextView(ctx);
        header.setText("Refeições - Dia " + dia);
        header.setTextSize(18f);
        header.setGravity(Gravity.CENTER);
        container.addView(header);

        // Se não houver refeições (não deveria, migrator cria), cria padrão 3
        if (refeicoes == null || refeicoes.isEmpty()) {
            refeicoes = criarRefeicoesPadraoParaDia(dia);
        }

        // Container dinâmico das refeições
        LinearLayout refeicoesList = new LinearLayout(ctx);
        refeicoesList.setOrientation(LinearLayout.VERTICAL);
        container.addView(refeicoesList);

        // Função local para refresh display
        Runnable[] refresh = new Runnable[1];
        refresh[0] = () -> {
            refeicoesList.removeAllViews();
            for (int i = 0; i < refeicoes.size(); i++) {
                Refeicao r = refeicoes.get(i);
                View card = criarCardRefeicaoView(ctx, r, refeicoes, i, refresh[0]);
                refeicoesList.addView(card);
            }
        };
        refresh[0].run();

        // Buttons: add/remove (limites)
        LinearLayout actionRow = new LinearLayout(ctx);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);
        actionRow.setGravity(Gravity.CENTER_VERTICAL);

        Button btnAdd = new Button(ctx);
        btnAdd.setText("Adicionar Refeição");
        btnAdd.setOnClickListener(v -> {
            if (refeicoes.size() >= 4) {
                Toast.makeText(ctx, "Máximo 4 refeições.", Toast.LENGTH_SHORT).show();
                return;
            }
            Refeicao nova = new Refeicao(dia, "Nova Refeição", null, null);
            long id = refeicaoDAO.insert(nova);
            // recarrega lista
            refeicoes.clear();
            refeicoes.addAll(refeicaoDAO.getByDia(dia));
            refresh[0].run();
        });

        Button btnRemove = new Button(ctx);
        btnRemove.setText("Remover Última");
        btnRemove.setOnClickListener(v -> {
            if (refeicoes.size() <= 3) {
                Toast.makeText(ctx, "Mínimo 3 refeições.", Toast.LENGTH_SHORT).show();
                return;
            }
            Refeicao ultimo = refeicoes.get(refeicoes.size()-1);
            refeicaoDAO.delete(ultimo);
            refeicoes.clear();
            refeicoes.addAll(refeicaoDAO.getByDia(dia));
            refresh[0].run();
        });

        actionRow.addView(btnAdd, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        actionRow.addView(btnRemove, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        container.addView(actionRow);

        builder.setView(sv);
        builder.setPositiveButton("Salvar e Fechar", (d,w) -> {
            // nada extra: as operações já persistiram via DAO
            Toast.makeText(ctx, "Refeições atualizadas.", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    // cria view card para cada refeição (nome, horario, editar ingredientes)
    private View criarCardRefeicaoView(Context ctx, Refeicao r, List<Refeicao> lista, int index, Runnable refresh) {
        LinearLayout card = new LinearLayout(ctx);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(16,16,16,16);
        card.setBackgroundResource(R.drawable.bg_card_refeicao);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,12,0,12);
        card.setLayoutParams(params);

        // linha: nome + editar nome
        LinearLayout row1 = new LinearLayout(ctx);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvNome = new TextView(ctx);
        tvNome.setText(r.getNome());
        tvNome.setTextSize(16f);
        LinearLayout.LayoutParams lpNome = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        row1.addView(tvNome, lpNome);

        Button btnEditarNome = new Button(ctx);
        btnEditarNome.setText("Editar nome");
        btnEditarNome.setOnClickListener(v -> {
            // simples dialog para editar nome
            EditText input = new EditText(ctx);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setText(r.getNome());
            new AlertDialog.Builder(ctx)
                    .setTitle("Nome da refeição")
                    .setView(input)
                    .setPositiveButton("OK", (d,w) -> {
                        r.setNome(input.getText().toString());
                        refeicaoDAO.update(r);
                        // atualizar tela
                        refresh.run();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
        row1.addView(btnEditarNome);
        card.addView(row1);

        // linha: horário + alterar horário (abre TimePicker)
        LinearLayout row2 = new LinearLayout(ctx);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvHorario = new TextView(ctx);
        tvHorario.setText("Horário: " + (r.getHorario() == null ? "--:--" : r.getHorario()));
        tvHorario.setTextSize(14f);
        LinearLayout.LayoutParams lpHorario = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        row2.addView(tvHorario, lpHorario);

        Button btnHorario = new Button(ctx);
        btnHorario.setText("Alterar horário");
        btnHorario.setOnClickListener(v -> {
            // abre TimePicker e aplica apenas a esta refeição por enquanto
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            TimePickerDialog tpd = new TimePickerDialog(ctx, (timePicker, h, m) -> {
                String hh = String.format("%02d:%02d", h, m);
                r.setHorario(hh);
                refeicaoDAO.update(r);
                // atualizar display
                refresh.run();
            }, hour, minute, true);
            tpd.show();
        });
        row2.addView(btnHorario);
        card.addView(row2);

        // linha: editar ingredientes
        LinearLayout row3 = new LinearLayout(ctx);
        row3.setOrientation(LinearLayout.HORIZONTAL);
        row3.setGravity(Gravity.CENTER_VERTICAL);

        Button btnEditarIngr = new Button(ctx);
        btnEditarIngr.setText("Editar ingredientes");
        btnEditarIngr.setOnClickListener(v -> {
            abrirDialogEditarIngredientes(r);
        });
        row3.addView(btnEditarIngr);

        // botão aplicar horário em vários dias (mini agenda)
        Button btnAplicarHorarioEmVarios = new Button(ctx);
        btnAplicarHorarioEmVarios.setText("Aplicar horário em dias...");
        btnAplicarHorarioEmVarios.setOnClickListener(v -> {
            abrirDialogAplicarHorarioEmVariosDias(r);
        });
        row3.addView(btnAplicarHorarioEmVarios);

        card.addView(row3);

        return card;
    }

    // dialog editar ingredientes de uma refeição: mostra itens atuais e permite adicionar usando IngredienteDAO
    private void abrirDialogEditarIngredientes(Refeicao refeicao) {
        Context ctx = this;
        AlertDialog.Builder b = new AlertDialog.Builder(ctx);
        ScrollView sv = new ScrollView(ctx);
        LinearLayout container = new LinearLayout(ctx);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(24,24,24,24);
        sv.addView(container);

        TextView title = new TextView(ctx);
        title.setText("Ingredientes - " + refeicao.getNome());
        title.setTextSize(16f);
        container.addView(title);

        // exibe lista atual de ingredientes
        List<RefeicaoIngrediente> lista = riDAO.getByRefeicao(refeicao.getId());
        LinearLayout listView = new LinearLayout(ctx);
        listView.setOrientation(LinearLayout.VERTICAL);
        container.addView(listView);

        Runnable[] refreshList = new Runnable[1];
        refreshList[0] = () -> {
            listView.removeAllViews();
            List<RefeicaoIngrediente> l = riDAO.getByRefeicao(refeicao.getId());
            for (RefeicaoIngrediente ri : l) {
                LinearLayout row = new LinearLayout(ctx);
                row.setOrientation(LinearLayout.HORIZONTAL);
                TextView tv = new TextView(ctx);
                String nomeDisplay = ri.getNomeCustom() != null ? ri.getNomeCustom() : String.valueOf(ri.getIngredienteId());
                // try to show ingredient name by id
                try {
                    com.example.projetoengenhariadesoftwareii.database.model.Ingrediente ing = db.ingredienteDAO().listarTodos().stream().filter(i -> i.getId() == ri.getIngredienteId()).findFirst().orElse(null);
                    if (ing != null) nomeDisplay = ing.getNome();
                } catch (Exception e) { /* ignore */ }
                tv.setText(nomeDisplay + " - " + ri.getQuantidade() + " " + (ri.getUnidade() != null ? ri.getUnidade() : ""));
                tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                row.addView(tv);

                Button btnDel = new Button(ctx);
                btnDel.setText("Remover");
                btnDel.setOnClickListener(v -> {
                    riDAO.delete(ri);
                    refreshList[0].run();
                });
                row.addView(btnDel);
                listView.addView(row);
            }
        };
        refreshList[0].run();

        // adicionar ingrediente (usa IngredienteDAO para escolher)
        Button btnAdd = new Button(ctx);
        btnAdd.setText("Adicionar ingrediente");
        btnAdd.setOnClickListener(v -> {
            // prepara lista de nomes
            List<com.example.projetoengenhariadesoftwareii.database.model.Ingrediente> todos = db.ingredienteDAO().listarTodos();
            if (todos.isEmpty()) {
                Toast.makeText(ctx, "Nenhum ingrediente cadastrado.", Toast.LENGTH_SHORT).show();
                return;
            }
            String[] nomes = new String[todos.size()];
            for (int i=0;i<todos.size();i++) nomes[i] = todos.get(i).getNome();

            final int[] selecionado = {-1};
            AlertDialog.Builder choose = new AlertDialog.Builder(ctx);
            choose.setTitle("Escolha ingrediente");
            choose.setSingleChoiceItems(nomes, -1, (dlg, which) -> selecionado[0] = which);
            LinearLayout chooseLayout = new LinearLayout(ctx);
            chooseLayout.setOrientation(LinearLayout.VERTICAL);
            EditText inputQtd = new EditText(ctx);
            inputQtd.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            inputQtd.setHint("Quantidade (ex: 100)");
            TextView tvUnidade = new TextView(ctx);
            // podemos exibir unidade ao escolher
            choose.setView(chooseLayout);
            chooseLayout.addView(inputQtd);
            chooseLayout.addView(tvUnidade);

            choose.setPositiveButton("Adicionar", (dlg, ww) -> {
                if (selecionado[0] < 0) { Toast.makeText(ctx, "Selecione um ingrediente.", Toast.LENGTH_SHORT).show(); return; }
                String qtdStr = inputQtd.getText().toString();
                double qtd = 0;
                try { qtd = Double.parseDouble(qtdStr); } catch (Exception ex) { qtd = 0; }
                com.example.projetoengenhariadesoftwareii.database.model.Ingrediente chosen = todos.get(selecionado[0]);
                RefeicaoIngrediente ri = new RefeicaoIngrediente();
                ri.setRefeicaoId(refeicao.getId());
                ri.setIngredienteId(chosen.getId());
                ri.setQuantidade(qtd);
                ri.setUnidade(chosen.getUnidade());
                ri.setNomeCustom(chosen.getNome());
                riDAO.insert(ri);
                refreshList[0].run();
            });
            choose.setNegativeButton("Cancelar", null);
            choose.show();
        });
        container.addView(btnAdd);

        b.setView(sv);
        b.setPositiveButton("OK", (d,w) -> { /* refresh depois */ });
        b.setNegativeButton("Cancelar", null);
        b.show();
    }

    // dialog aplica horário em vários dias (mini agenda reusa DiaAdapter)
    private void abrirDialogAplicarHorarioEmVariosDias(Refeicao refeicao) {
        // Implementação simplificada:
        // 1) abre mini agenda (reaproveita adapter usado em outras activities)
        // 2) ao selecionar dias e confirmar, atualiza todas as Refeicao (mesma nome) desses dias:
        List<DiaItem> dias = gerarDiasDoMes(); // você já tem método similar em outras activities (copiar)
        // Para ser prático aqui: vamos abrir dialog simples pedindo dia inicial e final (método rápido)
        final EditText inputDias = new EditText(this);
        inputDias.setHint("Ex: 5,6,7 ou 10-12");
        new AlertDialog.Builder(this)
                .setTitle("Aplicar horário em dias (ex: 5,6,7 ou 10-12)")
                .setView(inputDias)
                .setPositiveButton("Aplicar", (d,w) -> {
                    String s = inputDias.getText().toString().trim();
                    // parse simples
                    try {
                        if (s.contains("-")) {
                            String[] p = s.split("-");
                            int a = Integer.parseInt(p[0].trim());
                            int b = Integer.parseInt(p[1].trim());
                            for (int dia = a; dia <= b; dia++) {
                                aplicarHorarioParaDiaPorNome(refeicao.getNome(), dia, refeicao.getHorario());
                            }
                        } else if (s.contains(",")) {
                            String[] p = s.split(",");
                            for (String x: p) {
                                int dia = Integer.parseInt(x.trim());
                                aplicarHorarioParaDiaPorNome(refeicao.getNome(), dia, refeicao.getHorario());
                            }
                        } else {
                            int dia = Integer.parseInt(s.trim());
                            aplicarHorarioParaDiaPorNome(refeicao.getNome(), dia, refeicao.getHorario());
                        }
                        Toast.makeText(this, "Horário aplicado.", Toast.LENGTH_SHORT).show();
                    } catch (Exception ex) {
                        Toast.makeText(this, "Formato inválido.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void abrirDialogHorario(String refeicao) {

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_horario_refeicao, null);

        TimePicker picker = view.findViewById(R.id.timePicker);
        LinearLayout layoutDias = view.findViewById(R.id.layoutDias); // mini agenda igual CriarDietaActivity

        List<Integer> diasSelecionados = new ArrayList<>();

        // ← CRIE 31 BOTÕES dentro do layout dinamicamente (igual CriarDietaActivity)
        for (int d = 1; d <= 31; d++) {
            TextView tv = new TextView(this);
            tv.setText(String.valueOf(d));
            tv.setPadding(10, 10, 10, 10);
            tv.setBackgroundResource(R.drawable.bg_botao_dia);
            int finalD = d;

            tv.setOnClickListener(v -> {
                if (diasSelecionados.contains(finalD)) {
                    diasSelecionados.remove((Integer) finalD);
                    tv.setBackgroundResource(R.drawable.bg_botao_dia);
                } else {
                    diasSelecionados.add(finalD);
                    tv.setBackgroundResource(R.drawable.bg_botao_dia_selecionado);
                }
            });

            layoutDias.addView(tv);
        }

        new AlertDialog.Builder(this)
                .setTitle("Alterar horário - " + refeicao.toUpperCase())
                .setView(view)
                .setPositiveButton("Salvar", (d, w) -> {

                    if (diasSelecionados.isEmpty()) {
                        Toast.makeText(this, "Selecione ao menos 1 dia!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int hora = picker.getHour();
                    int minuto = picker.getMinute();
                    String horario = String.format("%02d:%02d", hora, minuto);

                    HorarioRefeicaoDAO horarioDAO = AppDatabase.getInstance(this).horarioRefeicaoDAO();

                    for (int dia : diasSelecionados) {
                        horarioDAO.salvar(new HorarioRefeicao(dia, refeicao, horario));
                    }

                    Toast.makeText(this, "Horários atualizados!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void aplicarHorarioParaDiaPorNome(String nomeRefeicao, int dia, String horario) {
        List<Refeicao> list = refeicaoDAO.getByDia(dia);
        for (Refeicao r: list) {
            if (r.getNome().equalsIgnoreCase(nomeRefeicao)) {
                r.setHorario(horario);
                refeicaoDAO.update(r);
                return;
            }
        }
        // se não encontrou, cria uma nova refeição com esse nome e horário
        Refeicao nova = new Refeicao(dia, nomeRefeicao, horario, null);
        refeicaoDAO.insert(nova);
    }

    // cria 3 refeições padrão e persiste (somente se não houver)
    private List<Refeicao> criarRefeicoesPadraoParaDia(int dia) {
        Refeicao r1 = new Refeicao(dia, "Café da Manhã", null, null);
        Refeicao r2 = new Refeicao(dia, "Almoço", null, null);
        Refeicao r3 = new Refeicao(dia, "Jantar", null, null);
        refeicaoDAO.insert(r1);
        refeicaoDAO.insert(r2);
        refeicaoDAO.insert(r3);
        return refeicaoDAO.getByDia(dia);
    }

    // método auxiliar (puxe/cole sua implementação real geradora de DiaItem em outras activities)
    private List<DiaItem> gerarDiasDoMes() {
        Calendar cal = Calendar.getInstance();
        int total = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        List<DiaItem> dias = new java.util.ArrayList<>();
        String[] nomes = {"Dom","Seg","Ter","Qua","Qui","Sex","Sáb"};
        for (int i=1;i<=total;i++) {
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.set(java.util.Calendar.DAY_OF_MONTH, i);
            dias.add(new DiaItem(i, nomes[c.get(java.util.Calendar.DAY_OF_WEEK) - 1], i == cal.get(java.util.Calendar.DAY_OF_MONTH)));
        }
        return dias;
    }

    // menu e navegação originais
    private void configurarMenu() {
        buttonLogo.setOnClickListener(v -> {
            Intent intent = new Intent(EditarDietaActivity.this, todayActivity.class);
            startActivity(intent);
        });

        buttonMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(EditarDietaActivity.this, buttonMenu);
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


//package com.example.projetoengenhariadesoftwareii;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.EditText;
//import android.widget.ImageButton;
//import android.widget.PopupMenu;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.projetoengenhariadesoftwareii.database.AppDatabase;
//import com.example.projetoengenhariadesoftwareii.database.DAO.DietaDAO;
//import com.example.projetoengenhariadesoftwareii.database.model.Dieta;
//
//public class EditarDietaActivity extends AppCompatActivity {
//
//    ImageButton buttonMenu, buttonLogo;
//    private EditText inputCafe, inputAlmoco, inputJantar;
//    private TextView textoDia;
//    private int diaSelecionado;
//    private DietaDAO dietaDao;
//    private Dieta dietaAtual;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_editar_dieta);
//
//        buttonMenu = findViewById(R.id.buttonMenu);
//        buttonLogo = findViewById(R.id.buttonlogo);
//
//        // --- inicialização ---
//        inputCafe = findViewById(R.id.inputCafeManha);
//        inputAlmoco = findViewById(R.id.inputAlmoco);
//        inputJantar = findViewById(R.id.inputJantar);
//        textoDia = findViewById(R.id.textoDiaSelecionado);
//
//        dietaDao = AppDatabase.getInstance(this).dietaDAO();
//
//        // --- recupera o dia vindo da tela anterior ---
//        diaSelecionado = getIntent().getIntExtra("diaSelecionado", -1);
//        textoDia.setText("Dia selecionado: " + diaSelecionado);
//
//        // --- carrega dieta salva ---
//        dietaAtual = dietaDao.getDietaPorDia(diaSelecionado);
//        if (dietaAtual != null) {
//            inputCafe.setText(dietaAtual.getCafeManha());
//            inputAlmoco.setText(dietaAtual.getAlmoco());
//            inputJantar.setText(dietaAtual.getJantar());
//        }
//
//        // --- botão salvar ---
//        findViewById(R.id.btnSalvar).setOnClickListener(v -> salvarDieta());
//        findViewById(R.id.btnExcluir).setOnClickListener(v -> excluirDieta());
//        findViewById(R.id.btnInicio).setOnClickListener(v -> finish());
//        findViewById(R.id.btnCriarDieta).setOnClickListener(v -> {
//            Intent intent = new Intent(EditarDietaActivity.this, CriarDietaActivity.class);
//            startActivityForResult(intent, 1);
//        });
//        findViewById(R.id.btnDietaPronta).setOnClickListener(v -> {
//            Intent intent = new Intent(EditarDietaActivity.this, DietasPreProntasActivity.class);
//            startActivity(intent);
//        });
//
//        configurarMenu();
//    }
//
//    private void configurarMenu() {
//        buttonLogo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(EditarDietaActivity.this, todayActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        buttonMenu.setOnClickListener(v -> {
//            PopupMenu popupMenu = new PopupMenu(EditarDietaActivity.this, buttonMenu);
//            popupMenu.getMenuInflater().inflate(R.menu.activity_menu, popupMenu.getMenu());
//            popupMenu.setOnMenuItemClickListener(item -> {
//                int id = item.getItemId();
//                if (id == R.id.opcao1) {
//                    startActivity(new Intent(this, todayActivity.class));
//                    return true;
//                } else if (id == R.id.opcao2) {
//                    startActivity(new Intent(this, ComprasMesActivity.class));
//                    return true;
//                } else if (id == R.id.opcao3) {
//                    startActivity(new Intent(this, RelatoriosActivity.class));
//                    return true;
//                } else if (id == R.id.opcao4) {
//                    startActivity(new Intent(this, SobreActivity.class));
//                    return true;
//                } else if (id == R.id.opcao5) {
//                    startActivity(new Intent(this, LoginActivity.class));
//                    return true;
//                }
//                return false;
//            });
//            popupMenu.show();
//        });
//    }
//
//    private void salvarDieta() {
//        String cafe = inputCafe.getText().toString();
//        String almoco = inputAlmoco.getText().toString();
//        String jantar = inputJantar.getText().toString();
//
//        Dieta dieta = new Dieta(diaSelecionado, cafe, almoco, jantar);
//        dietaDao.salvarDieta(dieta);
//
//        Toast.makeText(this, "Dieta salva com sucesso!", Toast.LENGTH_SHORT).show();
//        finish();
//    }
//
//    private void excluirDieta() {
//        if (dietaAtual != null) {
//            dietaDao.excluirDieta(dietaAtual);
//            Toast.makeText(this, "Dieta excluída!", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(this, "Nenhuma dieta para excluir!", Toast.LENGTH_SHORT).show();
//        }
//        finish();
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 1 && resultCode == RESULT_OK) {
//            // Atualiza a tela recarregando a dieta do banco
//            dietaAtual = dietaDao.getDietaPorDia(diaSelecionado);
//            if (dietaAtual != null) {
//                inputCafe.setText(dietaAtual.getCafeManha());
//                inputAlmoco.setText(dietaAtual.getAlmoco());
//                inputJantar.setText(dietaAtual.getJantar());
//            }
//        }
//    }
//
//}