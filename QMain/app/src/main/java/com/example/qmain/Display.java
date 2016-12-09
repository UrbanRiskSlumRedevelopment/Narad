package com.example.qmain;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Display extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.activity_display);
        TextView tv = new TextView(this);
        Bundle bundle = getIntent().getExtras();
        String display = bundle.getString("answers");
        tv.setText(display);
        tv.setTextSize(20);
        layout.addView(tv);
    }
}
