package com.green.auri.utils;

import android.util.Log;

import com.google.ar.sceneform.math.Vector3;

public class ARUtils {
    private final static float MAGIC_SIZE_SCALAR = 5;
    public static Vector3 buildVectorFromAngle(double theta, double distance){
        float x = (float)  Math.cos(Math.toRadians(theta));
        float y = (float)  Math.sin(Math.toRadians(theta));
        Log.i("APOS", "Theta: "+theta);
        Log.i("APOS", "Forward: "+(1*x));
        Log.i("APOS", "Left: "+(-1*y));
        Vector3 vector = new Vector3(MAGIC_SIZE_SCALAR*y, 0, -MAGIC_SIZE_SCALAR*x);
        return vector;
    }
}
