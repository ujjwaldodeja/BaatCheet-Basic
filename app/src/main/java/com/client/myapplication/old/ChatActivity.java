package com.client.myapplication.old;


import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.client.myapplication.Client;
import com.client.myapplication.Crypto.E2EE;
import com.client.myapplication.R;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.SecretKey;

public class ChatActivity extends AppCompatActivity {
    private Client client;
    private TextView chatTextView;
    private TextView lastSent;
//    private TextView lastReceived;
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
        lastSent = findViewById(R.id.lastSent);
//        lastReceived = findViewById(R.id.lastReceived);
        client = Client.getInstance();
        System.out.println("Instance created");
//        updateChatView("LOGGED IN");

        new SendCommandTask().execute("LIST"); //fetches the list automatically without external buttons

        sendButton.setOnClickListener(view -> sendMessage());

        listButton.setOnClickListener(view -> new SendCommandTask().execute("LIST"));

    }

    public void sendMessage() {
        String recipient = recipientEditText.getText().toString();
        String message = messageEditText.getText().toString();

        SecretKey recipientKey = client.getSharedSecret(recipient);

        System.out.println("RK" + recipientKey);
        String encryptedMessage = "";
        if (!recipient.isEmpty() && !message.isEmpty()) {
            try {
                encryptedMessage = E2EE.encrypt(message, recipientKey);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            new SendCommandTask().execute("TEXT~" + recipient + "~" + client.getName() + "~" + encryptedMessage);
            System.out.println(message + "MESSAGE_SENT at" + printTime());
            messageEditText.getText().clear();
            updateSentView(recipient + ": " + message);
        }
    }
    public void updateChatView(String message) {
        chatTextView.append(message + "\n");
    }

    public String printTime() {
        // Get the current time
        long currentTimeMillis = System.currentTimeMillis();
        // Create a Date object using the current time
        Date currentDate = new Date(currentTimeMillis);
        // Create a SimpleDateFormat object to format the time
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        // Format the current time
        String formattedTime = sdf.format(currentDate);

        // Print the formatted time
        return formattedTime;
    }

    public void updateSentView(String message) {
        lastSent.append(message + "\n");
    }

//    public void updateReceivedView(String message) {
//        lastReceived.append(message + "\n");
//    }
    private class SendCommandTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            client.sendCommand(params[0]);
            return null;
        }
    }
}
