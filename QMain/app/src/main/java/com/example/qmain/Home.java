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

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;

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
        String url = "http://18.111.31.12:4001/test";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        System.out.println("Response is: "+ response.substring(0,500));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                System.out.println("That didn't work!");
            }
        });

        queue.add(stringRequest);

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

