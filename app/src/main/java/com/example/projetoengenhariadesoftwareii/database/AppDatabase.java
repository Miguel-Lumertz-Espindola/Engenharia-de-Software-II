package com.example.projetoengenhariadesoftwareii.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.projetoengenhariadesoftwareii.database.DAO.DietaDAO;
import com.example.projetoengenhariadesoftwareii.database.DAO.DietaPreProntaDAO;
import com.example.projetoengenhariadesoftwareii.database.DAO.HorarioRefeicaoDAO;
import com.example.projetoengenhariadesoftwareii.database.DAO.IngredienteDAO;
import com.example.projetoengenhariadesoftwareii.database.DAO.RefeicaoDAO;
import com.example.projetoengenhariadesoftwareii.database.DAO.RefeicaoIngredienteDAO;
import com.example.projetoengenhariadesoftwareii.database.DAO.UsuarioDao;
import com.example.projetoengenhariadesoftwareii.database.model.Dieta;
import com.example.projetoengenhariadesoftwareii.database.model.DietaPreProntaModel;
import com.example.projetoengenhariadesoftwareii.database.model.HorarioRefeicao;
import com.example.projetoengenhariadesoftwareii.database.model.Ingrediente;
import com.example.projetoengenhariadesoftwareii.database.model.Refeicao;
import com.example.projetoengenhariadesoftwareii.database.model.RefeicaoIngrediente;
import com.example.projetoengenhariadesoftwareii.database.model.UsuarioModel;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Dieta.class, UsuarioModel.class, DietaPreProntaModel.class, Ingrediente.class, Refeicao.class, RefeicaoIngrediente.class, HorarioRefeicao.class}, version = 10, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instancia;

    // --- DAOs ---
    public abstract DietaDAO dietaDAO();
    public abstract UsuarioDao usuarioDao();
    public abstract DietaPreProntaDAO dietaPreProntaDAO();
    public abstract IngredienteDAO ingredienteDAO();
    public abstract RefeicaoDAO refeicaoDAO();
    public abstract RefeicaoIngredienteDAO refeicaoIngredienteDAO();
    public abstract HorarioRefeicaoDAO horarioRefeicaoDAO();


    // --- Singleton ---
    public static synchronized AppDatabase getInstance(Context context) {
        if (instancia == null) {
            instancia = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "app_database"
                    )
                    .allowMainThreadQueries()
                    .addMigrations(MIGRATION_9_10)
                    .build();
        }
        return instancia;
    }

    public static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // cria tabela refeicoes
            database.execSQL("CREATE TABLE IF NOT EXISTS `refeicoes` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`dia` INTEGER NOT NULL, " +
                    "`nome` TEXT, " +
                    "`horario` TEXT, " +
                    "`observacao` TEXT)");
            // cria tabela refeicao_ingredientes
            database.execSQL("CREATE TABLE IF NOT EXISTS `refeicao_ingredientes` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`refeicaoId` INTEGER NOT NULL, " +
                    "`ingredienteId` INTEGER NOT NULL, " +
                    "`quantidade` REAL, " +
                    "`unidade` TEXT, " +
                    "`nomeCustom` TEXT)");
        }
    };

}