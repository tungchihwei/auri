package com.green.auri.utils.location;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class LocationUtils {

    private static FusedLocationProviderClient mFusedLocationProviderClient;
    private static Boolean mLocation = true;

    public static void getCurrentLocation(Context context, LocationListener locationListener) { // get the device location
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
                            try {
                                Location mLastLocation = (Location) task.getResult();

//                            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                                double latitude = mLastLocation.getLatitude();
                                double longitude = mLastLocation.getLongitude();

                                locationListener.onLocationUpdated(true, latitude, longitude);
                            } catch (NullPointerException e) {
                                locationListener.onLocationUpdated(false, 0, 0);
                            }

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
