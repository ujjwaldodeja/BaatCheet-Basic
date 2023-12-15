package com.client.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
//    private static TextView chatTextView;
//    private EditText messageEditText;
//    private EditText recipientEditText;
    private Button chatButton;
    private Button quitButton;
    String description = "Bakchodi karni hai";
    String name = "Ujjwal";
//    private BufferedReader reader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatButton = findViewById(R.id.chatButton);

//        quitButton = findViewById(R.id.quitButton);
        System.out.println("Done");
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ChatActivity.class);
                startActivity(i);
            }
        });
    }

}
