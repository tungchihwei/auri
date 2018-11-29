package com.green.auri.utils;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class LocationUtils {

    public double latitude;
    public double longitude;
    Location mLastLocation;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Boolean mLocation = true;

    public void getCurrLoc(Context context) { // get the device location
        Log.d("thisClass", "getDeviceLocation: getting the devices current location");
        // use the location service
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        try{
            if(mLocation){ // if allow to find device location
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() { // @onLocationReturned and LocationListener
                    // Perform the location listener
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d("thisClass", "onComplete: found location!");
                            mLastLocation = (Location) task.getResult();

                            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                            latitude = mLastLocation.getLatitude();
                            longitude = mLastLocation.getLongitude();

                            Log.d("thisClass", String.format("latitude:%.3f longitude:%.3f",latitude,longitude));

                        }else{ // Error of finding the current location
                            Log.d("errorfinding", "onComplete: current location is null");
                        }
                    }
                });
            }
        } catch (SecurityException e){
            Log.e("cantgetLoc", "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }

}
