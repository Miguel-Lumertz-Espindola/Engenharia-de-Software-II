package com.example.projetoengenhariadesoftwareii.database.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.projetoengenhariadesoftwareii.database.model.RefeicaoIngrediente;

import java.util.List;

@Dao
public interface RefeicaoIngredienteDAO {

    @Insert
    long insert(RefeicaoIngrediente ri);

    @Update
    void update(RefeicaoIngrediente ri);

    @Delete
    void delete(RefeicaoIngrediente ri);

    @Query("SELECT * FROM refeicao_ingredientes WHERE refeicaoId = :refeicaoId")
    List<RefeicaoIngrediente> getByRefeicao(int refeicaoId);

    @Query("DELETE FROM refeicao_ingredientes WHERE refeicaoId = :refeicaoId")
    void deleteByRefeicao(int refeicaoId);
}

