package io.ionic.starter;

import android.os.Bundle;

import com.getcapacitor.BridgeActivity;
import com.mycompany.plugins.example.ExamplePlugin;

import com.google.firebase.FirebaseApp;

public class MainActivity extends BridgeActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
      super.onCreate(savedInstanceState);

      FirebaseApp.initializeApp(this);
      registerPlugin(ExamplePlugin.class);
    }
}



