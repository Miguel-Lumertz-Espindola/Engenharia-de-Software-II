package com.example.projetoengenhariadesoftwareii.database.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "dietas")
    public class Dieta {
        @PrimaryKey
        private int dia;

        private String cafeManha;
        private String almoco;
        private String jantar;

        public Dieta(int dia, String cafeManha, String almoco, String jantar) {
            this.dia = dia;
            this.cafeManha = cafeManha;
            this.almoco = almoco;
            this.jantar = jantar;
        }

        public int getDia() { return dia; }
        public String getCafeManha() { return cafeManha; }
        public String getAlmoco() { return almoco; }
        public String getJantar() { return jantar; }

        public void setCafeManha(String cafeManha) { this.cafeManha = cafeManha; }
        public void setAlmoco(String almoco) { this.almoco = almoco; }
        public void setJantar(String jantar) { this.jantar = jantar; }
    }


