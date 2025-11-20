package com.example.projetoengenhariadesoftwareii;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

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
import java.util.TimeZone;

public class AlterarHorarioDialog {

    private DiaAdapter diaAdapter;  // DECLARAÇÃO AQUI

    public interface Callback {
        void aoSalvar();
    }

    public static void mostrar(Context context, int diaOriginal, Refeicao refeicao, Callback callback) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_alterar_horario);
        dialog.setCancelable(true);

        TimePicker timePicker = dialog.findViewById(R.id.timePickerHorario);
        RecyclerView recyclerDias = dialog.findViewById(R.id.recyclerDiasHorario);
        TextView textoMesSemana = dialog.findViewById(R.id.textoMesSemana);
        Button btnSalvar = dialog.findViewById(R.id.btnSalvarHorarioDialog);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelarHorarioDialog);

        timePicker.setIs24HourView(true);

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        // --------------------- MINI AGENDA NOVA ------------------------
        Calendar calendario = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));
        int mesAtual = calendario.get(Calendar.MONTH);
        int anoAtual = calendario.get(Calendar.YEAR);
        int diaHoje = calendario.get(Calendar.DAY_OF_MONTH);
        int semanaAtual = 1;

        // gera todos os dias do mês
        List<DiaItem> diasDoMes = gerarDiasDoMes(mesAtual, anoAtual, diaHoje);

        // Set para manter os dias selecionados
        Set<Integer> diasSelecionados = new HashSet<>();

        // primeira semana
        List<DiaItem> primeiraSemana = getSubListaSemana(diasDoMes, 1);
        DiaAdapter diaAdapter = new DiaAdapter(primeiraSemana, diasSelecionados, dia -> {
            if (diasSelecionados.contains(dia)) diasSelecionados.remove(dia);
            else diasSelecionados.add(dia);
            diaAdapter.notifyDataSetChanged();
        });

        recyclerDias.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

        recyclerDias.setAdapter(diaAdapter);

        atualizarTextoMesSemana(textoMesSemana, mesAtual, semanaAtual);

        // Botão semana anterior
        dialog.findViewById(R.id.btnSemanaAnterior).setOnClickListener(v -> {
            if (semanaAtual > 1) {
                semanaAtual--;
                irParaSemana(semanaAtual, diasDoMes, diaAdapter, recyclerDias, textoMesSemana, mesAtual);
            }
        });

        // Botão próxima semana
        dialog.findViewById(R.id.btnProximaSemana).setOnClickListener(v -> {
            int totalSemanas = (int) Math.ceil(diasDoMes.size() / 7.0);
            if (semanaAtual < totalSemanas) {
                semanaAtual++;
                irParaSemana(semanaAtual, diasDoMes, diaAdapter, recyclerDias, textoMesSemana, mesAtual);
            }
        });
        // -------------------------------------------------------------------

        // SALVAR
        btnSalvar.setOnClickListener(v -> {
            int hora = timePicker.getHour();
            int minuto = timePicker.getMinute();
            String novoHorario = String.format(Locale.getDefault(), "%02d:%02d", hora, minuto);

            RefeicaoDAO rDao = AppDatabase.getInstance(context).RefeicaoDAO();
            DietaDAO dDao = AppDatabase.getInstance(context).dietaDAO();

            refeicao.setHorario(novoHorario);
            rDao.atualizarRefeicao(refeicao);

            List<Refeicao> listaOriginal = rDao.getRefeicoesPorDia(diaOriginal);
            atualizarDietaDadoRefeicoes(dDao, diaOriginal, listaOriginal);

            for (Integer diaAlvo : new HashSet<>(diasSelecionados)) {
                if (diaAlvo == diaOriginal) continue;

                List<Refeicao> refeicoesAlvo = rDao.getRefeicoesPorDia(diaAlvo);
                boolean achou = false;

                if (refeicoesAlvo != null) {
                    for (Refeicao r : refeicoesAlvo) {
                        if (r.getNome() != null && r.getNome().equalsIgnoreCase(refeicao.getNome())) {
                            r.setHorario(novoHorario);
                            rDao.atualizarRefeicao(r);
                            achou = true;
                            break;
                        }
                    }
                }

                if (!achou) {
                    Refeicao nova = new Refeicao(diaAlvo, refeicao.getNome(), novoHorario, refeicao.getDescricao());
                    rDao.inserirRefeicao(nova);
                }

                List<Refeicao> atualizadas = rDao.getRefeicoesPorDia(diaAlvo);
                atualizarDietaDadoRefeicoes(dDao, diaAlvo, atualizadas);
            }

            dialog.dismiss();
            if (callback != null) callback.aoSalvar();
        });

        dialog.show();
    }

    // --------------- FUNÇÕES AUXILIARES -------------------

    private static List<DiaItem> gerarDiasDoMes(int mes, int ano, int diaHoje) {
        List<DiaItem> dias = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, ano);
        cal.set(Calendar.MONTH, mes);

        int totalDias = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= totalDias; i++) {
            cal.set(Calendar.DAY_OF_MONTH, i);
            int diaSemana = cal.get(Calendar.DAY_OF_WEEK);
            boolean isHoje = (i == diaHoje);
            dias.add(new DiaItem(i, getNomeDiaCurto(diaSemana), isHoje));
        }
        return dias;
    }

    private static List<DiaItem> getSubListaSemana(List<DiaItem> todos, int semana) {
        int inicio = (semana - 1) * 7;
        int fim = Math.min(inicio + 7, todos.size());
        return new ArrayList<>(todos.subList(inicio, fim));
    }

    private static void irParaSemana(int semanaAtual, List<DiaItem> diasDoMes,
                                     DiaAdapter diaAdapter, RecyclerView recyclerDias,
                                     TextView textoMesSemana, int mesAtual) {

        List<DiaItem> novaLista = getSubListaSemana(diasDoMes, semanaAtual);
        diaAdapter.atualizarLista(novaLista);
        atualizarTextoMesSemana(textoMesSemana, mesAtual, semanaAtual);
    }

    private static void atualizarTextoMesSemana(TextView texto, int mes, int semana) {
        texto.setText(getNomeMes(mes) + " - Semana " + semana);
    }

    private static String getNomeMes(int mes) {
        String[] nomes = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};
        return nomes[mes];
    }

    private static String getNomeDiaCurto(int diaSemana) {
        switch (diaSemana) {
            case Calendar.SUNDAY: return "Dom";
            case Calendar.MONDAY: return "Seg";
            case Calendar.TUESDAY: return "Ter";
            case Calendar.WEDNESDAY: return "Qua";
            case Calendar.THURSDAY: return "Qui";
            case Calendar.FRIDAY: return "Sex";
            case Calendar.SATURDAY: return "Sáb";
            default: return "";
        }
    }

    private static void atualizarDietaDadoRefeicoes(DietaDAO dao, int dia, List<Refeicao> lista) {
        String cafe = "", almoco = "", jantar = "";

        if (lista != null) {
            for (Refeicao r : lista) {
                String tipo = (r.getNome() == null) ? "" : r.getNome().toLowerCase();
                String entry = r.getNome() + " (" + r.getHorario() + ")";
                if (tipo.contains("café") || tipo.contains("cafe") || tipo.contains("manhã") || tipo.contains("manha")) {
                    cafe = append(cafe, entry);
                } else if (tipo.contains("almoço") || tipo.contains("almoco")) {
                    almoco = append(almoco, entry);
                } else if (tipo.contains("jantar")) {
                    jantar = append(jantar, entry);
                } else {
                    if (jantar.isEmpty()) jantar = append(jantar, entry);
                    else if (almoco.isEmpty()) almoco = append(almoco, entry);
                    else if (cafe.isEmpty()) cafe = append(cafe, entry);
                    else jantar = append(jantar, entry);
                }
            }
        }
        Dieta d = new Dieta(dia, cafe, almoco, jantar);
        dao.salvarDieta(d);
    }

    private static String append(String base, String entry) {
        return base == null || base.isEmpty() ? entry : base + "; " + entry;
    }
}

//package com.example.projetoengenhariadesoftwareii;
//
//import android.app.Dialog;
//import android.content.Context;
//import android.widget.Button;
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
