package com.example.geohospital;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;

public class activity_bienvenida extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bienvenida);

        ImageView button = (ImageView)findViewById(R.id.imgLogo);
        final Animation myAnim = AnimationUtils.loadAnimation(this,R.anim.bounce);
        button.startAnimation(myAnim);
        this.setTitle("");

        FirebaseAuth.getInstance().signOut();
        new CountDownTimer(3000,1000){
            public void onTick(long millisUntilFinished) {

            }
            public void onFinish()
            {
                Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);

            }
        }.start();
    }
}
