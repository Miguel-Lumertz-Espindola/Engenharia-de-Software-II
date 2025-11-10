package com.example.projetoengenhariadesoftwareii;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
                abrirDialogVisualizar(DietasPreProntasActivity.this, dieta);
            }

            @Override
            public void onAdicionar(DietaPreProntaModel dieta) {
                Toast.makeText(DietasPreProntasActivity.this,
                        "Dieta \"" + dieta.getNomeDieta() + "\" adicionada!", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerDietaspreprontas.setAdapter(adapter);
    }

    // 🔹 Exibe o popup de visualização da dieta
    private void abrirDialogVisualizar(Context context, DietaPreProntaModel dieta) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_visualizar_dieta_pre_prontas, null);

        TextView titulo = dialogView.findViewById(R.id.tituloDieta);
        TextView descricao = dialogView.findViewById(R.id.textDescricao);
        LinearLayout layoutRefeicoes = dialogView.findViewById(R.id.layoutRefeicoes);

        titulo.setText(dieta.getNomeDieta());
        descricao.setText(dieta.getDescricao());

        // Mostra refeições
        adicionarRefeicao(layoutRefeicoes, "Café da Manhã", dieta.getCafeManha());
        adicionarRefeicao(layoutRefeicoes, "Almoço", dieta.getAlmoco());
        adicionarRefeicao(layoutRefeicoes, "Jantar", dieta.getJantar());

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setPositiveButton("Fechar", null)
                .create();

        dialog.show();
    }

    // 🔹 Cria dinamicamente cada refeição
    private void adicionarRefeicao(LinearLayout container, String titulo, String conteudo) {
        TextView tvTitulo = new TextView(container.getContext());
        tvTitulo.setText(titulo);
        tvTitulo.setTextColor(container.getResources().getColor(R.color.azulPetroleo));
        tvTitulo.setTextSize(16);
        tvTitulo.setTypeface(null, android.graphics.Typeface.BOLD);
        container.addView(tvTitulo);

        TextView tvConteudo = new TextView(container.getContext());
        tvConteudo.setText(conteudo == null || conteudo.isEmpty() ? "Nenhum item" : conteudo);
        tvConteudo.setTextColor(container.getResources().getColor(R.color.black));
        tvConteudo.setTextSize(14);
        tvConteudo.setPadding(16, 4, 0, 12);
        container.addView(tvConteudo);
    }
}
