package com.green.auri.utils.place;

import android.os.StrictMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlaceSearchUtils {

    private static String api_key = "AIzaSyCSSgGt6d67TiIUl0SiwEvkVkvGU1PL1-U";
    private static int PROXIMITY_RADIUS = 250; // 250 meters or 0.15 miles away

    public static String getSearchResults(String strUrl) throws IOException {
        try {
            URL url = new URL(strUrl);
            return getSearchResults(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getSearchResults(URL url) {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;

        // Set the policy
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                iStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            urlConnection.disconnect();
        }

        return data;
    }

    public static String buildQueryUrl(double latitude, double longitude, String nearbyPlace) {
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + api_key);

        return (googlePlacesUrl.toString());
    }

     public static List<HashMap<String, String>> parse(String jsonData) {
        try {
            JSONArray restaurantArray = new JSONObject(jsonData).getJSONArray("results");
            return getPlaces(restaurantArray);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static java.util.List<HashMap<String, String>> getPlaces(JSONArray jsonArray) {
        int placesCount = jsonArray.length();
        List<HashMap<String, String>> placesList = new ArrayList<>();
        HashMap<String, String> placeMap;

        for (int i = 0; i < placesCount; i++) {
            try {
                placeMap = getPlace((JSONObject) jsonArray.get(i));
                placesList.add(placeMap);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return placesList;
    }

    private static HashMap<String, String> getPlace(JSONObject googlePlaceJson) {
        HashMap<String, String> googlePlaceMap = new HashMap<String, String>();
        String placeName = "-NA-";
        String vicinity = "-NA-";
        String latitude = "";
        String longitude = "";
        String reference = "";
        String rating = "";
        String icon = "";
        String placeId = "";
        String photoRef = "";


        try {
            if (!googlePlaceJson.isNull("name")) {
                placeName = googlePlaceJson.getString("name");
            }
            if (!googlePlaceJson.isNull("vicinity")) {
                vicinity = googlePlaceJson.getString("vicinity");
            }

            if(!googlePlaceJson.isNull("photos")){
                photoRef = googlePlaceJson.getJSONArray("photos").getJSONObject(0).getString("photo_reference");
            }

            latitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");

            reference = parseField("reference", googlePlaceJson);
            rating = parseField("rating",googlePlaceJson);
            icon = parseField("icon",googlePlaceJson);
            placeId = parseField("place_id",googlePlaceJson);

            googlePlaceMap.put("place_name", placeName);
            googlePlaceMap.put("vicinity", vicinity);
            googlePlaceMap.put("lat", latitude);
            googlePlaceMap.put("lng", longitude);
            googlePlaceMap.put("reference", reference);
            googlePlaceMap.put("rating",rating);
            googlePlaceMap.put("icon",icon);
            googlePlaceMap.put("place_id", placeId);
            googlePlaceMap.put("photoRef", photoRef);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return googlePlaceMap;
    }

    /**
     * Parses a field from the given JSON object.
     * If it doesn't exists, return the empty string.
     * @param field
     * @param json
     * @return
     * @throws JSONException
     */
    private static String parseField(String field, JSONObject json) throws JSONException {
        if(json.has(field)){
            return json.getString(field);
        }
        return "";
    }

    /**
     * Filters PlaceSearchResults to only get those within certain distance.
     */
    public static List<PlaceSearchResult> filterResults(List<PlaceSearchResult> results, double maxDistance){
        List<PlaceSearchResult> filteredResults = new ArrayList<>();
        for (PlaceSearchResult result : results) {
            if (result.getDistance()<maxDistance){
                filteredResults.add(result);
            }
        }

        return filteredResults;
    }
}
