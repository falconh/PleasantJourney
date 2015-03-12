package com.example.han.pleasantjourney;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import com.firebase.client.* ;

import java.util.Map;


public class TrackingActivity extends ActionBarActivity {

    protected BackgroundLocationService tracker ;
    protected Intent mServiceIntent ;
    protected TextView Latitude ;
    protected TextView Longitude ;
    protected TextView Speed ;
    protected Firebase falconhRef ;
    protected String platNo ;
    protected String destination ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        Firebase.setAndroidContext(this);

        Latitude = (TextView) findViewById(R.id.textview_value_latitude);
        Longitude = (TextView)findViewById(R.id.textview_value_longitude);
        Speed = (TextView) findViewById(R.id.textview_value_current_speed);

        platNo = getIntent().getStringExtra("platno");
        destination = getIntent().getStringExtra("destination");

        falconhRef = new Firebase("https://falconh.firebaseio.com") ;
        falconhRef.child(platNo).child("Destination").setValue(destination);

        falconhRef.child(platNo).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot snapshot )
            {
                Object value = snapshot.getValue();
                if ( value != null )
                {
                    String lat = (String)((Map)value).get("Latitude");
                    String longi = (String)((Map)value).get("Longitude");
                    String speed = (String)((Map)value).get("Speed");

                    Latitude.setText(lat);
                    Longitude.setText(longi);
                    Speed.setText(speed);
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
        this.startService(mServiceIntent);
        Log.e("tracker", "started");


    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        this.stopService(mServiceIntent);
        Log.e("tracker","stop");
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
}