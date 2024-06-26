package com.client.myapplication.old;


import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.crypto.SecretKey;

public class StegChatActivity extends AppCompatActivity {
    private Client client;
//    private TextView chatTextView;
    private TextView lastSent;
    private TextView lastReceived;
    private TextView encryptedView;
    private ImageView imageView;
    private EditText messageEditText;
    private EditText recipientEditText;
    private Button sendButton;
    private Button listButton;
    private Button extractButton;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steg_chat);
//        chatTextView = findViewById(R.id.chatTextView);
        messageEditText = findViewById(R.id.messageEditText);
        recipientEditText = findViewById(R.id.recipientEditText);
        encryptedView = findViewById(R.id.encrypted);
        sendButton = findViewById(R.id.sendButton);
        listButton = findViewById(R.id.listButton);
        extractButton = findViewById(R.id.extract);
        lastSent = findViewById(R.id.lastSent);
        lastReceived = findViewById(R.id.lastReceived);
        imageView = findViewById(R.id.imageView);
        client = Client.getInstance();
        System.out.println("Instance created");
//        updateChatView("LOGGED IN");

//        new SendCommandTask().execute("LIST");

        sendButton.setOnClickListener(view -> sendMessage());
        listButton.setOnClickListener(view -> new SendCommandTask().execute("LIST"));
        extractButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
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
            Bitmap bmp = getBitmapFromAsset("posture.png");

            Bitmap coverCopy = ImageUtils.createCopy(bmp);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(); //creating a stream for the image
            coverCopy.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos); //compressing the image before putting in the message
            byte[] array = bos.toByteArray();       //array of original image


            System.out.println("STEG_CHECK 1: bytes = " + Arrays.toString(array)); //print the bytes before hiding the message
            System.out.println(message + "of length " + message.length() );
            System.out.println(encryptedMessage + "of length " + encryptedMessage.length());

            Bitmap encodedImage = ImageSteganography.encodeMessage(coverCopy, encryptedMessage);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            encodedImage.compress(Bitmap.CompressFormat.PNG, 0, out);
            byte[] encodedImageBytes = out.toByteArray();//to be hiding encrypted message later

            System.out.println("STEG_CHECK 2: bytes = " + Arrays.toString(encodedImageBytes)); //print the bytes after hiding the message

//            updateImageView(encodedImage); //this is the newly formed image

            new SendCommandTask().execute("SEND_IMAGE~" + recipient + "~" + client.getName() + "~" + Arrays.toString(encodedImageBytes) + "~" + encryptedMessage.length());
            System.out.println("IMAGE SENT at " + printTime());
            messageEditText.getText().clear();
            updateSentView(recipient + ": " + message);
        }
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
    public void updateReceivedView(String message) {
        lastReceived.append(message + "\n");
    }

    public void updateEncryptedView(String message) {
        encryptedView.append(message + "\n");
    }

    public void updateImageView(Uri image) {
        if (image != null) {
            runOnUiThread(() -> {
                imageView.setImageURI(image);
            });
        } else {
            System.out.println("URI not found");
        }
//        imageView.setImageBitmap(image);
    }

    public void updateImageView(Bitmap image) {
        if (image != null) {
            runOnUiThread(() -> {
                imageView.setImageBitmap(image);
            });
        } else {
            System.out.println("Bitmap not processsed");
        }
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