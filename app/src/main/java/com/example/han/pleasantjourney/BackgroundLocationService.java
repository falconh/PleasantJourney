package com.example.han.pleasantjourney;

/**
 * Created by Han on 3/12/2015.
 */
        import java.text.DateFormat ;
        import java.text.SimpleDateFormat ;
        import java.io.* ;
        import java.util.Date ;
        import java.util.List ;
        import java.util.ArrayList ;
        import java.util.Arrays ;

        import android.app.* ;
        import android.os.* ;
        import android.content.Intent ;
        import android.content.IntentSender ;
        import android.content.IntentFilter ;
        import android.location.Location ;
        import android.util.Log ;
        import android.widget.Toast;
        import android.support.v4.content.LocalBroadcastManager ;

        import com.google.android.gms.common.GooglePlayServicesClient;
        import com.google.android.gms.common.GooglePlayServicesUtil ;
        import com.google.android.gms.common.ConnectionResult ;
        import com.google.android.gms.common.api.GoogleApiClient;
        import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
        import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
        import com.google.android.gms.location.* ;
        import com.google.android.gms.maps.model.LatLng ;

 //       import com.example.gpsemulator.GeofenceUtils.REQUEST_TYPE ;

        import com.firebase.client.* ;

/**
 * BackgroundLocationService used for tracking user location in the background.
 *
 * @author cblack
 */
public class BackgroundLocationService extends Service implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener {

    IBinder mBinder = new LocalBinder();
    Firebase falconhRef ;
    int updateValue ;
    protected String fbPlatNo = "";

    private GoogleApiClient mLocationClient;
    private LocationRequest mLocationRequest;
    // Flag that indicates if a request is underway.
    private boolean mInProgress;

    private Boolean servicesAvailable = false;

    //Geofence related
    List<Geofence> mCurrentGeofences ;
    private IntentFilter mIntentFilter ;
    private PendingIntent mGeofencePendingIntent ;
    List<LatLng> busStop = new ArrayList<LatLng>();



    public class LocalBinder extends Binder {
        public BackgroundLocationService getServerInstance() {
            return BackgroundLocationService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        Log.e("firebase", "1");
        falconhRef = new Firebase("https://falconh.firebaseio.com") ;
        updateValue = 0 ;
        //falconhRef.child("Update").setValue("0");
        mInProgress = false;
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(Constants.UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(Constants.FASTEST_INTERVAL);

        servicesAvailable = servicesConnected();


        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationClient.connect();
        mGeofencePendingIntent = null ;


        //Geofence related

    }

    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            //Toast.makeText(this, "AConnected", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            //Toast.makeText(this, "ADisConnected", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public int onStartCommand (Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);

        fbPlatNo = intent.getStringExtra("platno");

        Log.e("onLocationChanged", "zz");
        if(!servicesAvailable || mLocationClient.isConnected() || mInProgress)
            return START_STICKY;

        setUpLocationClientIfNeeded();
        if(!mLocationClient.isConnected() || !mLocationClient.isConnecting() && !mInProgress)
        {
            appendLog(DateFormat.getDateTimeInstance().format(new Date()) + ": Started", Constants.LOG_FILE);
            mInProgress = true;
            mLocationClient.connect();
        }

        return START_STICKY;
    }

    /*
     * Create a new location client, using the enclosing class to
     * handle callbacks.
     */
    private void setUpLocationClientIfNeeded()
    {
        if(mLocationClient == null)
            mLocationClient = new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build();
    }

    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient, this);
    }

    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(
               mLocationClient, mLocationRequest, this);
    }

    // Define the callback method that receives location updates
    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        String msg = Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        updateValue++;
        falconhRef.child(fbPlatNo).child("Latitude").setValue(String.valueOf(location.getLatitude()));
        falconhRef.child(fbPlatNo).child("Longitude").setValue(String.valueOf(location.getLongitude()));
        falconhRef.child(fbPlatNo).child("Speed").setValue(String.valueOf(location.getSpeed()/1000));
        Log.d("debug", msg);
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        appendLog(msg, Constants.LOCATION_FILE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public String getTime() {
        SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return mDateFormat.format(new Date());
    }

    public void appendLog(String text, String filename)
    {
        File logFile = new File(filename);
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private PendingIntent createRequestPendingIntent() {

        // If the PendingIntent already exists
        if (null != mGeofencePendingIntent) {

            // Return the existing intent
            return mGeofencePendingIntent;

            // If no PendingIntent exists
        } else {

            // Create an Intent pointing to the IntentService
            Intent intent = new Intent("com.example.gpsemulator.ACTION_RECEIVE_GEOFENCE");
            /*
             * Return a PendingIntent to start the IntentService.
             * Always create a PendingIntent sent to Location Services
             * with FLAG_UPDATE_CURRENT, so that sending the PendingIntent
             * again updates the original. Otherwise, Location Services
             * can't match the PendingIntent to requests made with it.
             */
            return PendingIntent.getBroadcast(
                    getApplicationContext(),
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }



    @Override
    public void onDestroy(){
        // Turn off the request flag
        mInProgress = false;
        if(servicesAvailable && mLocationClient != null) {
            stopLocationUpdates();
            // Destroy the current location client
            mLocationClient = null;
        }
        // Display the connection status
        // Toast.makeText(this, DateFormat.getDateTimeInstance().format(new Date()) + ": Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        appendLog(DateFormat.getDateTimeInstance().format(new Date()) + ": Stopped", Constants.LOG_FILE);
        super.onDestroy();
    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
        // Display the connection status
        Log.e("onLocationChanged", "Connected");
        //Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        // Request location updates using static settings
        startLocationUpdates();
        appendLog(DateFormat.getDateTimeInstance().format(new Date()) + ": Connected", Constants.LOG_FILE);

    }


    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("onLocationChanged", "YE");
        mInProgress = false;

        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {

            // If no resolution is available, display an error dialog
        } else {

        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        //Log.i(TAG, "Connection suspended");
        mLocationClient.connect();
    }


}