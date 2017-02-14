package com.example.qmain;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import java.lang.reflect.Array;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        startActivity(intent);
    }

    public void sync(View view){
        String[] files = fileList();
        if (files.length > 0){
            for(int i = 0; i<files.length; i++){
                System.out.println(files[i]);
                deleteFile(files[i]);
            }
        }
    }
}
