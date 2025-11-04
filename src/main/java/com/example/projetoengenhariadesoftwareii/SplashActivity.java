package com.example.projetoengenhariadesoftwareii;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private ImageView biologo;
    private Animation animation;

    @SuppressLint({"MissingInflatedId", "ResourceType"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        biologo = findViewById(R.id.Logo_inicio);

        animation = AnimationUtils.loadAnimation(this, R.anim.splash);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish(); // Fecha a splash
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });

        biologo.startAnimation(animation);
    }
}
