package com.client.myapplication;

import com.client.myapplication.Crypto.DiffieHellmanKeyExchange;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;

public class Client {
    private static Client instance;
    BufferedReader reader;
    Socket socket = null;
    BufferedWriter writer;
    String username;
    String address = "192.168.1.167";
    int port = 8080;
    private boolean loggedIn = false;
    private boolean already = false;
    private boolean helloReceived = false;

    private KeyPair keyPair;

    private SecretKey serverSecretKey;

    private Client() {
//        try {
//            this.keyPair = DiffieHellmanKeyExchange.generateKeyPair();
//        } catch (NoSuchAlgorithmException e) {
////            throw new RuntimeException(e);
//            System.out.println("Client constructor : KeyPair cannot be generated");
//        }
        // Private constructor to enforce singleton pattern
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

                            //cases for the received commands from the client
                            switch (command[0]) {
                                case "REGISTERED":{
                                    System.out.println(line);
                                    String serverPublicKeyBase64 = command[1];

                                    //generating a secret with the server
                                    serverSecretKey = DiffieHellmanKeyExchange.performKeyExchange(serverPublicKeyBase64, keyPair);
                                    System.out.println("Generated shared secret:" + serverSecretKey);
                                }
                                case "LOGGED_IN": {
                                    System.out.println(line);
                                    loggedIn = true;
                                }
                                break;
                                case "ALREADYLOGGEDIN": {
                                    System.out.println(line);
                                    //Call for login again with a different username if already logged in
                                    already = true;
                                }
                                break;

                                case "LIST": {
                                    System.out.println(line);
                                    for (String user : line.split("~")) {
                                        System.out.println(user + "\n");
                                    }
                                }
                                break;

                                case "TEXT": {
                                    System.out.println(line);
//                            runOnUiThread(() -> updateChatView("Message from" + command[1] + command[command.length-1]));
//                              updateChatView("Message from" + command[1] + command[command.length-1]);
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
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                } catch (InvalidKeySpecException e) {
                    throw new RuntimeException(e);
                } catch (InvalidKeyException e) {
                    throw new RuntimeException(e);
                }
//                catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
////                    throw new RuntimeException(e);
//                    System.out.println("Client run method : could not create shared secret");
//                }
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
    public void setName(String name) {
        this.username = name;
    }


    public void logout() {
        loggedIn = false;
        already = false;
    }

    public KeyPair getKeyPair(){
        return keyPair;
    }

    public void setKeyPair(KeyPair generatedKeyPair) {
        this.keyPair = generatedKeyPair;
    }
}
