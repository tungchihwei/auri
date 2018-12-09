package com.green.auri;

import android.content.Context;
import android.content.res.Resources;
import android.os.StrictMode;
import android.util.Log;

import com.green.auri.favorites.FavoriteDetail;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MyHttp {

    URL url;
    HttpURLConnection urlConnection;

    public String httpGet(String id, Context myContext)
    {
        String result = "";

        // Set the policy
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        StringBuffer response = new StringBuffer();
        HttpURLConnection c = null;

        String PlaceApiKey = myContext.getResources().getString(R.string.place_api_key);

        try{
            url = new URL("https://maps.googleapis.com/maps/api/place/details/json?placeid=" + id + "&fields=reviews&key=" + PlaceApiKey);
        }catch (MalformedURLException e){
            throw new IllegalArgumentException("invalid url");
        }
        try {
            // Http connection and request
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            // Read the result
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sBuilder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sBuilder.append(line);
            }
            result = sBuilder.toString();
        }catch( Exception e) {
            e.printStackTrace();
        }
        finally {
            // Http disconnect
            urlConnection.disconnect();
        }
        return result;
    }
}
