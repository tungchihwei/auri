package com.green.auri;

import android.util.Log;

import java.util.HashMap;
import java.util.List;

public class searchAndPosition {

    public void executeSearchAndPosition(){
        //GetCurrentLocation
        //GetnearbyPlaces
        //GetCameraDirection

        //Then
        //PositionNearbyPlaces
        //Update AR
    }

    public void getCurrentLocation(){

    }

    public void getNearbyPlacesList(){

    }

    public void getCameraDirection(){

    }

    public void updateAuri(){
        
    }


    public void PositionNearbyPlaces(List<HashMap<String, String>> nearbyPlacesList,  double myLatitude, double myLongitude){
        PositionNearbyPlaces(nearbyPlacesList, myLatitude, myLongitude, 0);
    }

    // Position nearby places relative to current location
    // Iterate over nearbyPlaceList and get each position unit vector based on a reference angle
    public void PositionNearbyPlaces(List<HashMap<String, String>> nearbyPlacesList, double myLatitude, double myLongitude, double theta){
        Log.i("Position", "My Position: "+myLatitude+" "+myLongitude);

        for (int i = 0; i < nearbyPlacesList.size(); i++) {
            HashMap<String, String> currentGooglePlace = nearbyPlacesList.get(i);
            double lat = Double.parseDouble(currentGooglePlace.get("lat"));
            double lng = Double.parseDouble(currentGooglePlace.get("lng"));
            String placeName = currentGooglePlace.get("place_name");
            Log.i("Position", "Place: "+placeName+" Position: "+lat+" "+lng);
            double[] relativePositionList = RelativePosition(myLatitude, myLongitude, lat,lng,theta);
            Log.i("Position", relativePositionList[0]+ " " +relativePositionList[1]);
        }
    }

    // Calculate relative position
    // 1. Calculate unit vector from current location
    // 2. Rotate from true north using rotation matrix
    // 3. Return position array
    private double[] RelativePosition(double myLatitude, double myLongitude, double lat, double lng, double theta){
        //get deltas from your position
        double latChange = myLatitude - lat;
        double lngChange = myLongitude - lng;

        Log.i("Position","latChange"+latChange);
        Log.i("Position","lngChange"+lngChange);

        //distance, this will be the scalar to get to unit vector
        double r = Math.sqrt(latChange*latChange + lngChange*lngChange);

        //Generate unit vectors for new position
        double unitLat = latChange/r;
        double unitLng = lngChange/r;

        //Rotate vectors using rotation matrix formula
        double lat2 = Math.cos(theta)*unitLat - Math.sin(theta)*unitLng;
        double lng2 = Math.cos(theta)*unitLng + Math.sin(theta)*unitLat;


        Log.i("Position","unitLat: "+unitLat);
        Log.i("Position","unitLng: "+unitLng);
        Log.i("Position","rotated unitLat: "+lat2);
        Log.i("Position","rotated unitLng: "+lng2);

        double[] positions = {lat2, lng2};

        return positions;
    }

}
