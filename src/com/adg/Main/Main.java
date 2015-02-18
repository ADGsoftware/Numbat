package com.adg.Main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.*;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main extends Activity implements GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {
    public GoogleMap mMap;
    public ArrayList<Person> people = new ArrayList<Person>();
    public int ID = 0;
    ArrayList<String> sickMarkers = new ArrayList<String>();
    boolean complete = false;
    MoreMethods methods = new MoreMethods();
    Circle circle;
    private MapFragment mMapFragment;
    private ProgressBar progressBar;
    private float zoom;
    private double userLng;
    private double userLat;
    private boolean touchDown;
    private String userName = "";
    TextView notices;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.main);

        /*FIRST RUN!!!*/
        //Test if application has been started for the first time
        boolean firstRun = getApplicationContext().getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("firstRun", true);

        toast("Ready to compare!");

        if (firstRun) {
            toast("First run!");
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://electronneutrino.com/adg/numbat/instructions"));
            startActivity(browserIntent);
            String name = login(notices);
        } else {
            toast("Not a first run!");
            ID = getApplicationContext().getSharedPreferences("PREFERENCE", MODE_PRIVATE).getInt("userID", 0);
        }

        //Set firstRun to false, so dialog doesn't show again
        getApplicationContext().getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("firstRun", false).commit();

        MapsInitializer.initialize(getApplicationContext());

        //Screw all programming ethics you once thought were important
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

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

        /*
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
        */


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
//            Toast.makeText(getApplicationContext(), "Cannot access Google Map.", Toast.LENGTH_SHORT).show();
            if (mMap != null) {
                // The Map is verified. It is now safe to manipulate the map.
//                Toast.makeText(getApplicationContext(), "Google Map Accessed Successfully.", Toast.LENGTH_SHORT).show();
            }
        }

        //Set volume...
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) - am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
    }

    public String login(TextView notices) {
        String name = "";

        //the app is being launched for first time, do something
        Log.d("Comments", "First time");

        // first time task
        getName(notices);
        name = userName;

//        Toast.makeText(getApplicationContext(), "At login, your name is " + name + ".", Toast.LENGTH_SHORT).show();

        userName = name;

        return name;
    }

    private void getName(final TextView notices) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please enter your ID.");
        builder.setCancelable(false);

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                userName = input.getText().toString();
//                Toast.makeText(getApplicationContext(), "Hi, your ID is " + userName + ".", Toast.LENGTH_SHORT).show();
                ID = Integer.parseInt(userName);

                try {
                    postLogin(userName, notices);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                complete = true;
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void postLogin(String name, TextView notices) throws IOException {
        //Toast.makeText(getApplicationContext(), "Now, your name is " + name + ".", Toast.LENGTH_SHORT).show();

        int ID = Integer.parseInt(name);
        //Save the ID into the SharedPreferences
        getApplicationContext().getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putInt("userID", ID).commit();


        //Can continue now.

        //Send a request.
        String[] userInfo = new DatabaseManager().read(ID);

        String stringName = userInfo[0];
        userName = stringName;
        double lat = Double.parseDouble(userInfo[1]);
        double lng = Double.parseDouble(userInfo[2]);
        boolean healthy = Boolean.parseBoolean(userInfo[3]);

//        notices.setText("Welcome, " + userName + "!");

        //Play welcoming music
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.katyusha);
        mediaPlayer.start(); // no need to call prepare(); create() does that for you


        //Update the user's location and info.
        //new DatabaseManager().update(ID, userLat, userLng, true);
    }

    public void createPeople() {
        //people.add(new Person(0, "You", userLat, userLng));
        //Add all of the people in the databases
        for (int i = 24; i < 34; i++) {
            String[] personInfo = new DatabaseManager().read(i);
            String stringName = personInfo[0];
            double lat = Double.parseDouble(personInfo[1]);
            double lng = Double.parseDouble(personInfo[2]);
            boolean healthy = Boolean.parseBoolean(personInfo[3]);
            System.out.println("gyf7gysf8d7gds8f7gysd8f7g" + stringName + "" + lat + "" + lng);
            people.add(new Person(i, stringName, lat, lng));
        }
    }

    public void onUserLeaveHint() {
        mMap = mMapFragment.getMap();
        if (mMap == null) {
//            Toast.makeText(getApplicationContext(), "Cannot access Google Map. Ever.", Toast.LENGTH_SHORT).show();
        } else {
//            Toast.makeText(getApplicationContext(), "Accessed Google Map. Please Re-open Numbat to continue.", Toast.LENGTH_SHORT).show();
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

        //Draw a circle at the user's location
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(userLat, userLng))
                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.arrow_blue_transparent)))
                .title("Your Location")
                .snippet("You"));

        //Separate the world into rectangles
        rectangles();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
//        toast(marker.getId() + " vs " + userName);
        if (getIDByMarkerID(marker.getId()) == ID) {
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

    public void onResume() {
        super.onResume();
        if (mMap == null) {
//            toast("Please close and re-open Numbat.");
        } else {
//            toast("Welcome back!");
        }
    }


    //Useful methods

    //Create toast
    public void toast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    //Get user id by marker id
    public int getIDByMarkerID(String markerID) {
//        toast("The marker ID is " + markerID + ".");
        String[] charArray = markerID.split("", -1);
        return Integer.parseInt(charArray[2]) + 24;
    }

    //Divide the world up into rectangles
    public void rectangles() {
        double it = 0.1;
        double rITP = it / 2;
        double plusMinus = 1;
        for (double lat = userLat - plusMinus - rITP; lat < userLat + plusMinus + rITP; lat += it) {
            for (double lng = userLng - plusMinus - rITP; lng < userLng + plusMinus + rITP; lng += it) {
                Polygon polygon = mMap.addPolygon(new PolygonOptions()
                        .add(new LatLng(lat - rITP, lng - rITP), new LatLng(lat - rITP, lng + rITP), new LatLng(lat + rITP, lng - rITP), new LatLng(lat + rITP, lng + rITP))
                        .strokeColor(Color.BLACK)
                        .strokeWidth(3)
                        .fillColor(Color.BLUE));
                polygon.setFillColor(0x7F00FF00);
            }
        }
    }
}