package com.example.projetoengenhariadesoftwareii;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.projetoengenhariadesoftwareii.database.AppDatabase;
import com.example.projetoengenhariadesoftwareii.database.DAO.DietaDAO;
import com.example.projetoengenhariadesoftwareii.database.model.Dieta;

public class RefeicaoHelper {

    public static void mostrarRefeicoesSimples(Context context, ViewGroup container, int dia) {
        container.removeAllViews();
        if (container instanceof LinearLayout) {
            ((LinearLayout) container).setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            ((LinearLayout) container).setDividerDrawable(ContextCompat.getDrawable(context, R.drawable.bg_divider_refeicao));
        }

        DietaDAO dao = AppDatabase.getInstance(context).dietaDAO();
        Dieta dieta = dao.getDietaPorDia(dia);

        String cafe_manha = dieta != null ? dieta.getCafeManha() : "Frutas, ovos e café preto.";
        String almoco = dieta != null ? dieta.getAlmoco() : "Arroz, feijão, frango e salada.";
        //String cafe_tarde = dieta != null ? dieta.getJantar() : "Biscoito";
        String jantar = dieta != null ? dieta.getJantar() : "Sopa de legumes e suco natural.";

        String[] refeicoes = {"Café da Manhã", "Almoço", "Jantar"};
        String[] descricoes = {cafe_manha, almoco, jantar};

        for (int i = 0; i < refeicoes.length; i++) {
            LinearLayout card = new LinearLayout(context);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(30, 30, 30, 30);
            card.setBackgroundResource(R.drawable.bg_card_refeicao);
            card.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            card.setDividerDrawable(ContextCompat.getDrawable(context, R.drawable.bg_dividercard_refeicao));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            int margin = (int) (10 * context.getResources().getDisplayMetrics().density);
            params.setMargins(0, 25, 0, margin);
            card.setLayoutParams(params);

            TextView titulo = new TextView(context);
            titulo.setText(refeicoes[i]);
            titulo.setTextSize(18);
            titulo.setPadding(0, 0, 0, 3);
            titulo.setTextColor(context.getColor(R.color.black));
            titulo.setLayoutParams(params);


            TextView desc = new TextView(context);
            desc.setText(descricoes[i]);
            desc.setTextSize(15);
            desc.setTextColor(context.getColor(android.R.color.white));
            desc.setLayoutParams(params);

            card.addView(titulo);
            card.addView(desc);
            container.addView(card);
        }
    }
}