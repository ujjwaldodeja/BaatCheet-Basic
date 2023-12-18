package com.client.myapplication;


import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

public class ChatActivity extends AppCompatActivity {
    private Client client;
    private TextView chatTextView;
    private EditText messageEditText;
    private EditText recipientEditText;
    private Button sendButton;
    private Button listButton;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);
        chatTextView = findViewById(R.id.chatTextView);
        messageEditText = findViewById(R.id.messageEditText);
        recipientEditText = findViewById(R.id.recipientEditText);
        sendButton = findViewById(R.id.sendButton);
        listButton = findViewById(R.id.listButton);
        client = Client.getInstance();
        System.out.println("Instance created");
//        updateChatView("LOGGED IN");
//        new SendCommandTask().execute("LIST");
        sendButton.setOnClickListener(view -> sendMessage());
        listButton.setOnClickListener(view -> new SendCommandTask().execute("LIST"));
    }

    public void sendMessage() {
        String recipient = recipientEditText.getText().toString();
        String message = messageEditText.getText().toString();
        if (!recipient.isEmpty() && !message.isEmpty()) {
            new SendCommandTask().execute("TEXT~" + recipient + "~" + client.getName() + "~" + message);
            messageEditText.getText().clear();
            updateChatView("You to " + recipient + ": " + message);
        }
    }
    public void updateChatView(String message) {
        chatTextView.append(message + "\n");
    }


    private class SendCommandTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            client.sendCommand(params[0]);
            return null;
        }
    }
}
