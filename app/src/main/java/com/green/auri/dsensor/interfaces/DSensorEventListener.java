package com.green.auri.dsensor.interfaces;

import com.green.auri.dsensor.DProcessedSensorEvent;

/**
 * Call back for SensorManager.startDSensor
 * Created by Hoan on 2/28/2016.
 */
public interface DSensorEventListener {
    void onDSensorChanged(int changedDSensorTypes, DProcessedSensorEvent processedSensorEvent);
}
