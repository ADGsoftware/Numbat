package com.adg.Main;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.*;

import java.util.ArrayList;

public class Main extends Activity implements GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {
    public GoogleMap mMap;
    public ArrayList<Person> people = new ArrayList<Person>();
    Marker user;
    Marker dyushka;
    Marker grishka;
    Marker alik;
    Marker natasha;
    Marker google;
    ArrayList<String> sickMarkers = new ArrayList<String>();
    private MapFragment mMapFragment;
    private ProgressBar progressBar;
    //private ViewButton toListButton;
    private float zoom;
    private double userLng;
    private double userLat;
    private boolean touchDown;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.main);

        MapsInitializer.initialize(getApplicationContext());

        double lat = 0, lng = 0;

        //Get last known user location.
        //TODO: Get live-updating user location from listener. Use FINE_LOCATION
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        lat = location.getLatitude();
        lng = location.getLongitude();


        userLat = lat;
        userLng = lng;




        //Create people
        createPeople();




        //TODO: Create a notification method!
        //Create a notification with the latitude and longitude
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.numbat)
                        .setContentTitle("Coordinates")
                        .setContentText("Reporting Data from " + userLat + ", " + userLng);
        Intent resultIntent = new Intent(this, Main.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(Main.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(3, mBuilder.build());





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
        MapsInitializer.initialize(this);


        //TODO: Fix executePendingTransactions()! It does not work!
        android.app.FragmentManager f = getFragmentManager();
        f.executePendingTransactions();

        //Set the User's coordinates into the TextView bar above the map
        TextView notices = (TextView) findViewById(R.id.notices);

        notices.setText("You are currently reporting coordinates from " + lat + ", " + lng);

        mMap = mMapFragment.getMap();

        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            //Retry
            mMap = mMapFragment.getMap();
            Toast.makeText(getApplicationContext(), "Cannot access Google Map.", Toast.LENGTH_SHORT).show();
            if (mMap != null) {
                // The Map is verified. It is now safe to manipulate the map.
                Toast.makeText(getApplicationContext(), "Google Map Accessed Successfully.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void createPeople() {
        people.add(new Person(0, "You", userLat, userLng));
        people.add(new Person(1, "Dyushka", 42.424974, -71.2140763));
        people.add(new Person(2, "Grishka", 42.305753, -71.242723));
        people.add(new Person(3, "Natasha", 42.3233006, -71.2041064));
        people.add(new Person(4, "Alik", 42.2940449, -71.377857));
        people.add(new Person(5, "Android", 37.4219184, -122.0839834));
    }

    public void onUserLeaveHint() {
        mMap = mMapFragment.getMap();
        if (mMap == null) {
            Toast.makeText(getApplicationContext(), "Cannot access Google Map. Ever.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Accessed Google Map. Please Re-open Numbat to continue.", Toast.LENGTH_LONG).show();
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.ding);
            mediaPlayer.start(); // no need to call prepare(); create() does that for you
            makeMarkers();
        }
    }

    public void makeMarkers() {
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);

        //Create the markers
        for (Person person : people) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(person.getLat(), person.getLng()))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .title(person.getName())
                    .snippet("Healthy =)"));
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (!sickMarkers.contains(marker.getTitle())) {
            mMap.addMarker(new MarkerOptions()
                    .position(marker.getPosition())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .title(marker.getTitle())
                    .snippet("Sick!"));
            sickMarkers.add(marker.getTitle());

        } else {
            mMap.addMarker(new MarkerOptions()
                    .position(marker.getPosition())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .title(marker.getTitle())
                    .snippet("Healthy =)"));
            sickMarkers.remove(marker.getTitle());
        }
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        new AlertDialog.Builder(this)
                .setTitle("Information")
                .setMessage(marker.getTitle() + " is currently " + marker.getSnippet())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
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