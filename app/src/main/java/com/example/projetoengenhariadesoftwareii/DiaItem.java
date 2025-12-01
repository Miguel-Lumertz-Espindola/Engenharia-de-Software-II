package com.example.projetoengenhariadesoftwareii;

public class DiaItem {
    int numero;
    String nomeSemana;
    boolean isHoje;

    public DiaItem(int numero, String nomeSemana, boolean isHoje) {
        this.numero = numero;
        this.nomeSemana = nomeSemana;
        this.isHoje = isHoje;
    }
    // <<< ADICIONE ESTE MÉTODO >>>
    public int getDia() {
        return numero;
    }

    // GETTERS
    public int getNumero() {
        return numero;
    }

    public String getNomeSemana() {
        return nomeSemana;
    }

    public boolean isHoje() {
        return isHoje;
    }

    // SETTERS (opcional)
    public void setNumero(int numero) {
        this.numero = numero;
    }

    public void setNomeSemana(String nomeSemana) {
        this.nomeSemana = nomeSemana;
    }

    public void setHoje(boolean hoje) {
        isHoje = hoje;
    }
}

//package com.example.projetoengenhariadesoftwareii;
//
//public class DiaItem {
//    int numero;
//    String nomeSemana;
//    boolean isHoje;
//
//    public DiaItem(int numero, String nomeSemana, boolean isHoje) {
//        this.numero = numero;
//        this.nomeSemana = nomeSemana;
//        this.isHoje = isHoje;
//    }
//}