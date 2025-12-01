package com.example.projetoengenhariadesoftwareii.database.model;

public class Pergunta {
    private int id;
    private String descricao;
    private int tipoPergunta; // 1=obj, 2=freq, 3=praticidade, 4=rigor

    // getters e setters
    public int getId() { return id; }
    public String getDescricao() { return descricao; }
    public int getTipoPergunta() { return tipoPergunta; }

    public void setId(int id) { this.id = id; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setTipoPergunta(int tipoPergunta) { this.tipoPergunta = tipoPergunta; }
}

