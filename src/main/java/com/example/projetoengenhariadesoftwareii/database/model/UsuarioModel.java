package com.example.projetoengenhariadesoftwareii.database.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tb_usuario")
public class UsuarioModel {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String nome;
    private String email;
    private String senha;
    private int idade;
    private double peso;
    private double altura;
    private double imc;
    private String sexo;

    private String objetivo;      // Ex: Emagrecimento
    private String atividade;     // Ex: Moderado
    private String praticidade;   // Ex: Alta
    private String rigor;         // Ex: Rigoroso
    private double orcamento;     // Ex: 350.00
    private double pesoDesejado;  // Meta de peso
    private String restricoes;    // Ex: Lactose, Glúten, etc.

    // Getters e Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public int getIdade() { return idade; }
    public void setIdade(int idade) { this.idade = idade; }

    public double getPeso() { return peso; }
    public void setPeso(double peso) { this.peso = peso; }

    public double getAltura() { return altura; }
    public void setAltura(double altura) { this.altura = altura; }

    public double getImc() { return imc; }
    public void setImc(double imc) { this.imc = imc; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public String getObjetivo() { return objetivo; }
    public void setObjetivo(String objetivo) { this.objetivo = objetivo; }

    public String getAtividade() { return atividade; }
    public void setAtividade(String atividade) { this.atividade = atividade; }

    public String getPraticidade() { return praticidade; }
    public void setPraticidade(String praticidade) { this.praticidade = praticidade; }

    public String getRigor() { return rigor; }
    public void setRigor(String rigor) { this.rigor = rigor; }

    public double getOrcamento() { return orcamento; }
    public void setOrcamento(double orcamento) { this.orcamento = orcamento; }

    public double getPesoDesejado() { return pesoDesejado; }
    public void setPesoDesejado(double pesoDesejado) { this.pesoDesejado = pesoDesejado; }

    public String getRestricoes() { return restricoes; }
    public void setRestricoes(String restricoes) { this.restricoes = restricoes; }
}
