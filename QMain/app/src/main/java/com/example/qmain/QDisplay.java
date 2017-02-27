package com.example.qmain;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;

public class QDisplay extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qdisplay);
        LinearLayout layout = (LinearLayout) findViewById(R.id.activity_qdisplay);
        TextView displayText = new TextView(this);

        Intent intent = getIntent();
        String text = intent.getStringExtra(PastQs.RESULTS);
        displayText.setText(text);
        layout.addView(displayText);
        if(text.substring(0,4).equals("JPEG")){

        }
    }
}
