package com.example.han.pleasantjourney;

import android.content.BroadcastReceiver;
import android.os.AsyncTask;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import com.firebase.client.* ;
import android.support.v7.widget.CardView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Map;


public class TrackingActivity extends ActionBarActivity {

    protected Intent mServiceIntent ;
    protected TextView Latitude ;
    protected TextView Longitude ;
    protected TextView Speed ;
    protected TextView SpeedLimit;
    protected TextView VehicleState ;
    protected CardView speedCardView;
    protected CardView sensorCardView;
    protected Firebase falconhRef ;
    protected Firebase mVehicleLocation;
    protected String platNo ;
    protected String destination ;
    private String destinationLatLng;

    protected static double currentLatitude ;
    protected static double currentLongitude;
    protected static int currentSpeed = 0;
    protected static int currentSpeedLimit = 0;

    IntentFilter mIntentFilter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        Firebase.setAndroidContext(this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(BackgroundLocationService.LOCATION_BROADCAST_ACTION);
        mIntentFilter.addAction(BackgroundLocationService.SENSOR_BROADCAST_ACTION);

        registerReceiver(broadcastReceiver, mIntentFilter);

        Latitude = (TextView) findViewById(R.id.textview_value_latitude);
        Longitude = (TextView)findViewById(R.id.textview_value_longitude);
        Speed = (TextView) findViewById(R.id.textview_value_current_speed);
        SpeedLimit = (TextView) findViewById(R.id.textview_value_speedLimit);
        speedCardView = (CardView) findViewById(R.id.cardview_speed);
        sensorCardView = (CardView) findViewById(R.id.cardview_vehicle_state);
        VehicleState = (TextView) findViewById(R.id.textview_value_sensorAlert);

        platNo = getIntent().getStringExtra("platno");
        destination = getIntent().getStringExtra("destination");
        destinationLatLng = getIntent().getStringExtra("destinationLatLng");


        falconhRef = new Firebase("https://falconh.firebaseio.com") ;
        mVehicleLocation = falconhRef.child("vehicleLocation");
        mVehicleLocation.child(platNo).child("Destination").setValue(destination);
        mVehicleLocation.child(platNo).child("DestinationLatLng").setValue(destinationLatLng);

        mVehicleLocation.child(platNo).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot snapshot )
            {
                Object value = snapshot.getValue();
                if ( value != null )
                {
                    String lat = (String)((Map)value).get("Latitude");
                    String longi = (String)((Map)value).get("Longitude");
                    String speed = (String)((Map)value).get("Speed");

                    //Latitude.setText(lat);
                    //Longitude.setText(longi);
                    //Speed.setText(speed);
                    //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + lat + "\nLong: " + longi, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(FirebaseError arg0) {
                // TODO Auto-generated method stub

            }
        });

        mServiceIntent = new Intent(this,BackgroundLocationService.class);
        mServiceIntent.putExtra("platno",platNo);
        mServiceIntent.putExtra("destinationLatLng",destinationLatLng);
        this.startService(mServiceIntent);
        Log.e("tracker", "started");


    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        this.stopService(mServiceIntent);
        unregisterReceiver(broadcastReceiver);
        Log.i("tracker", "stop");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tracking, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected String buildOverpassURL(){
        final String OVERPASS_URL = "http://overpass-api.de/api/interpreter?data=[out:json];way(around:20.0,"
                                    + String.valueOf(currentLatitude) + "," + String.valueOf(currentLongitude)
                                    + ")[maxspeed];out;";

        Log.e("OverPass_URL", OVERPASS_URL);

        return OVERPASS_URL ;
    }

    protected void updateSpeedLimitUI(){
        SpeedLimit.setText(String.valueOf(currentSpeedLimit));

        if(currentSpeed > currentSpeedLimit){
            speedCardView.setCardBackgroundColor(Color.RED);
        }
        else{
            speedCardView.setCardBackgroundColor(Color.WHITE);
        }

    }

    protected void updateLocationUI(){
        Latitude.setText(String.valueOf(currentLatitude));
        Longitude.setText(String.valueOf(currentLongitude));
        Speed.setText(String.valueOf(currentSpeed));
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BackgroundLocationService.LOCATION_BROADCAST_ACTION)) {
                double defaultValue = 0.00 ;
                currentLatitude = intent.getDoubleExtra("currentLatitude", defaultValue);
                currentLongitude = intent.getDoubleExtra("currentLongitude", defaultValue);
                currentSpeed = intent.getIntExtra("currentSpeed", 0);
                updateLocationUI();
                new SpeedLimitTask().execute(buildOverpassURL());


            }
            else if(intent.getAction().equals(BackgroundLocationService.SENSOR_BROADCAST_ACTION)){
                String state = intent.getStringExtra("currentState");

                if( state.equals("true")){
                    sensorCardView.setCardBackgroundColor(Color.RED);
                    VehicleState.setText("Not Stable");
                }
                else{
                    sensorCardView.setCardBackgroundColor(Color.WHITE);
                    VehicleState.setText("Stable");
                }

                Log.e("StatecardviewChanged", "state :" + state);
            }
        }
    };
    private class SpeedLimitTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params){
            String data = "";
            try{
                HttpConnection http = new HttpConnection();
                data = http.readUrl(params[0]);
            }catch(Exception e){
                Log.e("SpeedLimitTask", e.toString());
            }

            JSONObject overpassJSON;
            JSONObject tagsContent;
            JSONArray roadElement ;
            String speedLimit = null ;

            if( data != null ){
                try{
                    overpassJSON = new JSONObject(data);

                    roadElement = overpassJSON.getJSONArray("elements");

                    if(roadElement != null)
                    {
                        tagsContent = roadElement.getJSONObject(0).getJSONObject("tags");

                        if(tagsContent != null){
                            speedLimit = tagsContent.getString("maxspeed");
                        }
                    }

                }
                catch(JSONException e){
                    Log.e("SpeedLimitJsonParse", e.toString());
                }
            }
            return speedLimit;
        }

        @Override
        protected void onPostExecute(String result){
            if(result != null){
                currentSpeedLimit = Integer.parseInt(result);
                updateSpeedLimitUI();
            }
            else if(currentSpeedLimit == 0){
                currentSpeedLimit = Constants.DEFAULT_SPEED_LIMIT ;
            }
        }
    }
}
