package com.example.projetoengenhariadesoftwareii;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.example.projetoengenhariadesoftwareii.database.model.UsuarioModel;

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
    private UsuarioModel usuarioLogado; // ← receber do login

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_dieta);

        // RECEBENDO O USUÁRIO AQUI ↓↓↓
        usuarioLogado = getIntent().getParcelableExtra("usuarioLogado");
        if (usuarioLogado == null) {
            Log.e("ERRO_USER", "Usuário NÃO recebido!");
        } else {
            Log.d("USER_OK", "Recebido: " + usuarioLogado.getNome());
        }

        buttonMenu = findViewById(R.id.buttonMenu);
        buttonLogo = findViewById(R.id.buttonlogo);
        textoDia = findViewById(R.id.textoDiaSelecionado);
        recycler = findViewById(R.id.recyclerRefeicoes);
        btnAdicionar = findViewById(R.id.btnAdicionarRefeicao);
        btnSalvar = findViewById(R.id.btnSalvar);

        dietaDao = AppDatabase.getInstance(this).dietaDAO();
        refeicaoDao = AppDatabase.getInstance(this).RefeicaoDAO();

        diaSelecionado = getIntent().getIntExtra("diaSelecionado", -1);
        textoDia.setText("Dia selecionado: " + diaSelecionado);

        carregarRefeicoesDoDB();

        adapter = new RefeicaoAdapter(lista, usuarioLogado, () -> {
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
            int idUsuario = (int) usuarioLogado.getId();
            Refeicao nova = new Refeicao(diaSelecionado, idUsuario,"Refeição " + (lista.size() + 1), "08:00", "");
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


        findViewById(R.id.btnInicio).setOnClickListener(v -> finish());
        findViewById(R.id.btnCriarDieta).setOnClickListener(v -> startActivity(new Intent(this, CriarDietaActivity.class)));
        findViewById(R.id.btnDietaPronta).setOnClickListener(v -> {
            Intent intent = new Intent(EditarDietaActivity.this, DietasPreProntasActivity.class);
            intent.putExtra("usuarioLogado", usuarioLogado); // apenas ENVIAR
            //intent.putExtra("usuarioLogado", usuarioLogado);  // <-- ENVIE O USUÁRIO AQUI!
            //UsuarioModel usuarioLogado = getIntent().getParcelableExtra("usuarioLogado");

//            if (usuarioLogado != null) {
//                Log.d("TESTE_USUARIO", "Recebido: " + usuarioLogado.getNome());
//            } else {
//                Log.d("TESTE_USUARIO", "ERRO: usuário não recebido");
//            }
//            Log.d("DEBUG_USER", "Recebido em <Activity>: " + (usuarioLogado==null? "NULL": usuarioLogado.getNome()));
            startActivity(intent);
        });

        configurarMenu();

        // inicialmente, botão salvar desabilitado (até modificar algo)
        btnSalvar.setEnabled(false);

    }

    private void carregarRefeicoesDoDB() {
        lista.clear();
        originais.clear();

        List<Refeicao> doDb = refeicaoDao.getRefeicoesPorDia(diaSelecionado, (int) usuarioLogado.getId());

        if (doDb == null || doDb.isEmpty()) {
            Dieta d = dietaDao.getDietaPorDia(diaSelecionado, (int) usuarioLogado.getId());
            int idUsuario = (int) usuarioLogado.getId();
            if (d != null) {
                // Criar refeições a partir da DIETA (vem da DietaPréPronta)
                refeicaoDao.inserirRefeicao(new Refeicao(diaSelecionado, idUsuario, "Café da Manhã", "08:00", d.getCafeManha()));
                refeicaoDao.inserirRefeicao(new Refeicao(diaSelecionado, idUsuario,"Almoço", "12:00", d.getAlmoco()));
                refeicaoDao.inserirRefeicao(new Refeicao(diaSelecionado, idUsuario,"Café da Tarde", "15:00", d.getCafeTarde()));
                refeicaoDao.inserirRefeicao(new Refeicao(diaSelecionado, idUsuario,"Jantar", "19:00", d.getJantar()));
                doDb = refeicaoDao.getRefeicoesPorDia(diaSelecionado, (int) usuarioLogado.getId());
            }
            else {
                // Só cria padrão se realmente não existe NADA
                Refeicao r1 = new Refeicao(diaSelecionado, idUsuario,"Café da Manhã", "08:00", "");
                Refeicao r2 = new Refeicao(diaSelecionado, idUsuario,"Almoço", "12:00", "");
                Refeicao r3 = new Refeicao(diaSelecionado, idUsuario,"Café da Tarde", "15:00", "");
                Refeicao r4 = new Refeicao(diaSelecionado, idUsuario,"Jantar", "19:00", "");
                refeicaoDao.inserirRefeicao(r1);
                refeicaoDao.inserirRefeicao(r2);
                refeicaoDao.inserirRefeicao(r3);
                refeicaoDao.inserirRefeicao(r4);
                doDb = refeicaoDao.getRefeicoesPorDia(diaSelecionado, (int) usuarioLogado.getId());
            }
        }
        int idUsuario = (int) usuarioLogado.getId();
        for (Refeicao r : doDb) {
            lista.add(r);
            Refeicao copy = new Refeicao(r.getDia(), idUsuario, r.getNome(), r.getHorario(), r.getDescricao());
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
        Refeicao existente = refeicaoDao.getRefeicaoPorDiaENome(r.getDia(), r.getIdUsuario(), r.getNome());
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
        String cafeTarde = "";
        String jantar = "";

        List<Refeicao> todas = refeicaoDao.getRefeicoesPorDia(diaSelecionado, (int) usuarioLogado.getId());

        for (Refeicao r : todas) {
            String nomeLower = r.getNome().toLowerCase();

            if (nomeLower.contains("café") || nomeLower.contains("cafe")) {
                cafe = append(cafe, r.getDescricao());
            } else if (nomeLower.contains("almoço") || nomeLower.contains("almoco")) {
                almoco = append(almoco, r.getDescricao());
            } else if (nomeLower.contains("jantar")) {
                jantar = append(jantar, r.getDescricao());
            } else if (nomeLower.contains("cafeTarde")) {
                cafeTarde = append(cafeTarde, r.getDescricao());
            }
        }
        // salva dietas com ingredientes reais
        int idUsuario = (int) usuarioLogado.getId();
        int idDietaPrePronta = 0;
        Dieta d = new Dieta(diaSelecionado, idUsuario, idDietaPrePronta, cafe, almoco, cafeTarde, jantar);
        dietaDao.salvarDieta(d);
        // AO FINAL DO SALVAMENTO DA DIETA
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish(); // <-- isso faz voltar para a todayActivity
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
                    Dieta d = dietaDao.getDietaPorDia(diaSelecionado, (int) usuarioLogado.getId());
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
