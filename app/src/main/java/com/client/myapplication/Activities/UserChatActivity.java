package com.client.myapplication.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.client.myapplication.Client;
import com.client.myapplication.Crypto.E2EE;
import com.client.myapplication.R;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.SecretKey;

public class UserChatActivity extends AppCompatActivity {
    private Client client;
    private String recipient;
    private EditText messageEditText;
    private Button sendButton;
    private LinearLayout messageContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_chat_activity);    //create layout
        messageContainer = findViewById(R.id.messageContainer);
        Intent intent = getIntent();
        if(intent != null) {
            String username = intent.getStringExtra("username");
            if(username!= null) {
                recipient = username;
            }
        }
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        client = Client.getInstance();
        System.out.println("Instance created");

        // Add more messages as needed...

//        client.setChatActivity(this);
        new SendCommandTask().execute("LIST"); //fetches the list automatically without external buttons
        sendButton.setOnClickListener(view -> sendMessage());
    }

    public void sendMessage() {
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
//            System.out.println(message + "MESSAGE_SENT at" + printTime());
            messageEditText.getText().clear();
            updateSentView(recipient + ": " + message);
        }
    }


    public void updateSentView(String message) {
        addMessage(message, true);
    }

    public void updateReceivedView(String message) {
        addMessage(message, false);
    }
    private class SendCommandTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            client.sendCommand(params[0]);
            return null;
        }
    }

    private void addMessage(String message, boolean sentByCurrentUser) {
        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setPadding(16, 8, 16, 8);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        if (sentByCurrentUser) {
            textView.setBackgroundResource(R.drawable.bg_sent_message);
            params.gravity = Gravity.END;
        } else {
            textView.setBackgroundResource(R.drawable.bg_received_message);
            params.gravity = Gravity.START;
        }

        params.setMargins(0, 8, 0, 8);
        textView.setLayoutParams(params);
        messageContainer.addView(textView);
    }
}
