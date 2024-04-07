package com.client.myapplication.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.client.myapplication.R;

public class MenuActivity extends AppCompatActivity {
    Button stegChat;
    Button chat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_menu);
        stegChat = findViewById(R.id.stegChatButton);
        chat = findViewById(R.id.chatButton);


        stegChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MenuActivity.this, StegChatActivity.class);   // replace with opening MenuActivity
                startActivity(i);
            }
        });

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MenuActivity.this, ChatActivity.class);   // replace with opening MenuActivity
                startActivity(i);
            }
        });
    }

}
