package com.example.projetoengenhariadesoftwareii.database.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ingredientes")
public class Ingrediente {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String nome;
    private String descricao;
    private String unidade; // ex: "gramas", "ml", "unidades"

    public Ingrediente(String nome, String descricao, String unidade) {
        this.nome = nome;
        this.descricao = descricao;
        this.unidade = unidade;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public String getUnidade() { return unidade; }

    public void setId(int id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setUnidade(String unidade) { this.unidade = unidade; }
}
