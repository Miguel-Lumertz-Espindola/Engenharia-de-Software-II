package com.example.projetoengenhariadesoftwareii.database.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "refeicoes")
public class Refeicao {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int dia;       // dia do mês (mesmo usado em Dieta.dia)
    private int idUsuario;
    private String nome;   // ex: "Café da Manhã", "Almoço", "Ceia"
    private String horario;// "HH:mm"
    private String descricao; // opcional (mantive para compatibilidade futura)

    public Refeicao(int dia, int idUsuario, String nome, String horario, String descricao) {
        this.dia = dia;
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.horario = horario;
        this.descricao = descricao;
    }

    public int getId() { return id; }
    public int getDia() { return dia; }
    public int getIdUsuario() { return idUsuario; }
    public String getNome() { return nome; }
    public String getHorario() { return horario; }
    public String getDescricao() { return descricao; }

    public void setId(int id) { this.id = id; }
    public void setDia(int dia) { this.dia = dia; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public void setNome(String nome) { this.nome = nome; }
    public void setHorario(String horario) { this.horario = horario; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
