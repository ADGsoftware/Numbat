package com.adg.Main;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
//import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Tim
 * Date: 05.10.13
 * Time: 10:12
 */
public class Main extends Activity {
    private MapFragment mMapFragment;
    private ProgressBar progressBar;
    //private ViewButton toListButton;
    private float zoom;
    private double userLng;
    private double userLat;
    private boolean touchDown;
    public GoogleMap mMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.main);

        MapsInitializer.initialize(getApplicationContext());

        double lat = 0, lng = 0;

        //Get last known user location.
        //TODO: Get live-updating user location from listener. Use FINE_LOCATION
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        lat = location.getLatitude();
        lng = location.getLongitude();

        userLat = lat;
        userLng = lng;

        zoom = 18.0f;
        CameraPosition cameraPosition = new CameraPosition(new LatLng(lat, lng), zoom, 0, 0);
        GoogleMapOptions options = new GoogleMapOptions();
        options.mapType(GoogleMap.MAP_TYPE_SATELLITE)
                .compassEnabled(false)
                .rotateGesturesEnabled(false)
                .tiltGesturesEnabled(false)
                .camera(cameraPosition)
                .mapType(GoogleMap.MAP_TYPE_NORMAL);

        mMapFragment = MapFragment.newInstance(options);
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.mapView, mMapFragment);
        fragmentTransaction.commit();
        executePendingTransactions();
        MapsInitializer.initialize(this);

        //Wait after commit
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Set the User's coordinates into the TextView bar above the map
        TextView notices = (TextView)findViewById(R.id.notices);

        notices.setText("You are currently reporting coordinates from " + lat + ", " + lng);

        mMap = mMapFragment.getMap();

        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            Toast.makeText(getApplicationContext(), "Cannot access Google Map. Due to dynamic fragments, try using executePendingTransactions()", Toast.LENGTH_LONG).show();
            //Retry
            mMap = mMapFragment.getMap();
            Toast.makeText(getApplicationContext(), "Cannot access Google Map.", Toast.LENGTH_LONG).show();
            if (mMap != null) {
                // The Map is verified. It is now safe to manipulate the map.
                Toast.makeText(getApplicationContext(), "Google Map Accessed Successfully.", Toast.LENGTH_LONG).show();
            }
        }

//        if (mMap != null) {
//            mMap.addMarker(new MarkerOptions()
//                    .position(new LatLng(lat, lng))
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//                    .title("You"));
//        }
    }

    public void executePendingTransactions() {
//        boolean d = FragmentManager.executePendingTransactions();
    }

    public void onUserLeaveHint() {
        mMap = mMapFragment.getMap();
        if (mMap == null) {
            Toast.makeText(getApplicationContext(), "Cannot access Google Map.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Accessed Google Map. Please Re-open Numbat to continue.", Toast.LENGTH_LONG).show();
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(userLat, userLng))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    .title("You"));
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(42.424974, -71.2140763))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    .title("Dyushka"));
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(42.305753, -71.242723))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    .title("Grishka"));
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(42.3233006, -71.2041064))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title("Natasha"));
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(42.2940449, -71.377857))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                    .title("Alik"));
        }
    }

//    public void onPause() {
//
//    }

//    public void onResume() {
//        super.onResume();
//
//        setContentView(R.layout.main);
//        if (mMap != null) {
//            Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_LONG).show();
//            mMap.addMarker(new MarkerOptions()
//                    .position(new LatLng(userLat, userLng))
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//                    .title("You"));
//        }
//    }
}