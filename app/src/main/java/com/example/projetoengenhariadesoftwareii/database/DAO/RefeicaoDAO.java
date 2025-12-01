package com.example.projetoengenhariadesoftwareii.database.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.projetoengenhariadesoftwareii.database.model.Refeicao;

import java.util.List;

@Dao
public interface RefeicaoDAO {
    @Query("SELECT * FROM refeicoes WHERE dia = :dia AND idUsuario = :idUsuario ORDER BY horario ASC")
    List<Refeicao> getRefeicoesPorDia(int dia, int idUsuario);
    @Query("SELECT * FROM refeicoes WHERE dia = :dia AND idUsuario = :idUsuario AND nome = :nome LIMIT 1")
    Refeicao getRefeicaoPorDiaENome(int dia, int idUsuario, String nome);
    @Query("SELECT * FROM refeicoes WHERE dia = :dia AND idUsuario = :idUsuario")
    List<Refeicao> buscarPorDiaEUsuario(int dia, int idUsuario);


    @Insert
    long inserirRefeicao(Refeicao refeicao);

    @Update
    void atualizarRefeicao(Refeicao refeicao);

    @Delete
    void excluirRefeicao(Refeicao refeicao);

    @Query("SELECT COUNT(*) FROM refeicoes WHERE dia = :dia")
    int contarPorDia(int dia);

    @Query("DELETE FROM refeicoes WHERE dia = :dia")
    void excluirPorDia(int dia);

//    @Query("SELECT * FROM refeicoes WHERE dia = :dia AND nome = :nome LIMIT 1")
//    Refeicao getRefeicaoPorDiaENome(int dia, int idUsuario, String nome);

    @Query("UPDATE refeicoes SET horario = :horario, descricao = :descricao WHERE id = :id")
    void atualizarHorarioEDescricaoPorId(int id, String horario, String descricao);
}

//package com.example.projetoengenhariadesoftwareii.database.DAO;
//
//import androidx.room.Dao;
//import androidx.room.Delete;
//import androidx.room.Insert;
//import androidx.room.Query;
//import androidx.room.Update;
//
//import com.example.projetoengenhariadesoftwareii.database.model.Refeicao;
//
//import java.util.List;
//
//@Dao
//public interface RefeicaoDAO {
//    @Query("SELECT * FROM refeicoes WHERE dia = :dia ORDER BY tipo ASC, horario ASC")
//    List<Refeicao> getRefeicoesPorDia(int dia);
//
//    @Insert
//    long inserirRefeicao(Refeicao refeicao);
//
//    @Update
//    void atualizarRefeicao(Refeicao refeicao);
//
//    @Delete
//    void excluirRefeicao(Refeicao refeicao);
//
//    @Query("DELETE FROM refeicoes WHERE dia = :dia")
//    void excluirPorDia(int dia);
//}
