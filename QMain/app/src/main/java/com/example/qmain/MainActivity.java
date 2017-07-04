package com.example.qmain;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.auth.api.Auth;

public class MainActivity extends AppCompatActivity {

    private static GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            // checks to see whether user has just signed out;
            // if so, updates shared preference username to an empty string
            if(getIntent().getStringExtra("out").equals("yes")){
                SaveSharedPreference.setUserName(MainActivity.this, "");
            }
        }catch(Exception e){System.out.println("no update needed");}

        // if user is already signed in (shared preference username exists), goes to project selection page
        String saved_user = SaveSharedPreference.getUserName(MainActivity.this);
        if(saved_user.length() > 0){
            Intent intent = new Intent(this, Projects.class);
            intent.putExtra("author", saved_user);
            startActivity(intent);
        }

        setContentView(R.layout.activity_main);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, new FailedConnection() /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener(){public void onClick(View v) {
            switch (v.getId()) {
                case R.id.sign_in_button:
                    signIn();
                    break;
            }
        }});


    }

    int RC_SIGN_IN = 1;

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public static GoogleApiClient getClient(){
        return mGoogleApiClient;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully
            String author;
            try {
                author = result.getSignInAccount().getEmail();
            }catch(Exception e){
                System.out.println("sign in failed");
                return;
            }
            // update shared preference username to signed in user's username
            SaveSharedPreference.setUserName(MainActivity.this, author);
            System.out.println(author);
            System.out.println("success");
            // take user to project selection
            start(author);
        } else {
            // Signed out
            System.out.println("sign in failed");
            System.out.println(result.getStatus());
        }
    }

    public void start(String author) {
        // move forward to project selection with username as author
        Intent intent = new Intent(this, Projects.class);
        intent.putExtra("author", author);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        // page refreshes when back button is pressed
        //Intent intent = new Intent(this, MainActivity.class);
        //startActivity(intent);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);

    }


}

class FailedConnection implements GoogleApiClient.OnConnectionFailedListener{
    public void onConnectionFailed(ConnectionResult connectionResult){
        System.out.println("failed connection");
    }
}
