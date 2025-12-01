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
import com.example.projetoengenhariadesoftwareii.database.model.UsuarioModel;

import java.util.ArrayList;
import java.util.List;


public class RefeicaoHelper {

    public static void mostrarRefeicoesSimples(Context context, ViewGroup container, int dia, int idUsuario) {
        container.removeAllViews();

        AppDatabase db = AppDatabase.getInstance(context);
        RefeicaoDAO refeicaoDAO = db.RefeicaoDAO();
        DietaDAO dietaDAO = db.dietaDAO();

        // 🔹 1. BUSCA REFEIÇÕES CRIADAS / EDITADAS
        //List<Refeicao> refeicoes = refeicaoDAO.getRefeicoesPorDia(dia, (int) usuarioLogado.getId());
        List<Refeicao> refeicoes = refeicaoDAO.buscarPorDiaEUsuario(dia, idUsuario);

        if (refeicoes == null) refeicoes = new ArrayList<>();
        Dieta dieta = dietaDAO.getDietaPorDia(dia, idUsuario); // usa idUsuario

       // RefeicaoDAO refeicaoDAO = AppDatabase.getInstance(context).RefeicaoDAO();


        if (container instanceof LinearLayout) {
            ((LinearLayout) container).setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            ((LinearLayout) container).setDividerDrawable(
                    ContextCompat.getDrawable(context, R.drawable.bg_divider_refeicao)
            );
        }

        // agora mesclar sem duplicar
        if (dieta != null) {
            String[][] pares = {
                    {"Café da Manhã", dieta.getCafeManha(), "08:00"},
                    {"Almoço", dieta.getAlmoco(), "12:00"},
                    {"Café da Tarde", dieta.getCafeTarde(), "15:00"},
                    {"Jantar", dieta.getJantar(), "19:00"}
            };

            for (String[] p : pares) {
                String nomeBase = p[0];
                String conteudo = p[1];
                String horarioPadrao = p[2];

                if (conteudo == null || conteudo.trim().isEmpty()) continue;

                boolean encontrou = false;
                Refeicao existente = null;

                for (Refeicao rr : refeicoes) {
                    String nomeLower = rr.getNome() == null ? "" : rr.getNome().toLowerCase();
                    if (nomeLower.contains(nomeBase.toLowerCase())) {
                        existente = rr;
                        encontrou = true;
                        break;
                    }
                }

                if (encontrou) {
                    // ATUALIZAR no BANCO SE NECESSÁRIO
                    if (existente.getDescricao() == null || existente.getDescricao().isEmpty()) {
                        existente.setDescricao(conteudo);
                    }
                    if (existente.getHorario() == null || existente.getHorario().isEmpty()) {
                        existente.setHorario(horarioPadrao);
                    }
                    refeicaoDAO.atualizarRefeicao(existente); // <-- AGORA CORRETO
                } else {
                    // INSERIR NOVO CORRETAMENTE NO BANCO
                    Refeicao nova = new Refeicao(dia, idUsuario, nomeBase, horarioPadrao, conteudo);
                    refeicaoDAO.inserirRefeicao(nova);
                    refeicoes.add(nova);
                }
            }
        }


        // 🔹 SE NÃO TIVER NENHUMA REFEIÇÃO NEM DIETA – MOSTRA PADRÃO
        if ((refeicoes == null || refeicoes.isEmpty()) && dieta == null) {
            refeicoes = new ArrayList<>();
            refeicoes.add(new Refeicao(dia, idUsuario,"Café da Manhã", "08:00", "Frutas, ovos e café preto."));
            refeicoes.add(new Refeicao(dia, idUsuario,"Almoço", "12:00", "Arroz, feijão, frango e salada."));
            refeicoes.add(new Refeicao(dia, idUsuario,"Café da Tarde", "15:00", "Arroz, feijão, frango e salada."));
            refeicoes.add(new Refeicao(dia, idUsuario,"Jantar", "19:00", "Sopa de legumes e suco natural."));
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