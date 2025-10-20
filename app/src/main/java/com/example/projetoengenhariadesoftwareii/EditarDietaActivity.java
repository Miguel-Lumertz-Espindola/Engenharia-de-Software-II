package com.example.projetoengenhariadesoftwareii;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projetoengenhariadesoftwareii.database.AppDatabase;
import com.example.projetoengenhariadesoftwareii.database.DAO.DietaDAO;
import com.example.projetoengenhariadesoftwareii.database.Dieta;

public class EditarDietaActivity extends AppCompatActivity {

    ImageButton buttonMenu, buttonLogo;
    private EditText inputCafe, inputAlmoco, inputJantar;
    private TextView textoDia;
    private int diaSelecionado;
    private DietaDAO dietaDao;
    private Dieta dietaAtual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_dieta);

                buttonMenu = findViewById(R.id.buttonMenu);
        buttonLogo = findViewById(R.id.buttonlogo);

        // --- inicialização ---
        inputCafe = findViewById(R.id.inputCafeManha);
        inputAlmoco = findViewById(R.id.inputAlmoco);
        inputJantar = findViewById(R.id.inputJantar);
        textoDia = findViewById(R.id.textoDiaSelecionado);

        dietaDao = AppDatabase.getInstance(this).dietaDAO();

        // --- recupera o dia vindo da tela anterior ---
        diaSelecionado = getIntent().getIntExtra("diaSelecionado", -1);
        textoDia.setText("Dia selecionado: " + diaSelecionado);

        // --- carrega dieta salva ---
        dietaAtual = dietaDao.getDietaPorDia(diaSelecionado);
        if (dietaAtual != null) {
            inputCafe.setText(dietaAtual.getCafeManha());
            inputAlmoco.setText(dietaAtual.getAlmoco());
            inputJantar.setText(dietaAtual.getJantar());
        }

        // --- botão salvar ---
        findViewById(R.id.btnSalvar).setOnClickListener(v -> salvarDieta());
        findViewById(R.id.btnExcluir).setOnClickListener(v -> excluirDieta());
        findViewById(R.id.btnInicio).setOnClickListener(v -> finish());

        configurarMenu();
    }

    private void configurarMenu() {
        buttonMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(EditarDietaActivity.this, buttonMenu);
            popupMenu.getMenuInflater().inflate(R.menu.activity_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.opcao1) {
                    startActivity(new Intent(this, todayActivity.class));
                    return true;
                } else if (id == R.id.opcao2) {
                    startActivity(new Intent(this, ComprasMesActivity.class));
                    return true;
                } else if (id == R.id.opcao3) {
                    startActivity(new Intent(this, RelatoriosActivity.class));
                    return true;
                } else if (id == R.id.opcao4) {
                    startActivity(new Intent(this, SobreActivity.class));
                    return true;
                } else if (id == R.id.opcao5) {
                    startActivity(new Intent(this, LoginActivity.class));
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });
    }

    private void salvarDieta() {
        String cafe = inputCafe.getText().toString();
        String almoco = inputAlmoco.getText().toString();
        String jantar = inputJantar.getText().toString();

        Dieta dieta = new Dieta(diaSelecionado, cafe, almoco, jantar);
        dietaDao.salvarDieta(dieta);

        Toast.makeText(this, "Dieta salva com sucesso!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void excluirDieta() {
        if (dietaAtual != null) {
            dietaDao.excluirDieta(dietaAtual);
            Toast.makeText(this, "Dieta excluída!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Nenhuma dieta para excluir!", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}

