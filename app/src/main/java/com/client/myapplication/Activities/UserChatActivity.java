package com.client.myapplication.Activities;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.client.myapplication.Client;
import com.client.myapplication.Crypto.DiffieHellmanKeyExchange;
import com.client.myapplication.Crypto.E2EE;
import com.client.myapplication.R;
import com.client.myapplication.Session;
import com.client.myapplication.Stego.ImageSteganography;
import com.client.myapplication.Stego.ImageUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

import javax.crypto.SecretKey;

public class UserChatActivity extends AppCompatActivity {
    private Client client;
    private String recipient;
    private EditText messageEditText;
    private Button sendButton;
    private Button sendImage;
    private LinearLayout messageContainer;
    private Session session;

    public synchronized KeyPair getKeyPair() {
        return keyPair;
    }

    private KeyPair keyPair;

    private String recipientPublicKey = null;
    private SecretKey secretKey = null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_chat_activity);    //create layout
        messageContainer = findViewById(R.id.messageContainer);

        Intent intent = getIntent();
        if(intent != null) {
            String recipient_name = intent.getStringExtra("recipient");
            if(recipient_name!= null) {
                recipient = recipient_name;
            }
        }
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        sendImage = findViewById(R.id.sendImageButton);
        client = Client.getInstance();
        System.out.println("Instance created");
        client.addUserActivity(this);

        //initiate the initial session
        try {
            this.keyPair = DiffieHellmanKeyExchange.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        String publicKeyBase64 = DiffieHellmanKeyExchange.getPublicKeyBase64(keyPair);
        System.out.println("PUBLIC KEY GENERATED:" + publicKeyBase64);

        if(client.ifPending(recipient)) {
            new SendCommandTask().execute("SESSION_ACCEPT~" + recipient + "~" + publicKeyBase64); //accepts a session with the recipient and shares the public key
            try {
                updateRecipientPublicKey(client.getPendingKey(recipient));
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeySpecException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeyException e) {
                throw new RuntimeException(e);
            } catch (NoSuchProviderException e) {
                throw new RuntimeException(e);
            }
            client.removePending(recipient);
        } else {
            new SendCommandTask().execute("SESSION_REQUEST~" + recipient + "~" + publicKeyBase64); //requests a session with the recipient and shares the public key
        }
        try {   //wait to receive Key back
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        session = new Session();
        sendButton.setOnClickListener(view -> sendMessage());
        sendImage.setOnClickListener(view -> sendImage());
    }

    public void sendMessage() {
        String message = messageEditText.getText().toString();
        System.out.println("RK:" + secretKey);
        if(secretKey != null) {
            String encryptedMessage = "";
            if (!message.isEmpty()) {
                try {
//                encryptedMessage = E2EE.encrypt(message, recipientKey);
                    encryptedMessage = E2EE.encrypt(message, secretKey);    //using the secret key generated
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                new SendCommandTask().execute("TEXT~" + recipient + "~" + client.getName() + "~" + encryptedMessage);
//            System.out.println(message + "MESSAGE_SENT at" + printTime());
                messageEditText.getText().clear();
                updateSentView(message);
            }
        } else {
            System.out.println("SECRET KEY IS NOT GENERATED YET");
        }
    }

    public void sendImage() {
        String message = messageEditText.getText().toString();
        System.out.println("RK:" + secretKey);

        String encryptedMessage = "";
        if (!message.isEmpty()) {
            try {
                encryptedMessage = E2EE.encrypt(message, secretKey);     // this needs to be hidden in an image
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Bitmap bmp = getBitmapFromAsset("posture.png");

            Bitmap coverCopy = ImageUtils.createCopy(bmp);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(); //creating a stream for the image
            coverCopy.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos); //compressing the image before putting in the message
            byte[] array = bos.toByteArray();       //array of original image
            System.out.println("STEG_CHECK 1: bytes = " + Arrays.toString(array)); //print the bytes before hiding the message

//            System.out.println(message + "of length " + message.length() );
//            System.out.println(encryptedMessage + "of length " + encryptedMessage.length());

            Bitmap encodedImage = ImageSteganography.encodeMessage(coverCopy, encryptedMessage);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            encodedImage.compress(Bitmap.CompressFormat.PNG, 0, out);
            byte[] encodedImageBytes = out.toByteArray();//to be hiding encrypted message later
            System.out.println("STEG_CHECK 2: bytes = " + Arrays.toString(encodedImageBytes)); //print the bytes after hiding the message

            System.out.println(encryptedMessage.length());
            new SendCommandTask().execute("SEND_IMAGE~" + recipient + "~" + Arrays.toString(encodedImageBytes) + "~" + encryptedMessage.length());
//            System.out.println("IMAGE SENT at " + printTime());
            messageEditText.getText().clear();

            updateImage(encodedImage, true); //this is the newly formed image

            updateSentView(message);
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

    public void updateSentView(String message) {
        addMessage(message, true);
    }

    public void updateReceivedView(String enMessage) {
        updateCount(); //increments the count of messages for this session
        String message = null;
        System.out.println(secretKey);
        try {
            message = E2EE.decrypt(enMessage, secretKey); //decrypts and prints the received messages
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String finalMessage = message;
        runOnUiThread(() -> {
            addMessage(finalMessage, false);
        });
    }

    public synchronized String getRecipient() {
        return recipient;
    }

    public synchronized void updateRecipientPublicKey(String newKey) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchProviderException {
        if(recipientPublicKey != null) {
            if (!Objects.equals(newKey, recipientPublicKey)) {
                recipientPublicKey = newKey;
                newKeyPair(); //generates new key pair and then creates a secret key
                secretKey = DiffieHellmanKeyExchange.performKeyExchange(recipientPublicKey, this.keyPair);
            } else {
                System.out.println(newKey);
                System.out.println(recipientPublicKey);
                System.out.println("Same key is received again");
            }
        } else {
            recipientPublicKey = newKey;
            secretKey = DiffieHellmanKeyExchange.performKeyExchange(recipientPublicKey, this.keyPair);
        }
    }

    public void requestAccepted(String newKey) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchProviderException {
        SecretKey old = secretKey;
        System.out.println(Objects.equals(newKey, recipientPublicKey));
        if (!Objects.equals(newKey, recipientPublicKey)) {
            this.recipientPublicKey = newKey;
            this.secretKey = DiffieHellmanKeyExchange.performKeyExchange(recipientPublicKey, this.keyPair);
            session.reset();
        } else {
            System.out.println(newKey);
            System.out.println(recipientPublicKey);
            System.out.println("Same key is received again");
        }
        System.out.println(old == secretKey);
    }

    public synchronized void newKeyPair() {
        KeyPair newPair;
        try {
            newPair = DiffieHellmanKeyExchange.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        if (newPair == keyPair) {
            System.out.println("COULD NOT GENERATE NEW KEYPAIR");
        } else {
            this.keyPair = newPair;
            System.out.println("NEW PAIR GENERATED");
        }
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
    public void updateImage(Bitmap image, boolean sentByCurrentUser) {
        ImageView imageView = new ImageView(this);
        imageView.setBackgroundResource(R.drawable.image_border);
        if (image != null) {
            imageView.setImageBitmap(image);
        } else {
            System.out.println("Bitmap not processsed");
        }
        imageView.setPadding(16, 8, 16, 8);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        if (sentByCurrentUser) {
            params.gravity = Gravity.END;
            params.setMargins(0, 8, 0, 8);
            imageView.setLayoutParams(params);
            messageContainer.addView(imageView);
        } else {
            params.gravity = Gravity.START;
            params.setMargins(0, 8, 0, 8);
            imageView.setLayoutParams(params);
            runOnUiThread(() -> {
                messageContainer.addView(imageView);
            });
        }
    }

    private void updateCount(){
        session.incrementCount();   //increments the count for every message that is exchanged
        System.out.println(session.getCOUNT());
        if (session.isOver()) {
            System.out.println("New Session Needed"); //or reset session
            //create new Session  SESSION~recipient -- this is just to send an update
            session.reset();
            newKeyPair(); //the user requesting a new session will generate a new keyPair
            String publicKeyBase64 = DiffieHellmanKeyExchange.getPublicKeyBase64(keyPair);
            new SendCommandTask().execute("SESSION_REQUEST~" + recipient + "~" + publicKeyBase64); //can directly create a new session here, and skip this step
            // need to figure out how to set a new key here
        }
    }

}
