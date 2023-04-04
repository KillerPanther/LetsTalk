package com.example.letstalk.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.letstalk.R;
import com.example.letstalk.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    GoogleSignInClient mGoogleSignInClient;
    int RC_SIGN_IN=11;//for opening google logged in dialoge box
    FirebaseAuth mAuth;
    FirebaseDatabase database;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth=FirebaseAuth.getInstance();

          // for one time  signIn until uninstall or deleted that application

        if(mAuth.getCurrentUser()!=null) {
            gotoNextActivity();
        }
        database=FirebaseDatabase.getInstance();

        GoogleSignInOptions gso=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)// these three
                        .requestIdToken(getString(R.string.default_web_client_id)) //lines  are the code for
                        .requestEmail()// creating a google
                         .build();      //signed in
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso); //dialog box




        findViewById(R.id.loginBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=mGoogleSignInClient.getSignInIntent();
                startActivityForResult(intent,RC_SIGN_IN);


//                startActivity(new Intent(LoginActivity.this, MainActivity.class));

            }
        });

    }
    void gotoNextActivity(){
        startActivity((new Intent(LoginActivity.this, MainActivity.class)));//jump from one activity to another activity
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_SIGN_IN){
            Task<GoogleSignInAccount> task= GoogleSignIn.getSignedInAccountFromIntent(data);
            GoogleSignInAccount account=task.getResult();
            authWithGoogle(account.getIdToken());
        }else{
            Toast.makeText(this, "Click on the email id..", Toast.LENGTH_SHORT).show();
        }

    }
    void authWithGoogle(String idTocken){
        AuthCredential credential= GoogleAuthProvider.getCredential(idTocken,null);//For signIn google
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user=task.getResult().getUser();
                            User firebaseUser=new User(user.getUid(),user.getDisplayName(),user.getPhotoUrl().toString(),"--",500);


                            //Store the data into the database
                            database.getReference()
                                            .child("profiles")
                                                    .child(user.getUid())
                                                            .setValue(firebaseUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                startActivity(new Intent(LoginActivity.this,MainActivity.class));
                                                finishActivity(0);
                                            }else{
                                                Toast.makeText(LoginActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            Log.e("profile",user.getPhotoUrl().toString()); //for getting profile picture in log from google email id
                        }else{
                            Log.e("err",task.getException().getLocalizedMessage());//for finding the issues  during the sign in
                        }
                    }
                });
    }

}