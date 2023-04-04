package com.example.letstalk.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.letstalk.R;
import com.google.firebase.auth.FirebaseAuth;

public class WelcomeActivity extends AppCompatActivity {

    FirebaseAuth auth;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        auth=FirebaseAuth.getInstance();  // for one time  signIn until uninstall or deleted that application
        if(auth.getCurrentUser()!=null){
            gotoNextActivity();


        }

        findViewById(R.id.getStarted).setOnClickListener(new View.OnClickListener() {   //set click listner on the button for action
            @Override
            public void onClick(View v) {
                gotoNextActivity();
            }
        });
    }
    void gotoNextActivity(){
        startActivity((new Intent(WelcomeActivity.this, LoginActivity.class)));//jump from one activity to another activity
        finish();
    }
}