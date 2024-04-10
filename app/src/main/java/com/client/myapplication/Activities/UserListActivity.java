package com.client.myapplication.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.client.myapplication.Client;
import com.client.myapplication.R;

public class UserListActivity extends AppCompatActivity {
    Client client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_list);
        Intent intent = getIntent();
        // Assume onlineUsersList is a list of online users fetched from somewhere
//        String[] onlineUsersList = {"User1", "User2", "User3", "User4", "User5", "User6", "User7", "User8", "User9", "User10"};
        String[] onlineUsersList = intent.getStringArrayExtra("users"); //accessing the list of online users -- still need to find a synchronised method for this,
        //can try creating a synchronised getter in client, and create an instance here
        System.out.println(onlineUsersList);
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

    private void startChatSession(String username) {
        // Implement logic to start a new chat session with the selected user
        // For example, you can start a new activity passing the selected username
        Intent intent = new Intent(UserListActivity.this, UserChatActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }
}
