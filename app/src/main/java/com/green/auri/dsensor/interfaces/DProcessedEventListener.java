package com.green.auri.dsensor.interfaces;

import com.green.auri.dsensor.DSensorEvent;

/**
 * Call back for SensorManager.startDProcessedSensor
 * Created by Hoan on 2/28/2016.
 */
public interface DProcessedEventListener {
    void onProcessedValueChanged(DSensorEvent dSensorEvent);
}
