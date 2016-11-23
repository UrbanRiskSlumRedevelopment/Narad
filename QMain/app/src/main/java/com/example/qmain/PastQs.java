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

public class PastQs extends AppCompatActivity {
    public final static String RESULTS = "";
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_qs);

        LinearLayout layout = (LinearLayout) findViewById(R.id.activity_past_qs);

        String[] files = fileList();
        if(files.length > 0) {
            for (int i = 1; i < files.length; i++) {
                System.out.println(files[i]);
                String btext = files[i].substring(0, files[i].length() - 4);
                Button bt = new Button(this);
                bt.setText(btext);
                bt.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                        ActionBar.LayoutParams.WRAP_CONTENT));

                bt.setOnClickListener(new View.OnClickListener() {
                                          public void onClick(View v) {
                                              Button b = (Button) v;
                                              System.out.println(b.getText());
                                              String filename = b.getText() + ".txt";
                                              FileInputStream file = null;
                                              try{
                                                  file = openFileInput(filename);
                                              }catch(Exception e){}
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
            System.out.println();
        }

    }

}
