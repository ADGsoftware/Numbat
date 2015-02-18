package com.adg.Main;

import com.google.android.gms.maps.model.Marker;

public class Person {
    public Marker marker;
    private int id;
    private String name;
    private double lat;
    private double lng;
    private boolean healthy;

    public Person(int id, String name, double lat, double lng) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;

        this.healthy = true; //By default
    }

    //Getters

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public boolean isHealthy() {
        return healthy;
    }

    //Setters

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }
}