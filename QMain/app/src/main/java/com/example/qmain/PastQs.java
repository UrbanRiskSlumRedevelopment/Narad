package com.example.qmain;

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

public class PastQs extends AppCompatActivity {
    public final static String RESULTS = "";
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_qs);
        ScrollView sv = (ScrollView) findViewById(R.id.activity_past_qs);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        sv.addView(layout);

        String[] files = fileList();
        if(files.length > 0) {
            for (int i = 1; i < files.length; i++) {
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

                if("JPEG".equals(btext.substring(0,4))){
                    bt.setOnClickListener(new View.OnClickListener() {
                                              public void onClick(View v) {
                                                  Button b = (Button) v;
                                                  System.out.println(b.getText());
                                                  String filename = b.getText() + ".jpeg";
                                                  FileInputStream file = null;
                                                  try{
                                                      file = openFileInput(filename);
                                                  }catch(Exception e){
                                                      System.out.println("cannot open file");
                                                  }

                                                  Bitmap myBitmap = BitmapFactory.decodeStream(file);

                                                  String result = (String) b.getText();
                                                  Intent intent = new Intent(context, QDisplay.class);
                                                  intent.putExtra(RESULTS, filename);
                                                  intent.putExtra("bitmap", myBitmap);

                                                  startActivity(intent);
                                                  System.out.print(result);
                                                  // put image in putextra, display it in QDisplay

                                              }
                                          }
                    );
                    layout.addView(bt);
                }
                else{
                    bt.setOnClickListener(new View.OnClickListener() {
                                              public void onClick(View v) {
                                                  Button b = (Button) v;
                                                  System.out.println(b.getText());
                                                  String filename = b.getText() + ".txt";
                                                  FileInputStream file = null;
                                                  try{
                                                      file = openFileInput(filename);
                                                  }catch(Exception e){
                                                      System.out.println("cannot open file");
                                                  }
                                                  StringBuilder sb = new StringBuilder();
                                                  try{
                                                      BufferedReader reader = new BufferedReader(new InputStreamReader(file));
                                                      String line = null;
                                                      while ((line = reader.readLine()) != null) {
                                                          //sb.append(line).append("\n");
                                                          //System.out.println(line);

                                                          while(line.contains("~~")){
                                                              String nline = line.substring(0,line.indexOf("~~"));
                                                              line = line.substring(line.indexOf("~~")+2);
                                                              sb.append(nline).append("\n");
                                                              System.out.println(nline);
                                                          }
                                                          //sb.append(line).append("\n");
                                                          //System.out.println("i");

                                                      }
                                                      file.close();
                                                  }catch(Exception e){
                                                      e.printStackTrace();
                                                  }
                                                  String result = sb.toString();
                                                  Intent intent = new Intent(context, QDisplay.class);
                                                  intent.putExtra(RESULTS, result);
                                                  startActivity(intent);
                                                  System.out.print(result);
                                              }
                                          }
                    );
                    layout.addView(bt);
                }
            }
            System.out.println();
        }

    }

}
