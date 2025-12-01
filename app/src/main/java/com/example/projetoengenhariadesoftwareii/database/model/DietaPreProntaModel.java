package com.example.projetoengenhariadesoftwareii.database.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "dietas_pre_prontas")
public class DietaPreProntaModel {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String nomeDieta;
    private String descricao;
    private String cafeManha;
    private String almoco;
    private String cafeTarde;
    private String jantar;
    // 🔥 NOVO: IDs das perguntas
    private int objetivoId;
    private int atividadeId;
    private int praticidadeId;
    private int rigorId;

    //private List<Integer> respostasCompativeis; // ids das respostas por pergunta

    public DietaPreProntaModel(String nomeDieta, String descricao, String cafeManha, String almoco, String cafeTarde, String jantar,  int objetivoId, int atividadeId, int praticidadeId, int rigorId) {
        this.nomeDieta = nomeDieta;
        this.descricao = descricao;
        this.cafeManha = cafeManha;
        this.almoco = almoco;
        this.cafeTarde = cafeTarde;
        this.jantar = jantar;
        this.objetivoId = objetivoId;
        this.atividadeId = atividadeId;
        this.praticidadeId = praticidadeId;
        this.rigorId = rigorId;
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

    public String getCafeTarde() { return cafeTarde; }
    public void setCafeTarde(String cafeTarde) { this.cafeTarde = cafeTarde; }

    public String getJantar() { return jantar; }
    public void setJantar(String jantar) { this.jantar = jantar; }

    public int getObjetivoId() { return objetivoId; }
    public void setObjetivoId(int objetivoId) { this.objetivoId = objetivoId; }

    public int getAtividadeId() { return atividadeId; }
    public void setAtividadeId(int atividadeId) { this.atividadeId = atividadeId; }

    public int getPraticidadeId() { return praticidadeId; }
    public void setPraticidadeId(int praticidadeId) { this.praticidadeId = praticidadeId; }

    public int getRigorId() { return rigorId; }
    public void setRigorId(int rigorId) { this.rigorId = rigorId; }
}
