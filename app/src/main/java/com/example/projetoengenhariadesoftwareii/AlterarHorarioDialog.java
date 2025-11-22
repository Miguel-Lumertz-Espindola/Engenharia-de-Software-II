package com.example.projetoengenhariadesoftwareii;

import android.app.Dialog;
import android.content.Context;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AlterarHorarioDialog {
    private static DiaAdapter diaAdapter;

    public interface Callback {
        void aoSalvar();
    }

    public static void mostrar(Context context, int diaOriginal, Refeicao refeicao, Callback callback) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_alterar_horario);
        dialog.setCancelable(true);

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
            atualizarDieta(dDao, diaOriginal, rDao.getRefeicoesPorDia(diaOriginal));

            for (Integer dia : new HashSet<>(selecionados)) {
                if (dia == diaOriginal) continue;

                List<Refeicao> lista = rDao.getRefeicoesPorDia(dia);
                boolean achou = false;
                if (lista != null) {
                    for (Refeicao r : lista) {
                        if (r.getNome().equalsIgnoreCase(refeicao.getNome()))
                        {
                            r.setHorario(novoHorario);
                            rDao.atualizarRefeicao(r);
                            achou = true;
                            break;
                        }
                    }
                }
                if (!achou) {
                    Refeicao nova = new Refeicao(dia, refeicao.getNome(), novoHorario, refeicao.getDescricao());
                    rDao.inserirRefeicao(nova);
                }
                atualizarDieta(dDao, dia, rDao.getRefeicoesPorDia(dia));
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


    private static void atualizarSemana(int semana, List<DiaItem> dias, DiaAdapter adp,
                                        TextView txt, int mes) {
        adp.atualizarLista(getSemana(dias, semana));
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

    private static void atualizarDieta(DietaDAO dao, int dia, List<Refeicao> lista) {
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
        Dieta existente = dao.getDietaPorDia(dia);
        if (existente == null) {
            dao.inserirDieta(new Dieta(dia, cafe, almoco, cafeTarde, jantar));
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


//package com.example.projetoengenhariadesoftwareii;
//
//import android.app.Dialog;
//import android.content.Context;
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.TimePicker;
//
//import androidx.recyclerview.widget.GridLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.projetoengenhariadesoftwareii.database.AppDatabase;
//import com.example.projetoengenhariadesoftwareii.database.DAO.DietaDAO;
//import com.example.projetoengenhariadesoftwareii.database.DAO.RefeicaoDAO;
//import com.example.projetoengenhariadesoftwareii.database.model.Dieta;
//import com.example.projetoengenhariadesoftwareii.database.model.Refeicao;
//
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Locale;
//import java.util.Set;
//
///**
// * Dialog para alterar horário com mini-agenda (seleção de múltiplos dias).
// * Compatível com o DiaAdapter/DiaItem do seu projeto.
// */
//public class AlterarHorarioDialog {
//
//    public interface Callback {
//        void aoSalvar();
//    }
//
//
//
//    public static void mostrar(Context context, int diaOriginal, Refeicao refeicao, Callback callback) {
//        Dialog dialog = new Dialog(context);
//        dialog.setContentView(R.layout.dialog_alterar_horario);
//        dialog.setCancelable(true);
//
//        TimePicker timePicker = dialog.findViewById(R.id.timePickerHorario);
//        RecyclerView recyclerDias = dialog.findViewById(R.id.recyclerDiasHorario);
//
//        Button btnSalvar = dialog.findViewById(R.id.btnSalvarHorarioDialog);
//        Button btnCancelar = dialog.findViewById(R.id.btnCancelarHorarioDialog);
//
//        btnCancelar.setOnClickListener(v -> dialog.dismiss());
//
//        btnSalvar.setOnClickListener(v -> {
//            String novoHorario = timePicker.getHour() + ":" + timePicker.getMinute();
//            refeicao.setHorario(novoHorario);
//
//            if (callback != null) callback.aoSalvar();
//
//            dialog.dismiss();
//        });
//
//        timePicker.setIs24HourView(true);
//
//        // --- monta lista de DiaItem para o mês atual ---
//        Calendar cal = Calendar.getInstance();
//        int mesAtual = cal.get(Calendar.MONTH);
//        int anoAtual = cal.get(Calendar.YEAR);
//
//        Calendar tmp = Calendar.getInstance();
//        tmp.set(Calendar.YEAR, anoAtual);
//        tmp.set(Calendar.MONTH, mesAtual);
//        int maxDias = tmp.getActualMaximum(Calendar.DAY_OF_MONTH);
//
//        List<DiaItem> diaItems = new ArrayList<>();
//        for (int d = 1; d <= maxDias; d++) {
//            Calendar c = Calendar.getInstance();
//            c.set(Calendar.YEAR, anoAtual);
//            c.set(Calendar.MONTH, mesAtual);
//            c.set(Calendar.DAY_OF_MONTH, d);
//            String nomeSemana = getNomeDiaCurto(c.get(Calendar.DAY_OF_WEEK));
//            diaItems.add(new DiaItem(d, nomeSemana, false));
//        }
//
//        // set que mantém os dias selecionados
//        Set<Integer> selecionados = new HashSet<>();
//
//        // listener compatível com todayActivity: recebe int dia
//        DiaAdapter.OnDiaClickListener diaListener = dia -> {
//            if (selecionados.contains(dia)) selecionados.remove(dia);
//            else selecionados.add(dia);
//        };
//
//        // cria adapter com os 3 parâmetros esperados
//        DiaAdapter diaAdapter = new DiaAdapter(diaItems, selecionados, diaListener);
//        recyclerDias.setLayoutManager(new GridLayoutManager(context, 7)); // mini-agenda em grid
//        recyclerDias.setAdapter(diaAdapter);
//
//        // salvar: atualiza refeição do dia original e dias selecionados
//        btnSalvar.setOnClickListener(v -> {
//            int hora = timePicker.getHour();
//            int minuto = timePicker.getMinute();
//            String novoHorario = String.format(Locale.getDefault(), "%02d:%02d", hora, minuto);
//
//            RefeicaoDAO rDao = AppDatabase.getInstance(context).RefeicaoDAO();
//            DietaDAO dDao = AppDatabase.getInstance(context).dietaDAO();
//
//            // 1) atualiza a refeição do dia original (objeto passado)
//            refeicao.setHorario(novoHorario);
//            rDao.atualizarRefeicao(refeicao);
//
//            // sincroniza dieta do dia original
//            List<Refeicao> listaOriginal = rDao.getRefeicoesPorDia(diaOriginal);
//            atualizarDietaDadoRefeicoes(dDao, diaOriginal, listaOriginal);
//
//            // 2) aplica para cada dia selecionado
//            for (Integer diaAlvo : new HashSet<>(selecionados)) {
//                if (diaAlvo == diaOriginal) continue;
//
//                List<Refeicao> refeicoesAlvo = rDao.getRefeicoesPorDia(diaAlvo);
//                boolean achou = false;
//
//                if (refeicoesAlvo != null) {
//                    for (Refeicao r : refeicoesAlvo) {
//                        if (r.getNome() != null && r.getNome().equalsIgnoreCase(refeicao.getNome())) {
//                            r.setHorario(novoHorario);
//                            rDao.atualizarRefeicao(r);
//                            achou = true;
//                            break;
//                        }
//                    }
//                }
//
//                if (!achou) {
//                    Refeicao nova = new Refeicao(diaAlvo, refeicao.getNome(), novoHorario, refeicao.getDescricao());
//                    rDao.inserirRefeicao(nova);
//                }
//
//                List<Refeicao> atualizadas = rDao.getRefeicoesPorDia(diaAlvo);
//                atualizarDietaDadoRefeicoes(dDao, diaAlvo, atualizadas);
//            }
//
//            dialog.dismiss();
//            if (callback != null) callback.aoSalvar();
//        });
//
//        dialog.show();
//    }
//
//    // monta e salva Dieta a partir da lista de Refeição (mesma lógica que você usa)
//    private static void atualizarDietaDadoRefeicoes(DietaDAO dao, int dia, List<Refeicao> lista) {
//        String cafe = "";
//        String almoco = "";
//        String jantar = "";
//
//        if (lista != null) {
//            for (Refeicao r : lista) {
//                String tipo = (r.getNome() == null) ? "" : r.getNome().toLowerCase();
//                String entry = r.getNome() + " (" + r.getHorario() + ")";
//                if (tipo.contains("café") || tipo.contains("cafe") || tipo.contains("manhã") || tipo.contains("manha")) {
//                    cafe = append(cafe, entry);
//                } else if (tipo.contains("almoço") || tipo.contains("almoco")) {
//                    almoco = append(almoco, entry);
//                } else if (tipo.contains("jantar")) {
//                    jantar = append(jantar, entry);
//                } else {
//                    if (jantar.isEmpty()) jantar = append(jantar, entry);
//                    else if (almoco.isEmpty()) almoco = append(almoco, entry);
//                    else if (cafe.isEmpty()) cafe = append(cafe, entry);
//                    else jantar = append(jantar, entry);
//                }
//            }
//        }
//
//        Dieta d = new Dieta(dia, cafe, almoco, jantar);
//        dao.salvarDieta(d);
//    }
//
//    private static String append(String base, String entry) {
//        if (base == null || base.isEmpty()) return entry;
//        return base + "; " + entry;
//    }
//
//    private static String getNomeDiaCurto(int diaSemana) {
//        switch (diaSemana) {
//            case Calendar.SUNDAY: return "Dom";
//            case Calendar.MONDAY: return "Seg";
//            case Calendar.TUESDAY: return "Ter";
//            case Calendar.WEDNESDAY: return "Qua";
//            case Calendar.THURSDAY: return "Qui";
//            case Calendar.FRIDAY: return "Sex";
//            case Calendar.SATURDAY: return "Sáb";
//            default: return "";
//        }
//    }
//}
