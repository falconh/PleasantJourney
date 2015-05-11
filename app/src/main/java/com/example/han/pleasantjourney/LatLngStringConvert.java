package com.example.han.pleasantjourney;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Han on 5/12/2015.
 */
public class LatLngStringConvert {

    private static double latitude;
    private static double longitude;
    private static String latLngString ;

    LatLngStringConvert(){

    }


    LatLngStringConvert(String latLngString){
        this.latLngString = latLngString ;
    }

    public double getLatitude(){
        return this.latitude;
    }

    public double getLongitude(){
        return this.longitude;
    }

    public void setStringLatLng(String tempLatLngString){
        latLngString = tempLatLngString ;
    }

    public void convertStringLatLng(){
        stringToDouble(latLngString);
    }

    public void stringToDouble(String tempLatLngString){
        String[] tempLatLng = tempLatLngString.split(",");
        latitude = Double.parseDouble(tempLatLng[0]);
        longitude = Double.parseDouble(tempLatLng[1]);
    }
}
