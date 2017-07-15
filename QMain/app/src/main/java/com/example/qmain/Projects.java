package com.example.qmain;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.ArrayList;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.content.Intent;
import android.content.Context;
import android.widget.LinearLayout;
import com.android.volley.toolbox.Volley;
import com.android.volley.RequestQueue;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import android.widget.EditText;
import java.util.HashMap;

/**
 * Project selection page
 */
public class Projects extends AppCompatActivity {

    Context context = this;
    HashMap<String, Integer> ProjectCodes = new HashMap<>();
    String author;  // user username

    /**
     * Builds project selection page
     *
     * @param savedInstanceState saved instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            // checks to see whether user has just left a project
            // when user leaves project, projects page activity is started with intent extra "out" as "yes"
            // if so, updates shared preference project information to empty strings
            if (getIntent().getStringExtra("out").equals("yes")) {
                SaveSharedPreference.setProjectInfo(Projects.this, "", "", "", "");
            }
        }catch(Exception e){System.out.println("no update needed");}
        // checks to see whether a project has previously been selected and not exited from;
        // if it has, its info will be available from shared preference, app proceeds to project main page
        ArrayList<String> pinfo = SaveSharedPreference.getProjectInfo(Projects.this);
        author = SaveSharedPreference.getUserName(Projects.this);  // username stored in SharedPreferences when user signs in
        if(pinfo.size() == 4){
            Intent intent = new Intent(context, Home.class);
            intent.putExtra("action_bar", pinfo.get(0));
            intent.putExtra("project", pinfo.get(1));
            intent.putExtra("city", pinfo.get(2));
            intent.putExtra("org", pinfo.get(3));
            intent.putExtra("author", author);
            startActivity(intent);
            this.finish();
        }
        setContentView(R.layout.activity_projects);

        LinearLayout layout = (LinearLayout) findViewById(R.id.activity_projects);

        // EditTexts in which user enters organization, city, and project
        final EditText org = new EditText(this);
        org.setHint("Organization");
        final EditText city = new EditText(this);
        city.setHint("City");
        final EditText project = new EditText(this);
        project.setHint("Project");
        layout.addView(org);
        layout.addView(city);
        layout.addView(project);

        // hitting select button calls sync() with what is currently in the EditTexts as parameters
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

        // sign out button signs user out and takes them back to the sign in page
        Button signout = new Button(this);
        String so = "Sign Out";
        signout.setText(so);
        signout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestSignOut();
            }
        });
        layout.addView(signout);

        // sets up UI for all views on page (hides soft keyboard if view is clicked and is not an EditText)
        setupUI(layout);
        for(int j = 0; j < layout.getChildCount(); j++){
            setupUI(layout.getChildAt(j));
        }


    }

    /**
     * Given the organization, city, and project, forms a url and sends a get request for the project
     * questionnaire at the url
     *
     * @param s_org organization
     * @param s_city city
     * @param s_project project
     */
    public void sync(final String s_org, final String s_city, final String s_project){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://risklabdev.mit.edu:8001/form";
        url += "/"+s_org+"/"+s_city+"/"+s_project;

        // assigns a unique local hash code for the project
        // checks to see if a hash code has already been generated, if not generates a new one
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

        // code below is dummy stand-in code that does not query the database for questionnaires
        // as long as the user enters "Cambridge" as city, they can proceed to whatever project they specify
        // app loads questionnaire from local file questionnaire_with_nums.xml, proceeds to project home page
        //*****************************************************************
        if(s_city.equals("Cambridge")){
            Intent intent = new Intent(context, Home.class);
            intent.putExtra("action_bar", s_project);
            System.out.println(hash);
            intent.putExtra("project", hash);
            intent.putExtra("city", s_city);
            intent.putExtra("org", s_org);
            intent.putExtra("author", author);
            SaveSharedPreference.setProjectInfo(Projects.this, s_project, hash, s_city, s_org);
            startActivity(intent);
            this.finish();
        }else{
            String em = "The organization, city, and/or project you are looking for does not exist. Please try again.";
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(em);
            AlertDialog alert = builder.create();
            alert.show();
        }
        //*****************************************************************

        // below is the actual code for sending a request to the server for a questionnaire
        // writes questionnaire into local file, proceeds to project home page if request successful
        // sends over project information (city, organization, project hash, project name)
        // generates error message if request not successful
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
                        intent.putExtra("author", author);
                        SaveSharedPreference.setProjectInfo(Projects.this, s_project, hash, s_city, s_org);
                        startActivity(intent);
                        this.finish();  // user cannot navigate back to page unless they exit their project
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
                    }
                }
        );

        queue.add(req);
                */
    }

    /**
     * hides soft keyboard
     *
     * @param activity current activity
     * @param view view interacted with
     */
    public static void hideSoftKeyboard (Activity activity, View view)
    {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    public Activity a = this;

    /**
     * If view is not EditText, sets onTouchListener that closes soft keyboard when view is interacted with
     *
     * @param view view to be monitored
     */
    public void setupUI(View view) {
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(a, v);
                    return false;
                }
            });
        }
    }

    GoogleApiClient client = MainActivity.getClient();

    /**
     * Signs user out and takes them to sign in page
     */
    private void signOut() {
        try {
            client.connect();
            Auth.GoogleSignInApi.signOut(client);
        }catch(Exception e){
            System.out.println("sign out failed");
            return;
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("out", "yes");
        startActivity(intent);
        Projects.this.finish();
    }

    /**
     * Prompts user if they are sure they want to sign out, calls signOut() if sure
     */
    public void requestSignOut() {
        // builds dialog with yes and no options
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

