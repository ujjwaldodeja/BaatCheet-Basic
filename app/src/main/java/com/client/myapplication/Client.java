package com.client.myapplication;

import com.client.myapplication.Activities.UserChatActivity;
import com.client.myapplication.Activities.UserListActivity;
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
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

public class Client {
    private static Client instance;
    Socket socket = null;
    BufferedWriter writer;
    String username;
    String address = "145.126.89.230";
    int port = 8080;
    private boolean loggedIn = false;
    private boolean already = false;

    private KeyPair keyPair;

    private SecretKey serverSecretKey;



    private Map<String, String> userKeys = new HashMap<>();
    private Map<String, SecretKey> secretKeys = new HashMap<>();
    private UserListActivity userListActivity;
    private Map<String, String> pending = new HashMap<>();

    public synchronized Map<String, UserChatActivity> getUserActivities() {
        return userActivities;
    }

    public void setUserActivities(Map<String, UserChatActivity> userActivities) {
        this.userActivities = userActivities;
    }

    private Map<String, UserChatActivity> userActivities = new HashMap<>();
    private Client() {
    }

    public void setUserListActivity(UserListActivity userListActivity) {
        this.userListActivity = userListActivity;
    }

    public synchronized Map<String, String> getUserKeys() {
        return userKeys;
    }

    public void addUserActivity(UserChatActivity userChatActivity){
        userActivities.putIfAbsent(userChatActivity.getRecipient(), userChatActivity);
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
                                case "REGISTERED": {
                                    String serverPublicKeyBase64 = command[1];
                                    //generating a secret with the server
//                                    serverSecretKey = DiffieHellmanKeyExchange.performKeyExchange(serverPublicKeyBase64, keyPair);
                                    System.out.println("Generated secret shared with server:" + serverSecretKey);
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
                                        String userPublicKeyBase64 = userDetail[1];
//                                        System.out.println(userPublicKeyBase64);
                                        userKeys.putIfAbsent(userName, userPublicKeyBase64);
//                                        System.out.println(users[i] + "\n");
                                    }
//                                    System.out.println(userKeys + "\n");
                                    System.out.println("SECRET" + secretKeys);
                                }
                                break;
                                case "NEW_USER" : {
                                    String[] userDetails = command[1].split(",");
                                    System.out.println(Arrays.toString(userDetails));
                                    String username = userDetails[0];
                                    String userPublicKeyBase64 = userDetails[1];
                                    System.out.println(userPublicKeyBase64);
                                    userKeys.putIfAbsent(username, userPublicKeyBase64);    //recipient public key is received
                                    sendUpdateToUserList(username);
                                }
                                break;
                                case "TEXT": {
                                    updateTextMessages(command[1], command[2]);
                                     //changed to directly accesing function
                                }
                                break;
                                case "IMAGE": {
                                    updateImageMessages(command[1], command[2], Integer.parseInt(command[3]));
                                }
                                break;
                                case "SESSION_REQUEST" : {
                                    String sender = command[1];
                                    String key = command[2];
                                    UserChatActivity currentUser = userActivities.getOrDefault(sender, null);
                                    if (currentUser != null) {
                                        currentUser.updateRecipientPublicKey(key); // new session can be created directly at the sender -- this is to make sure new keys are generated
                                        System.out.println("GREAT 1");
                                        sendCommand("SESSION_ACCEPT~" + sender + "~" + DiffieHellmanKeyExchange.getPublicKeyBase64(currentUser.getKeyPair()));
                                    } else {
                                        System.out.println("FUCK 1");
                                        System.out.println("NO USER ACTIVITY - CREATE NEW ONE");
                                        addPending(sender, key);
                                        //not at all sure if this will work or not
                                    }
                                }
                                break;
                                case "SESSION_ACCEPT" : {
                                    String sender = command[1];
                                    String key = command[2];
                                    UserChatActivity currentUser = userActivities.getOrDefault(sender, null);
                                    if (currentUser != null) {
                                        currentUser.requestAccepted(key); // the new session request was accepted by the other user
                                        System.out.println("GREAT 2");
                                    } else {
                                        System.out.println("NO USER ACTIVITY - SHOULD HAVE BEEN FOUND");
                                        System.out.println("FUCK 2");
                                    }
                                    //create new session keys with this recipient
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
                } catch (NoSuchProviderException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
    }

    private void updateTextMessages(String sender, String message){
        UserChatActivity user =  userActivities.get(sender);
        if(user!= null) {
            user.updateReceivedView(message);
        }
    }
    private void updateImageMessages(String sender, String imageText, int messageLength ){
        UserChatActivity user = userActivities.get(sender);
        if(user!= null) {
            user.updateUserChatImage(imageText, messageLength, false);
        }
    }
    public synchronized boolean ifPending(String username) {
        return pending.get(username) != null;
    }
    private synchronized void addPending(String username, String key) {
        pending.putIfAbsent(username, key);
    }

    public synchronized void removePending(String username) {
        pending.remove(username);
    }


    private void sendUpdateToUserList(String user) {
        if(userListActivity!= null) {
            userListActivity.updateList(user);
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

    public synchronized String[] getUsers() {
        System.out.println("USER_KEYS FOUND" + userKeys.keySet());
        return userKeys.keySet().toArray(new String[0]);
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

    public synchronized String getPendingKey(String recipient) {
        return pending.get(recipient);
    }
}
