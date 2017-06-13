package com.example.qmain;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.ArrayList;
import java.lang.reflect.Field;
import java.io.File;

import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.content.Intent;
import android.content.Context;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.StringRequest;
import java.io.FileOutputStream;
import com.android.volley.RequestQueue;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;

import android.widget.EditText;
import java.util.HashMap;

public class Projects extends AppCompatActivity {

    Context context = this;
    HashMap<String, Integer> ProjectCodes = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);
        /*
        something something querying database for different questionnaire versions
         */
        // dummy stand-in
        ArrayList<Object> files = new ArrayList<>();
        LinearLayout layout = (LinearLayout) findViewById(R.id.activity_projects);
        /*
        Field[] fields = R.raw.class.getFields();
        ArrayList files = new ArrayList();
        System.out.println(fields.length);
        */
        /*
        files.add("questionnaire");
        for(int i = 0; i < files.size(); i++){
            System.out.println(files.get(i));
            Button b = new Button(this);
            b.setText(files.get(i).toString());
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sync();
                    Intent intent = new Intent(context, Home.class);
                    startActivity(intent);
                }
            });
            layout.addView(b);
        }
        */

        // GET request for organizations, cities, projects
        final EditText org = new EditText(this);
        org.setHint("Organization");
        final EditText city = new EditText(this);
        city.setHint("City");
        final EditText project = new EditText(this);
        project.setHint("Project");
        layout.addView(org);
        layout.addView(city);
        layout.addView(project);
        Button select = new Button(this);
        String s = "Select";
        select.setText(s);
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sync(org.getText().toString(), city.getText().toString(), project.getText().toString());
            }
        });
        layout.addView(select);

        setupUI(layout);
        for(int j = 0; j < layout.getChildCount(); j++){
            setupUI(layout.getChildAt(j));
        }


    }

    public void sync(final String s_org, final String s_city, final String s_project){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://18.111.85.206:8001/form";
        //String url = "http://risklabdev.mit.edu:8001/form";
        url += "/"+s_org+"/"+s_city+"/"+s_project;
        String p = (s_org+s_city+s_project);
        int h;
        if(ProjectCodes.containsKey(p) && ProjectCodes.get(p) != null){
            h = ProjectCodes.get(p);
        }else{
            h = p.hashCode();
            while(ProjectCodes.containsValue(h)){
                h = p.hashCode();
            }
            ProjectCodes.put(p, h);
        }

        final String hash = Integer.toString(Math.abs(h));

        if(s_city.equals("Cambridge")){
            Intent intent = new Intent(context, Home.class);
            intent.putExtra("action_bar", s_project);
            System.out.println(hash);
            intent.putExtra("project", hash);
            intent.putExtra("city", s_city);
            intent.putExtra("org", s_org);
            startActivity(intent);
        }else{
            String em = "The organization, city, and/or project you are looking for does not exist. Please try again.";
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(em);
            AlertDialog alert = builder.create();
            alert.show();
        }

        /*
        StringRequest req = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        System.out.println(response);
                        String filename = "questions_from_url.xml";
                        FileOutputStream fos = null;
                        try{
                            fos = openFileOutput(filename, Context.MODE_PRIVATE);
                            fos.write(response.getBytes());
                        }catch(Exception e){
                            System.out.println(filename);
                        }

                        Intent intent = new Intent(context, Home.class);
                        intent.putExtra("action_bar", s_project);
                        System.out.println(hash);
                        intent.putExtra("project", hash);
                        intent.putExtra("city", s_city);
                        intent.putExtra("org", s_org);
                        startActivity(intent);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        // handle error response

                        error.printStackTrace();
                        String em = "The organization, city, and/or project you are looking for does not exist. Please try again.";
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage(em);
                        AlertDialog alert = builder.create();
                        alert.show();
                        /*

                        System.out.println(ProjectCodes.keySet());

                        Intent intent = new Intent(context, Home.class);
                        intent.putExtra("action_bar", s_project);
                        intent.putExtra("org", s_org);
                        intent.putExtra("city", s_city);
                        System.out.println(s_org+s_city);
                        intent.putExtra("project", hash);
                        startActivity(intent);
                        //


                    }
                }
        );

        queue.add(req);
                */
    }

    // hides soft keyboard
    public static void hideSoftKeyboard (Activity activity, View view)
    {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    // calls hideSoftKeyboard on non-EditText views
    public Activity a = this;
    public void setupUI(View view) {
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

                    hideSoftKeyboard(a, v);
                    return false;
                }
            });
        }
    }

    GoogleApiClient client = MainActivity.getClient();

    private void signOut() {
        try {
            client.connect();
            Auth.GoogleSignInApi.signOut(client);
        }catch(Exception e){}
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you would like to sign out?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        signOut();
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


// project directories