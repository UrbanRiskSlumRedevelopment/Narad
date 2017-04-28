package com.example.qmain;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Button;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import android.content.Intent;
import android.content.Context;
import android.widget.ScrollView;
import java.io.File;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import java.util.HashMap;
import android.widget.TextView;
import java.util.ArrayList;

public class PastQs extends AppCompatActivity {
    public final static String RESULTS = "";
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_qs);
        ScrollView sv = (ScrollView) findViewById(R.id.activity_past_qs);
        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        sv.addView(layout);
        final HashMap<String,LinearLayout> hm = new HashMap<String,LinearLayout>();

        String title = "Past Questionnaires";
        getSupportActionBar().setTitle(title);


        String[] files = fileList();

        if(files.length > 0) {
            for (int i = 1; i < files.length; i++) {
                String month = files[i].substring(0,8);
                if(!(files[i].endsWith("jpeg") || files[i].endsWith("json") || files[i].endsWith("txt"))){
                    System.out.println(files[i]);
                    continue;
                }
                if(!month.substring(0,4).equals("JPEG") && !files[i].endsWith("json")){
                    LinearLayout lo;
                    if(hm.containsKey(month)){
                        lo = hm.get(month);
                    }else{
                        lo = new LinearLayout(this);
                        lo.setOrientation(LinearLayout.VERTICAL);
                        TextView tv = new TextView(this);
                        tv.setText(month.substring(0,4)+"-"+month.substring(4,6)+"-"+month.substring(6,8));
                        lo.addView(tv);
                        hm.put(month, lo);
                    }

                    System.out.println(files[i]);
                    String btext;
                    if(files[i].substring(files[i].length()-1).equals("g")){
                        btext = files[i].substring(0, files[i].length()-5);
                    }
                    else {
                        btext = files[i].substring(0, files[i].length()-4);
                    }
                    Button bt = new Button(this);
                    bt.setText(btext);
                    bt.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                            ActionBar.LayoutParams.WRAP_CONTENT));

                    bt.setOnClickListener(new View.OnClickListener() {
                                                  public void onClick(View v) {
                                                      Button b = (Button) v;
                                                      System.out.println(b.getText());
                                                      String filename = b.getText() + ".txt";
                                                      String jpeg = b.getText() + ".jpeg";
                                                      String jsonfile = b.getText() + ".json";
                                                      String jsonstring = "";
                                                      jpeg = "JPEG_"+jpeg;
                                                      FileInputStream file = null;
                                                      try{
                                                          file = openFileInput(filename);
                                                      }catch(Exception e){
                                                          System.out.println("cannot open file");
                                                      }
                                                      FileInputStream json = null;
                                                      try{
                                                          json = openFileInput(jsonfile);
                                                      }catch(Exception e){
                                                          System.out.println("cannot open file/no json");
                                                      }
                                                      StringBuilder sb = new StringBuilder();
                                                      try{
                                                          BufferedReader reader = new BufferedReader(new InputStreamReader(file));
                                                          BufferedReader jreader = new BufferedReader(new InputStreamReader(json));
                                                          String line = null;
                                                          while ((line = reader.readLine()) != null) {
                                                              System.out.println(line);

                                                              while(line.contains("~~")){
                                                                  String nline = line.substring(0,line.indexOf("~~"));
                                                                  line = line.substring(line.indexOf("~~")+2);
                                                                  sb.append(nline).append("\n");
                                                              }
                                                          }
                                                          jsonstring = jreader.readLine();
                                                          json.close();
                                                          file.close();
                                                      }catch(Exception e){
                                                          e.printStackTrace();
                                                          System.out.println("input stream didn't close");
                                                      }

                                                      String result = sb.toString();
                                                      Intent intent = new Intent(context, QDisplay.class);
                                                      intent.putExtra(RESULTS, result);
                                                      intent.putExtra("date", b.getText());
                                                      intent.putExtra("json", jsonstring);
                                                      startActivity(intent);
                                                  }
                                              }
                    );
                    lo.addView(bt);


                }
            }
            System.out.println();
        }

        for(String month: hm.keySet()){
            Button m = new Button(this);
            m.setText(month.substring(0,4)+"-"+month.substring(4,6)+"-"+month.substring(6));
            m.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Button b = (Button) v;
                    System.out.println(b.getText());
                    String key = (String)b.getText();
                    key = key.substring(0,4)+key.substring(5,7)+key.substring(8,10);
                    for(int i = 0;i<layout.getChildCount();i++){
                        View view = layout.getChildAt(i);
                        if(view instanceof LinearLayout){
                            layout.removeView(view);
                            i -= 1;
                        }
                    }
                    layout.addView(hm.get(key));

                }
            });
            layout.addView(m);
        }

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);

    }

}
