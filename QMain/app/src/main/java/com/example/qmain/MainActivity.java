package com.example.qmain;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.util.Log;

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
            if(getIntent().getStringExtra("out").equals("yes")){
                SaveSharedPreference.setUserName(MainActivity.this, "");
            }
        }catch(Exception e){}

        String saved_user = SaveSharedPreference.getUserName(MainActivity.this);
        if(saved_user.length() > 0){
            Intent intent = new Intent(this, Projects.class);
            intent.putExtra("author", saved_user);
            startActivity(intent);
        }

        setContentView(R.layout.activity_main);

        //getSupportActionBar().setTitle("Sign In");

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
            // Signed in successfully, show authenticated UI.
            String author = result.getSignInAccount().getEmail();
            SaveSharedPreference.setUserName(MainActivity.this, author);
            System.out.println(author);
            System.out.println("success");
            start(author);
        } else {
            // Signed out, show unauthenticated UI.
            System.out.println("fail");
        }
    }

    public void start(String author) {
        Intent intent = new Intent(this, Projects.class);
        intent.putExtra("author", author);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }

}

class FailedConnection implements GoogleApiClient.OnConnectionFailedListener{
    public void onConnectionFailed(ConnectionResult connectionResult){
        System.out.println("whoops");
    }
}
