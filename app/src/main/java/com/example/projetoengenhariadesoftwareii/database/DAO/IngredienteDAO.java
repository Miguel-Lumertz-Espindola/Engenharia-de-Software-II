package com.example.projetoengenhariadesoftwareii.database.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.projetoengenhariadesoftwareii.database.model.Ingrediente;

import java.util.List;

@Dao
public interface IngredienteDAO {
    @Insert
    void inserirIngrediente(Ingrediente ingrediente);

    @Query("SELECT * FROM ingredientes ORDER BY nome ASC")
    List<Ingrediente> listarTodos();

    @Query("DELETE FROM ingredientes")
    void limparTabela();
}
