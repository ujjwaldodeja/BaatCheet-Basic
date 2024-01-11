package com.client.myapplication.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.client.myapplication.Client;
import com.client.myapplication.R;

public class MainActivity extends AppCompatActivity {
    private Client client;
    private Button chatButton;
    private TextView updateView;
    private Button signUpButton;
    private Button logInButton;
    EditText usernameText;
    EditText passwordText;
    String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logInButton = findViewById(R.id.logInButton);
        signUpButton = findViewById(R.id.signUpButton);
        updateView = findViewById(R.id.updateView);
        usernameText = findViewById(R.id.usernameText);
        passwordText = findViewById(R.id.passwordText);
//        System.out.println("Done");

        client = Client.getInstance();
        System.out.println(client.getKeyPair());
//        client.logout(); //should be logged out when going back form chatActivity ---not very import for nwo
        new ConnectTask().execute();
        client.setChatActivity(new ChatActivity());
//        updateView.append("");

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = usernameText.getText().toString();
                String password = passwordText.getText().toString();
                String loginCommand = "LOGIN~" + username + "~" + password;
                client.setName(username);
                new SendCommandTask().execute(loginCommand);

                if(client.isLoggedIn() || client.isAlready()) {
                    Intent i = new Intent(MainActivity.this, MenuActivity.class);
                    startActivity(i);
                } else {
                    updateView.append("INVALID Credentials");
                }
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
    private class ConnectTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            return client.connect(); // Move the connection logic to the background thread
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
//                System.out.println("CONNECTION ESTABLISHED");
                updateView.append("CONNECTION ESTABLISHED");
                client.startReceiving();
            } else {
//                System.out.println("CONNECTION NOT MADE");
                updateView.append("CONNECTION NOT MADE");
            }
        }
    }

    private class SendCommandTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            client.sendCommand(params[0]);
            return null;
        }
    }

}
