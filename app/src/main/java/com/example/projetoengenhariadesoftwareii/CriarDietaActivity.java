package com.example.projetoengenhariadesoftwareii;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projetoengenhariadesoftwareii.database.*;
import com.example.projetoengenhariadesoftwareii.database.DAO.DietaDAO;
import com.example.projetoengenhariadesoftwareii.database.DAO.IngredienteDAO;
import com.example.projetoengenhariadesoftwareii.database.DAO.RefeicaoDAO;
import com.example.projetoengenhariadesoftwareii.database.model.Dieta;
import com.example.projetoengenhariadesoftwareii.database.model.Ingrediente;
import com.example.projetoengenhariadesoftwareii.database.model.Refeicao;
import com.example.projetoengenhariadesoftwareii.database.model.UsuarioModel;

import java.util.*;

public class CriarDietaActivity extends AppCompatActivity {

    private AppDatabase db;
    ImageButton buttonMenu, buttonLogo;
    private IngredienteDAO ingredienteDAO;
    private DietaDAO dietaDAO;

    private LinearLayout layoutCafe, layoutAlmoco, layoutJantar, layoutCafeTarde;
    private Button btnAddCafe, btnAddAlmoco, btnAddCafeTarde, btnAddJantar, btnAdicionarDietas;
    private RecyclerView recyclerDias;
    private Calendar calendario = Calendar.getInstance();
    private List<DiaItem> diasDoMes = new ArrayList<>();
    private int mesAtual, anoAtual, diaHoje, semanaAtual = 1;
    private TextView textoMesSemana;
    private Set<Integer> diasSelecionados = new HashSet<>();
    private Map<String, List<String>> refeicoes = new HashMap<>();
    private DiaAdapter diaAdapter;
    private static UsuarioModel usuarioLogado; // ← receber do login

    private final String[] nomesDias = {"Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_dieta);

        buttonMenu = findViewById(R.id.buttonMenu);
        buttonLogo = findViewById(R.id.buttonlogo);

        // Inicializa datas
        calendario = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));
        mesAtual = calendario.get(Calendar.MONTH);
        anoAtual = calendario.get(Calendar.YEAR);
        diaHoje = calendario.get(Calendar.DAY_OF_MONTH);

        textoMesSemana = findViewById(R.id.textoMesSemana);

        db = AppDatabase.getInstance(this);
        ingredienteDAO = db.ingredienteDAO();
        dietaDAO = db.dietaDAO();

        layoutCafe = findViewById(R.id.layoutCafe);
        layoutAlmoco = findViewById(R.id.layoutAlmoco);
        layoutCafeTarde = findViewById(R.id.layoutCafeTarde);
        layoutJantar = findViewById(R.id.layoutJantar);

        btnAddCafe = findViewById(R.id.btnAddCafe);
        btnAddAlmoco = findViewById(R.id.btnAddAlmoco);
        btnAddCafeTarde = findViewById(R.id.btnAddCafeTarde);
        btnAddJantar = findViewById(R.id.btnAddJantar);
        btnAdicionarDietas = findViewById(R.id.btnAdicionarDietas);

        recyclerDias = findViewById(R.id.recyclerDias);
        recyclerDias.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Gera todos os dias do mês
        diasDoMes = gerarDiasDoMes(mesAtual, anoAtual);

        // Cria adapter inicial com a primeira semana
        List<DiaItem> primeiraSemana = getSubListaSemana(1);
        diaAdapter = new DiaAdapter(primeiraSemana, diasSelecionados, dia -> {
            if (!diasSelecionados.contains(dia)) {
                diasSelecionados.add(dia);
            } else {
                diasSelecionados.remove(dia);
            }
            diaAdapter.notifyDataSetChanged();
        });

        recyclerDias.setAdapter(diaAdapter);
        atualizarTextoMesSemana();

        // Botões de navegação entre semanas
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

        // Inicializa mapas de refeição
        refeicoes.put("cafe", new ArrayList<>());
        refeicoes.put("almoco", new ArrayList<>());
        refeicoes.put("cafeTarde", new ArrayList<>());
        refeicoes.put("jantar", new ArrayList<>());

        btnAddCafe.setOnClickListener(v -> abrirDialogIngrediente("cafe", layoutCafe));
        btnAddAlmoco.setOnClickListener(v -> abrirDialogIngrediente("almoco", layoutAlmoco));
        btnAddCafeTarde.setOnClickListener(v -> abrirDialogIngrediente("cafeTarde", layoutCafeTarde));
        btnAddJantar.setOnClickListener(v -> abrirDialogIngrediente("jantar", layoutJantar));

        btnAdicionarDietas.setOnClickListener(v -> salvarDietasSelecionadas());

        // Preenche ingredientes se estiver vazio
        if (ingredienteDAO.listarTodos().isEmpty()) {
            prePopularIngredientes();
        }
        configurarMenu();
    }

    //FORA DO ONCREATE:
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

    private void abrirDialogIngrediente(String tipo, LinearLayout container) {
        List<Ingrediente> ingredientes = ingredienteDAO.listarTodos();
        if (ingredientes.isEmpty()) {
            Toast.makeText(this, "Nenhum ingrediente cadastrado!", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_adicionar_ingrediente, null);
        Spinner spinner = dialogView.findViewById(R.id.spinnerIngredientes);
        EditText inputQtd = dialogView.findViewById(R.id.inputQuantidade);
        TextView textDescricao = dialogView.findViewById(R.id.textDescricaoIngrediente);

        // 🔹 Adapter personalizado para mudar cor do texto
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                getNomesIngredientes(ingredientes)
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setTextColor(getResources().getColor(R.color.azulpadrao)); // Cor do item selecionado
                text.setTextSize(16);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setTextColor(getResources().getColor(R.color.black));
                text.setBackgroundResource(R.drawable.bg_dialog);
                text.setPadding(20, 20, 20, 20);
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Exibe a descrição do primeiro ingrediente
        textDescricao.setText(ingredientes.get(0).getDescricao());
        textDescricao.setVisibility(View.VISIBLE);

        // Atualiza descrição ao trocar ingrediente no spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                textDescricao.setText(ingredientes.get(position).getDescricao());
                // 👇 muda também a cor do item selecionado dinamicamente, se quiser
                ((TextView) view).setTextColor(getResources().getColor(R.color.azulpadrao));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Adicionar Ingrediente")
                .setView(dialogView)
                .setPositiveButton("Adicionar", (d, w) -> {
                    int pos = spinner.getSelectedItemPosition();
                    Ingrediente ing = ingredientes.get(pos);
                    String qtd = inputQtd.getText().toString();

                    if (qtd.isEmpty()) {
                        Toast.makeText(this, "Informe a quantidade!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<String> listaRefeicao = refeicoes.get(tipo);
                    for (String item : listaRefeicao) {
                        if (item.startsWith(ing.getNome() + " ")) {
                            Toast.makeText(this, "Ingrediente já adicionado nesta refeição!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    String texto = ing.getNome() + " - " + qtd + " " + ing.getUnidade();
                    listaRefeicao.add(texto);
                    TextView tv = new TextView(this);
                    tv.setText(texto);
                    container.addView(tv);
                })
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
    }


    private List<String> getNomesIngredientes(List<Ingrediente> lista) {
        List<String> nomes = new ArrayList<>();
        for (Ingrediente i : lista) nomes.add(i.getNome());
        return nomes;
    }

    private void salvarDietasSelecionadas() {
        if (diasSelecionados.isEmpty()) {
            Toast.makeText(this, "Selecione ao menos um dia!", Toast.LENGTH_SHORT).show();
            return;
        }

//        for (int dia : diasSelecionados) {
//            String cafe = String.join("\n", refeicoes.get("cafe"));
//            String almoco = String.join("\n", refeicoes.get("almoco"));
//            String jantar = String.join("\n", refeicoes.get("jantar"));
//            dietaDAO.salvarDieta(new Dieta(dia, cafe, almoco, jantar));
//        }

        int idDietaPrePronta = 0;  // ou algum valor recebido de Intent ou Spinner
        for (int dia : diasSelecionados) {
            String cafe = String.join("\n", refeicoes.get("cafe"));
            String almoco = String.join("\n", refeicoes.get("almoco"));
            String cafeTarde = String.join("\n", refeicoes.get("cafeTarde"));
            String jantar = String.join("\n", refeicoes.get("jantar"));
            int idUsuario = (int) usuarioLogado.getId();

            // salva Dieta (mantém a tabela dietas)
            Dieta d = new Dieta(dia, idUsuario, idDietaPrePronta, cafe, almoco, cafeTarde, jantar);
            dietaDAO.salvarDieta(d);

            // sincroniza com refeicoes (inserir ou atualizar por nome)
            RefeicaoDAO rDao = db.RefeicaoDAO();

            upsertRefeicao(rDao, dia, idUsuario,"Café da Manhã", "08:00", cafe);
            upsertRefeicao(rDao, dia, idUsuario,"Almoço", "12:00", almoco);
            upsertRefeicao(rDao, dia, idUsuario,"Café da Tarde", "08:00", cafeTarde);
            upsertRefeicao(rDao, dia, idUsuario,"Jantar", "19:00", jantar);
        }


        Toast.makeText(this, "Dietas salvas com sucesso!", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void upsertRefeicao(RefeicaoDAO rDao, int dia, int idUsuario, String nome, String horario, String descricao) {
        Refeicao existente = rDao.getRefeicaoPorDiaENome(dia, idUsuario, nome);
        //int idUsuario = (int) usuarioLogado.getId();
        if (existente != null) {
            existente.setHorario(horario);
            existente.setDescricao(descricao);
            rDao.atualizarRefeicao(existente);
        } else {
            Refeicao nova = new Refeicao(dia, idUsuario, nome, horario, descricao);
            rDao.inserirRefeicao(nova);
        }
    }

    private void prePopularIngredientes() {
        ingredienteDAO.inserirIngrediente(new Ingrediente("Arroz", "Base de carboidrato", "gramas"));
        ingredienteDAO.inserirIngrediente(new Ingrediente("Frango", "Proteína magra", "gramas"));
        ingredienteDAO.inserirIngrediente(new Ingrediente("Banana", "Fonte de potássio", "unidades"));
        ingredienteDAO.inserirIngrediente(new Ingrediente("Leite", "Fonte de cálcio", "ml"));
        ingredienteDAO.inserirIngrediente(new Ingrediente("Ovos", "Fonte de proteína", "unidades"));
    }

    private void configurarMenu() {
        buttonLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CriarDietaActivity.this, todayActivity.class);
                startActivity(intent);
            }
        });

        buttonMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(CriarDietaActivity.this, buttonMenu);
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