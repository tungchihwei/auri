package com.green.auri;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.green.auri.dsensor.DProcessedSensor;
import com.green.auri.dsensor.DSensor;
import com.green.auri.dsensor.DSensorEvent;
import com.green.auri.dsensor.DSensorManager;
import com.green.auri.dsensor.interfaces.DProcessedEventListener;

public class Camdir extends AppCompatActivity {

    private TextView res;
    private TextView res_;

    private int OFFSET = 40;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camdir);
        res = findViewById(R.id.res); // orientation result
        res_ = findViewById(R.id.res_); // degree

    }


    @Override
    protected void onResume() {
        super.onResume();

        DSensorManager.startDProcessedSensor(this, DProcessedSensor.TYPE_COMPASS_FLAT_ONLY_AND_DEPRECIATED_ORIENTATION,
                new DProcessedEventListener() {
                    @Override
                    public void onProcessedValueChanged(DSensorEvent dSensorEvent) {
                        // update UI
                        // dSensorEvent.values[0] is the azimuth.
                        if (dSensorEvent.sensorType == DSensor.TYPE_DEPRECIATED_ORIENTATION) {
                            float v = Math.round(dSensorEvent.values[0]) + OFFSET;
                            v = v > 360 ? v - 360 : v;
                            if (v > 337.5 || v < 22.5){
                                res.setText("NORTH");
                            }else if (v > 22.5 && v < 67.5){
                                res.setText("NORTH EAST");
                            }else if (v > 67.5 && v < 112.5) {
                                res.setText("EAST");
                            }else if (v > 112.5 && v < 157.5) {
                                res.setText("SOUTH EAST");
                            }else if (v > 157.5 && v < 202.5) {
                                res.setText("SOUTH");
                            }else if (v > 202.5 && v < 247.5) {
                                res.setText("SOUTH WEST");
                            }else if (v > 247.5 && v < 292.5) {
                                res.setText("WEST");
                            }else if (v > 292.5 && v < 337.5) {
                                res.setText("NORTH WEST");
                            }
                            res_.setText(String.valueOf(v));
                        }
                    }
                });
    }

    @Override
    protected void onPause() {
        DSensorManager.stopDSensor();

        super.onPause();
    }

}



