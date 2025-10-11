package com.example.projetoengenhariadesoftwareii.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.projetoengenhariadesoftwareii.database.DAO.DietaDAO;

@Database(entities = {Dieta.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instancia;

    public abstract DietaDAO dietaDAO();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instancia == null) {
            instancia = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "app_database")
                    .allowMainThreadQueries()
                    .build();
        }
        return instancia;
    }
}
