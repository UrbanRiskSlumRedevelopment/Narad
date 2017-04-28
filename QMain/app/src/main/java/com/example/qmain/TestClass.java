package com.example.qmain;

import android.os.AsyncTask;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Erica on 4/26/17.
 */

public abstract class TestClass extends AsyncTask<Void, Void, Void> {

    private Exception exception;

    protected void doInBackground(String... urls) {
        System.out.println("background");
    }

    public static void make_post(String urls){
        HttpURLConnection client = null;
        try {
            URL url = new URL(urls);

            client = (HttpURLConnection) url.openConnection();
            client.setRequestMethod("POST");
            client.setRequestProperty("one", "two three");
            client.setDoOutput(true);

            OutputStream outputPost = new BufferedOutputStream(client.getOutputStream());
            //writeStream(outputPost);
            outputPost.flush();
            outputPost.close();

            //client.setFixedLengthStreamingMode(outputPost.getBytes().length);
            //client.setChunkedStreamingMode(0);

        }catch(MalformedURLException e){
            System.out.println("url failed");
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if(client != null) // Make sure the connection is not null.
                client.disconnect();
        }
    }


}
