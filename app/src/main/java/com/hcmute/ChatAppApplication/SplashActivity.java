package com.hcmute.ChatAppApplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.hcmute.ChatAppApplication.login.LoginActivity;
//declare class for splash activity
public class SplashActivity extends AppCompatActivity {
    //declare image view
    private ImageView ivSplash;
    //declare for text view
    private TextView tvSplash;
    //declare animation
    private Animation animation;

    @Override
    //action when create
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if(getSupportActionBar()!= null){
            getSupportActionBar().hide();
        }

        ivSplash = findViewById(R.id.ivSplash);
        tvSplash = findViewById(R.id.tvSplash);

        animation = AnimationUtils.loadAnimation(this, R.anim.splash_animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            //action when start animation
            public void onAnimationStart(Animation animation) {

            }

            @Override
            //action when animation end
            public void onAnimationEnd(Animation animation) {

                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();

            }

            @Override
            //action when repeat animation
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        ivSplash.setAnimation(animation);
        tvSplash.setAnimation(animation);
    }
}