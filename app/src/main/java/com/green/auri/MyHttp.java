package com.green.auri;

import android.content.res.Resources;
import android.os.StrictMode;
import android.util.Log;

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

    public String httpGet(String id)
    {
        String result = "";

        // Set the policy
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        StringBuffer response = new StringBuffer();
        HttpURLConnection c = null;
        String PlaceApiKey = "AIzaSyCYC1v9VdeXlgssng4wjyEZD2OghGRHwac\n";

        try{
            url = new URL("https://maps.googleapis.com/maps/api/place/details/json?placeid=" + id + "&fields=reviews&key=" + PlaceApiKey);
            Log.i("jsons", "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + id + "&fields=reviews&key=" +PlaceApiKey);
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
