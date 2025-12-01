package com.example.projetoengenhariadesoftwareii.database.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "horarios_refeicoes")
public class HorarioRefeicao {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int dia;            // 1..31
    private String refeicao;    // "cafe", "almoco", "jantar"
    private String horario;     // "07:30", "12:00" etc.

    public HorarioRefeicao(int dia, String refeicao, String horario) {
        this.dia = dia;
        this.refeicao = refeicao;
        this.horario = horario;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getDia() { return dia; }
    public String getRefeicao() { return refeicao; }
    public String getHorario() { return horario; }

    public void setHorario(String horario) { this.horario = horario; }
}
