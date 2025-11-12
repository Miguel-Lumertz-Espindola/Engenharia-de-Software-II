package com.example.projetoengenhariadesoftwareii;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projetoengenhariadesoftwareii.database.AppDatabase;
import com.example.projetoengenhariadesoftwareii.database.model.DietaPreProntaModel;
import com.example.projetoengenhariadesoftwareii.database.model.Dieta;
import com.example.projetoengenhariadesoftwareii.database.DAO.DietaDAO;
import java.util.*;

public class DietasPreProntasActivity extends AppCompatActivity {

    private RecyclerView recyclerDietaspreprontas, recyclerDias;
    private AppDatabase db;
    private DietaDAO dietaDAO;

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

        recyclerDietaspreprontas = findViewById(R.id.recyclerDietaspreprontas);
        recyclerDias = findViewById(R.id.recyclerDias);
        textoMesSemana = findViewById(R.id.textoMesSemana);

        recyclerDietaspreprontas.setLayoutManager(new LinearLayoutManager(this));
        recyclerDias.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        db = AppDatabase.getInstance(this);
        dietaDAO = db.dietaDAO();

        db.dietaPreProntaDAO().excluirTodas();

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
        if (db.dietaPreProntaDAO().getTodas().isEmpty()) {
            db.dietaPreProntaDAO().inserir(new DietaPreProntaModel(
                    "Dieta Fit Equilibrada",
                    "Alimentação balanceada para quem busca emagrecimento saudável",
                    "Iogurte natural (200g) + granola (100g) + banana (2un)",
                    "Peito de frango grelhado + arroz integral + salada",
                    "Omelete de claras + salada verde"
            ));

            db.dietaPreProntaDAO().inserir(new DietaPreProntaModel(
                    "Dieta Proteica",
                    "Ideal para ganho de massa muscular",
                    "Ovos mexidos + pão integral + suco natural",
                    "Carne magra + batata-doce + brócolis",
                    "Peixe grelhado + legumes no vapor"
            ));

            db.dietaPreProntaDAO().inserir(new DietaPreProntaModel(
                    "Dieta Vegetariana",
                    "Foco em proteínas vegetais e equilíbrio nutricional",
                    "Vitamina de frutas com aveia",
                    "Lentilha + arroz integral + legumes refogados",
                    "Tofu grelhado + salada com grão-de-bico"
            ));
        }

        List<DietaPreProntaModel> listaDietas = db.dietaPreProntaDAO().getTodas();

        DietaPreProntaAdapter adapter = new DietaPreProntaAdapter(listaDietas, new DietaPreProntaAdapter.OnDietaClickListener() {
            @Override
            public void onVisualizar(DietaPreProntaModel dieta) {
                abrirDialogVisualizar(DietasPreProntasActivity.this, dieta);
            }

            @Override
            public void onAdicionar(DietaPreProntaModel dieta) {
                adicionarDietaNosDiasSelecionados(dieta);
            }
        });

        recyclerDietaspreprontas.setAdapter(adapter);
    }

    // 🔹 Salva a dieta pré-pronta nos dias marcados (com ingredientes formatados)
    private void adicionarDietaNosDiasSelecionados(DietaPreProntaModel dieta) {
        if (diasSelecionados.isEmpty()) {
            Toast.makeText(this, "Selecione ao menos um dia na mini agenda!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔸 Formatar refeições
        String cafe = formatarRefeicao(dieta.getCafeManha());
        String almoco = formatarRefeicao(dieta.getAlmoco());
        String jantar = formatarRefeicao(dieta.getJantar());

        // 🔸 Salvar para cada dia selecionado
        for (int dia : diasSelecionados) {
            Dieta nova = new Dieta(dia, cafe, almoco, jantar);
            dietaDAO.salvarDieta(nova);
        }

        Toast.makeText(this,
                "Dieta \"" + dieta.getNomeDieta() + "\" adicionada nos dias selecionados!",
                Toast.LENGTH_SHORT).show();
    }

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
        diaAdapter.atualizarLista(subLista);
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
}