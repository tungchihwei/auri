package com.green.auri;

import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchAndPosition {

    private static final String TAG = "SearchAndPosition";
    private FusedLocationProviderClient mFusedLocationProviderClient;


    public void executeSearchAndPosition(){
        //GetCurrentLocation
        //GetnearbyPlaces
        //GetCameraDirection

        //Then
        //PositionNearbyPlaces
        //Update AR
        double[] myCoordinates = getCurrentLocation();
        double myLatitude = myCoordinates[0];
        double myLongitude = myCoordinates[1];

        List<HashMap<String, String>> myNearbyPlacesList = getNearbyPlacesList();

        double angleFromNorth = getCameraDirection();

        //Name of restaurant
        //X coordinate as string
        //Y coordinate as string
        //Rating
        //Image URL
        HashMap<String, String> PositionNearbyPlaces



    }

    public double[] getCurrentLocation(){
//        double[] currentLocation = findDeviceLocation();
//        int failArray[] = {-1,-1};
//        if (currentLocation.equals(failArray)){
//            Log.i(TAG, "Failed to get current location");
//        }
//        return currentLocation;
        return null;
    }

    public List<HashMap<String, String>> getNearbyPlacesList(){
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
        return null;
    }

    public double getCameraDirection(){
        Camdir sensor = new Camdir();
        return sensor.getAngle();
    }

    public void updateAuri(){

    }


    public void PositionNearbyPlaces(List<HashMap<String, String>> nearbyPlacesList,  double myLatitude, double myLongitude){
        PositionNearbyPlaces(nearbyPlacesList, myLatitude, myLongitude, 0);
    }

    // Position nearby places relative to current location
    // Iterate over nearbyPlaceList and get each position unit vector based on a reference angle
    public List<HashMap<String, String>> PositionNearbyPlaces(List<HashMap<String, String>> nearbyPlacesList, double myLatitude, double myLongitude, double theta){
        Log.i("Position", "My Position: "+myLatitude+" "+myLongitude);
        List<HashMap<String, String>> positionedPlaces = new ArrayList<>();

        for (int i = 0; i < nearbyPlacesList.size(); i++) {
            HashMap<String, String> currentGooglePlace = nearbyPlacesList.get(i);

            //Get place information
            double lat = Double.parseDouble(currentGooglePlace.get("lat"));
            double lng = Double.parseDouble(currentGooglePlace.get("lng"));
            String placeName = currentGooglePlace.get("place_name");
            String rating = currentGooglePlace.get("rating");
            String URL = currentGooglePlace.get("icon");

            Log.i("Position", "Place: "+placeName+" Position: "+lat+" "+lng);

            //Get the relative position data
            double[] relativePositionList = RelativePosition(myLatitude, myLongitude, lat,lng,theta);

            //Create a new Positioned Place item with all relaevent data for AR
            HashMap<String, String> positionedPlace = new HashMap<String, String>();
            positionedPlace.put("Name",placeName);
            positionedPlace.put("X",Double.toString(relativePositionList[0]));
            positionedPlace.put("Y",Double.toString(relativePositionList[1]));
            positionedPlace.put("Distance",Double.toString(relativePositionList[2]));
            positionedPlace.put("Rating",rating);
            positionedPlace.put("URL",URL);

            positionedPlaces.add(positionedPlace);

            Log.i("Position", relativePositionList[0]+ " " +relativePositionList[1]);
        }

        return positionedPlaces;
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

        double[] positions = {lat2, lng2, r};

        return positions;
    }

}
