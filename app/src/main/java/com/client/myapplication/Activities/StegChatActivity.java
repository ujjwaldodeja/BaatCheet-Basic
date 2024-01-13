package com.client.myapplication.Activities;


import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.client.myapplication.Client;
import com.client.myapplication.Crypto.E2EE;
import com.client.myapplication.Stego.ImageSteganography;
import com.client.myapplication.Stego.ImageUtils;
import com.client.myapplication.R;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

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

        sendButton.setOnClickListener(view -> sendMessage2());
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
                encryptedMessage = E2EE.encrypt(message, recipientKey);     // this needs to be hidden in an image
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            byte[] imageBytes = ImageUtils.getImageBytes(getApplicationContext(),"posture.png"); //getting the bytes of the original image
            System.out.println("STEG_CHECK 1: bytes = " + Arrays.toString(imageBytes));
//            System.out.println(); //print the bitmap of the image before encoding and after encoding the message

            byte[] encodedImageBytes = ImageSteganography.hideMessage(imageBytes, encryptedMessage);
            System.out.println("STEG_CHECK 2: bytes = " + Arrays.toString(encodedImageBytes));

            new SendCommandTask().execute("SEND_IMAGE~" + recipient + "~" + client.getName() + "~" + encodedImageBytes);
            System.out.println("IMAGE SENT");
            messageEditText.getText().clear();
            updateSentView(recipient + ": " + message);
        }
   }

    public void sendMessage2() {
        String recipient = recipientEditText.getText().toString();
        String message = messageEditText.getText().toString();

        SecretKey recipientKey = client.getSharedSecret(recipient);

        System.out.println("RK" + recipientKey);

        String encryptedMessage = "";
        if (!recipient.isEmpty() && !message.isEmpty()) {
//            try {
//                encryptedMessage = E2EE.encrypt(message, recipientKey);     // this needs to be hidden in an image
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
            Bitmap bmp = getBitmapFromAsset("ad-hoc.jpeg");
            System.out.println("BITMAP CHECK: " + bmp.toString());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            byte[] array = bos.toByteArray();

            System.out.println(Arrays.toString(array));
            new SendCommandTask().execute("SEND_IMAGE~" + recipient + "~" + client.getName() + "~" + Arrays.toString(array));
            System.out.println("IMAGE SENT");
            messageEditText.getText().clear();
            updateSentView(recipient + ": " + message);
        }
    }


    public void updateSentView(String message) {
        lastSent.append(message + "\n");
    }
    public void updateReceivedView(String message) {
        lastReceived.append(message + "\n");
    }

    public void updateImageView(Uri image) {
        if (image != null) {
            imageView.setImageURI(image);
        } else {
            System.out.println("URI not found");
        }
//        imageView.setImageBitmap(image);
    }
    private Bitmap getBitmapFromAsset(String strName) {
        AssetManager assetManager = getAssets();
        InputStream istr = null;
        try {
            istr = assetManager.open(strName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(istr);
        return bitmap;
    }

    private class SendCommandTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            client.sendCommand(params[0]);
            return null;
        }
    }
}
