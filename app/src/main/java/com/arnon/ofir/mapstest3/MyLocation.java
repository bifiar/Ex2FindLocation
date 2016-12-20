package com.arnon.ofir.mapstest3;

/**
 * Created by Ofir on 12/18/2016.
 */

public class MyLocation {
    private String latitude;
    private String longitude;

    public MyLocation() {
    }

    public MyLocation(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}
