package com.example.qmain;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
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
import java.io.FileInputStream;
import android.util.DisplayMetrics;
import java.io.File;
import android.app.Activity;

public class QDisplay extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qdisplay);
        final LinearLayout layout = (LinearLayout) findViewById(R.id.activity_qdisplay);
        final TextView displayText = new TextView(this);
        DisplayMetrics dm = this.getResources().getDisplayMetrics();
        int width = dm.widthPixels;

        Intent intent = getIntent();
        String text = intent.getStringExtra(PastQs.RESULTS);
        final String json = intent.getStringExtra("json");
        displayText.setText(text);

        final TextView json_text = new TextView(this);
        json_text.setText(json);
        json_text.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        final ScrollView sv = new ScrollView(this);
        sv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        sv.addView(displayText);

        final ScrollView jsv = new ScrollView(this);
        jsv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        jsv.addView(json_text);

        final ScrollView isv = new ScrollView(this);
        isv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        LinearLayout isvll = new LinearLayout(this);
        isvll.setOrientation(LinearLayout.VERTICAL);
        isv.addView(isvll);

        Button t = new Button(this);
        Button j = new Button(this);
        t.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try{
                    layout.removeView(jsv);
                    layout.removeView(sv);
                    layout.removeView(isv);
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
                    layout.removeView(jsv);
                    layout.removeView(isv);
                }catch(Exception e){}
                layout.addView(jsv);
            }
        });
        layout.addView(t);
        layout.addView(j);

        Button im = new Button(this);
        im.setText("Image");
        im.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try{
                    layout.removeView(jsv);
                    layout.removeView(sv);
                    layout.removeView(isv);
                }catch(Exception e){}
                layout.addView(isv);
            }
        });


        getSupportActionBar().setTitle(intent.getStringExtra("date"));

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File[] images = storageDir.listFiles();
        FileInputStream img = null;
        Bitmap imgBitmap = null;
        for(int i = 0; i < images.length; i++){
            if(images[i].getName().contains(intent.getStringExtra("date"))){
                String iname = images[i].getName();
                System.out.println(iname);
                int istart = iname.indexOf("_t__") + 4;
                int iend = iname.indexOf("__t_");
                String itag = iname.substring(istart, iend).replace("_"," ");
                TextView tagtext = new TextView(this);
                tagtext.setText(itag);
                isvll.addView(tagtext);
                try{
                    imgBitmap = BitmapFactory.decodeFile(storageDir+"/"+iname);
                    ImageView myImage = new ImageView(this);
                    float w = imgBitmap.getWidth();
                    float h = imgBitmap.getHeight();
                    float hw_ratio = width/w;
                    float new_h = hw_ratio*h;
                    int nh = (int) new_h;
                    Bitmap scaled = Bitmap.createScaledBitmap(imgBitmap, width, nh, true);
                    myImage.setImageBitmap(scaled);
                    myImage.setBackgroundColor(Color.CYAN);
                    isvll.addView(myImage);
                }catch(Exception e){
                    System.out.println("cannot open file/no img");
                }
            }
        }

        if(isvll.getChildCount() > 0){
            layout.addView(im);
        }


    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, PastQs.class);
        startActivity(intent);

    }
}
