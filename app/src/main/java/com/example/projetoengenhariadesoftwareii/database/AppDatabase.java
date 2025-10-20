package com.example.projetoengenhariadesoftwareii.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.projetoengenhariadesoftwareii.database.DAO.DietaDAO;
import com.example.projetoengenhariadesoftwareii.database.DAO.UsuarioDao;
import com.example.projetoengenhariadesoftwareii.database.model.UsuarioModel;

@Database(entities = {Dieta.class, UsuarioModel.class}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instancia;

    // --- DAOs ---
    public abstract DietaDAO dietaDAO();
    public abstract UsuarioDao usuarioDao();

    // --- Singleton ---
    public static synchronized AppDatabase getInstance(Context context) {
        if (instancia == null) {
            instancia = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "app_database"
                    )
                    .allowMainThreadQueries() // opcional (evite em produção)
                    .fallbackToDestructiveMigration() // evita crash em mudanças de schema
                    .build();
        }
        return instancia;
    }
}
