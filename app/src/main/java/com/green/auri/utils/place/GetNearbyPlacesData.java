package com.green.auri.utils.place;


import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;

/**
 * A class that extends from async task, gets the nearby places data
 * and
 */
public class GetNearbyPlacesData extends AsyncTask<Object, String, String> {
    private String googlePlacesData;
    private GoogleMap mMap;
    private String url;
    private double latitude;
    private double longitude;

    protected GetNearbyPlacesData(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;

    }

    @Override
    protected String doInBackground(Object... params) {
        try {
            mMap = (GoogleMap) params[0];
            url = (String) params[1];
            PlaceReader downloadUrl = new PlaceReader();
            googlePlacesData = downloadUrl.readUrl(url); // Used to retrieve data from URL from googlePlacesData
        } catch (Exception e) {
            e.printStackTrace();
        }
        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String result) { // after retrieving the data it is in json
        // Use the PlaceParser class to parse the json file on the stuff we want
        List<HashMap<String, String>> nearbyPlacesList;
        PlaceParser dataParser = new PlaceParser();

        nearbyPlacesList =  dataParser.parse(result); // parse json file and store in a HashMap list
        ShowNearbyPlaces(nearbyPlacesList); // Pass thru this function to show nearby restuarants on the map
    }

    private double[] RelativePosition(double lat, double lng){
        double latChange = latitude - lat;
        double lngChange = longitude - lng;
        double r = Math.sqrt(latChange*latChange + lngChange*lngChange);
        double unitLat = latChange/r;
        double unitLng = lngChange/r;

        return new double[]{unitLat, unitLng};

    }

    private void ShowNearbyPlaces(List<HashMap<String, String>> nearbyPlacesList) {

        // Iterate through the nearby places that were returned
        // and place a marker for each.
        for (int i = 0; i < nearbyPlacesList.size(); i++) {
            try {
                MarkerOptions markerOptions = new MarkerOptions();
                HashMap<String, String> googlePlace = nearbyPlacesList.get(i);
                double lat = Double.parseDouble(googlePlace.get("lat"));
                double lng = Double.parseDouble(googlePlace.get("lng"));
                String placeName = googlePlace.get("place_name");
                String vicinity = googlePlace.get("vicinity");
                String rating = googlePlace.get("rating");
                String Place_Id = googlePlace.get("place_id");

                // Set up the marker:
                LatLng latLng = new LatLng(lat, lng);
                markerOptions.position(latLng);
                markerOptions.title(placeName + " : " + vicinity + " : " + rating + " :" + Place_Id);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));


                // Add the marker and move the camera to make it visible.
                mMap.addMarker(markerOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            } catch (NullPointerException e) {
                continue;
            }
        }
    }
}
