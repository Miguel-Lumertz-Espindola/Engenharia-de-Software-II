package com.example.projetoengenhariadesoftwareii.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.projetoengenhariadesoftwareii.database.DAO.DietaDAO;
import com.example.projetoengenhariadesoftwareii.database.DAO.DietaPreProntaDAO;
import com.example.projetoengenhariadesoftwareii.database.DAO.IngredienteDAO;
import com.example.projetoengenhariadesoftwareii.database.DAO.RefeicaoDAO;
import com.example.projetoengenhariadesoftwareii.database.DAO.UsuarioDao;
import com.example.projetoengenhariadesoftwareii.database.model.Dieta;
import com.example.projetoengenhariadesoftwareii.database.model.DietaPreProntaModel;
import com.example.projetoengenhariadesoftwareii.database.model.Ingrediente;
import com.example.projetoengenhariadesoftwareii.database.model.Refeicao;
import com.example.projetoengenhariadesoftwareii.database.model.UsuarioModel;

@Database(entities = {Dieta.class, UsuarioModel.class, DietaPreProntaModel.class, Ingrediente.class, Refeicao.class}, version = 27, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instancia;

    // --- DAOs ---
    public abstract DietaDAO dietaDAO();
    public abstract UsuarioDao usuarioDao();
    public abstract DietaPreProntaDAO dietaPreProntaDAO();
    public abstract IngredienteDAO ingredienteDAO(); // ✅ corrigido
    public abstract RefeicaoDAO RefeicaoDAO();

    // --- Singleton ---
    public static synchronized AppDatabase getInstance(Context context) {
        if (instancia == null) {
            instancia = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "app_database"
                    )
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instancia;
    }
}