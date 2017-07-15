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
import java.util.HashMap;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.io.File;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Home page for project
 */
public class Home extends AppCompatActivity {
    String author = ""; // username of user

    public final static String RESULTS = "";
    Context context = this;
    static Boolean isTouched = false;
    String prjt; // unique project hash; files belonging to surveys filled out for project are identified with "hc*"+prjt
    String city; // project city
    String orga; // project organization

    /**
     * Builds home page for project
     *
     * @param savedInstanceState saved instance, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        author = getIntent().getStringExtra("author");

        final LinearLayout layout = (LinearLayout) findViewById(R.id.home_ll);
        layout.setOrientation(LinearLayout.VERTICAL);
        final HashMap<String,Button> date_hm = new HashMap<>(); // hash map mapping questionnaires/surveys' buttons to their dates
        final HashMap<String,Button> city_hm = new HashMap<>(); // hash map mapping questionnaires/surveys' buttons to their names (city, organization, project)

        // sets project name, city, and organization
        String project = getIntent().getStringExtra("project");
        System.out.println(project);
        prjt = project;
        city = getIntent().getStringExtra("city");
        orga = getIntent().getStringExtra("org");
        System.out.println(city+orga);

        String[] files = fileList();
        final String hashc = "hc*"+project;

        // sets title of page to project name
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getIntent().getStringExtra("action_bar"));
        }

        // iterates through all files available in local storage (where completed questionnaires are stored)
        // for every text file (text version of a completed questionnaire), creates a button
        // reads json and text files of questionnaire answers
        // button opens QDisplay for questionnaire with answers from files and project information for when user navigates back from QDisplay
        if(files.length > 0) {
            for(int i = 0; i < files.length; i++) {
                // skips all files not belonging to current project
                if(!files[i].contains("hc*"+project)){
                    continue;
                }
                // skips all non-jpeg, json, or txt files
                String month = files[i].substring(0,8);
                if(!(files[i].endsWith("jpeg") || files[i].endsWith("json") || files[i].endsWith("txt"))){
                    System.out.println(files[i]);
                    continue;
                }
                // if file is a text file, creates a button for its associated questionnaire
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
                                                  String filename = btxt + ".txt"; // questionnaire text file
                                                  String jsonfile = btxt + ".json"; // questionnaire json file
                                                  String jsonstring = "";
                                                  String result = "";
                                                  // attempts to open and read json and txt files
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
                                                  if(file != null && json != null) {
                                                      try {
                                                          BufferedReader reader = new BufferedReader(new InputStreamReader(file));
                                                          BufferedReader jreader = new BufferedReader(new InputStreamReader(json));
                                                          String line;
                                                          while ((line = reader.readLine()) != null) {
                                                              System.out.println(line);

                                                              while (line.contains("~~")) {
                                                                  String nline = line.substring(0, line.indexOf("~~"));
                                                                  line = line.substring(line.indexOf("~~") + 2);
                                                                  sb.append(nline).append("\n");
                                                              }
                                                          }
                                                          jsonstring = jreader.readLine();
                                                          json.close();
                                                          file.close();
                                                      } catch (Exception e) {
                                                          e.printStackTrace();
                                                          System.out.println("input stream didn't close");
                                                      }
                                                      result = sb.toString();
                                                  }
                                                  // passes json and text answers, project information to QDisplay
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
                                                  intent.putExtra("author", author);
                                                  startActivity(intent);
                                              }
                                          }
                    );
                    // gets questionnaire date, adds button to date hash map
                    String date = (String) bt.getText();
                    if(date.contains("  ")) {
                        date = date.substring(date.lastIndexOf("  ") + 2, date.lastIndexOf(" ")) + date.substring(date.lastIndexOf(" "));
                    }
                    date = date.replace(" ","");
                    System.out.println(date);
                    date_hm.put(date,bt);
                    // gets questionnaire city/organization/project, adds button to city hash map
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

        // scroll views for the linear layouts containing alphabetized and date-ordered surveys the user switches between
        ScrollView datesv = new ScrollView(this);
        ScrollView alphasv = new ScrollView(this);

        final LinearLayout by_date = new LinearLayout(this);
        final LinearLayout by_alpha = new LinearLayout(this);
        by_date.setOrientation(LinearLayout.VERTICAL);
        by_alpha.setOrientation(LinearLayout.VERTICAL);
        by_date.setBackgroundColor(Color.CYAN);
        by_alpha.setBackgroundColor(Color.GREEN);

        final SortedSet<String> dates = new TreeSet<>(date_hm.keySet()); // surveys ordered by date
        final SortedSet<String> cities = new TreeSet<>(city_hm.keySet()); // surveys ordered alphabetically

        // switch with two states, alphabetically sorted and chronologically sorted
        SwitchCompat sc = new SwitchCompat(this);
        sc.setTextOff("Alphabetically");
        sc.setTextOn("By Date");
        sc.setShowText(true);

        layout.addView(sc);
        datesv.addView(by_date);
        alphasv.addView(by_alpha);
        layout.addView(datesv);
        layout.addView(alphasv);

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

        // every time toggle changes, populates either dates view or city view with surveys in appropriate order
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

    /**
     * Opens a blank new questionnaire with project information and username
     *
     * @param view view clicked on to start action
     */
    public void newVPQ(View view){
        Intent intent = new Intent(this, PVQ.class);
        intent.putExtra("author",author);
        intent.putExtra("project", prjt);
        intent.putExtra("project_name", getIntent().getStringExtra("action_bar"));
        intent.putExtra("city", city);
        intent.putExtra("org", orga);
        startActivity(intent);
    }

    /**
     * Removes all of project's completed surveys and related files from local storage
     * removes all images from project's surveys from picture directory
     *
     * @param view view clicked to start action
     */
    public void delete_all(View view){
        // files relating to project identified with "hc*"+prjt
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
        File[] jfiles;
        if(storageDir == null){
            jfiles = new File[]{};
        }else{
            jfiles = storageDir.listFiles();
        }
        if(jfiles.length > 0){
            for(int i = 0; i<jfiles.length; i++){
                System.out.println(jfiles[i].getName());
                if(jfiles[i].getName().contains("hc*"+prjt)) {
                    jfiles[i].delete();
                }
            }
        }

        // restarts home page with cleared file list
        Intent intent = new Intent(context, Home.class);
        intent.putExtra("action_bar", getIntent().getStringExtra("action_bar"));
        intent.putExtra("project", prjt);
        intent.putExtra("city", city);
        intent.putExtra("org", orga);
        intent.putExtra("author", author);
        startActivity(intent);
        Home.this.finish();

    }

    /**
     * Asks user if they are sure they want to exit, returns user to project selection page if yes
     *
     * @param view view clicked to start action
     */
    public void exit(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you would like to leave this project?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Intent intent = new Intent(context, Projects.class);
                        intent.putExtra("out", "yes");
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        Home.this.finish();
                        System.out.println("finished");
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



