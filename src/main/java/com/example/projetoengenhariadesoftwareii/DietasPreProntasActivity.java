package com.example.projetoengenhariadesoftwareii;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DietasPreProntasActivity extends AppCompatActivity {

    RecyclerView recyclerDietaspreprontas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dietas_pre_prontas);

        recyclerDietaspreprontas = findViewById(R.id.recyclerDietaspreprontas);

        recyclerDietaspreprontas.setLayoutManager(new LinearLayoutManager(this));

        // Exemplo: depois você cria um adapter e coloca aqui:
        // recyclerDietaspreprontas.setAdapter(new DietaAdapter(listaDeDietas));
    }
}
