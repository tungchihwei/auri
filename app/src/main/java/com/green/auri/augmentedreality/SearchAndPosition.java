package com.green.auri.augmentedreality;

import android.util.Log;

import com.green.auri.utils.place.PlaceSearchResult;
import com.green.auri.utils.place.PlaceSearchUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchAndPosition {

    private static final double BUCKET_SIZE = 20;

    // Position nearby places relative to current location
    // Iterate over nearbyPlaceList and get each position unit vector based on a reference angle
    public static HashMap<Double, List<PlaceSearchResult>> PositionNearbyPlaces(List<HashMap<String, String>> nearbyPlacesList, double myLatitude, double myLongitude, double theta) {

        List<PlaceSearchResult> positionedPlaces = new ArrayList<>();

        for (int i = 0; i < nearbyPlacesList.size(); i++) {
            HashMap<String, String> currentGooglePlace = nearbyPlacesList.get(i);
            PlaceSearchResult newPositionedPlace = new PlaceSearchResult(currentGooglePlace);

            double lat = newPositionedPlace.getLat();
            double lng = newPositionedPlace.getLng();
            String placeName = newPositionedPlace.getPlaceName();

            //Get the relative position data
            double[] relativePositionList = RelativePosition(myLatitude, myLongitude, lat, lng, theta);

            //Add new calculated information
            newPositionedPlace.setX(relativePositionList[0]);
            newPositionedPlace.setY(relativePositionList[1]);
            newPositionedPlace.setDistance(relativePositionList[2]);
            newPositionedPlace.setBucket(relativePositionList[3]);

            positionedPlaces.add(newPositionedPlace);
        }

        positionedPlaces = PlaceSearchUtils.filterResults(positionedPlaces, .0015);

        // Place the results into the buckets based on the angle they correspond to.
        HashMap<Double, List<PlaceSearchResult>> bucketedPlaces = new HashMap<>();
        for (PlaceSearchResult positionedPlace : positionedPlaces) {
            double bucket = positionedPlace.getBucket();
            if (bucketedPlaces.containsKey(bucket)) {
                bucketedPlaces.get(bucket).add(positionedPlace);
            } else {
                List<PlaceSearchResult> placesInBucket = new ArrayList<>();
                placesInBucket.add(positionedPlace);
                bucketedPlaces.put(bucket, placesInBucket);
            }
        }

        return bucketedPlaces;
    }

    // Calculate relative position
    // 1. Calculate unit vector from current location
    // 2. Rotate from true north using rotation matrix
    // 3. Return position array
    private static double[] RelativePosition(double myLatitude, double myLongitude, double lat, double lng, double theta) {
        //get deltas from your position
        double latChange = lat - myLatitude;
        double lngChange = lng - myLongitude;

        //distance, this will be the scalar to get to unit vector
        double r = Math.sqrt(latChange * latChange + lngChange * lngChange);

        //Generate unit vectors for new position
        double unitLat = latChange / r;
        double unitLng = lngChange / r;

        /*
        I	Use the calculator value
II	Add 180° to the calculator value
III	Add 180° to the calculator value
IV	Add 360° to the calculator value
         */
        double angleOfInflectionFromNorth = -(Math.toDegrees(Math.atan(latChange / lngChange)) - 90);
        double quickTheta = angleOfInflectionFromNorth - theta;
        double radians = Math.toRadians(theta);

        //Rotate vectors using rotation matrix formula
        double lat2 = Math.cos(radians) * unitLng - Math.sin(radians) * unitLat;
        double lng2 = Math.cos(radians) * unitLat + Math.sin(radians) * unitLng;

        //Convert to polar coordinates and degrees for bucketing
        if (quickTheta < 0) {
            quickTheta += 360;
        }

        //Rotate everything by half a bucket so that we can center better
        quickTheta = (quickTheta + BUCKET_SIZE / 2) % 360;
        double bucket = Math.floor(quickTheta / BUCKET_SIZE) * BUCKET_SIZE;
        double[] positions = {lat2, lng2, r, bucket};

        return positions;
    }

}
