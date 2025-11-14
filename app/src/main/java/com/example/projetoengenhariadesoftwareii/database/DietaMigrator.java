package com.example.projetoengenhariadesoftwareii.database;

import com.example.projetoengenhariadesoftwareii.database.AppDatabase;
import com.example.projetoengenhariadesoftwareii.database.DAO.RefeicaoDAO;
import com.example.projetoengenhariadesoftwareii.database.model.Dieta;
import com.example.projetoengenhariadesoftwareii.database.model.Refeicao;
import android.content.Context;

public class DietaMigrator {

    // cria 3 refeições a partir da string da Dieta (se ainda não existir)
    public static void migrateIfNeeded(Context context, int dia) {
        AppDatabase db = AppDatabase.getInstance(context);
        RefeicaoDAO rDao = db.refeicaoDAO();
        if (!rDao.getByDia(dia).isEmpty()) return; // já existe

        Dieta d = db.dietaDAO().getDietaPorDia(dia);
        if (d == null) return;

        // transformar cafés/almoco/jantar em Refeicao simples
        Refeicao r1 = new Refeicao(dia, "Café da Manhã", null, null);
        Refeicao r2 = new Refeicao(dia, "Almoço", null, null);
        Refeicao r3 = new Refeicao(dia, "Jantar", null, null);

        long id1 = rDao.insert(r1);
        long id2 = rDao.insert(r2);
        long id3 = rDao.insert(r3);

        // OBS: aqui não criamos RefeicaoIngrediente automaticamente.
        // O conteúdo textual antigo permanece em Dieta.js e pode servir de fallback.
    }
}

