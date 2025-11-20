package com.example.projetoengenhariadesoftwareii;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetoengenhariadesoftwareii.RefeicaoAdapter;
import com.example.projetoengenhariadesoftwareii.database.AppDatabase;
import com.example.projetoengenhariadesoftwareii.database.DAO.DietaDAO;
import com.example.projetoengenhariadesoftwareii.database.DAO.RefeicaoDAO;
import com.example.projetoengenhariadesoftwareii.database.model.Dieta;
import com.example.projetoengenhariadesoftwareii.database.model.Refeicao;

import java.util.ArrayList;
import java.util.List;

public class EditarDietaActivity extends AppCompatActivity {

    ImageButton buttonMenu, buttonLogo;
    private TextView textoDia;
    private int diaSelecionado;
    private DietaDAO dietaDao;
    private RefeicaoDAO refeicaoDao;

    private RecyclerView recycler;
    private RefeicaoAdapter adapter;
    private List<Refeicao> lista = new ArrayList<>();

    // snapshot inicial (originais do DB), usado para identificar remoções/novos
    private List<Refeicao> originais = new ArrayList<>();

    private Button btnAdicionar, btnSalvar, btnExcluirDieta;
    private boolean modificou = false; // flag: se houve alguma alteração em memória

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_dieta);

        buttonMenu = findViewById(R.id.buttonMenu);
        buttonLogo = findViewById(R.id.buttonlogo);
        textoDia = findViewById(R.id.textoDiaSelecionado);
        recycler = findViewById(R.id.recyclerRefeicoes);
        btnAdicionar = findViewById(R.id.btnAdicionarRefeicao);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnExcluirDieta = findViewById(R.id.btnExcluirDieta);

        dietaDao = AppDatabase.getInstance(this).dietaDAO();
        refeicaoDao = AppDatabase.getInstance(this).RefeicaoDAO();

        diaSelecionado = getIntent().getIntExtra("diaSelecionado", -1);
        textoDia.setText("Dia selecionado: " + diaSelecionado);

        carregarRefeicoesDoDB();

        adapter = new RefeicaoAdapter(lista, () -> {
            modificou = true;
            // opcional: alterar visual do botão salvar para indicar que há mudanças
            btnSalvar.setEnabled(true);
        });

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        // Adicionar — apenas em memória
        btnAdicionar.setOnClickListener(v -> {
            if (lista.size() >= 5) {
                Toast.makeText(this, "Máximo de 5 refeições permitido", Toast.LENGTH_SHORT).show();
                return;
            }
            Refeicao nova = new Refeicao(diaSelecionado, "Refeição " + (lista.size() + 1), "08:00", "");
            lista.add(nova);
            adapter.notifyItemInserted(lista.size() - 1);
            modificou = true;
            btnSalvar.setEnabled(true);
        });

        // Salvar — persiste todas as alterações (inserir/atualizar/excluir)
        btnSalvar.setOnClickListener(v -> {
            persistirAlteracoes();
            modificou = false;
            btnSalvar.setEnabled(false);
            Toast.makeText(this, "Alterações salvas", Toast.LENGTH_SHORT).show();
            // você pode fechar ou ficar na tela; eu mantenho na tela
        });


        // Confirmar exclusão de DIETA inteira
        btnExcluirDieta.setOnClickListener(v -> confirmarExcluirDieta());

        findViewById(R.id.btnInicio).setOnClickListener(v -> finish());
        findViewById(R.id.btnCriarDieta).setOnClickListener(v -> startActivity(new Intent(this, CriarDietaActivity.class)));
        findViewById(R.id.btnDietaPronta).setOnClickListener(v -> startActivity(new Intent(this, DietasPreProntasActivity.class)));

        configurarMenu();

        // inicialmente, botão salvar desabilitado (até modificar algo)
        btnSalvar.setEnabled(false);
    }

    private void carregarRefeicoesDoDB() {
        lista.clear();
        originais.clear();

        List<Refeicao> doDb = refeicaoDao.getRefeicoesPorDia(diaSelecionado);

        if (doDb == null || doDb.isEmpty()) {
            Dieta d = dietaDao.getDietaPorDia(diaSelecionado);
            if (d != null) {
                // Criar refeições a partir da DIETA (vem da DietaPréPronta)
                refeicaoDao.inserirRefeicao(new Refeicao(diaSelecionado, "Café da Manhã", "08:00", d.getCafeManha()));
                refeicaoDao.inserirRefeicao(new Refeicao(diaSelecionado, "Almoço", "12:00", d.getAlmoco()));
                refeicaoDao.inserirRefeicao(new Refeicao(diaSelecionado, "Jantar", "19:00", d.getJantar()));
                doDb = refeicaoDao.getRefeicoesPorDia(diaSelecionado);
            }
            else {
                // Só cria padrão se realmente não existe NADA
                Refeicao r1 = new Refeicao(diaSelecionado, "Café da Manhã", "08:00", "");
                Refeicao r2 = new Refeicao(diaSelecionado, "Almoço", "12:00", "");
                Refeicao r3 = new Refeicao(diaSelecionado, "Jantar", "19:00", "");
                refeicaoDao.inserirRefeicao(r1);
                refeicaoDao.inserirRefeicao(r2);
                refeicaoDao.inserirRefeicao(r3);
                doDb = refeicaoDao.getRefeicoesPorDia(diaSelecionado);
            }
        }

        for (Refeicao r : doDb) {
            lista.add(r);
            Refeicao copy = new Refeicao(r.getDia(), r.getNome(), r.getHorario(), r.getDescricao());
            copy.setId(r.getId());
            originais.add(copy);
        }
    }

    public void recarregarDepoisDoDialog() {
        carregarRefeicoesDoDB();
        adapter.notifyDataSetChanged();
    }

    private void persistirAlteracoes() {
        // 1) deletar removidas
        List<Integer> idsAtuais = new ArrayList<>();
        for (Refeicao r : lista) if (r.getId() != 0) idsAtuais.add(r.getId());
        for (Refeicao old : originais) {
            if (!idsAtuais.contains(old.getId())) {
                refeicaoDao.excluirRefeicao(old);
            }
        }

        // 2) ❗NÃO USAR mais inserirRefeicao diretamente → usar UPSERT
        for (Refeicao r : lista) {
            upsertRefeicao(r);   // <= substitui TUDO em inserir/atualizar
        }

        // 3) dietas sincronizadas corretamente
        sincronizarDietaCorreta();

        carregarRefeicoesDoDB();
        adapter.notifyDataSetChanged();
    }

    // IMPORTANTE: evita duplicação
    private void upsertRefeicao(Refeicao r) {
        Refeicao existente = refeicaoDao.getRefeicaoPorDiaENome(r.getDia(), r.getNome());
        if (existente != null) {
            // Atualiza os campos
            existente.setHorario(r.getHorario());
            existente.setDescricao(r.getDescricao());
            refeicaoDao.atualizarRefeicao(existente);
            r.setId(existente.getId()); // mantém referência na lista
        } else {
            long newId = refeicaoDao.inserirRefeicao(r);
            if (newId > 0) r.setId((int) newId);
        }
    }

    private void sincronizarDietaCorreta() {
        String cafe = "";
        String almoco = "";
        String jantar = "";

        List<Refeicao> todas = refeicaoDao.getRefeicoesPorDia(diaSelecionado);

        for (Refeicao r : todas) {
            String nomeLower = r.getNome().toLowerCase();

            if (nomeLower.contains("café") || nomeLower.contains("cafe")) {
                cafe = append(cafe, r.getDescricao());
            } else if (nomeLower.contains("almoço") || nomeLower.contains("almoco")) {
                almoco = append(almoco, r.getDescricao());
            } else if (nomeLower.contains("jantar")) {
                jantar = append(jantar, r.getDescricao());
            }
        }
        // salva dietas com ingredientes reais
        Dieta d = new Dieta(diaSelecionado, cafe, almoco, jantar);
        dietaDao.salvarDieta(d);
    }

    private String append(String base, String entry) {
        if (base == null || base.isEmpty()) return entry;
        return base + "; " + entry;
    }

    private void confirmarExcluirDieta() {
        new AlertDialog.Builder(this)
                .setTitle("Excluir dieta")
                .setMessage("Tem certeza que deseja excluir a dieta deste dia? Isso removerá todas as refeições do dia.")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    Dieta d = dietaDao.getDietaPorDia(diaSelecionado);
                    if (d != null) dietaDao.excluirDieta(d);
                    refeicaoDao.excluirPorDia(diaSelecionado);
                    Toast.makeText(this, "Dieta excluída", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void configurarMenu() {
        buttonLogo.setOnClickListener(v -> startActivity(new Intent(EditarDietaActivity.this, todayActivity.class)));
        buttonMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(EditarDietaActivity.this, buttonMenu);
            popupMenu.getMenuInflater().inflate(R.menu.activity_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.opcao1) { startActivity(new Intent(this, todayActivity.class)); return true; }
                else if (id == R.id.opcao2) { startActivity(new Intent(this, ComprasMesActivity.class)); return true; }
                else if (id == R.id.opcao3) { startActivity(new Intent(this, RelatoriosActivity.class)); return true; }
                else if (id == R.id.opcao4) { startActivity(new Intent(this, SobreActivity.class)); return true; }
                else if (id == R.id.opcao5) { startActivity(new Intent(this, LoginActivity.class)); return true; }
                return false;
            });
            popupMenu.show();
        });
    }

    @Override
    public void onBackPressed() {
        if (modificou) {
            new AlertDialog.Builder(this)
                    .setTitle("Alterações não salvas")
                    .setMessage("Existem alterações não salvas. Deseja sair sem salvar?")
                    .setPositiveButton("Sair sem salvar", (d, w) -> super.onBackPressed())
                    .setNegativeButton("Cancelar", null)
                    .setNeutralButton("Salvar e sair", (d, w) -> {
                        persistirAlteracoes();
                        super.onBackPressed();
                    })
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // não persiste automaticamente; apenas recarrega do DB se quiseres descartar alterações
        // carregarRefeicoesDoDB(); // NÃO recarregar automaticamente para evitar sobrescrever in-memory
    }
}

//package com.example.projetoengenhariadesoftwareii;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ImageButton;
//import android.widget.PopupMenu;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.projetoengenhariadesoftwareii.adapters.RefeicaoAdapter;
//import com.example.projetoengenhariadesoftwareii.database.AppDatabase;
//import com.example.projetoengenhariadesoftwareii.database.DAO.DietaDAO;
//import com.example.projetoengenhariadesoftwareii.database.DAO.RefeicaoDAO;
//import com.example.projetoengenhariadesoftwareii.database.model.Dieta;
//import com.example.projetoengenhariadesoftwareii.database.model.Refeicao;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class EditarDietaActivity extends AppCompatActivity {
//
//    ImageButton buttonMenu, buttonLogo;
//    private TextView textoDia;
//    private int diaSelecionado;
//    private DietaDAO dietaDao;
//    private RefeicaoDAO refeicaoDao;
//
//    private RecyclerView recycler;
//    private RefeicaoAdapter adapter;
//    private List<Refeicao> lista = new ArrayList<>();
//
//    private Button btnAdicionar, btnSalvar;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_editar_dieta);
//
//        buttonMenu = findViewById(R.id.buttonMenu);
//        buttonLogo = findViewById(R.id.buttonlogo);
//        textoDia = findViewById(R.id.textoDiaSelecionado);
//        recycler = findViewById(R.id.recyclerRefeicoes);
//        btnAdicionar = findViewById(R.id.btnAdicionarRefeicao);
//        btnSalvar = findViewById(R.id.btnSalvar);
//
//        dietaDao = AppDatabase.getInstance(this).dietaDAO();
//        refeicaoDao = AppDatabase.getInstance(this).RefeicaoDAO();
//
//        diaSelecionado = getIntent().getIntExtra("diaSelecionado", -1);
//        textoDia.setText("Dia selecionado: " + diaSelecionado);
//
//        carregarRefeicoes();
//
//        adapter = new RefeicaoAdapter(lista, refeicaoDao, () -> {
//            ajustarBotoesExcluir();
//            sincronizarDieta();
//        });
//
//        recycler.setLayoutManager(new LinearLayoutManager(this));
//        recycler.setAdapter(adapter);
//
//        btnAdicionar.setOnClickListener(v -> {
//            int count = refeicaoDao.contarPorDia(diaSelecionado);
//            if (count >= 5) {
//                Toast.makeText(this, "Máximo de 5 refeições permitido", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            // adiciona nova refeição com nome padrão e horário padrão
//            Refeicao nova = new Refeicao(diaSelecionado, "Refeição " + (count+1), "08:00", "");
//            refeicaoDao.inserirRefeicao(nova);
//            carregarRefeicoes();
//            adapter.notifyDataSetChanged();
//            ajustarBotoesExcluir();
//            sincronizarDieta();
//        });
//
//        btnSalvar.setOnClickListener(v -> {
//            sincronizarDieta();
//            Toast.makeText(this, "Dieta salva", Toast.LENGTH_SHORT).show();
//            finish();
//        });
//
//        findViewById(R.id.btnInicio).setOnClickListener(v -> finish());
//        findViewById(R.id.btnCriarDieta).setOnClickListener(v -> {
//            startActivity(new Intent(this, CriarDietaActivity.class));
//        });
//        findViewById(R.id.btnDietaPronta).setOnClickListener(v -> {
//            startActivity(new Intent(this, DietasPreProntasActivity.class));
//        });
//
//        configurarMenu();
//    }
//
//    private void carregarRefeicoes() {
//        lista.clear();
//        List<Refeicao> db = refeicaoDao.getRefeicoesPorDia(diaSelecionado);
//        if (db == null || db.isEmpty()) {
//            // se não existir ainda, cria 3 refeições padrão (para compatibilidade)
//            Refeicao r1 = new Refeicao(diaSelecionado, "Café da Manhã", "08:00", "");
//            Refeicao r2 = new Refeicao(diaSelecionado, "Almoço", "12:00", "");
//            Refeicao r3 = new Refeicao(diaSelecionado, "Jantar", "19:00", "");
//            refeicaoDao.inserirRefeicao(r1);
//            refeicaoDao.inserirRefeicao(r2);
//            refeicaoDao.inserirRefeicao(r3);
//            db = refeicaoDao.getRefeicoesPorDia(diaSelecionado);
//        }
//        lista.addAll(db);
//    }
//
//    // ajusta visibilidade/exclusão dependendo da regra mínimo=3
//    private void ajustarBotoesExcluir() {
//        int count = lista.size();
//        // se estiver usando adapter que exclui diretamente, prevenimos exclusão por UI: quando count==3, desabilitar botões excluir
//        // aqui, como o botão de excluir já remove via DAO, vamos re-carregar para garantir que não permita excluir se count==3
//        if (count <= 3) {
//            // percorre e desabilita o botão excluir dos itens (simples: recarregar adapter e impedir excluir)
//            // implementamos prevenção no próprio listener da Activity (abaixo)
//        }
//    }
//
//    // Sincroniza a tabela `dietas` para manter compatibilidade com todayActivity
//    private void sincronizarDieta() {
//        // monta strings agrupando por nomes tradicionais (Café, Almoço, Jantar); para itens extras, concatena em "jantar" ou "outros"
//        String cafe = "";
//        String almoco = "";
//        String jantar = "";
//        List<Refeicao> todas = refeicaoDao.getRefeicoesPorDia(diaSelecionado);
//        for (Refeicao r : todas) {
//            String tipo = r.getNome().toLowerCase();
//            String entry = r.getNome() + " (" + r.getHorario() + ")";
//            if (tipo.contains("café") || tipo.contains("cafe") || tipo.contains("manhã") || tipo.contains("manha")) {
//                cafe = append(cafe, entry);
//            } else if (tipo.contains("almoço") || tipo.contains("almoco")) {
//                almoco = append(almoco, entry);
//            } else if (tipo.contains("jantar")) {
//                jantar = append(jantar, entry);
//            } else {
//                // tenta encaixar: se jantar vazio, coloca em jantar, senão em almoco, senão cafe, senão em "jantar"
//                if (jantar.isEmpty()) jantar = append(jantar, entry);
//                else if (almoco.isEmpty()) almoco = append(almoco, entry);
//                else if (cafe.isEmpty()) cafe = append(cafe, entry);
//                else jantar = append(jantar, entry);
//            }
//        }
//        Dieta d = new Dieta(diaSelecionado, cafe, almoco, jantar);
//        dietaDao.salvarDieta(d);
//    }
//
//    private String append(String base, String entry) {
//        if (base == null || base.isEmpty()) return entry;
//        return base + "; " + entry;
//    }
//
//    private void configurarMenu() {
//        buttonLogo.setOnClickListener(v -> {
//            startActivity(new Intent(EditarDietaActivity.this, todayActivity.class));
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
//    @Override
//    protected void onResume() {
//        super.onResume();
//        carregarRefeicoes();
//        adapter.notifyDataSetChanged();
//    }
//}



//package com.example.projetoengenhariadesoftwareii;
//
//import android.app.TimePickerDialog;
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ImageButton;
//import android.widget.PopupMenu;
//import android.widget.TextView;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.projetoengenhariadesoftwareii.adapters.RefeicaoAdapter;
//import com.example.projetoengenhariadesoftwareii.database.AppDatabase;
//import com.example.projetoengenhariadesoftwareii.database.DAO.DietaDAO;
//import com.example.projetoengenhariadesoftwareii.database.DAO.RefeicaoDAO;
//import com.example.projetoengenhariadesoftwareii.database.model.Dieta;
//import com.example.projetoengenhariadesoftwareii.database.model.Refeicao;
//
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.List;
//
//public class EditarDietaActivity extends AppCompatActivity {
//
//    ImageButton buttonMenu, buttonLogo;
//    private TextView textoDia;
//    private int diaSelecionado;
//    private DietaDAO dietaDao;
//    private RefeicaoDAO refeicaoDao;
//    private Dieta dietaAtual;
//
//    private RecyclerView recyclerRefeicoes;
//    private RefeicaoAdapter adapter;
//    private List<Refeicao> refeicoesList = new ArrayList<>();
//
//    private Button btnAdicionarRefeicao;
//    private Button btnSalvar;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_editar_dieta);
//
//        buttonMenu = findViewById(R.id.buttonMenu);
//        buttonLogo = findViewById(R.id.buttonlogo);
//
//        textoDia = findViewById(R.id.textoDiaSelecionado);
//        recyclerRefeicoes = findViewById(R.id.recyclerRefeicoes);
//        btnAdicionarRefeicao = findViewById(R.id.btnAdicionarRefeicao);
//        btnSalvar = findViewById(R.id.btnSalvar);
//
//        dietaDao = AppDatabase.getInstance(this).dietaDAO();
//        refeicaoDao = AppDatabase.getInstance(this).RefeicaoDAO();
//
//        // --- recupera o dia vindo da tela anterior ---
//        diaSelecionado = getIntent().getIntExtra("diaSelecionado", -1);
//        textoDia.setText("Dia selecionado: " + diaSelecionado);
//
//        // --- carrega dieta salva (tabela Dieta) ---
//        dietaAtual = dietaDao.getDietaPorDia(diaSelecionado);
//
//        // --- carrega refeições do DB ---
//        loadRefeicoes();
//
//        // --- setup RecyclerView ---
//        adapter = new RefeicaoAdapter(this, refeicoesList, refeicaoDao, new RefeicaoAdapter.OnRefeicaoChangedListener() {
//            @Override
//            public void onRefeicaoDeleted(Refeicao refeicao) {
//                // opcional: atualizar UI quando algo for excluído
//            }
//
//            @Override
//            public void onRefeicaoUpdated(Refeicao refeicao) {
//                // opcional: ações após atualização
//            }
//        });
//        recyclerRefeicoes.setLayoutManager(new LinearLayoutManager(this));
//        recyclerRefeicoes.setAdapter(adapter);
//
//        // --- botões ---
//        btnAdicionarRefeicao.setOnClickListener(v -> mostrarDialogAdicionar());
//        btnSalvar.setOnClickListener(v -> salvarDieta());
//
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
//    private void loadRefeicoes() {
//        List<Refeicao> l = refeicaoDao.getRefeicoesPorDia(diaSelecionado);
//        if (l != null) {
//            refeicoesList.clear();
//            refeicoesList.addAll(l);
//        }
//    }
//
//    private void mostrarDialogAdicionar() {
//        // diálogos: escolher tipo, nome e horário
//        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_refeicao, null);
//        EditText inputNome = dialogView.findViewById(R.id.inputNomeRefeicao);
//        EditText inputTipo = dialogView.findViewById(R.id.inputTipoRefeicao);
//        EditText inputHorario = dialogView.findViewById(R.id.inputHorarioRefeicao);
//
//        inputHorario.setOnClickListener(v -> {
//            // abre TimePicker
//            Calendar now = Calendar.getInstance();
//            int h = now.get(Calendar.HOUR_OF_DAY);
//            int m = now.get(Calendar.MINUTE);
//            TimePickerDialog tpd = new TimePickerDialog(this, (view, hourOfDay, minute) ->
//                    inputHorario.setText(String.format("%02d:%02d", hourOfDay, minute)), h, m, true);
//            tpd.show();
//        });
//
//        new AlertDialog.Builder(this)
//                .setTitle("Adicionar refeição")
//                .setView(dialogView)
//                .setPositiveButton("Adicionar", (d, w) -> {
//                    String nome = inputNome.getText().toString().trim();
//                    String tipo = inputTipo.getText().toString().trim();
//                    String horario = inputHorario.getText().toString().trim();
//
//                    if (nome.isEmpty()) nome = "Refeição";
//                    if (tipo.isEmpty()) tipo = "Outros";
//                    if (horario.isEmpty()) horario = "08:00";
//
//                    Refeicao nova = new Refeicao(diaSelecionado, tipo, nome, horario);
//                    long id = refeicaoDao.inserirRefeicao(nova);
//                    // Room não retorna id para @Insert void; se usar long é necessário ajustar @Insert retorno
//                    // To keep simple, recarregamos lista
//                    loadRefeicoes();
//                    adapter.atualizarLista(refeicoesList);
//                })
//                .setNegativeButton("Cancelar", null)
//                .show();
//    }
//
//    private void salvarDieta() {
//        // Agrupa refeições por tipo e cria strings para cafe/almoco/jantar conforme tipo
//        String cafe = "";
//        String almoco = "";
//        String jantar = "";
//        String outros = "";
//
//        List<Refeicao> todas = refeicaoDao.getRefeicoesPorDia(diaSelecionado);
//        for (Refeicao r : todas) {
//            String tipo = r.getTipo().toLowerCase();
//            String entrada = r.getNome() + " (" + r.getHorario() + ")";
//            if (tipo.contains("cafe") || tipo.contains("manha") || tipo.contains("café")) {
//                cafe = appendWithComma(cafe, entrada);
//            } else if (tipo.contains("almoco") || tipo.contains("almoço")) {
//                almoco = appendWithComma(almoco, entrada);
//            } else if (tipo.contains("jantar")) {
//                jantar = appendWithComma(jantar, entrada);
//            } else {
//                outros = appendWithComma(outros, entrada);
//            }
//        }
//
//        Dieta nova = new Dieta(diaSelecionado, cafe, almoco, jantar);
//        dietaDao.salvarDieta(nova);
//
//        Toast.makeText(this, "Dieta salva com sucesso!", Toast.LENGTH_SHORT).show();
//        finish();
//    }
//
//    private String appendWithComma(String base, String entry) {
//        if (base == null || base.isEmpty()) return entry;
//        return base + "; " + entry;
//    }
//
//    private void configurarMenu() {
//        buttonLogo.setOnClickListener(v -> {
//            Intent intent = new Intent(EditarDietaActivity.this, todayActivity.class);
//            startActivity(intent);
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
//}


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



//package com.example.projetoengenhariadesoftwareii;
//
//import android.content.Intent;
//import android.os.Bundle;
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
//            startActivity(intent);
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
//}