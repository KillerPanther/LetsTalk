package com.example.letstalk.models;
import android.webkit.JavascriptInterface;


import com.example.letstalk.Activities.call_Activity;

//constructor
public class InterfaceJava {

    call_Activity callActivity;

    public InterfaceJava(call_Activity callActivity){
        this.callActivity=callActivity;
    }
    @JavascriptInterface
    public void onPeerConnected(){
        callActivity.onPeerConnected();

    }

}
