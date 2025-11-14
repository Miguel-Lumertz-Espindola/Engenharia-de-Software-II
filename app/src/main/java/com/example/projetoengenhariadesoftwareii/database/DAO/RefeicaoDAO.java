package com.example.projetoengenhariadesoftwareii.database.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.projetoengenhariadesoftwareii.database.model.Refeicao;

import java.util.List;

@Dao
public interface RefeicaoDAO {

    @Insert
    long insert(Refeicao r);

    @Update
    void update(Refeicao r);

    @Delete
    void delete(Refeicao r);

    @Query("SELECT * FROM refeicoes WHERE dia = :dia ORDER BY id ASC")
    List<Refeicao> getByDia(int dia);

    @Query("DELETE FROM refeicoes WHERE dia = :dia")
    void deleteByDia(int dia);
}
