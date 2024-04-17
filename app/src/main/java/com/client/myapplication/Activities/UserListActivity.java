package com.client.myapplication.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.client.myapplication.Client;
import com.client.myapplication.R;

import java.util.Arrays;

public class UserListActivity extends AppCompatActivity {
    Client client;
    String[] onlineUsersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_list);

        client = Client.getInstance();
        client.setUserListActivity(this);
        if(client!= null) {
            new SendCommandTask().execute("LIST");
        }           //asks for the online users once the activity is started
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        onlineUsersList = client.getUsers();
        System.out.println(Arrays.toString(onlineUsersList));

        //can try creating a synchronised getter in client, and create an instance here
        System.out.println("USER LIST RECEIVED~" + Arrays.toString(onlineUsersList));
        LinearLayout userButtonContainer = findViewById(R.id.userButtonContainer);
        // Create a button for each online user and add it to the layout
        for (String username : onlineUsersList) {
            Button userButton = new Button(this);
            userButton.setText(username);
            userButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Handle button click to start a chat session with this user
                    startChatSession(username);
                }
            });
            // Add the button to the LinearLayout
            userButtonContainer.addView(userButton);
        }
    }

    public void updateList(String username) {
        //        onlineUsersList  // add the new user to online user list array
        runOnUiThread(() -> {
            LinearLayout userButtonContainer = findViewById(R.id.userButtonContainer);
            Button userButton = new Button(this);
            userButton.setText(username);
            userButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Handle button click to start a chat session with this user
                    startChatSession(username);
                }
            });
            // Add the button to the LinearLayout
            userButtonContainer.addView(userButton);
        });
    }


    private void startChatSession(String username) {
        // Implement logic to start a new chat session with the selected user
        // For example, you can start a new activity passing the selected username
        Intent intent = new Intent(UserListActivity.this, UserChatActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    private class SendCommandTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            client.sendCommand(params[0]);
            return null;
        }
    }
}
