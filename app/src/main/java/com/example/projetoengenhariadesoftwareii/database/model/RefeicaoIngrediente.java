package com.example.projetoengenhariadesoftwareii.database.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "refeicao_ingredientes")
public class RefeicaoIngrediente {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int refeicaoId;   // FK para Refeicao.id
    private int ingredienteId; // FK para Ingrediente.id (tabela existente)
    private double quantidade; // ex: 100.0
    private String unidade;    // ex: "gramas", "ml", "unidades"
    private String nomeCustom; // opcional: caso queira salvar nome custom (substituições)

    public RefeicaoIngrediente() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRefeicaoId() { return refeicaoId; }
    public void setRefeicaoId(int refeicaoId) { this.refeicaoId = refeicaoId; }

    public int getIngredienteId() { return ingredienteId; }
    public void setIngredienteId(int ingredienteId) { this.ingredienteId = ingredienteId; }

    public double getQuantidade() { return quantidade; }
    public void setQuantidade(double quantidade) { this.quantidade = quantidade; }

    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }

    public String getNomeCustom() { return nomeCustom; }
    public void setNomeCustom(String nomeCustom) { this.nomeCustom = nomeCustom; }
}
