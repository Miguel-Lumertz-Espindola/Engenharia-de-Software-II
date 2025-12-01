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
    // 🔹 PEGAR DIETAS DE UM USUÁRIO POR DIA
    @Query("SELECT * FROM dietas WHERE dia = :dia AND idUsuario = :idUsuario LIMIT 1")
    Dieta getDietaPorDia(int dia, int idUsuario);

    // 🔹 INSERIR DIETA PRÉ-PRONTA NO DIA
    @Query("INSERT INTO dietas (dia, idUsuario, idDietaPrePronta) VALUES (:dia, :idUsuario, :idDietaPrePronta)")
    void inserirNoDia(int dia, int idUsuario, int idDietaPrePronta);
    @Query("SELECT * FROM dietas WHERE dia = :dia LIMIT 1")
    Dieta getDietaPorDia(int dia);

    @Insert(onConflict = OnConflictStrategy.IGNORE) // NÃO SOBRESCREVE MAIS
    void inserirDieta(Dieta dieta);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void salvarDieta(Dieta dieta);

    @Update
    void atualizarDieta(Dieta dieta);

    @Delete
    void excluirDieta(Dieta dieta);
}