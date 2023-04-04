package com.example.letstalk.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.example.letstalk.R;
import com.example.letstalk.databinding.ActivityMainBinding;
import com.example.letstalk.models.User;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database; //for showing coins  from  database
    long coins=0;
    String[]  permissions =new String[] {Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO}; //for taking  the permission from the  user
    private  int requestCode=1;
    User user;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        MobileAds.initialize(this, initializationStatus -> {
        });


        ProgressDialog progress = new ProgressDialog(this);
        progress.show();

        auth= FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        FirebaseUser currentUser=auth.getCurrentUser();

        database.getReference().child("profiles")
                .child(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progress.dismiss();
                         user =snapshot.getValue(User.class);

                        coins=user.getCoins();

                        binding.Coins.setText("You have:"+coins);


                        Glide.with(MainActivity.this)//for  loading profile picture
                                .load(user.getProfile())
                                .into(binding.profilePicture); //for getting profile picture from email Id

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //FIND BUTTON

        binding.findBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPermissionsGranted()){
                    if(coins >5){

                        coins =coins -5;  // for decrease coins  by 5
                        database.getReference().child("profiles")
                                .child(currentUser.getUid())
                                .child("coins")
                                .setValue(coins);
                        Intent intent=new Intent(MainActivity.this,ConnectingActivity.class);
                        intent.putExtra("profile",user.getProfile());
                        startActivity(intent);
                        Toast.makeText(MainActivity.this, "Call finding..", Toast.LENGTH_SHORT).show();
//                        startActivity(new Intent(MainActivity.this,ConnectingActivity.class));

                    }

                }else{
                   askPermission();
                }
            }
        });

        // Reward Button
        binding.rewardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RewardActivity.class));
            }
        });





    }
    void askPermission(){
        ActivityCompat.requestPermissions(this,permissions,requestCode);
    }

    private boolean  isPermissionsGranted(){
        for(String permission: permissions){
            if(ActivityCompat.checkSelfPermission(this,permission)!= PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }
}