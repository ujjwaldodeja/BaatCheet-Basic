package com.client.myapplication.old;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.client.myapplication.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private final String username = "zuhayr"; // Set a unique username for each client
    private TextView chatTextView;
    private EditText messageEditText;
    private EditText recipientEditText;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatTextView = findViewById(R.id.chatTextView);
        messageEditText = findViewById(R.id.messageEditText);
        recipientEditText = findViewById(R.id.recipientEditText);
        sendButton = findViewById(R.id.sendButton);

        new Thread(() -> {
        try {
            socket = new Socket("145.126.98.161", 8080);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.out.println("Server not found");
        }
            System.out.println("Connected with Server");
        sendMessage("HELLO~" + username);
        System.out.println("HELLO sent");
        // Start a thread to listen for incoming messages
        new Thread(() -> {
            try {
                while (true) {
                    String[] message = reader.readLine().split("~");
                    switch(message[0]) {
                        case "HELLO": {
                            System.out.println("HELLO Exchanged");
                            sendMessage("LOGIN~" + username);
                        }
                        break;
                        case "LOGIN": {
                            System.out.println("LOGIN received");
                        }
                        break;
                        case "TEXT" : {
                            System.out.println("TEXT received");
                            runOnUiThread(() -> updateChatView("Message from" + message[1] + message[message.length-1]));
                        }
                        break;
                        default: {
                            System.out.println(message + "type is not recognised");
                        }
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        }).start();

        sendButton.setOnClickListener(view -> {
            new Thread(() -> {
                String recipient = recipientEditText.getText().toString();
                String message = messageEditText.getText().toString();

                if (!recipient.isEmpty() && !message.isEmpty()) {
                    sendMessage("TEXT~" + recipient + "~" + username + "~" + message);
                    messageEditText.getText().clear();
                    updateChatView("You to " + recipient + ": " + message);
                }
            }).start();
        });

    }
    private void sendMessage(String message) {
        writer.println(message);
        writer.flush();
    }
    private void updateChatView(String message) {
        chatTextView.append(message + "\n");
    }
}
