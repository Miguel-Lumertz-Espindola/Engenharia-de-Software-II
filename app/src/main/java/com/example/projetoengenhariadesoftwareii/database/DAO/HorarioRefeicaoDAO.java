package com.example.projetoengenhariadesoftwareii.database.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.projetoengenhariadesoftwareii.database.model.HorarioRefeicao;

@Dao
public interface HorarioRefeicaoDAO {

    @Query("SELECT * FROM horarios_refeicoes WHERE dia = :dia AND refeicao = :refeicao LIMIT 1")
    HorarioRefeicao getHorario(int dia, String refeicao);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void salvar(HorarioRefeicao horario);

    @Query("DELETE FROM horarios_refeicoes WHERE dia = :dia AND refeicao = :refeicao")
    void apagar(int dia, String refeicao);
}
