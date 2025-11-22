package com.example.projetoengenhariadesoftwareii.database.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "refeicoes")
public class Refeicao {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int dia;       // dia do mês (mesmo usado em Dieta.dia)
    private String nome;   // ex: "Café da Manhã", "Almoço", "Ceia"
    private String horario;// "HH:mm"
    private String descricao; // opcional (mantive para compatibilidade futura)

    public Refeicao(int dia, String nome, String horario, String descricao) {
        this.dia = dia;
        this.nome = nome;
        this.horario = horario;
        this.descricao = descricao;
    }

    public int getId() { return id; }
    public int getDia() { return dia; }
    public String getNome() { return nome; }
    public String getHorario() { return horario; }
    public String getDescricao() { return descricao; }

    public void setId(int id) { this.id = id; }
    public void setDia(int dia) { this.dia = dia; }
    public void setNome(String nome) { this.nome = nome; }
    public void setHorario(String horario) { this.horario = horario; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}

//package com.example.projetoengenhariadesoftwareii.database.model;
//
//import androidx.room.Entity;
//import androidx.room.PrimaryKey;
//
//@Entity(tableName = "refeicoes")
//public class Refeicao {
//    @PrimaryKey(autoGenerate = true)
//    private int id;
//
//    private int dia;           // dia ao qual a refeição pertence
//    private String tipo;       // "CafeManha", "Almoco", "Jantar" (ou qualquer label)
//    private String nome;       // nome da refeição
//    private String horario;    // horário como "08:30"
//
//    public Refeicao(int dia, String tipo, String nome, String horario) {
//        this.dia = dia;
//        this.tipo = tipo;
//        this.nome = nome;
//        this.horario = horario;
//    }
//
//    public int getId() { return id; }
//    public int getDia() { return dia; }
//    public String getTipo() { return tipo; }
//    public String getNome() { return nome; }
//    public String getHorario() { return horario; }
//
//    public void setId(int id) { this.id = id; }
//    public void setDia(int dia) { this.dia = dia; }
//    public void setTipo(String tipo) { this.tipo = tipo; }
//    public void setNome(String nome) { this.nome = nome; }
//    public void setHorario(String horario) { this.horario = horario; }
//}
