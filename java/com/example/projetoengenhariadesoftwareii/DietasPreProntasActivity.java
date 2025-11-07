package com.example.projetoengenhariadesoftwareii;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetoengenhariadesoftwareii.adapter.DietaPreProntaAdapter;
import com.example.projetoengenhariadesoftwareii.database.AppDatabase;
import com.example.projetoengenhariadesoftwareii.database.model.DietaPreProntaModel;

import java.util.List;

public class DietasPreProntasActivity extends AppCompatActivity {

    RecyclerView recyclerDietaspreprontas;
    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dietas_pre_prontas);

        recyclerDietaspreprontas = findViewById(R.id.recyclerDietaspreprontas);
        recyclerDietaspreprontas.setLayoutManager(new LinearLayoutManager(this));

        db = AppDatabase.getInstance(this);

        // 🔹 Insere dietas pré-prontas só se o banco estiver vazio
        if (db.dietaPreProntaDAO().getTodas().isEmpty()) {
            db.dietaPreProntaDAO().inserir(new DietaPreProntaModel(
                    "Dieta Fit Equilibrada",
                    "Alimentação balanceada para quem busca emagrecimento saudável",
                    "Iogurte natural + granola + banana",
                    "Peito de frango grelhado + arroz integral + salada",
                    "Omelete de claras + salada verde"
            ));

            db.dietaPreProntaDAO().inserir(new DietaPreProntaModel(
                    "Dieta Proteica",
                    "Ideal para ganho de massa muscular",
                    "Ovos mexidos + pão integral + suco natural",
                    "Carne magra + batata-doce + brócolis",
                    "Peixe grelhado + legumes no vapor"
            ));

            db.dietaPreProntaDAO().inserir(new DietaPreProntaModel(
                    "Dieta Vegetariana",
                    "Foco em proteínas vegetais e equilíbrio nutricional",
                    "Vitamina de frutas com aveia",
                    "Lentilha + arroz integral + legumes refogados",
                    "Tofu grelhado + salada com grão-de-bico"
            ));
        }

        // 🔹 Carrega e mostra as dietas
        List<DietaPreProntaModel> listaDietas = db.dietaPreProntaDAO().getTodas();

        DietaPreProntaAdapter adapter = new DietaPreProntaAdapter(listaDietas, new DietaPreProntaAdapter.OnDietaClickListener() {
            @Override
            public void onVisualizar(DietaPreProntaModel dieta) {
                Toast.makeText(DietasPreProntasActivity.this,
                        "Visualizando: " + dieta.getNomeDieta(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdicionar(DietaPreProntaModel dieta) {
                Toast.makeText(DietasPreProntasActivity.this,
                        dieta.getNomeDieta() + " adicionada à sua lista!", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerDietaspreprontas.setAdapter(adapter);
    }
}
