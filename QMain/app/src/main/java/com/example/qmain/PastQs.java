package com.example.qmain;

import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Button;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import android.content.Intent;
import android.content.Context;
import android.widget.ScrollView;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import android.view.MotionEvent;
import android.widget.CompoundButton;


public class PastQs extends AppCompatActivity {
    public final static String RESULTS = "";
    Context context = this;
    static Boolean isTouched = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_qs);
        ScrollView sv = (ScrollView) findViewById(R.id.activity_past_qs);
        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        sv.addView(layout);
        final HashMap<String,Button> date_hm = new HashMap<>();
        final HashMap<String,Button> city_hm = new HashMap<>();

        String title = "Past Questionnaires";
        getSupportActionBar().setTitle(title);


        String[] files = fileList();

        if(files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                String month = files[i].substring(0,8);
                if(!(files[i].endsWith("jpeg") || files[i].endsWith("json") || files[i].endsWith("txt"))){
                    System.out.println(files[i]);
                    continue;
                }
                if(!month.substring(0,4).equals("JPEG") && !files[i].endsWith("json")){

                    System.out.println(files[i]);
                    String btext;

                    if(files[i].substring(files[i].length()-1).equals("g")){
                        btext = files[i].substring(0, files[i].length()-5);
                    }
                    else {
                        btext = files[i].substring(0, files[i].length()-4);
                    }

                    Button bt = new Button(this);
                    btext = btext.replace("_", " ");
                    bt.setText(btext);
                    bt.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                            ActionBar.LayoutParams.WRAP_CONTENT));

                    bt.setOnClickListener(new View.OnClickListener() {
                                                  public void onClick(View v) {
                                                      Button b = (Button) v;
                                                      System.out.println(b.getText());
                                                      String btxt = (String) b.getText();
                                                      btxt = btxt.replace(" ", "_");
                                                      String filename = btxt + ".txt";
                                                      String jpeg = btxt + ".jpeg";
                                                      String jsonfile = btxt + ".json";
                                                      String jsonstring = "";
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
                                                          String line;
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
                                                      String date = btxt;
                                                      System.out.println(btxt);
                                                      date = date.substring(date.lastIndexOf("__")+2);
                                                      intent.putExtra("date", date);
                                                      intent.putExtra("json", jsonstring);
                                                      startActivity(intent);
                                                  }
                                              }
                    );
                    String date = (String) bt.getText();
                    System.out.println(date);
                    if(date.contains("  ")) {
                        date = date.substring(date.lastIndexOf("  ") + 2, date.lastIndexOf(" ")) + date.substring(date.lastIndexOf(" "));
                    }
                    System.out.println(date);
                    date = date.replace(" ","");
                    System.out.println(date);
                    //int dn = Integer.parseInt(date);
                    date_hm.put(date,bt);
                    String city = (String) bt.getText();
                    try {
                        city = city.substring(0, city.lastIndexOf("  "));
                    }catch(StringIndexOutOfBoundsException e){
                        city = city.substring(city.indexOf(" ")+1);
                    }
                    city_hm.put(city,bt);
                }
            }
        }



        final LinearLayout by_date = new LinearLayout(this);
        final LinearLayout by_alpha = new LinearLayout(this);
        by_date.setOrientation(LinearLayout.VERTICAL);
        by_alpha.setOrientation(LinearLayout.VERTICAL);
        by_date.setBackgroundColor(Color.CYAN);
        by_alpha.setBackgroundColor(Color.GREEN);

        final SortedSet<String> dates = new TreeSet<>(date_hm.keySet());
        final SortedSet<String> cities = new TreeSet<>(city_hm.keySet());

        SwitchCompat sc = new SwitchCompat(this);
        sc.setTextOff("Alphabetically");
        sc.setTextOn("By Date");
        sc.setShowText(true);

        layout.addView(sc);
        layout.addView(by_date);
        layout.addView(by_alpha);

        for(String city: cities){
            by_alpha.addView(city_hm.get(city));
            System.out.println(city);
        }

        sc.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                isTouched = true;
                return false;
            }
        });

        sc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isTouched) {
                    isTouched = false;
                    if (isChecked) {
                        by_alpha.removeAllViews();
                        by_date.removeAllViews();
                        for(String date: dates){
                            System.out.println(date);
                            by_date.addView(date_hm.get(date));
                        }
                        System.out.println(by_date.getChildCount());
                    }
                    else {
                        by_date.removeAllViews();
                        by_alpha.removeAllViews();
                        for(String city: cities){
                            System.out.println(city);
                            by_alpha.addView(city_hm.get(city));
                        }
                        System.out.println(by_alpha.getChildCount());
                    }
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);

    }

}

// organization criterion should be read from survey metadata
