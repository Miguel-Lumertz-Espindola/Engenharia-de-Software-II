package com.example.projetoengenhariadesoftwareii;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.example.projetoengenhariadesoftwareii.database.AppDatabase;
import com.example.projetoengenhariadesoftwareii.database.DAO.IngredienteDAO;
import com.example.projetoengenhariadesoftwareii.database.model.Ingrediente;

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

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        AppDatabase db = AppDatabase.getInstance(this);
        IngredienteDAO ingredienteDAO = db.ingredienteDAO();

        if (ingredienteDAO.listarTodos().isEmpty()) {
            ingredienteDAO.inserirIngrediente(new Ingrediente("Arroz Integral", "Grãos integrais ricos em fibras", "gramas"));
            ingredienteDAO.inserirIngrediente(new Ingrediente("Frango Grelhado", "Fonte de proteína magra", "gramas"));
            ingredienteDAO.inserirIngrediente(new Ingrediente("Banana", "Rica em potássio e energia natural", "unidades"));
            ingredienteDAO.inserirIngrediente(new Ingrediente("Leite Desnatado", "Fonte de cálcio com baixo teor de gordura", "ml"));
            ingredienteDAO.inserirIngrediente(new Ingrediente("Ovos Cozidos", "Proteína completa e nutritiva", "gramas"));
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Define o dia de hoje com timezone de Brasília
        Calendar calBR = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));
        diaHoje = calBR.get(Calendar.DAY_OF_MONTH);

        // Inicializa os componentes
        buttonMenu = findViewById(R.id.buttonMenu);
        buttonLogo = findViewById(R.id.buttonlogo);
        textoMesSemana = findViewById(R.id.textoMesSemana);
        btnSemanaAnterior = findViewById(R.id.btnSemanaAnterior);
        btnProximaSemana = findViewById(R.id.btnProximaSemana);
        recyclerDias = findViewById(R.id.recyclerDias);
        btnEditarDieta = findViewById(R.id.btnEditarDieta);

        recyclerDias.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        // Gera TODOS os dias do mês (listaTodosDias) e inicializa adapter com a SUBLIST da semana 1
        gerarTodosDiasDoMes();
        atualizarParaSemana(1); // IMPORTANTÍSSIMO: define semana 1 na inicialização

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
            diaAdapter.atualizarLista(sub);
        }

        String nomeMes = new DateFormatSymbols().getMonths()[mesAtual];
        textoMesSemana.setText(getNomeMes(mesAtual) + " - Semana " + semanaAtual);
        // garante que as refeições do dia atual/selecionado apareçam
        mostrarRefeicoesDoDia(diaSelecionadoAtual == -1 ? diaHoje : diaSelecionadoAtual);
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
                    diaSelecionado
            );
        });
    }

    private void abrirTelaEdicaoDieta() {
        int diaParaEditar = (diaSelecionadoAtual != -1) ? diaSelecionadoAtual : diaHoje;
        Intent intent = new Intent(this, EditarDietaActivity.class);
        intent.putExtra("diaSelecionado", diaParaEditar);
        startActivity(intent);
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
        if (diaSelecionadoAtual != -1) {
            mostrarRefeicoesDoDia(diaSelecionadoAtual);
        }
    }
}