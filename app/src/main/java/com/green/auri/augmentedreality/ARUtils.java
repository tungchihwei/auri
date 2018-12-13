package com.green.auri.augmentedreality;

import com.google.ar.sceneform.math.Vector3;

public class ARUtils {
    private final static float MAGIC_SIZE_SCALAR = 5;

    /**
     * Returns the correct vector (x,y,z) from a given angle and distance.
     * @param theta
     * @param distance
     * @return
     */
    public static Vector3 buildVectorFromAngle(double theta, double distance){
        float x = (float)  Math.cos(Math.toRadians(theta));
        float y = (float)  Math.sin(Math.toRadians(theta));
        return new Vector3(MAGIC_SIZE_SCALAR*y, 0, - MAGIC_SIZE_SCALAR * x);
    }
}
