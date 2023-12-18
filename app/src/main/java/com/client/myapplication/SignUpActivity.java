package com.client.myapplication;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.client.myapplication.Crypto.DiffieHellmanKeyExchange;

import java.security.NoSuchAlgorithmException;

public class SignUpActivity extends AppCompatActivity {
    private Client client;
    Button signUpButton;
    EditText usernameText;
    EditText passwordText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_activity);
        usernameText = findViewById(R.id.usernameText);
        passwordText = findViewById(R.id.passwordText);
        signUpButton = findViewById(R.id.signUpButton);
        client = Client.getInstance();
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameText.getText().toString();
                String password = passwordText.getText().toString();

                //sending the client's public key to the server upon registration
                try {
                    client.setKeyPair(DiffieHellmanKeyExchange.generateKeyPair());
                } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
                    System.out.println("Registration key problem : KeyPair cannot be generated");
                }
                String clientPublicKeyBase64 = DiffieHellmanKeyExchange.getPublicKeyBase64(client.getKeyPair());
                String registration = "REGISTER~" + username + "~" + password + "~" + clientPublicKeyBase64;
                new SendCommandTask().execute(registration);

                // After sending the command, navigate back to MainActivity
                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    private class SendCommandTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            client.sendCommand(params[0]);
            return null;
        }
    }
}
