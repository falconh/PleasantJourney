package com.example.han.pleasantjourney;

/**
 * Created by Han on 4/26/2015.
 */

import java.lang.Math.*;

public class FusedSensorManager {



        private float[] acceValues  ;
        private float[] gyroValues  ;
        private float processedAcceValue ;
        private static final float alphaValue = (float) 0.6 ;

        FusedSensorManager(){
            processedAcceValue = 0 ;
        }

        public void setAcceValue(float[] acceValues){
            this.acceValues = acceValues ;
        }

        public void setGyroValue(float[] gyroValues){
            this.gyroValues = gyroValues ;
        }

        public float[] getAcceValue(){
            return acceValues ;
        }

        public float[] getGyroValue(){
            return gyroValues ;
        }

        public float getProcessedAcceValue(){

            return processedAcceValue ;
        }

    public void calcAccelometer(float gravity_earth){

        float x = acceValues[0];
        float y = acceValues[1];
        float z = acceValues[2];

        float[] gravity = new float[3];
        float[] linear_acceleration = new float[3];

        gravity[0] = alphaValue * gravity_earth + (1 - alphaValue) * x;
        gravity[1] = alphaValue * gravity_earth + (1 - alphaValue) * y;
        gravity[2] = alphaValue * gravity_earth + (1 - alphaValue) * z;

        // Remove the gravity contribution with the high-pass filter.
        linear_acceleration[0] = x - gravity[0];
        linear_acceleration[1] = y - gravity[1];
        linear_acceleration[2] = z - gravity[2];

        float accelationSquareRoot = (float)Math.sqrt(linear_acceleration[0]*linear_acceleration[0]
                                            + linear_acceleration[1]*linear_acceleration[1]
                                            + linear_acceleration[2]*linear_acceleration[2]);


        processedAcceValue = accelationSquareRoot ;
    }

}
