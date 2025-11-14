package com.example.projetoengenhariadesoftwareii.database.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "refeicoes")
public class Refeicao {

    @PrimaryKey(autoGenerate = true)
    private int id;

    // ligações
    private int dia;       // dia do mês (1..31) - escolhi dia em vez de dietaId para simplicidade
    private String nome;   // "Café da Manhã", "Almoço", "Café Tarde", "Jantar"
    private String horario; // "08:00" ou null
    private String observacao; // campo livre (opcional)

    // getters/setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getDia() { return dia; }
    public void setDia(int dia) { this.dia = dia; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    // construtores
    public Refeicao() {}

    public Refeicao(int dia, String nome, String horario, String observacao) {
        this.dia = dia;
        this.nome = nome;
        this.horario = horario;
        this.observacao = observacao;
    }
}
