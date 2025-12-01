package com.example.projetoengenhariadesoftwareii.database.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tb_usuario")
public class UsuarioModel implements Parcelable {

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

    private int objetivoId;
    private int atividadeId;
    private int praticidadeId;
    private int rigorId;
    private double orcamento;     // Ex: 350.00
    private double pesoDesejado;  // Meta de peso
    private String restricoes;    // Ex: Lactose, Glúten, etc.
    private boolean dietaInicialJaAdicionada = false;

    // ===================== CONSTRUTOR PADRÃO =====================
    public UsuarioModel() {
    }

    // ===================== CONSTRUTOR PARA PARCEL =====================
    protected UsuarioModel(Parcel in) {
        id = in.readLong();
        nome = in.readString();
        email = in.readString();
        senha = in.readString();
        idade = in.readInt();
        peso = in.readDouble();
        altura = in.readDouble();
        imc = in.readDouble();
        sexo = in.readString();
        objetivoId = in.readInt();
        atividadeId = in.readInt();
        praticidadeId = in.readInt();
        rigorId = in.readInt();
        orcamento = in.readDouble();
        pesoDesejado = in.readDouble();
        restricoes = in.readString();
    }

    // ===================== PARCELABLE =====================
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(nome);
        dest.writeString(email);
        dest.writeString(senha);
        dest.writeInt(idade);
        dest.writeDouble(peso);
        dest.writeDouble(altura);
        dest.writeDouble(imc);
        dest.writeString(sexo);
        dest.writeInt(objetivoId);
        dest.writeInt(atividadeId);
        dest.writeInt(praticidadeId);
        dest.writeInt(rigorId);
        dest.writeDouble(orcamento);
        dest.writeDouble(pesoDesejado);
        dest.writeString(restricoes);
    }

    @Override
    public int describeContents() { return 0; }

    public static final Creator<UsuarioModel> CREATOR = new Creator<UsuarioModel>() {
        @Override
        public UsuarioModel createFromParcel(Parcel in) { return new UsuarioModel(in); }

        @Override
        public UsuarioModel[] newArray(int size) { return new UsuarioModel[size]; }
    };
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

    public int getObjetivoId() { return objetivoId; }
    public void setObjetivoId(int objetivoId) { this.objetivoId = objetivoId; }

    public int getAtividadeId() { return atividadeId; }
    public void setAtividadeId(int atividadeId) { this.atividadeId = atividadeId; }

    public int getPraticidadeId() { return praticidadeId; }
    public void setPraticidadeId(int praticidadeId) { this.praticidadeId = praticidadeId; }

    public int getRigorId() { return rigorId; }
    public void setRigorId(int rigorId) { this.rigorId = rigorId; }

    public double getOrcamento() { return orcamento; }
    public void setOrcamento(double orcamento) { this.orcamento = orcamento; }

    public double getPesoDesejado() { return pesoDesejado; }
    public void setPesoDesejado(double pesoDesejado) { this.pesoDesejado = pesoDesejado; }

    public String getRestricoes() { return restricoes; }
    public void setRestricoes(String restricoes) { this.restricoes = restricoes; }
    public boolean isDietaInicialJaAdicionada() {
        return dietaInicialJaAdicionada;
    }
    public void setDietaInicialJaAdicionada(boolean dietaInicialJaAdicionada) {
        this.dietaInicialJaAdicionada = dietaInicialJaAdicionada;
    }
}