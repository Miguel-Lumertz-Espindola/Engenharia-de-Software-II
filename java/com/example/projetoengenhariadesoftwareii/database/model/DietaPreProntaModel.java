package com.example.projetoengenhariadesoftwareii.database.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "dietas_pre_prontas")
public class DietaPreProntaModel {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String nomeDieta;
    private String descricao;
    private String cafeManha;
    private String almoco;
    private String jantar;

    public DietaPreProntaModel(String nomeDieta, String descricao, String cafeManha, String almoco, String jantar) {
        this.nomeDieta = nomeDieta;
        this.descricao = descricao;
        this.cafeManha = cafeManha;
        this.almoco = almoco;
        this.jantar = jantar;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNomeDieta() { return nomeDieta; }
    public void setNomeDieta(String nomeDieta) { this.nomeDieta = nomeDieta; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getCafeManha() { return cafeManha; }
    public void setCafeManha(String cafeManha) { this.cafeManha = cafeManha; }

    public String getAlmoco() { return almoco; }
    public void setAlmoco(String almoco) { this.almoco = almoco; }

    public String getJantar() { return jantar; }
    public void setJantar(String jantar) { this.jantar = jantar; }
}
