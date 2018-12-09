package com.green.auri;


import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;

public class GetNearbyPlacesData extends AsyncTask<Object, String, String> {
    // a class that extends from async task
    // gets nearby place data and our case is when the user pressed the "Restaurant" button
    private String googlePlacesData;
    private GoogleMap mMap;
    private String url;
    private double latitude;
    private double longitude;
    private final double SIGNIFICANCE_THRESHOLD = .001;

    protected GetNearbyPlacesData(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;

    }

    @Override
    protected String doInBackground(Object... params) {
        try {
            Log.d("GetNearbyPlacesData", "doInBackground entered");
            mMap = (GoogleMap) params[0];
            url = (String) params[1];
            DownloadUrl downloadUrl = new DownloadUrl();
            googlePlacesData = downloadUrl.readUrl(url); // Used to retrieve data from URL from googlePlacesData
            Log.d("GooglePlacesReadTask", "doInBackground Exit");
        } catch (Exception e) {
            Log.d("GooglePlacesReadTask", e.toString());
        }
        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String result) { // after retrieving the data it is in json
        // Use the DataParser class to parse the json file on the stuff we want
        Log.d("GooglePlacesReadTask", "onPostExecute Entered");
        List<HashMap<String, String>> nearbyPlacesList;
        DataParser dataParser = new DataParser();
        nearbyPlacesList =  dataParser.parse(result); // parse json file and store in a HashMap list
        ShowNearbyPlaces(nearbyPlacesList); // Pass thru this function to show nearby restuarants on the map
        Log.d("GooglePlacesReadTask", "onPostExecute Exit");
    }

    private void PositionNearbyPlaces(List<HashMap<String, String>> nearbyPlacesList){
        Log.i("Position", "My Position: "+latitude+" "+longitude);
        for (int i = 0; i < nearbyPlacesList.size(); i++) {
            HashMap<String, String> googlePlace = nearbyPlacesList.get(i);
            double lat = Double.parseDouble(googlePlace.get("lat"));
            double lng = Double.parseDouble(googlePlace.get("lng"));
            String placeName = googlePlace.get("place_name");
            Log.i("Position", "Place: "+placeName+" Position: "+lat+" "+lng);
            double[] relativePositionList = RelativePosition(lat,lng);
            Log.i("Position", relativePositionList[0]+ " " +relativePositionList[1]);
        }
    }

    private double[] RelativePosition(double lat, double lng){
        double latChange = latitude - lat;
        double lngChange = longitude - lng;
        Log.i("Position","latChange"+latChange);
        Log.i("Position","lngChange"+lngChange);

        double r = Math.sqrt(latChange*latChange + lngChange*lngChange);

        double unitLat = latChange/r;
        double unitLng = lngChange/r;

        double theta = Math.toDegrees(Math.atan((lngChange)/(latChange)));

        Log.i("Position","unitLat: "+unitLat);
        Log.i("Position","unitLng: "+unitLng);


        return new double[]{unitLat, unitLng};

    }

    private void ShowNearbyPlaces(List<HashMap<String, String>> nearbyPlacesList) {

        // Iterate through the nearby places that were returned
        // and place a marker for each.
        for (int i = 0; i < nearbyPlacesList.size(); i++) {
            Log.d("onPostExecute","Entered into showing locations");

            try {
                MarkerOptions markerOptions = new MarkerOptions();
                HashMap<String, String> googlePlace = nearbyPlacesList.get(i);
                Log.i("!!!!", googlePlace.toString());
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

        // TODO: Position them, Get current location, Get direction you are facing, Calculate degree offset from direction you are facing
    }
}
