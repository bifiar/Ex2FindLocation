package com.arnon.ofir.mapstest3;

/**
 * Created by zahar on 22/12/16.
 */

public class LocationOnMap {
    private String latitude;
    private String longitude;
    private String permissionsOrMac;


    public LocationOnMap() {
    }

    public LocationOnMap(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LocationOnMap(String latitude, String longitude, String permissionsOrMac) {
        this.latitude = latitude;

        this.longitude = longitude;
        this.permissionsOrMac=permissionsOrMac;
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

    public String getPermissionsOrMac() {
        return permissionsOrMac;
    }

    public void setPermissionsOrMac(String permissionsOrMac) {
        this.permissionsOrMac = permissionsOrMac;
    }

    public String toString(){
        return  "{"+latitude+" , "+longitude + "}";
    }
}
