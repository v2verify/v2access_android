/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.validvoice.voxidem.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;

public class SimplePedometer implements SensorEventListener, StepListener {

    ///
    ///
    ///

    public interface PedometerListener {
        void onStep(long timeNs, int steps);
    }

    ///
    ///
    ///

    private SensorManager mSensorManager;
    private Sensor mStepDetectorSensor;
    private Sensor mStepCounterSensor;
    private SimpleStepDetector mStepDetector;
    private PedometerListener mListener;
    private int mSteps = 0;
    private float mStepOffset = 0;
    private boolean mRunning = false;

    ///
    ///
    ///

    public SimplePedometer(@NonNull Context context, @NonNull PedometerListener listener) {
        mListener = listener;

        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        if(mSensorManager == null) {
            return;
        }

        mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if(mStepDetectorSensor == null) {
            mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if(mStepDetectorSensor == null) {
                return;
            }
            mStepDetector = new SimpleStepDetector(this);
        } else {
            mStepCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }
    }

    ///
    ///
    ///

    public void start() {
        if(!mRunning) {
            mRunning = true;
            mSensorManager.registerListener(this, mStepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST);
            if(mStepCounterSensor != null) {
                mSensorManager.registerListener(this, mStepCounterSensor, SensorManager.SENSOR_DELAY_FASTEST);
            }
        }
        mSteps = 0;
    }

    public void stop() {
        if(mRunning) {
            mRunning = false;
            mSensorManager.unregisterListener(this);
        }
    }

    ///
    /// SensorEventListener Methods
    ///

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mStepDetector.updateAccel(event.timestamp, event.values[0], event.values[1], event.values[2]);
        } else if(event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            onStep(0);
        } else if(event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            if(mStepOffset == 0) {
                mStepOffset = event.values[0];
            } else {
                float steps = event.values[0] - mStepOffset;
                if(steps != 0) {
                    mSteps = (int)((mSteps + steps) / 2);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onStep(long timeNs) {
        ++mSteps;
        mListener.onStep(timeNs, mSteps);
    }

}
