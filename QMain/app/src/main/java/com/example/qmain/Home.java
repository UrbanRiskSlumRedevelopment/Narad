package com.example.qmain;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.io.File;

public class Home extends AppCompatActivity {
    String author = "";
    GoogleApiClient client = MainActivity.getClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        author = getIntent().getStringExtra("author");
        getSupportActionBar().setTitle("Home");

    }

    public void newQ(View view) {
        Intent intent = new Intent(this, Questionnaire.class);
        startActivity(intent);
    }

    public void pastQ(View view){
        Intent intent = new Intent(this, PastQs.class);
        startActivity(intent);
    }

    public void newVPQ(View view){
        Intent intent = new Intent(this, PVQ.class);
        intent.putExtra("author",author);
        startActivity(intent);
    }

    public void sync(View view){
        //TestClass.make_post("http://18.111.31.12:4001/test");

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://10.0.2.2:8011/test";

        JSONObject jo = null;
        String jsonString = "";

        for(int i = 0; i<fileList().length; i++){
            if(fileList()[i].contains("json")){
                String jsonfile = fileList()[i];
                FileInputStream json = null;
                try{
                    json = openFileInput(jsonfile);
                }catch(Exception e){
                    System.out.println("cannot open file/no json");
                }
                BufferedReader jreader = new BufferedReader(new InputStreamReader(json));
                try {
                    jsonString = jreader.readLine();
                }catch(Exception e){
                    e.printStackTrace();
                }
                /*
                try {
                    jo = new JSONObject(jsonString);
                }catch(Exception e){
                    e.printStackTrace();
                }*/
                break;
            }
        }

        //System.out.println(jo.toString());
        final String jss = jsonString; //jo.toString();

        JSONObject body = new JSONObject();
        try {
            body.put("data", jss);
        }catch(Exception e){
            e.printStackTrace();
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, body, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                System.out.println("Response: " + response.toString());
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub

            }
        });

        queue.add(jsonRequest);


        /*
        String[] files = fileList();
        if (files.length > 0){
            for(int i = 0; i<files.length; i++){
                System.out.println(files[i]);
                deleteFile(files[i]);
            }
        }
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File[] jfiles = storageDir.listFiles();
        if(jfiles.length > 0){
            for(int i = 0; i<jfiles.length; i++){
                System.out.println(jfiles[i].getName());
                jfiles[i].delete();
            }
        }
        */

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to sign out?")
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

    private void signOut() {
        try {
            client.connect();
            Auth.GoogleSignInApi.signOut(client);
        }catch(Exception e){}
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}

