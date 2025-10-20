package com.example.projetoengenhariadesoftwareii;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
//import android.widget.RecyclerView;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.os.Bundle;

import java.text.DateFormatSymbols;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class todayActivity extends AppCompatActivity {

    ImageButton buttonMenu, buttonLogo, btnSemanaAnterior, btnProximaSemana;
    TextView textoMesSemana;
    RecyclerView recyclerDias;
    Button btnEditarDieta;

    int semanaAtual = 1;
    Calendar calendario = Calendar.getInstance();

    int mesAtual = calendario.get(Calendar.MONTH);
    int anoAtual = calendario.get(Calendar.YEAR);
    int diaHoje; // atualizado com fuso horário correto

    DiaAdapter diaAdapter;
    List<DiaItem> listaDias;
    int diaSelecionado = -1;
    private int diaSelecionadoAtual = -1; // guarda o último dia clicado

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Define o dia de hoje com base no fuso horário de Brasília
        ZonedDateTime agora = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"));
        diaHoje = agora.getDayOfMonth();

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

        gerarDiasDaSemana();

        btnSemanaAnterior.setOnClickListener(v -> {
            if (semanaAtual > 1) {
                semanaAtual--;
                gerarDiasDaSemana();
            }
        });

        btnProximaSemana.setOnClickListener(v -> {
            if (semanaAtual < 5) {
                semanaAtual++;
                gerarDiasDaSemana();
            }
        });

        btnEditarDieta.setOnClickListener(v -> abrirTelaEdicaoDieta());

        configurarMenu();
    }

    private void gerarDiasDaSemana() {
        int primeiroDia = (semanaAtual - 1) * 7 + 1;
        int ultimoDia = Math.min(primeiroDia + 6, calendario.getActualMaximum(Calendar.DAY_OF_MONTH));

        listaDias = new ArrayList<>();
        for (int dia = primeiroDia; dia <= ultimoDia; dia++) {
            boolean isHoje = (dia == diaHoje);
            listaDias.add(new DiaItem(dia, getDiaSemana(dia), isHoje));
        }

        // Define a ação ao clicar em um dia
        diaAdapter = new DiaAdapter(listaDias, dia -> {
            diaSelecionado = dia;
            diaSelecionadoAtual = dia; // Atualiza o dia selecionado real
            mostrarRefeicoesDoDia(dia);
            Toast.makeText(this, "Dia " + dia + " selecionado!", Toast.LENGTH_SHORT).show();
        });
        recyclerDias.setAdapter(diaAdapter);

        String nomeMes = new DateFormatSymbols().getMonths()[mesAtual];
        textoMesSemana.setText(
                nomeMes.substring(0, 1).toUpperCase() + nomeMes.substring(1) + " - Semana " + semanaAtual
        );

        mostrarRefeicoesDoDia(diaSelecionadoAtual == -1 ? diaHoje : diaSelecionadoAtual);
    }

    private String getDiaSemana(int diaDoMes) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, diaDoMes);
        String[] dias = {"Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"};
        return dias[c.get(Calendar.DAY_OF_WEEK) - 1];
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

        // Atualiza automaticamente as refeições do dia selecionado
        if (diaSelecionadoAtual != -1) {
            mostrarRefeicoesDoDia(diaSelecionadoAtual);
        }
    }
}
