package com.example.projetoengenhariadesoftwareii.database.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.projetoengenhariadesoftwareii.database.model.Dieta;

@Dao
public interface DietaDAO {
    @Query("SELECT * FROM dietas WHERE dia = :dia LIMIT 1")
    Dieta getDietaPorDia(int dia);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void salvarDieta(Dieta dieta);

    @Delete
    void excluirDieta(Dieta dieta);
}
