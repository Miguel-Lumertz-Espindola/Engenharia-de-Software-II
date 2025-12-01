package com.example.projetoengenhariadesoftwareii;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetoengenhariadesoftwareii.database.AppDatabase;
import com.example.projetoengenhariadesoftwareii.database.DAO.DietaDAO;
import com.example.projetoengenhariadesoftwareii.database.DAO.RefeicaoDAO;
import com.example.projetoengenhariadesoftwareii.database.model.Dieta;
import com.example.projetoengenhariadesoftwareii.database.model.Refeicao;
import com.example.projetoengenhariadesoftwareii.database.model.UsuarioModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AlterarHorarioDialog {
    private DiaAdapter diaAdapter;
    private static UsuarioModel usuarioLogado; // ← receber do login
    //Refeicao escolhida = rDao.getRefeicaoPorId(idDaRefeicao); // exemplo!
    private Context context;
    final Set<Integer> diasSelecionados = new HashSet<>();
    private Callback callback;

    public AlterarHorarioDialog(Context context, UsuarioModel usuarioLogado, Callback callback) {
        this.context = context;
        this.usuarioLogado = usuarioLogado;
        this.callback = callback;
    }


    public interface Callback {
        void aoSalvar();
        void onHorarioAlterado();
    }

    public void mostrar(Context context, int diaOriginal, Refeicao refeicao, UsuarioModel usuario, Callback callback) {
        //usuarioLogado = usuario;
        //AlterarHorarioDialog.usuarioLogado = usuario;
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_alterar_horario);
        dialog.setCancelable(true);
        if (usuario == null) {
            Log.e("AlterarHorarioDialog", "❌ usuario recebido é NULL!");
        } else {
            Log.d("AlterarHorarioDialog", "✔ usuario recebido: " + usuario.getId());
        }

        TimePicker timePicker = dialog.findViewById(R.id.timePickerHorario);
        RecyclerView recyclerDias = dialog.findViewById(R.id.recyclerDias);
        TextView txtMesSemana = dialog.findViewById(R.id.textoMesSemana);

        Button btnSalvar = dialog.findViewById(R.id.btnSalvarHorarioDialog);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelarHorarioDialog);
        ImageButton btnAnt = dialog.findViewById(R.id.btnSemanaAnterior);
        ImageButton btnProx = dialog.findViewById(R.id.btnProximaSemana);

        timePicker.setIs24HourView(true);
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        // ---------- CALENDÁRIO ----------
        Calendar cal = Calendar.getInstance();
        final int mesAtual = cal.get(Calendar.MONTH);
        final int anoAtual = cal.get(Calendar.YEAR);
        final int diaHoje = cal.get(Calendar.DAY_OF_MONTH);

        List<DiaItem> diasDoMes = gerarDiasDoMes(mesAtual, anoAtual, diaHoje);
        final Set<Integer> selecionados = new HashSet<>();

        // semana começa na 1
        final int[] semanaAtual = new int[]{1};

        // monta primeira semana
        List<DiaItem> semanaLista = getSemana(diasDoMes, semanaAtual[0]);

        // ADAPTER
        diaAdapter = new DiaAdapter(semanaLista, selecionados, dia -> {
            if (selecionados.contains(dia)) selecionados.remove(dia);
            else selecionados.add(dia);
            diaAdapter.notifyDataSetChanged();
        });

        recyclerDias.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        recyclerDias.setAdapter(diaAdapter);
        atualizarTextoMesSemana(txtMesSemana, mesAtual, semanaAtual[0]);

        // BOTÕES DE SEMANA — só funcionam se existirem no XML!
        if (btnAnt != null) {
            btnAnt.setOnClickListener(v -> {
                if (semanaAtual[0] > 1) {
                    semanaAtual[0]--;
                    atualizarSemana(semanaAtual[0], diasDoMes, diaAdapter, txtMesSemana, mesAtual);
                }
            });
        }

        if (btnProx != null) {
            btnProx.setOnClickListener(v -> {
                int totalSemanas = (int) Math.ceil(diasDoMes.size() / 7.0);
                if (semanaAtual[0] < totalSemanas) {
                    semanaAtual[0]++;
                    atualizarSemana(semanaAtual[0], diasDoMes, diaAdapter, txtMesSemana, mesAtual);
                }
            });
        }

        // SALVAR
        btnSalvar.setOnClickListener(v -> {
            int hora = timePicker.getHour();
            int minuto = timePicker.getMinute();
            String novoHorario = String.format(Locale.getDefault(), "%02d:%02d", hora, minuto);

            RefeicaoDAO rDao = AppDatabase.getInstance(context).RefeicaoDAO();
            DietaDAO dDao = AppDatabase.getInstance(context).dietaDAO();

            refeicao.setHorario(novoHorario);
            rDao.atualizarRefeicao(refeicao);

            //atualizarDieta(dDao, diaOriginal, rDao.getRefeicoesPorDia(diaOriginal,  (int) usuarioLogado.getId()), usuarioLogado, refeicao.getId());

            for (Integer dia : new HashSet<>(selecionados)) {
                if (dia == diaOriginal) continue;

                int idUsuario = (int) usuarioLogado.getId();
                List<Refeicao> lista = rDao.getRefeicoesPorDia(dia, idUsuario);
                boolean achou = false;
                if (lista != null) {
                    for (Refeicao r : lista) {
                        if (r.getNome().equalsIgnoreCase(refeicao.getNome()))
                        {
                            r.setHorario(novoHorario);
                            rDao.atualizarRefeicao(r);
                            achou = true;
                            //break;
                        }
                    }
                }
                if (!achou) {
                    Refeicao nova = new Refeicao(dia, idUsuario, refeicao.getNome(), novoHorario, refeicao.getDescricao());
                    rDao.inserirRefeicao(nova);
                }
                // BUSCA NOVAMENTE DEPOIS DE ALTERAR / INSERIR
                List<Refeicao> listaAtualizada = rDao.getRefeicoesPorDia(dia, idUsuario);

                // 🔥 PEGAR A DIETA CORRETA ANTES DE ATUALIZAR
                Dieta dieta = dDao.getDietaPorDia(dia, idUsuario);
                int idDieta = (dieta != null) ? (int) dieta.getId() : -1;

                atualizarDieta(dDao, dia, lista, usuarioLogado, idDieta);
                //atualizarDieta(dDao, dia, rDao.getRefeicoesPorDia(dia, (int) usuarioLogado.getId()), usuarioLogado, refeicao.getId());
            }

            dialog.dismiss();
            if (callback != null) callback.aoSalvar();
        });

        dialog.show();
    }

    // ---------- MINI-AGENDA ----------
    private static List<DiaItem> gerarDiasDoMes(int mes, int ano, int diaHoje) {
        List<DiaItem> lista = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, ano);
        cal.set(Calendar.MONTH, mes);
        int max = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int d = 1; d <= max; d++) {
            cal.set(Calendar.DAY_OF_MONTH, d);
            lista.add(new DiaItem(d, getNomeDiaCurto(cal.get(Calendar.DAY_OF_WEEK)), d == diaHoje));
        }
        return lista;
    }

//    private static List<DiaItem> getSemana(List<DiaItem> dias, int semana) {
//        int inicio = (semana - 1) * 7;
//        int fim = Math.min(inicio + 7, dias.size());
//        return dias.subList(inicio, fim);
//    }
private static List<DiaItem> getSemana(List<DiaItem> dias, int semana) {
    int inicio = (semana - 1) * 7;
    int fim = Math.min(inicio + 7, dias.size());

    // return dias.subList(inicio, fim);  // ❌ ERRO
    // ✔️ solução segura:
    return new ArrayList<>(dias.subList(inicio, fim));
}


    private void atualizarSemana(int semana, List<DiaItem> dias, DiaAdapter adp,
                                        TextView txt, int mes) {
        adp.atualizarLista(getSemana(dias, semana), diasSelecionados);
        atualizarTextoMesSemana(txt, mes, semana);
    }

    private static void atualizarTextoMesSemana(TextView txt, int mes, int semana) {
        txt.setText(getNomeMes(mes) + " - Semana " + semana);
    }

    private static String getNomeMes(int m) {
        String[] meses = {"Janeiro","Fevereiro","Março","Abril","Maio","Junho",
                "Julho","Agosto","Setembro","Outubro","Novembro","Dezembro"};
        return meses[m];
    }

    // ---------- DIETA ----------
//    private static void atualizarDieta(DietaDAO dao, int dia, List<Refeicao> lista) {
//        String cafeManha="", almoco="", cafeTarde="", jantar="";
//        if (lista != null) {
//            for (Refeicao r : lista) {
//                String tipo = r.getNome().toLowerCase();
//                String item = r.getNome() + " (" + r.getHorario() + ")";
//                if (tipo.contains("cafeManha")) cafeManha = append(cafeManha, item);
//                else if (tipo.contains("almoço")) almoco = append(almoco, item);
//                else if (tipo.contains("cafeTarde")) cafeTarde = append(cafeTarde, item);
//                else jantar = append(jantar, item);
//            }
//        }
//        dao.salvarDieta(new Dieta(dia, cafeManha, almoco, cafeTarde, jantar));
//    }

    private static void atualizarDieta(DietaDAO dao, int dia, List<Refeicao> lista, UsuarioModel usuarioLogado, int idDietaPrePronta) {
        // mantém as descrições ORIGINAIS
        String cafe="", almoco="", cafeTarde="", jantar="";

        if (lista != null) {
            for (Refeicao r : lista) {
                String nome = r.getNome().toLowerCase();
                String descricao = r.getDescricao();  // <-- MUITO IMPORTANTE!

                // se não tiver descrição, melhor não sobrescrever!
                if (descricao == null || descricao.trim().isEmpty()) continue;

                if (nome.contains("café da manhã") || nome.contains("cafe da manha")) {
                    cafe = append(cafe, descricao);
                }
                else if (nome.contains("almoço") || nome.contains("almoco")) {
                    almoco = append(almoco, descricao);
                }
                else if (nome.contains("café da tarde") || nome.contains("cafe da tarde")) {
                    cafeTarde = append(cafeTarde, descricao);
                }
                else if (nome.contains("jantar")) {
                    jantar = append(jantar, descricao);
                }
            }
        }

        // Atualiza a dieta sem sobrescrever tudo
        int idUsuario = (int) usuarioLogado.getId();           // pegue o usuário logado
        Dieta existente = dao.getDietaPorDia(dia, (int) usuarioLogado.getId());
        if (existente == null) {
            dao.inserirDieta(new Dieta(dia, idUsuario, idDietaPrePronta, cafe, almoco, cafeTarde, jantar));
        } else {
            if (!cafe.isEmpty())     existente.setCafeManha(cafe);
            if (!almoco.isEmpty())   existente.setAlmoco(almoco);
            if (!cafeTarde.isEmpty())existente.setCafeTarde(cafeTarde);
            if (!jantar.isEmpty())   existente.setJantar(jantar);
            dao.atualizarDieta(existente);
        }
    }


    private static String append(String base, String v) {
        return base.isEmpty() ? v : base + "; " + v;
    }

    private static String getNomeDiaCurto(int d) {
        return new String[]{"Dom","Seg","Ter","Qua","Qui","Sex","Sáb"}[d-1];
    }
}