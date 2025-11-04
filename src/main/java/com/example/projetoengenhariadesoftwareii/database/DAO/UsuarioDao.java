package com.example.projetoengenhariadesoftwareii.database.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.projetoengenhariadesoftwareii.database.model.UsuarioModel;

import java.util.List;

@Dao
public interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long salvarUsuario(UsuarioModel usuario);

    @Update
    void atualizarUsuario(UsuarioModel usuario);

    @Delete
    void excluirUsuario(UsuarioModel usuario);

    // Corrigido: nome da tabela deve ser o mesmo de UsuarioModel
    @Query("SELECT * FROM tb_usuario")
    List<UsuarioModel> listarUsuarios();

    @Query("SELECT * FROM tb_usuario WHERE email = :email AND senha = :senha LIMIT 1")
    UsuarioModel autenticarUsuario(String email, String senha);
}
