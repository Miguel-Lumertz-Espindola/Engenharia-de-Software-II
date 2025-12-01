package com.example.projetoengenhariadesoftwareii.database.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "dietas")
public class Dieta {
    @PrimaryKey
    private long id; // AGORA TEM UM ID ÚNICO!

    private int dia;
    private int idUsuario;
    private int idDietaPrePronta;  // 🔹 NOVO CAMPO

    private String cafeManha;
    private String almoco;
    private String cafeTarde;
    private String jantar;

    // 🔹 NEW – horários vinculados a cada refeição
    private String horarioCafe;
    private String horarioAlmoco;
    private String horarioCafeTarde;
    private String horarioJantar;

    public Dieta(int dia, int idUsuario, int idDietaPrePronta, String cafeManha, String almoco, String cafeTarde, String jantar) {
        this.dia = dia;
        this.idUsuario = idUsuario;
        this.idDietaPrePronta = idDietaPrePronta;
        this.cafeManha = cafeManha;
        this.almoco = almoco;
        this.cafeTarde = cafeTarde;
        this.jantar = jantar;

        // 🔹 Coloquei valores padrão para evitar erro ao carregar
        this.horarioCafe = "08:00";
        this.horarioAlmoco = "12:00";
        this.horarioCafeTarde = "15:00";
        this.horarioJantar = "19:00";
    }
    public Dieta() {}

    // GETTERS
    public long getId() { return id; }
    public int getDia() { return dia; }
    public int getIdUsuario() { return idUsuario; }
    public int getIdDietaPrePronta() { return idDietaPrePronta; }
    public String getCafeManha() { return cafeManha; }
    public String getAlmoco() { return almoco; }
    public String getCafeTarde() { return cafeTarde; }
    public String getJantar() { return jantar; }

    public String getHorarioCafe() { return horarioCafe; }
    public String getHorarioAlmoco() { return horarioAlmoco; }
    public String getHorarioCafeTarde() { return horarioCafeTarde; }
    public String getHorarioJantar() { return horarioJantar; }

    // SETTERS
    public void setId(long id) { this.id = id; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public void setDia(int dia) { this.dia = dia; }
    public void setIdDietaPrePronta(int idDietaPrePronta) { this.idDietaPrePronta = idDietaPrePronta; }
    public void setCafeManha(String cafeManha) { this.cafeManha = cafeManha; }
    public void setAlmoco(String almoco) { this.almoco = almoco; }
    public void setCafeTarde(String cafeTarde) { this.cafeTarde = cafeTarde; }
    public void setJantar(String jantar) { this.jantar = jantar; }

    public void setHorarioCafe(String horarioCafe) { this.horarioCafe = horarioCafe; }
    public void setHorarioAlmoco(String horarioAlmoco) { this.horarioAlmoco = horarioAlmoco; }
    public void setHorarioCafeTarde(String horarioCafeTarde) { this.horarioCafeTarde = horarioCafeTarde; }
    public void setHorarioJantar(String horarioJantar) { this.horarioJantar = horarioJantar; }
}