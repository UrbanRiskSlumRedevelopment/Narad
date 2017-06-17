package com.example.qmain;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.MotionEvent;
import android.view.View;
import android.os.AsyncTask.Status;
import android.service.carrier.CarrierMessagingService.ResultCallback;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.AuthFailureError;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.RelativeLayout;

import java.io.File;
import java.util.SortedSet;
import java.util.TreeSet;

public class Home extends AppCompatActivity {
    String author = "";
    GoogleApiClient client = MainActivity.getClient();

    public final static String RESULTS = "";
    Context context = this;
    static Boolean isTouched = false;
    String[] fileArray;
    String prjt;
    String city;
    String orga;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        author = getIntent().getStringExtra("author");

        final LinearLayout layout = (LinearLayout) findViewById(R.id.home_ll);
        layout.setOrientation(LinearLayout.VERTICAL);
        final HashMap<String,Button> date_hm = new HashMap<>();
        final HashMap<String,Button> city_hm = new HashMap<>();

        String project = getIntent().getStringExtra("project");
        System.out.println(project);
        prjt = project;
        city = getIntent().getStringExtra("city");
        orga = getIntent().getStringExtra("org");
        System.out.println(city+orga);

        String[] files = fileList();
        final String hashc = "hc*"+project;

        getSupportActionBar().setTitle(getIntent().getStringExtra("action_bar"));

        if(files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                if(!files[i].contains("hc*"+project)){
                    continue;
                }
                String month = files[i].substring(0,8);
                if(!(files[i].endsWith("jpeg") || files[i].endsWith("json") || files[i].endsWith("txt"))){
                    System.out.println(files[i]);
                    continue;
                }
                if(!month.substring(0,4).equals("JPEG") && !files[i].endsWith("json")){

                    System.out.println(files[i]);
                    String btext = files[i].substring(0, files[i].indexOf("hc*"));
                    System.out.println(btext);
                    System.out.println(hashc);

                    Button bt = new Button(this);
                    btext = btext.replace("_", " ");
                    bt.setText(btext);
                    bt.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                            ActionBar.LayoutParams.WRAP_CONTENT));

                    bt.setOnClickListener(new View.OnClickListener() {
                                              public void onClick(View v) {
                                                  Button b = (Button) v;
                                                  System.out.println(b.getText());
                                                  String btxt = b.getText() + hashc;
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
                                                  intent.putExtra("project",prjt);
                                                  intent.putExtra("project_name", getIntent().getStringExtra("action_bar"));
                                                  intent.putExtra(RESULTS, result);
                                                  String date = btxt;
                                                  System.out.println(btxt);
                                                  if(date.lastIndexOf("__") != -1){
                                                      date = date.substring(date.lastIndexOf("__") + 2, date.indexOf("hc*"));
                                                  }
                                                  intent.putExtra("filename", btxt);
                                                  intent.putExtra("date", date);
                                                  intent.putExtra("json", jsonstring);
                                                  intent.putExtra("city", city);
                                                  intent.putExtra("org", orga);
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
                    date_hm.put(date,bt);
                    String city = (String) bt.getText();
                    try {
                        city = city.substring(0, city.lastIndexOf("  "));
                    }catch(StringIndexOutOfBoundsException e){
                        if(city.substring(0,1).equals(" ")){
                            city = city.substring(1);
                        }
                    }
                    city_hm.put(city.toLowerCase(),bt);
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

    public void pastQ(View view){
        Intent intent = new Intent(this, PastQs.class);
        startActivity(intent);
    }

    public void newVPQ(View view){
        Intent intent = new Intent(this, PVQ.class);
        intent.putExtra("author",author);
        intent.putExtra("project", prjt);
        intent.putExtra("project_name", getIntent().getStringExtra("action_bar"));
        intent.putExtra("city", city);
        intent.putExtra("org", orga);
        startActivity(intent);
    }

    public void sync(View view){

        String[] files = fileList();
        if (files.length > 0){
            for(int i = 0; i<files.length; i++){
                System.out.println(files[i]);
                if(files[i].contains("hc*"+prjt)) {
                    deleteFile(files[i]);
                }
            }
        }
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File[] jfiles = storageDir.listFiles();
        if(jfiles.length > 0){
            for(int i = 0; i<jfiles.length; i++){
                System.out.println(jfiles[i].getName());
                if(jfiles[i].getName().contains("hc*"+prjt)) {
                    jfiles[i].delete();
                }
            }
        }

        Intent intent = new Intent(context, Home.class);
        intent.putExtra("action_bar", getIntent().getStringExtra("action_bar"));
        intent.putExtra("project", prjt);
        startActivity(intent);




    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you would like to leave this project?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Intent intent = new Intent(context, Projects.class);
                        intent.putExtra("out", "yes");
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }

}



