package com.example.letstalk.Activities;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.letstalk.R;
import com.example.letstalk.databinding.ActivityCallBinding;
import com.example.letstalk.models.InterfaceJava;
import com.example.letstalk.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

public class call_Activity extends AppCompatActivity {

    ActivityCallBinding binding;
    String uniqueId="";
    FirebaseAuth  auth;
    String username=  "";
    String friendsUsername ="";
    boolean isPeerConnected =false;

    DatabaseReference firebaseRef;
    boolean isAudio= true;
    boolean isVideo=true;
    String  createdBy;

    boolean pageExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityCallBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        auth=FirebaseAuth.getInstance();
        firebaseRef = FirebaseDatabase.getInstance().getReference().child("users");

        username=getIntent().getStringExtra("username");
        String incoming =  getIntent().getStringExtra("incoming");
        createdBy = getIntent().getStringExtra("createdBy");
//         friendsUsername="";
//
//         if(incoming.equalsIgnoreCase(friendsUsername))
//             friendsUsername=incoming;

        friendsUsername=incoming;

             setUpWebView();

             // MIC BUTTON

             binding.micBtn.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     isAudio = !isAudio;
                     callJavaScriptFunction("javascript:toggleAudio(\""+isAudio+"\")");
                     if(isAudio){
                         binding.micBtn.setImageResource((R.drawable.btn_unmute_normal));
                     }
                     else{
                         binding.micBtn.setImageResource((R.drawable.btn_mute_normal));
                     }
                 }
             });

             // VIDEO BUTTON

             binding.videoBtn.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     isAudio = !isAudio;
                     callJavaScriptFunction("javascript:toggleVideo(\""+isVideo+"\")");
                     if(isVideo){
                         binding.videoBtn.setImageResource((R.drawable.btn_video_normal));
                     }
                     else{
                         binding.videoBtn.setImageResource((R.drawable.btn_video_muted));
                     }
                 }
             });

             //end call  button
        binding.endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });




    }
    //setUp web view
    @SuppressLint("SetJavaScriptEnabled")
    void  setUpWebView(){
        //make a Web view client
        binding.webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                // for creating a audio  and video call request
//                super.onPermissionRequest(request);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }
        });

        // Java script enabling
        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        binding.webView.addJavascriptInterface(new InterfaceJava(this),"Android");
        loadVideoCall();

    }
public void loadVideoCall() {
    //load  call.html
    String filePath = "file:android_asset/call.html";
    binding.webView.loadUrl(filePath);

    binding.webView.setWebViewClient(new WebViewClient() {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            initializePeer();
        }
    });
}


        void initializePeer(){
        uniqueId = getUniqueId();
        callJavaScriptFunction("javascript:init(\""+ uniqueId + "\")");

        if(createdBy.equalsIgnoreCase(username)){
            if(pageExit)
                return;
            firebaseRef.child(username).child("connId").setValue(uniqueId);
            firebaseRef.child(username).child("isAvailable").setValue(true);


            binding.loadingGroup.setVisibility(View.GONE);
            binding.controls.setVisibility(View.VISIBLE);

            FirebaseDatabase.getInstance().getReference()
                    .child("profiles")
                    .child(friendsUsername)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user =snapshot.getValue(User.class);

                            Glide.with(call_Activity.this).load(user.getProfile())
                                    .into(binding.profile);
                            binding.name.setText(user.getName());
                            binding.city.setText(user.getCity());
                                                }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

        }else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    friendsUsername = createdBy;
                    FirebaseDatabase.getInstance().getReference()
                            .child("profiles")
                            .child(friendsUsername)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    User user =snapshot.getValue(User.class);

                                    Glide.with(call_Activity.this).load(user.getProfile())
                                            .into(binding.profile);
                                    binding.name.setText(user.getName());
                                    binding.city.setText(user.getCity());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                    FirebaseDatabase.getInstance().getReference()
                            .child("users")
                            .child(friendsUsername)
                            .child("connId")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.getValue()!=null){
                                        sendCallRequest();

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                }




            },3000);
        }


}

    // PEER CONNECTED

    public void onPeerConnected(){
        isPeerConnected= true;
    }


    // Send  call request function
    void sendCallRequest(){
        if(!isPeerConnected) {
            Toast.makeText(call_Activity.this, "You ar not  connected. Please check  your connection", Toast.LENGTH_SHORT).show();
            return;
        }

        listenConnId();

    }

    void listenConnId(){
        firebaseRef.child(friendsUsername).child("connId").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue()==null)
                    return;

                binding.loadingGroup.setVisibility(View.GONE);

                binding.controls.setVisibility(View.VISIBLE);
                String  connId =snapshot.getValue(String.class);
                callJavaScriptFunction("javascript:startCall(\""+connId+"\")");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }



//Calling java script function in android studio
void callJavaScriptFunction(String function){
        binding.webView.post(new Runnable() {
            @Override
            public void run() {
                binding.webView.evaluateJavascript(function,null);
            }
        });
}

String getUniqueId(){
        return UUID.randomUUID().toString();
}



//For ending  call

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pageExit=true;
        firebaseRef.child(createdBy).setValue(null);
        finish();

    }


}