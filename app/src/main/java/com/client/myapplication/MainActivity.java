package com.client.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private Button chatButton;
    private Button signUpButton;
    private Button logInButton;
    EditText usernameText;
    EditText passwordText;
    String name = "Ujjwal";
//    private BufferedReader reader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatButton = findViewById(R.id.chatButton);
        logInButton = findViewById(R.id.logInButton);
//        quitButton = findViewById(R.id.quitButton);
        signUpButton = findViewById(R.id.signUpButton);
        System.out.println("Done");
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ChatActivity.class);
                startActivity(i);
            }
        });
        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String username = usernameText.getText().toString();
//                String password = passwordText.getText().toString();
//                String loginCommand = "LOGIN~" + username + "~" + password;
                //sendCommand(loginCommand);
                Intent i = new Intent(MainActivity.this, ChatActivity.class);
                startActivity(i);
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(i);
            }
        });
    }
}
