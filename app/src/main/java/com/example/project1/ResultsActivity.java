package com.example.project1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ResultsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        TextView textView = (TextView) findViewById(R.id.endScreen);
        textView.setTextSize(32);

        Intent intent = getIntent();
        String msg = intent.getStringExtra("result");
        textView.setText(msg);

        Button playAgainBtn = (Button) findViewById(R.id.playAgainBtn);
        playAgainBtn.setOnClickListener(this::onButtonClick);
    }
    public void onButtonClick(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
