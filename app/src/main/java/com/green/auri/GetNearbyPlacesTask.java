package com.green.auri;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.green.auri.utils.PlaceSearchListener;
import com.green.auri.utils.PlaceSearchUtils;

import java.util.HashMap;
import java.util.List;

public class GetNearbyPlacesTask extends AsyncTask<Object, List<HashMap<String, String>>, List<HashMap<String, String>>> {
    // a class that extends from async task
    // gets nearby place data and our case is when the user pressed the "Restaurant" button
    private String googlePlacesData;
    private String url;
    private PlaceSearchListener psl;

    @Override
    public List<HashMap<String, String>> doInBackground(Object... params) {
        try {
            Log.d("GetNearbyPlacesTask", "doInBackground entered");
            url = (String) params[0];
            psl = (PlaceSearchListener) params[1];
            googlePlacesData = PlaceSearchUtils.getSearchResults(url); // Used to retrieve data from URL from googlePlacesData
            Log.d("GooglePlacesReadTask", "doInBackground Exit");
        } catch (Exception e) {
            Log.d("GooglePlacesReadTask", e.toString());
        }

        List<HashMap<String, String>> nearbyPlacesList;
        nearbyPlacesList = PlaceSearchUtils.parse(googlePlacesData); // parse json file and store in a HashMap
        Log.i("POSITIONED", String.valueOf(nearbyPlacesList));

        return nearbyPlacesList;
    }

    @Override
    public void onPostExecute(List<HashMap<String, String>> result) { // after retrieving the data it is in json
        // so we use the DataParser class to parse the json file on the stuff we want
        Log.d("GooglePlacesReadTask", "onPostExecute Entered");
        psl.onPlaceSearchComplete(result);
        Log.d("GooglePlacesReadTask", "onPostExecute Exit");
    }
}
