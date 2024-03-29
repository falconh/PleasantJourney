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
        import java.lang.Math ;
        import java.util.Map;

        import android.app.* ;
        import android.os.* ;
        import android.content.Intent ;
        import android.content.IntentSender ;
        import android.content.IntentFilter ;
        import android.location.Location ;
        import android.util.Log ;
        import android.widget.Toast;
        import android.support.v4.content.LocalBroadcastManager ;

        //for sensors
        import android.hardware.Sensor;
        import android.hardware.SensorEvent;
        import android.hardware.SensorEventListener;
        import android.hardware.SensorManager;

        //GooglePlayServices related
        import com.google.android.gms.common.GooglePlayServicesUtil ;
        import com.google.android.gms.common.ConnectionResult ;
        import com.google.android.gms.common.api.GoogleApiClient;
        import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
        import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
        import com.google.android.gms.location.* ;
        import com.google.android.gms.maps.model.LatLng ;

 //       import com.example.gpsemulator.GeofenceUtils.REQUEST_TYPE ;

        import com.firebase.client.* ;
        import com.firebase.geofire.*;

/**
 * BackgroundLocationService used for tracking user location in the background.
 *
 * @author cblack
 */
public class BackgroundLocationService extends Service implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener,
        SensorEventListener {

    IBinder mBinder = new LocalBinder();
    //Firebase + GeoFire
    protected Firebase falconhRef ;
    protected Firebase mVehicleLocation ;
    protected Firebase mPresentationSwitch;
    protected Firebase mGeoFire ;
    protected Firebase mGeoFireData ;
    protected GeoFire mGeoFireHash ;

    //Plat number
    protected String fbPlatNo = "";

    //Location service related
    private GoogleApiClient mLocationClient;
    private LocationRequest mLocationRequest;

    //Sensors related
    protected SensorManager mSensorManager;
    protected Sensor mAccelerometer;
    protected Sensor mGyrometer;
    protected Sensor mRotationVector;
    protected FusedSensorManager mFusedSensorManager ;
    private boolean isAcceExist ;
    private boolean isGyroExist;
    private boolean isRotationVectorExist;
    private boolean previousFusedSensorValue = false;
    private boolean currentFusedSensorValue = false;
    private long number_of_sensor_data = 0;
    private double model_stable = 0 ;


    // Flag that indicates if a request is underway.
    private boolean mInProgress;

    private Boolean servicesAvailable = false;

    //destination coordinate
    private static String destinationLatLng ;
    private static LatLng destinationCoord ;
    private static String journeyID ;

    //location related
    protected int currentSpeed = 0 ;
    protected double currentLatitude = 0.0;
    protected double currentLongitude = 0.0;
    protected int currentMode = 0 ;

    //sqlite
    DatabaseHandler db ;

    //Broadcast related
    public static final String LOCATION_BROADCAST_ACTION = "com.example.han.pleasantjourney.locationevent";
    public static final String SENSOR_BROADCAST_ACTION = "com.example.han.pleasantjourney.sensorevent";
    Intent locationIntent;
    Intent sensorIntent;

    //Geofence related
    protected List<Geofence> mCurrentGeofences ;
    private IntentFilter mIntentFilter ;
    private PendingIntent mGeofencePendingIntent ;
    //List<LatLng> busStop = new ArrayList<LatLng>();



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
        mVehicleLocation = falconhRef.child("vehicleLocation");
        mPresentationSwitch = falconhRef.child("presentationSwitch");
        mGeoFire = falconhRef.child("GeoFire");
        mGeoFireData = mGeoFire.child("geoFireData");
        mGeoFireHash = new GeoFire(mGeoFire.child("geoFireHash"));
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

        //Sensors
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mFusedSensorManager = new FusedSensorManager();
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if(mAccelerometer != null){
            isAcceExist = true ;
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
            Log.i("LocationService","Accelerometer registered");
        }
        else{
            isAcceExist = false ;
            Log.e("LocationService","Accelerometer is not registered");
        }

        mGyrometer = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        if(mGyrometer != null){
            isGyroExist = true ;
            mSensorManager.registerListener(this, mGyrometer, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
            Log.i("LocationService","Gyrometer registered");
        }
        else{
            isGyroExist = false ;
            Log.e("LocationService","Gyrometer is not registered");
        }

        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        if(mRotationVector != null){
            isRotationVectorExist = true ;
            mSensorManager.registerListener(this, mRotationVector, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
            Log.i("LocationService","RotationVector registered");
        }
        else{
            isRotationVectorExist = false ;
            Log.e("LocationService","RotationVector is not registered");
        }

        db = new DatabaseHandler(this);

        locationIntent = new Intent(LOCATION_BROADCAST_ACTION);
        sensorIntent = new Intent(SENSOR_BROADCAST_ACTION);

        mPresentationSwitch.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Object value = dataSnapshot.getValue();

                if(value != null){
                    String mode = (String)((Map)value).get("mode");

                    currentMode = Integer.parseInt(mode);

                    if(currentMode == 1){
                        String lat = (String)((Map)value).get("Latitude");
                        String longi = (String)((Map)value).get("Longitude");
                        String speed = (String)((Map)value).get("Speed");

                        currentLatitude = Double.parseDouble(lat);
                        currentLongitude = Double.parseDouble(longi);
                        currentSpeed = Integer.parseInt(speed);

                        mVehicleLocation.child(fbPlatNo).child(journeyID).child("Latitude").setValue(String.valueOf(currentLatitude));
                        mVehicleLocation.child(fbPlatNo).child(journeyID).child("Longitude").setValue(String.valueOf(currentLongitude));
                        mVehicleLocation.child(fbPlatNo).child(journeyID).child("Speed").setValue(String.valueOf(currentSpeed));

                        sendLocationUpdatesToUI();
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

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
        destinationLatLng = intent.getStringExtra("destinationLatLng");
        journeyID = intent.getStringExtra("journeyID");

        LatLngStringConvert tempConverter = new LatLngStringConvert(destinationLatLng);
        tempConverter.convertStringLatLng();
        destinationCoord = new LatLng(tempConverter.getLatitude(),tempConverter.getLongitude());


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

        if(currentMode == 0) {
            currentSpeed = ValueRounder.roundDecimal(location.getSpeed() * Constants.HOUR_MULTIPLIER * Constants.UNIT_MULTIPLIERS, 0);
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();

            mVehicleLocation.child(fbPlatNo).child(journeyID).child("Latitude").setValue(String.valueOf(location.getLatitude()));
            mVehicleLocation.child(fbPlatNo).child(journeyID).child("Longitude").setValue(String.valueOf(location.getLongitude()));
            mVehicleLocation.child(fbPlatNo).child(journeyID).child("Speed").setValue(String.valueOf(currentSpeed));

            sendLocationUpdatesToUI();
        }

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

        //Unregister sensor listener
        if( isAcceExist || isGyroExist ){
            mSensorManager.unregisterListener(this);
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
        Log.i("LocationService", "Connected");
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

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(isAcceExist){
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                mFusedSensorManager.setAcceValue(event.values);
                mFusedSensorManager.calcAccelometer(SensorManager.GRAVITY_EARTH);
                SensorDatabase tempSensorHolder = new SensorDatabase();
                tempSensorHolder.latitude = currentLatitude ;
                tempSensorHolder.longitude = currentLongitude ;
                tempSensorHolder.speed = currentSpeed ;
                tempSensorHolder.p_value = mFusedSensorManager.getProcessedAcceValue();
                tempSensorHolder.r_value = (float) Math.sqrt(event.values[0]*event.values[0] + event.values[1]*event.values[1]
                                            + event.values[2]*event.values[2]);
                tempSensorHolder.platno = fbPlatNo;
                db.addRecordToAccTable(tempSensorHolder);

                model_stable = (model_stable*number_of_sensor_data + tempSensorHolder.p_value)/(number_of_sensor_data+1) ;
                number_of_sensor_data++;

                if(model_stable != 0.0 && number_of_sensor_data > Constants.CALIBRATION_THRESHOLD){
                    if(tempSensorHolder.p_value > model_stable){
                        if((tempSensorHolder.p_value - model_stable)/model_stable > Constants.RATIO_O){
                            previousFusedSensorValue = currentFusedSensorValue;
                            currentFusedSensorValue = true ;
                        }
                        else{
                            previousFusedSensorValue = currentFusedSensorValue;
                            currentFusedSensorValue = false ;
                        }
                    }
                    else{
                        previousFusedSensorValue = currentFusedSensorValue;
                        currentFusedSensorValue = false ;
                    }


                }

                sendSensorUpdatesToUI();

            }
        }

        if(isRotationVectorExist){
            if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
                SensorDatabase tempSensorHolder = new SensorDatabase();
                tempSensorHolder.latitude = currentLatitude ;
                tempSensorHolder.longitude = currentLongitude ;
                tempSensorHolder.speed = currentSpeed ;
                tempSensorHolder.r_value = (float) Math.sqrt(event.values[0]*event.values[0] + event.values[1]*event.values[1]
                        + event.values[2]*event.values[2]);
                tempSensorHolder.platno = fbPlatNo;
                //db.addRecordToRotationTable(tempSensorHolder);
            }
        }

        if(isGyroExist){
            if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
                mFusedSensorManager.setGyroValue(event.values);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void sendLocationUpdatesToUI(){

        locationIntent.putExtra("currentLatitude", currentLatitude);
        locationIntent.putExtra("currentLongitude", currentLongitude);
        locationIntent.putExtra("currentSpeed", currentSpeed);

        sendBroadcast(locationIntent);

        Log.i("LocationBroadCast","LocationBroadcastSent");
    }

    protected void sendSensorUpdatesToUI(){

        if(previousFusedSensorValue != currentFusedSensorValue){
            sensorIntent.putExtra("currentState" , Boolean.toString(currentFusedSensorValue));
            sendBroadcast(sensorIntent);
        }
    }

}