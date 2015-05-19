package com.example.han.pleasantjourney;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.AutoCompleteTextView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks{

    Toolbar mToolbar;
    private EditText platno;
    private EditText server_IP;
    private static final String LOG_TAG = "MainActivity";
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private AutoCompleteTextView destination;
    private GoogleApiClient mGoogleApiClient;
    private PlaceArrayAdapter mPlaceArrayAdapter;
    private LatLng destinationLatLng ;
    private String destinationName ;
    private String chosenDestinationDescription;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        destinationLatLng = new LatLng(0.0,0.0);

        mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .build();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        platno = (EditText) findViewById(R.id.platno);
        destination = (AutoCompleteTextView) findViewById(R.id.destination);
        server_IP = (EditText) findViewById(R.id.server_ip);

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }


        mPlaceArrayAdapter = new PlaceArrayAdapter(this, android.R.layout.simple_list_item_1,
                Constants.MALAYSIA_VIEW, null);

        destination.setOnItemClickListener(mAutocompleteClickListener);
        destination.setAdapter(mPlaceArrayAdapter);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i(LOG_TAG, "Selected: " + item.description);
            chosenDestinationDescription = item.description.toString() ;
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            Log.i(LOG_TAG, "Fetching details for ID: " + item.placeId);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e(LOG_TAG, "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);

            destinationLatLng = place.getLatLng();
            destinationName = place.getName().toString();

            places.release();
        }
    };

    public void startActivityTwo(View view)
    {

        new LocalDatabaseTask().execute(buildLocalDatabseURL());
    }

    protected String buildLocalDatabseURL(){

        if(destinationName == null ) {
            destinationName = destination.getText().toString();
        }


        String serverIP = "";

        if(server_IP.getText().toString() == null || server_IP.getText().toString().equals("")) {
            serverIP = Constants.SERVER_IP;
        }
        else
        {
            serverIP = server_IP.getText().toString();
        }
        String LOCAL_DATABASE_URL = "http://" + serverIP + "/register/journey"; /*+ "platno="
                                            + platno.getText().toString() + "&destination="
                                            + destinationName + "&dcoord="
                                            + Double.toString(destinationLatLng.latitude)
                                            + "," + Double.toString(destinationLatLng.longitude) ;*/

        //LOCAL_DATABASE_URL = LOCAL_DATABASE_URL.replace(" ","%");

        return LOCAL_DATABASE_URL ;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }


    @Override
    public void onConnected(Bundle bundle) {
        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
        Log.i(LOG_TAG, "Google Places API connected.");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mPlaceArrayAdapter.setGoogleApiClient(null);
        Log.e(LOG_TAG, "Google Places API connection suspended.");
    }

    private class LocalDatabaseTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params){
            String data = "";
            Log.e("LocalDatabaseTask", "data is " + params[0]);
            try{
                RestClient http = new RestClient(params[0]);
                http.AddParam("platno", platno.getText().toString());
                http.AddParam("destination", destinationName);
                http.AddParam("dcoord", Double.toString(destinationLatLng.latitude)
                        + "," + Double.toString(destinationLatLng.longitude));

                //data = http.readUrl(params[0]);
                http.Execute(RestClient.RequestMethod.GET);
                data = http.getResponse();
                Log.e("LocalDatabaseTask", "data is " + data);
                Log.e("LocalDatabaseTask", params[0]);
            }catch(Exception e){
                Log.e("LocalDatabaseTask", e.toString());
            }

            JSONObject journeyIDJSON ;
            String journeyIDString = "0" ;

            try{
                journeyIDJSON = new JSONObject(data);
                journeyIDString = journeyIDJSON.getString("journeyid");
            }catch(JSONException e){
                Log.e("LocalDatabaseTask", e.toString());
            }catch(Exception e){
                Log.e("LocalDatabaseTask", e.toString());
            }


            return journeyIDString ;
        }

        @Override
        protected void onPostExecute(String results){
            if(results != null)
            {
                Intent secondActivity = new Intent(getApplicationContext(), TrackingActivity.class);
                secondActivity.putExtra("platno",platno.getText().toString());
                if(destinationName == null ) {
                    destinationName = destination.getText().toString();
                }

                secondActivity.putExtra("destination", destinationName);



                secondActivity.putExtra("destinationLatLng", Double.toString(destinationLatLng.latitude)
                        + "," + Double.toString(destinationLatLng.longitude));

                secondActivity.putExtra("journeyID", results);
                startActivity(secondActivity);
            }
        }
    }
}
