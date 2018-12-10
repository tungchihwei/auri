package com.green.auri;

import android.util.Log;

import com.green.auri.utils.PlaceSearchResult;
import com.green.auri.utils.PlaceSearchUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchAndPosition {

    private static final double BUCKET_SIZE = 20;

    // Position nearby places relative to current location
    // Iterate over nearbyPlaceList and get each position unit vector based on a reference angle
    public static HashMap<Double, List<PlaceSearchResult>> PositionNearbyPlaces(List<HashMap<String, String>> nearbyPlacesList, double myLatitude, double myLongitude, double theta){
//        Log.i("Position", "My Position: "+myLatitude+" "+myLongitude);
        Log.i("ANGLE", "My angle from North is: "+theta);
//        Log.i("POSITIONED", "Positioning nearby places");

        List<PlaceSearchResult> positionedPlaces = new ArrayList<>();

        for (int i = 0; i < nearbyPlacesList.size(); i++) {
            HashMap<String, String> currentGooglePlace = nearbyPlacesList.get(i);
            PlaceSearchResult newPositionedPlace = new PlaceSearchResult(currentGooglePlace);

            double lat = newPositionedPlace.getLat();
            double lng = newPositionedPlace.getLng();
            String placeName = newPositionedPlace.getPlaceName();

            Log.i("Position", "Place: "+placeName+" Position: "+lat+" "+lng);

            //Get the relative position data
            Log.i("ANGLE","Place: "+placeName);
            double[] relativePositionList = RelativePosition(myLatitude, myLongitude, lat,lng,theta);

            //Add new calculated information
            newPositionedPlace.setX(relativePositionList[0]);
            newPositionedPlace.setY(relativePositionList[1]);
            newPositionedPlace.setDistance(relativePositionList[2]);
            newPositionedPlace.setBucket(relativePositionList[3]);

            positionedPlaces.add(newPositionedPlace);

            Log.i("Position", relativePositionList[0]+ " " +relativePositionList[1]);
        }

        positionedPlaces = PlaceSearchUtils.filterResults(positionedPlaces, .0015);

        HashMap<Double, List<PlaceSearchResult>> bucketedPlaces = new HashMap<>();
        for(PlaceSearchResult positionedPlace:positionedPlaces){
            double bucket = positionedPlace.getBucket();
            if(bucketedPlaces.containsKey(bucket)){
                bucketedPlaces.get(bucket).add(positionedPlace);
            }
            else{
                List<PlaceSearchResult> placesInBucket = new ArrayList<>();
                placesInBucket.add(positionedPlace);
                bucketedPlaces.put(bucket, placesInBucket);
            }
        }

        Log.i("ANGLE", String.valueOf(bucketedPlaces.toString()));

        return bucketedPlaces;
    }

    // Calculate relative position
    // 1. Calculate unit vector from current location
    // 2. Rotate from true north using rotation matrix
    // 3. Return position array
    private static double[] RelativePosition(double myLatitude, double myLongitude, double lat, double lng, double theta){
        //get deltas from your position
        double latChange = lat-myLatitude;
        double lngChange = lng-myLongitude;

        Log.i("Position","latChange"+latChange);
        Log.i("Position","lngChange"+lngChange);

        //distance, this will be the scalar to get to unit vector
        double r = Math.sqrt(latChange*latChange + lngChange*lngChange);

        //Generate unit vectors for new position
        double unitLat = latChange/r;
        if(unitLat>0){
            Log.i("POSITION", "Location is North");
        }
        else {
            Log.i("POSITION", "Location is South");
        }
        double unitLng = lngChange/r;
        if(unitLng>0){
            Log.i("POSITION", "Location is East");
        }
        else {
            Log.i("POSITION", "Location is West");
        }

        /*
        I	Use the calculator value
II	Add 180° to the calculator value
III	Add 180° to the calculator value
IV	Add 360° to the calculator value
         */
        double angleOfInflectionFromNorth = -(Math.toDegrees(Math.atan(latChange/lngChange))-90);
        Log.i("ANGLE", "Naive angle measurement: "+angleOfInflectionFromNorth);
//        if(unitLat>=0 && unitLng>=0){
//            Log.i("POSITION", "Location is North East");
//        }
//        else if (unitLat>0 && unitLng<0){
//            Log.i("POSITION", "Location is North West");
//            angleOfInflectionFromNorth += 180;
//        }
//        else if(unitLat<0 && unitLng<0){
//            Log.i("POSITION", "Location is South West");
//            angleOfInflectionFromNorth += 180;
//        }
//        else {
//            Log.i("POSITION", "Location is South East");
//            angleOfInflectionFromNorth += 180;
//        }
//        Log.i("ANGLE", "Final Angle of Inflection: "+angleOfInflectionFromNorth);

//        double angleOfInflectionFromNorth = 180 +
        double quickTheta =  angleOfInflectionFromNorth - theta ;
        Log.i("ANGLE", "Change in angle: "+ quickTheta);

        double radians = Math.toRadians(theta);

        //Rotate vectors using rotation matrix formula
        double lat2 = Math.cos(radians)*unitLng - Math.sin(radians)*unitLat;
        double lng2 = Math.cos(radians)*unitLat + Math.sin(radians)*unitLng;

        //Convert to polar coordinates and degrees for bucketing
        if(quickTheta<0){
            quickTheta+=360;
        }

        Log.i("ANGLE", "normalized quickTheta: "+ quickTheta);

        //Rotate everything by half a bucket so that we can center better
        quickTheta = (quickTheta + BUCKET_SIZE/2)%360;
        double bucket =  Math.floor(quickTheta/BUCKET_SIZE)*BUCKET_SIZE;

        Log.i("ANGLE","Bucket: "+bucket);


        Log.i("Position","unitLat: "+unitLat);
        Log.i("Position","unitLng: "+unitLng);
        Log.i("Position","rotated unitLat: "+lat2);
        Log.i("Position","rotated unitLng: "+lng2);

        double[] positions = {lat2, lng2, r, bucket};

        return positions;
    }

}
