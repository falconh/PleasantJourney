package com.example.han.pleasantjourney;

/**
 * Created by Han on 3/12/2015.
 */
import android.text.format.DateUtils ;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class Constants {

    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    private static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    public static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 2;
    // A fast frequency ceiling in milliseconds
    public static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    // Stores the lat / long pairs in a text file
    public static final String LOCATION_FILE = "sdcard/location.txt";
    // Stores the connect / disconnect data in a text file
    public static final String LOG_FILE = "sdcard/log.txt";

    //For the use of speed calculation
    public static final int HOUR_MULTIPLIER = 3600;
    public static final double UNIT_MULTIPLIERS = 0.001;

    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;

    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * DateUtils.HOUR_IN_MILLIS;

    public static final float GEOFENCE_RADIUS = 10 ;

    //Google Places API related
    public static final LatLng southWest = new LatLng(-1.210626,100.493449);

    public static final LatLng northEast = new LatLng(8.312580,104.651775);

    public static final LatLngBounds MALAYSIA_VIEW = new LatLngBounds(southWest,northEast);

    //Sensor related
    public static final float RATIO_O = (float) 0.3 ;

    //Default Speed Limit
    public static final int DEFAULT_SPEED_LIMIT = 110 ;

    public static final String GOOGLE_API_SERVER_KEY = "AIzaSyAUz96HRFxcRfje8Hqk8us6-ABOWM08OYw";

    //Countdown timer related
    public static final long COUNTDOWN_START = 3000;

    public static final long COUNTDOWN_INTERVAL = 1000;


    /**
     * Suppress default constructor for noninstantiability
     */
    private Constants() {
        throw new AssertionError();
    }

}
