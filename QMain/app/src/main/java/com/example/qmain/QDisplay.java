package com.example.qmain;

import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;

public class QDisplay extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qdisplay);
        final LinearLayout layout = (LinearLayout) findViewById(R.id.activity_qdisplay);
        final TextView displayText = new TextView(this);

        Intent intent = getIntent();
        String text = intent.getStringExtra(PastQs.RESULTS);
        final String json = intent.getStringExtra("json");
        displayText.setText(text);

        final TextView json_text = new TextView(this);
        json_text.setText(json);
        json_text.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        //layout.addView(json_text);
        //layout.addView(displayText);

        final ScrollView sv = new ScrollView(this);
        sv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        sv.addView(displayText);

        Button t = new Button(this);
        Button j = new Button(this);
        t.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try{
                    layout.removeView(json_text);
                }catch(Exception e){}
                layout.addView(sv);
            }
        });
        t.setText("Text");
        j.setText("JSON");
        j.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try{
                    layout.removeView(sv);
                }catch(Exception e){}
                layout.addView(json_text);
            }
        });
        layout.addView(t);
        layout.addView(j);


        getSupportActionBar().setTitle(intent.getStringExtra("date"));
        System.out.println(text);
        System.out.println("***");
        if(text.substring(0,4).equals("JPEG") || true){
            Bitmap myBitmap = (Bitmap) intent.getParcelableExtra("bitmap");
            ImageView myImage = new ImageView(this);
            myImage.setImageBitmap(myBitmap);
            layout.addView(myImage);

        }


    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, PastQs.class);
        startActivity(intent);

    }
}
