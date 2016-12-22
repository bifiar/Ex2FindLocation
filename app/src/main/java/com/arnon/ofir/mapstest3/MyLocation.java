package com.arnon.ofir.mapstest3;

/**
 * Created by Ofir on 12/18/2016.
 */

public class MyLocation {
    private String latitude;
    private String longitude;
    private String permissions;

    public MyLocation() {
    }

    public String getPermissions() {
        return permissions;
    }

    public MyLocation(String latitude, String longitude, String permissions) {
        this.latitude = latitude;

        this.longitude = longitude;
        this.permissions=permissions;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {return longitude;}
}
