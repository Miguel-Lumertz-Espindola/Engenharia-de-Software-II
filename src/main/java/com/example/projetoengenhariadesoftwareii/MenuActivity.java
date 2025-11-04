package com.example.projetoengenhariadesoftwareii;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    ImageButton buttonMenu;

    @SuppressLint({"MissingInflatedId", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.menu.activity_menu);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        buttonMenu = findViewById(R.id.buttonMenu);

        buttonMenu.setOnClickListener(new View.OnClickListener() {
            @Override // No need for @SuppressLint("ResourceType") here if using correct resource type
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(MenuActivity.this, buttonMenu);
                // CORRECT LINE: Inflate your menu XML file
                popupMenu.getMenuInflater().inflate(R.menu.activity_menu, popupMenu.getMenu()); // Assuming your menu file is my_popup_menu.xml

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();
                        if (itemId == R.id.opcao1) {
                            Toast.makeText(MenuActivity.this, "Você clicou em Inicio", Toast.LENGTH_SHORT).show();
                            return true;
                        } else if (itemId == R.id.opcao2) {
                            Toast.makeText(MenuActivity.this, "Você clicou em Compras do mês", Toast.LENGTH_SHORT).show();
                            return true;
                        } else if (itemId == R.id.opcao3) {
                            Toast.makeText(MenuActivity.this, "Você clicou em Relatórios", Toast.LENGTH_SHORT).show();
                            return true;
                        } else if (itemId == R.id.opcao4) {
                            Toast.makeText(MenuActivity.this, "Você clicou em Sobre Nós", Toast.LENGTH_SHORT).show();
                            return true;
                        } else if (itemId == R.id.opcao5) {
                            Toast.makeText(MenuActivity.this, "Você clicou em Sair", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        return false;
                    }
                });

                popupMenu.show();
            }
        });
    }
}