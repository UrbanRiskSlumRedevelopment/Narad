package com.example.qmain;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.ArrayList;
import java.lang.reflect.Field;
import java.io.File;

import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.content.Context;
import android.widget.LinearLayout;

public class Projects extends AppCompatActivity {

    Context context = this;

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
        files.add("questionnaire");
        for(int i = 0; i < files.size(); i++){
            System.out.println(files.get(i));
            Button b = new Button(this);
            b.setText(files.get(i).toString());
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, Home.class);
                    startActivity(intent);
                }
            });
            layout.addView(b);
        }

    }
}
