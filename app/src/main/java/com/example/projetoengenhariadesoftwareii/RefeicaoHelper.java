package com.example.projetoengenhariadesoftwareii;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.projetoengenhariadesoftwareii.database.AppDatabase;
import com.example.projetoengenhariadesoftwareii.database.DAO.DietaDAO;
import com.example.projetoengenhariadesoftwareii.database.Dieta;

public class RefeicaoHelper {

    public static void mostrarRefeicoesSimples(Context context, ViewGroup container, int dia) {
        container.removeAllViews();

        DietaDAO dao = AppDatabase.getInstance(context).dietaDAO();
        Dieta dieta = dao.getDietaPorDia(dia);

        String cafe = dieta != null ? dieta.getCafeManha() : "Frutas, ovos e café preto.";
        String almoco = dieta != null ? dieta.getAlmoco() : "Arroz, feijão, frango e salada.";
        String jantar = dieta != null ? dieta.getJantar() : "Sopa de legumes e suco natural.";

        String[] refeicoes = {"Café da manhã", "Almoço", "Jantar"};
        String[] descricoes = {cafe, almoco, jantar};

        for (int i = 0; i < refeicoes.length; i++) {
            LinearLayout card = new LinearLayout(context);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(20, 20, 20, 20);
            card.setBackgroundResource(R.drawable.bg_card_refeicao);
            card.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            TextView titulo = new TextView(context);
            titulo.setText(refeicoes[i]);
            titulo.setTextSize(18);
            titulo.setTextColor(context.getColor(R.color.azulPetroleo));

            TextView desc = new TextView(context);
            desc.setText(descricoes[i]);
            desc.setTextSize(15);
            desc.setTextColor(context.getColor(android.R.color.black));

            card.addView(titulo);
            card.addView(desc);
            container.addView(card);
        }
    }


//    public static void mostrarRefeicoesSimples(Context context, ViewGroup container, int dia) {
//        container.removeAllViews();
//
//        String[] refeicoes = {"Café da manhã", "Almoço", "Jantar"};
//        String[] descricoes = {
//                "Frutas, ovos e café preto.",
//                "Arroz, feijão, frango e salada.",
//                "Sopa de legumes e suco natural."
//        };
//
//        for (int i = 0; i < refeicoes.length; i++) {
//            LinearLayout card = new LinearLayout(context);
//            card.setOrientation(LinearLayout.VERTICAL);
//            card.setPadding(20, 20, 20, 20);
//            card.setBackgroundResource(R.drawable.bg_card_refeicao);
//            card.setLayoutParams(new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT
//            ));
//
//            TextView titulo = new TextView(context);
//            titulo.setText(refeicoes[i]);
//            titulo.setTextSize(18);
//            titulo.setTextColor(context.getColor(R.color.azulPetroleo));
//
//            TextView desc = new TextView(context);
//            desc.setText(descricoes[i]);
//            desc.setTextSize(15);
//            desc.setTextColor(context.getColor(android.R.color.black));
//
//            card.addView(titulo);
//            card.addView(desc);
//            container.addView(card);
//        }
//    }
}


