package com.example.magazynieruz_mobile;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textWelcome = findViewById(R.id.textWelcome);
        String username = getIntent().getStringExtra("username");
        textWelcome.setText(getString(R.string.welcome_message, username));
    }
}
