package com.example.projetoengenhariadesoftwareii.database.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.projetoengenhariadesoftwareii.database.model.DietaPreProntaModel;

import java.util.List;

@Dao
public interface DietaPreProntaDAO {

    @Query("SELECT * FROM dietas_pre_prontas")
    List<DietaPreProntaModel> getTodas();

    @Query("SELECT * FROM dietas_pre_prontas WHERE id = :id LIMIT 1")
    DietaPreProntaModel getPorId(int id);

    @Query("SELECT * FROM dietas_pre_prontas WHERE nomeDieta LIKE :nome LIMIT 1")
    DietaPreProntaModel getPorNome(String nome);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void inserir(DietaPreProntaModel dieta);

    @Update
    void atualizar(DietaPreProntaModel dieta);

    @Delete
    void excluir(DietaPreProntaModel dieta);

    @Query("DELETE FROM dietas_pre_prontas")
    void excluirTodas();
}
