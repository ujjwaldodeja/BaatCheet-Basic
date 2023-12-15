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
    Socket socket = null;
    BufferedWriter writer;
    String name = "Zuhi";
    String address = "145.126.96.38";
    int port = 8080;
    private TextView chatTextView;
    private EditText messageEditText;
    private EditText recipientEditText;
    private Button sendButton;
    private boolean loggedIn = false;
    private boolean already = false;
    private boolean helloReceived = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);
        chatTextView = findViewById(R.id.chatTextView);
        messageEditText = findViewById(R.id.messageEditText);
        recipientEditText = findViewById(R.id.recipientEditText);
        sendButton = findViewById(R.id.sendButton);
        System.out.println("Fucked it here");
        new ConnectTask().execute();
        sendCommand("USERNAME~" + getName());
        updateChatView("USERNAME SENT");
        sendButton.setOnClickListener(view -> sendMessage());
    }
    private class ConnectTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            return connect(); // Move the connection logic to the background thread
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                updateChatView("CONNECTION ESTABLISHED");
                startReceiving();
            } else {
                updateChatView("CONNECTION NOT MADE");
            }
        }
    }

    public void sendMessage() {
        String recipient = recipientEditText.getText().toString();
        String message = messageEditText.getText().toString();
        if (!recipient.isEmpty() && !message.isEmpty()) {
            sendCommand("TEXT~" + recipient + "~" + getName() + "~" + message);
            messageEditText.getText().clear();
            updateChatView("You to " + recipient + ": " + message);
        }
    }
    public void updateChatView(String message) {
        chatTextView.append(message + "\n");
    }

    /**
     * Connects the client to the server with the address and port.

     * @return true if the connection was successful, false if not
     */
    //@ requires address != null && port > 0;
    public boolean connect() {
        try {
            socket = new Socket(address, port);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            return true;
        } catch (IOException e) {

            return false;
        }
    }

    /**
     * Closes the connection with the server.
     */
    //@ requires !socket.isClosed();
    //@ ensures socket.isClosed();
    public void close() {
        try {
            System.out.println("Connection lost");
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class SendCommandTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String command = params[0];
            try {
                writer.write(command);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                // Handle exceptions or close the connection as needed
                close();
            }
            return null;
        }
    }
    /**
     * Sends the wanted string to the server.
     *
     * @param command string
     */
    //@ requires command.equals(null);
    public void sendCommand(String command) {
        new SendCommandTask().execute(command);
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

                                case "LOGGED_IN": {
                                    System.out.println(line);
                                    updateChatView(line);
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
                                    updateChatView(line);
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
                }
            }
        });
        thread.start();
    }

    /**
     * Returns true if the helloReceived boolean is true, false if it is false.
     *
     * @return helloReceived boolean
     */
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

    /**
     * Returns the name of the client.
     *
     * @return name String
     */
    //@ ensures \result.equals(name);
    public String getName() {
        return name;
    }

    /**
     * Sets the client's name.
     *
     * @param name String
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * Provides with the choice of choosing an AI to make moves for myPlayer.
     *
     * @throws IOException Exception
     */

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
    public void initiateLogin(String user) throws IOException {
        already = false;
        loggedIn = false;
        setName(user);
        sendCommand("LOGIN~" + user);
        while (true) {
            if (isLoggedIn()) {
                break;          //if already logged in, login again
            }
            if (isAlready()) {
                break;
            }
        }
    }
}
