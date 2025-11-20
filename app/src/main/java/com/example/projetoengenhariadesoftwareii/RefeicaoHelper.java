package com.example.projetoengenhariadesoftwareii;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.projetoengenhariadesoftwareii.database.AppDatabase;
import com.example.projetoengenhariadesoftwareii.database.DAO.DietaDAO;
import com.example.projetoengenhariadesoftwareii.database.DAO.RefeicaoDAO;
import com.example.projetoengenhariadesoftwareii.database.model.Dieta;
import com.example.projetoengenhariadesoftwareii.database.model.Refeicao;

import java.util.ArrayList;
import java.util.List;

public class RefeicaoHelper {

    public static void mostrarRefeicoesSimples(Context context, ViewGroup container, int dia) {
        container.removeAllViews();

        if (container instanceof LinearLayout) {
            ((LinearLayout) container).setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            ((LinearLayout) container).setDividerDrawable(
                    ContextCompat.getDrawable(context, R.drawable.bg_divider_refeicao)
            );
        }

        AppDatabase db = AppDatabase.getInstance(context);
        RefeicaoDAO refeicaoDAO = db.RefeicaoDAO();
        DietaDAO dietaDAO = db.dietaDAO();

        // 🔹 1. BUSCA REFEIÇÕES CRIADAS / EDITADAS
        List<Refeicao> refeicoes = refeicaoDAO.getRefeicoesPorDia(dia);

        // 🔹 2. BUSCA DIETAS (CriarDieta e DietasPreProntas)
        Dieta dieta = dietaDAO.getDietaPorDia(dia);

        // agora mesclar sem duplicar
        if (dieta != null) {
            // helper para normalizar nome
            String[][] pares = {
                    {"Café da Manhã", dieta.getCafeManha(), "08:00"},
                    {"Almoço", dieta.getAlmoco(), "12:00"},
                    {"Jantar", dieta.getJantar(), "19:00"}
            };

            for (String[] p : pares) {
                String nomeBase = p[0];
                String conteudo = p[1];
                String horarioPadrao = p[2];

                if (conteudo == null || conteudo.trim().isEmpty()) continue;

                // procurar por refeição existente com nome que contenha a raiz (ignora sufixos)
                boolean encontrou = false;
                for (Refeicao rr : refeicoes) {
                    String nomeLower = rr.getNome() == null ? "" : rr.getNome().toLowerCase();
                    if (nomeLower.contains(nomeBase.toLowerCase()) || nomeBase.toLowerCase().contains(nomeLower)) {
                        // atualiza descrição se estiver vazio ou for placeholder
                        if (rr.getDescricao() == null || rr.getDescricao().isEmpty() || rr.getDescricao().startsWith("Café") || rr.getDescricao().startsWith("Almoço")) {
                            rr.setDescricao(conteudo);
                        }
                        if (rr.getHorario() == null || rr.getHorario().isEmpty()) {
                            rr.setHorario(horarioPadrao);
                        }
                        encontrou = true;
                        break;
                    }
                }
                if (!encontrou) {
                    // adicionar como Refeição derivada da Dieta (apenas para exibição)
                    refeicoes.add(new Refeicao(dia, nomeBase, horarioPadrao, conteudo));
                }
            }
        }

        // 🔹 SE NÃO TIVER NENHUMA REFEIÇÃO NEM DIETA – MOSTRA PADRÃO
        if ((refeicoes == null || refeicoes.isEmpty()) && dieta == null) {
            refeicoes = new ArrayList<>();
            refeicoes.add(new Refeicao(dia, "Café da Manhã", "08:00", "Frutas, ovos e café preto."));
            refeicoes.add(new Refeicao(dia, "Almoço", "12:00", "Arroz, feijão, frango e salada."));
            refeicoes.add(new Refeicao(dia, "Jantar", "19:00", "Sopa de legumes e suco natural."));
        }

        // 🔹 EXIBE NA TELA
        for (Refeicao r : refeicoes) {

            LinearLayout card = new LinearLayout(context);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(30, 30, 30, 30);
            card.setBackgroundResource(R.drawable.bg_card_refeicao);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            int margin = (int) (10 * context.getResources().getDisplayMetrics().density);
            params.setMargins(0, 25, 0, margin);
            card.setLayoutParams(params);

            // 🔹 Linha com TÍTULO + HORÁRIO
            LinearLayout linha = new LinearLayout(context);
            linha.setOrientation(LinearLayout.HORIZONTAL);

            TextView titulo = new TextView(context);
            titulo.setText(r.getNome());
            titulo.setTextSize(18);
            titulo.setTextColor(context.getColor(R.color.black));
            titulo.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            ));

            TextView hora = new TextView(context);
            hora.setText(r.getHorario() == null ? "" : r.getHorario());
            hora.setTextSize(14);

            linha.addView(titulo);
            linha.addView(hora);

            // 🔹 DESCRIÇÃO
            TextView desc = new TextView(context);
            desc.setText(r.getDescricao() == null ? "" : r.getDescricao());
            desc.setTextSize(15);
            desc.setTextColor(context.getColor(android.R.color.white));
            desc.setLayoutParams(params);

            // Adiciona ao card
            card.addView(linha);
            card.addView(desc);
            container.addView(card);
        }
    }
}


//package com.example.projetoengenhariadesoftwareii;
//
//import android.content.Context;
//import android.view.ViewGroup;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import androidx.core.content.ContextCompat;
//
//import com.example.projetoengenhariadesoftwareii.database.AppDatabase;
//import com.example.projetoengenhariadesoftwareii.database.DAO.DietaDAO;
//import com.example.projetoengenhariadesoftwareii.database.DAO.RefeicaoDAO;
//import com.example.projetoengenhariadesoftwareii.database.model.Dieta;
//import com.example.projetoengenhariadesoftwareii.database.model.Refeicao;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class RefeicaoHelper {
//
//    public static void mostrarRefeicoesSimples(Context context, ViewGroup container, int dia) {
//        container.removeAllViews();
//        if (container instanceof LinearLayout) {
//            ((LinearLayout) container).setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
//            ((LinearLayout) container).setDividerDrawable(ContextCompat.getDrawable(context, R.drawable.bg_divider_refeicao));
//        }
//
//
//
//        RefeicaoDAO rDao = AppDatabase.getInstance(context).RefeicaoDAO();
//        List<Refeicao> refeicoes = rDao.getRefeicoesPorDia(dia);
//
//        if (refeicoes == null || refeicoes.isEmpty()) {
//            // fallback para manter compatibilidade: usa Dieta
//            DietaDAO dao = AppDatabase.getInstance(context).dietaDAO();
//            Dieta dieta = dao.getDietaPorDia(dia);
//            String cafe = dieta != null ? dieta.getCafeManha() : "Frutas, ovos e café preto.";
//            String almoco = dieta != null ? dieta.getAlmoco() : "Arroz, feijão, frango e salada.";
//            String jantar = dieta != null ? dieta.getJantar() : "Sopa de legumes e suco natural.";
//            refeicoes = new ArrayList<>();
//            refeicoes.add(new Refeicao(dia, "Café da Manhã", "08:00", cafe));
//            refeicoes.add(new Refeicao(dia, "Almoço", "12:00", almoco));
//            refeicoes.add(new Refeicao(dia, "Jantar", "19:00", jantar));
//        }
//
//        for (Refeicao r : refeicoes) {
//            LinearLayout card = new LinearLayout(context);
//            card.setOrientation(LinearLayout.VERTICAL);
//            card.setPadding(30, 30, 30, 30);
//            card.setBackgroundResource(R.drawable.bg_card_refeicao);
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT
//            );
//            int margin = (int) (10 * context.getResources().getDisplayMetrics().density);
//            params.setMargins(0, 25, 0, margin);
//            card.setLayoutParams(params);
//
//            // título com horário ao lado
//            LinearLayout linha = new LinearLayout(context);
//            linha.setOrientation(LinearLayout.HORIZONTAL);
//
//            TextView titulo = new TextView(context);
//            titulo.setText(r.getNome());
//            titulo.setTextSize(18);
//            titulo.setTextColor(context.getColor(R.color.black));
//            titulo.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
//
//            TextView hora = new TextView(context);
//            hora.setText(r.getHorario() == null ? "" : r.getHorario());
//            hora.setTextSize(14);
//            hora.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//
//            TextView desc = new TextView(context);
//            desc.setText(r.getDescricao() == null ? "" : r.getDescricao());
//            desc.setTextSize(15);
//            desc.setTextColor(context.getColor(android.R.color.white));
//            desc.setLayoutParams(params);
//
//            linha.addView(titulo);
//            linha.addView(hora);
//
//            card.addView(linha);
//            card.addView(desc);
//            container.addView(card);
//        }
//    }


//    public static void mostrarRefeicoesSimples(Context context, ViewGroup container, int dia) {
//        container.removeAllViews();
//        if (container instanceof LinearLayout) {
//            ((LinearLayout) container).setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
//            ((LinearLayout) container).setDividerDrawable(ContextCompat.getDrawable(context, R.drawable.bg_divider_refeicao));
//        }
//
//        DietaDAO dao = AppDatabase.getInstance(context).dietaDAO();
//        Dieta dieta = dao.getDietaPorDia(dia);
//
//        String cafe_manha = dieta != null ? dieta.getCafeManha() : "Frutas, ovos e café preto.";
//        String almoco = dieta != null ? dieta.getAlmoco() : "Arroz, feijão, frango e salada.";
//        //String cafe_tarde = dieta != null ? dieta.getJantar() : "Biscoito";
//        String jantar = dieta != null ? dieta.getJantar() : "Sopa de legumes e suco natural.";
//
//        String[] refeicoes = {"Café da Manhã", "Almoço", "Jantar"};
//        String[] descricoes = {cafe_manha, almoco, jantar};
//
//        for (int i = 0; i < refeicoes.length; i++) {
//            LinearLayout card = new LinearLayout(context);
//            card.setOrientation(LinearLayout.VERTICAL);
//            card.setPadding(30, 30, 30, 30);
//            card.setBackgroundResource(R.drawable.bg_card_refeicao);
//            card.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
//            card.setDividerDrawable(ContextCompat.getDrawable(context, R.drawable.bg_dividercard_refeicao));
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT
//            );
//            int margin = (int) (10 * context.getResources().getDisplayMetrics().density);
//            params.setMargins(0, 25, 0, margin);
//            card.setLayoutParams(params);
//
//            TextView titulo = new TextView(context);
//            titulo.setText(refeicoes[i]);
//            titulo.setTextSize(18);
//            titulo.setPadding(0, 0, 0, 3);
//            titulo.setTextColor(context.getColor(R.color.black));
//            titulo.setLayoutParams(params);
//
//
//            TextView desc = new TextView(context);
//
//            desc.setText(descricoes[i]);
//
//
//            desc.setTextSize(15);
//            desc.setTextColor(context.getColor(android.R.color.white));
//            desc.setLayoutParams(params);
//
//            card.addView(titulo);
//            card.addView(desc);
//            container.addView(card);
//        }
//
//    }
