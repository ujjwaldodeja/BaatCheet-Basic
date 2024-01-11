package com.client.myapplication;

import android.graphics.Bitmap;

import com.client.myapplication.Activities.ChatActivity;
import com.client.myapplication.Activities.StegChatActivity;
import com.client.myapplication.Crypto.DiffieHellmanKeyExchange;
import com.client.myapplication.Crypto.E2EE;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

public class Client {
    private static Client instance;
    BufferedReader reader;
    Socket socket = null;
    BufferedWriter writer;
    String username;
    String address = "145.126.0.90";
    int port = 8080;
    private boolean loggedIn = false;
    private boolean already = false;
    private boolean helloReceived = false;

    private KeyPair keyPair;

    private SecretKey serverSecretKey;
    private Map<String, String> userKeys = new HashMap<>();
    private Map<String, SecretKey> secretKeys = new HashMap<>();
    private StegChatActivity stegActivity;

    private Client() {
//        try {
//            this.keyPair = DiffieHellmanKeyExchange.generateKeyPair();
//        } catch (NoSuchAlgorithmException e) {
////            throw new RuntimeException(e);
//            System.out.println("Client constructor : KeyPair cannot be generated");
//        }
        // Private constructor to enforce singleton pattern
    }

    private ChatActivity chatActivity;


    public void setChatActivity(ChatActivity chatActivity) {
        this.chatActivity = chatActivity;
    }

    private void updateChatViewOnUiThread(String message) {
        if (chatActivity != null) {
            chatActivity.updateChatView(message);
        }
    }

    public void setStegActivity(StegChatActivity stegActivity) {
        this.stegActivity = stegActivity;
    }
    private void updateImageViewOnUiThread(Bitmap image) {
        if (stegActivity != null) {       //just updates the received message view
            stegActivity.updateImageView(image);
        }
    }
    private void updateRecievedViewOnUiThread(String message) {
        if (stegActivity != null) {       //just updates the received message view
            stegActivity.updateReceivedView(message);
        }
    }

    public static synchronized Client getInstance() {
        if (instance == null) {
            instance = new Client();
        }
        return instance;
    }
    public boolean connect() {
        try {
            socket = new Socket(address, port);
            System.out.println(socket);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void startReceiving() {
        Thread thread = new Thread(new Runnable() {
            /**
             * Mainly receives messages from the connected server and act accordingly.
             */
            @Override
            public void run() {
                try {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
                    String line;
                    //run until the socket is closed
                    while (!socket.isClosed()) {
                        line = reader.readLine();
                        if (line != null) {
                            String[] command = line.split("~");
                            System.out.println(line);
                            //cases for the received commands from the client
                            switch (command[0]) {

                                case "REGISTERED":{

                                    String serverPublicKeyBase64 = command[1];

                                    //generating a secret with the server
                                    serverSecretKey = DiffieHellmanKeyExchange.performKeyExchange(serverPublicKeyBase64, keyPair);
                                    System.out.println("Generated shared secret:" + serverSecretKey);
                                }
                                case "LOGGED_IN": {
                                    loggedIn = true;
                                }
                                break;
                                case "ALREADYLOGGEDIN": {
                                    //Call for login again with a different username if already logged in
                                    already = true;
                                }
                                break;

                                case "LIST": {
                                    String[] users = line.split("~");
                                    for (int i = 1; i < users.length; i++) {
                                        String[] userDetail = users[i].split(",");
                                        String userName = userDetail[0];
                                        System.out.println(userName);
                                        String userPublicKeyBase64 = userDetail[1];
                                        System.out.println(userPublicKeyBase64);
                                        userKeys.putIfAbsent(userName, userPublicKeyBase64);
                                        SecretKey userSecret = DiffieHellmanKeyExchange.performKeyExchange(userPublicKeyBase64, getKeyPair());
                                        secretKeys.putIfAbsent(userName, userSecret);
                                        System.out.println(users[i] + "\n");
                                    }
                                    System.out.println(userKeys + "\n");
                                    System.out.println("SECRET" + secretKeys);
                                }
                                break;

                                case "TEXT": {
                                    String[] params = line.split("~");
                                    System.out.println("\n" + E2EE.decrypt(params[2], secretKeys.get(params[1])));
                                    updateChatViewOnUiThread("Message from " + command[1] + " " + E2EE.decrypt(params[2], secretKeys.get(params[1])));
                                }
                                break;
                                case "IMAGE": {
                                      System.out.println(line);
//                                    //process the image
                                      updateImageViewOnUiThread(ImageUtils.reformImage(command[2].getBytes()));

//                                    String[] params = line.split("~");
//                                    System.out.println("\n" + E2EE.decrypt(params[2], secretKeys.get(params[1])));
//                                    updateReceivedViewOnUiThread("Message from " + command[1] + " " + E2EE.decrypt(params[2], secretKeys.get(params[1])));
//
                                }
                                break;
                                default:
                                    System.out.println(line + " is not recognized.");
                            }
                        }
                    }
                } catch (SocketException e) {
                    System.out.println("Connection with the server was lost. Thank you for playing!");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
    }

    public void sendCommand(String command) {
        try {
            writer.write(command);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            // Handle exceptions or close the connection as needed
            close();
        }
    }
    public void close() {
        try {
            System.out.println("Connection lost");
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Initiates the hello sequence by sending a hello message with the client description,
     * and wait until isHelloReceived returns true.
     *
     * @param clientDescription String
     * @throws IOException Exception
     */
    //@ requires clientDescription.equals(null);
    //@ ensures isHelloReceived() && clientDescription.equals(description);
    public void initiateHello(String clientDescription) throws IOException {
        sendCommand("HELLO~" + clientDescription);
        while (true) {
            if (isHelloReceived()) {
                break;
            }
        }
    }

    /**
     * Initiates the login sequence by sending a login message with the client's name,
     * and wait until isLoggedIn and isAlready returns true.
     *
     * @throws IOException Exception
     */
    //@ requires user.equals(null);
    //@ ensures isLoggedIn() && isAlready() && name.equals(user);
    public void initiateLogin(String username, String password) throws IOException {
        already = false;
        loggedIn = false;
//        setName(username);
        sendCommand("LOGIN~" + username + "~" +  password);
        while (true) {
            if (isLoggedIn()) {
                break;          //if already logged in, login again
            }
            if (isAlready()) {
                break;
            }
        }
    }
    public synchronized boolean isHelloReceived() {
        return helloReceived;
    }

    /**
     * Returns true if the loggedIn boolean is true, false if it is false.
     *
     * @return loggedIn boolean
     */
    public synchronized boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * Returns the already boolean.
     *
     * @return already boolean
     */
    public synchronized boolean isAlready() {
        return already;
    }

    //@ ensures \result.equals(name);
    public String getName() {
        return username;
    }

    /**
     * Sets the client's name.
     *
     * @param name String
     */
    public synchronized void setName(String name) {
        this.username = name;
    }


    public synchronized void logout() {
        loggedIn = false;
        already = false;
    }

    public synchronized KeyPair getKeyPair(){
        return keyPair;
    }

    public synchronized void setKeyPair(KeyPair generatedKeyPair) {
        this.keyPair = generatedKeyPair;
    }

    public synchronized SecretKey getSharedSecret(String recipient){
        return secretKeys.get(recipient);
    }


}
