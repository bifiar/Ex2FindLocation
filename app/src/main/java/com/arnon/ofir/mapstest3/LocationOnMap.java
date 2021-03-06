package com.arnon.ofir.mapstest3;

/**
 * Created by zahar on 22/12/16.
 */

public class LocationOnMap {
    private String latitude;
    private String longitude;

    public LocationOnMap() {
    }

    public LocationOnMap(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String toString(){
        return  "{"+latitude+" , "+longitude + "}";
    }
}
