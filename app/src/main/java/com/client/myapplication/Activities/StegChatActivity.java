package com.client.myapplication.Activities;


import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.client.myapplication.Client;
import com.client.myapplication.ImageUtils;
import com.client.myapplication.R;


import javax.crypto.SecretKey;

public class StegChatActivity extends AppCompatActivity {
    private Client client;
//    private TextView chatTextView;
    private TextView lastSent;
    private TextView lastReceived;
    private ImageView imageView;
    private EditText messageEditText;
    private EditText recipientEditText;
    private Button sendButton;
    private Button listButton;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steg_chat);
//        chatTextView = findViewById(R.id.chatTextView);
        messageEditText = findViewById(R.id.messageEditText);
        recipientEditText = findViewById(R.id.recipientEditText);
        sendButton = findViewById(R.id.sendButton);
        listButton = findViewById(R.id.listButton);
        lastSent = findViewById(R.id.lastSent);
        lastReceived = findViewById(R.id.lastReceived);
        imageView = findViewById(R.id.imageView);
        client = Client.getInstance();
        System.out.println("Instance created");
        client.setStegActivity(this);     // creates a steg activity for the current client
//        updateChatView("LOGGED IN");
//        new SendCommandTask().execute("LIST");
        sendButton.setOnClickListener(view -> sendMessage());
        listButton.setOnClickListener(view -> new SendCommandTask().execute("LIST"));
    }

    public void sendMessage() {
        String recipient = recipientEditText.getText().toString();
        String message = messageEditText.getText().toString();

        SecretKey recipientKey = client.getSharedSecret(recipient);

        System.out.println("RK" + recipientKey);

//        String encryptedMessage = "";
//        if (!recipient.isEmpty() && !message.isEmpty()) {
//            try {
//                encryptedMessage = E2EE.encrypt(message, recipientKey);     // this needs to be hidden in an image
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }

//            new SendCommandTask().execute("SEND_IMAGE~" + recipient + "~" + client.getName() + "~" + encryptedMessage);
            //the encrypted message has to be replaced by a bytes of an image file which hides the message inside
            byte[] image = ImageUtils.getImageBytes(getApplicationContext(),"posture.png");
            System.out.println(image);
            new SendCommandTask().execute("SEND_IMAGE~" + recipient + "~" + client.getName() + "~" + image);
            System.out.println("IMAGE SENT");
            messageEditText.getText().clear();
            updateSentView(recipient + ": " + message);
        }
//    }
//    public void updateChatView(String message) {
//        lastReceived.append(message + "\n");
//    }

    public void updateSentView(String message) {
        lastSent.append(message + "\n");
    }
    public void updateReceivedView(String message) {
        lastReceived.append(message + "\n");
    }

    public void updateImageView(Bitmap image) {
        imageView.setImageBitmap(image);
    }

    private class SendCommandTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            client.sendCommand(params[0]);
            return null;
        }
    }
}
