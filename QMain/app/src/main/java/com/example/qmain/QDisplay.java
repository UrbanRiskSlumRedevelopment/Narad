package com.example.qmain;

import android.graphics.BitmapFactory;
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
import android.util.DisplayMetrics;
import java.io.File;
import com.android.volley.RequestQueue;
import com.android.volley.Request;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;
import android.content.Context;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.Response;
import com.android.volley.VolleyError;

public class QDisplay extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Context context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qdisplay);
        final LinearLayout layout = (LinearLayout) findViewById(R.id.activity_qdisplay);

        DisplayMetrics dm = this.getResources().getDisplayMetrics(); // for determining image dimensions
        int width = dm.widthPixels;

        Intent intent = getIntent();

        final TextView displayText = new TextView(this); // TextView containing question/answer text
        String text = intent.getStringExtra(Home.RESULTS); // survey questions/answers text
        displayText.setText(text);

        final TextView json_text = new TextView(this); // TextView containing question/answer json
        final String json = intent.getStringExtra("json"); // survey questions/answers json
        json_text.setText(json);
        json_text.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        final ScrollView sv = new ScrollView(this); // ScrollView holding question/answer text
        sv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        sv.addView(displayText);

        final ScrollView jsv = new ScrollView(this); // ScrollView holding question/answer json
        jsv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        jsv.addView(json_text);

        final ScrollView isv = new ScrollView(this); // ScrollView holding LinearLayout to display images
        isv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        LinearLayout isvll = new LinearLayout(this);
        isvll.setOrientation(LinearLayout.VERTICAL);
        isv.addView(isvll);

        Button t = new Button(this); // button for displaying text
        Button j = new Button(this); // button for displaying json
        Button im = new Button(this); // button for displaying images

        // text, json, and image buttons all remove all three ScrollViews from layout, then restores
        // the one corresponding to itself

        t.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try{
                    layout.removeView(jsv);
                    layout.removeView(sv);
                    layout.removeView(isv);
                }catch(Exception e){System.out.println("already removed");}
                layout.addView(sv);
            }
        });
        String text_string = "Text";
        String json_string = "JSON";
        String image_string = "Image";
        t.setText(text_string);
        j.setText(json_string);
        j.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try{
                    layout.removeView(sv);
                    layout.removeView(jsv);
                    layout.removeView(isv);
                }catch(Exception e){System.out.println("already removed");}
                layout.addView(jsv);
            }
        });
        layout.addView(t);
        layout.addView(j);
        im.setText(image_string);
        im.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try{
                    layout.removeView(jsv);
                    layout.removeView(sv);
                    layout.removeView(isv);
                }catch(Exception e){System.out.println("already removed");}
                layout.addView(isv);
            }
        });

        // titles survey display page after date and time at which survey was created
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(intent.getStringExtra("date"));
        }

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES); // directory where app stores pictures
        File[] images;
        if(storageDir != null) {
            images = storageDir.listFiles(); // array of all images in picture directory
        }else{
            images = new File[]{};
        }

        Bitmap imgBitmap;
        for(int i = 0; i < images.length; i++){
            // iterates through all images and opens ones belonging to survey
            if(images[i].getName().contains(intent.getStringExtra("date"))){
                // extracts image tag from image file name and displays it in TextView
                // adds TextView to linear layout for displaying images
                String iname = images[i].getName();
                System.out.println(iname);
                int istart = iname.indexOf("_t__") + 4;
                int iend = iname.indexOf("__t_");
                String itag = iname.substring(istart, iend).replace("_"," ");
                TextView tagtext = new TextView(this);
                tagtext.setText(itag);
                isvll.addView(tagtext);
                try{
                    // opens image as bitmap and adds it to linear layout
                    imgBitmap = BitmapFactory.decodeFile(storageDir+"/"+iname);
                    ImageView myImage = new ImageView(this);
                    float w = imgBitmap.getWidth();
                    float h = imgBitmap.getHeight();
                    float hw_ratio = width/w;
                    float new_h = hw_ratio*h;
                    int nh = (int) new_h;
                    Bitmap scaled = Bitmap.createScaledBitmap(imgBitmap, width, nh, true);
                    myImage.setImageBitmap(scaled);
                    isvll.addView(myImage);
                }catch(Exception e){
                    System.out.println("cannot open file/no img");
                }
            }
        }

        // adds image button if there are images belonging to survey
        if(isvll.getChildCount() > 0){
            layout.addView(im);
        }

        // sync button sends put request to server with json of survey question/answers
        Button sync = new Button(this);
        String sync_string = "Sync";
        sync.setText(sync_string);
        sync.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RequestQueue queue = Volley.newRequestQueue(context);
                String url = "http://risklabdev.mit.edu:8001/survey";

                JSONObject body = new JSONObject();
                try {
                    body.put("data", json);
                }catch(Exception e){
                    e.printStackTrace();
                }

                JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.PUT, url, body, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("Response: " + response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        System.out.println("push failed");

                    }
                });

                queue.add(jsonRequest);
            }
        });

        layout.addView(sync);

        // delete button deletes survey's text file and json file from local storage
        final String filename = getIntent().getStringExtra("filename");
        final Button del = new Button(this);
        String del_string = "Delete";
        del.setText(del_string);
        del.setOnClickListener(new View.OnClickListener(){
            public void onClick(View w){
                System.out.println(filename+".txt");
                System.out.println(deleteFile(filename+".txt"));
                System.out.println(deleteFile(filename+".json"));
                onBackPressed();
            }
        }
        );

        layout.addView(del);



    }

    // returns user to project home page using project information from project home page
    // passed from home page when opening survey page
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Home.class);
        intent.putExtra("project", getIntent().getStringExtra("project"));
        intent.putExtra("action_bar", getIntent().getStringExtra("project_name"));
        intent.putExtra("city", getIntent().getStringExtra("city"));
        intent.putExtra("org", getIntent().getStringExtra("org"));
        startActivity(intent);

    }
}
