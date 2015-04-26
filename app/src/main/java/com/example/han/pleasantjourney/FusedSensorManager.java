package com.example.han.pleasantjourney;

/**
 * Created by Han on 4/26/2015.
 */
public class FusedSensorManager {



        private float[] acceValues  ;
        private float[] gyroValues  ;
        private float processedAcceValue ;

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

        float accelationSquareRoot = (x * x + y * y + z * z)
                / (gravity_earth * gravity_earth);

        processedAcceValue = accelationSquareRoot ;
    }

}
