package com.arnon.ofir.mapstest3;

import java.io.Serializable;

/**
 * Created by Ofir on 12/20/2016.
 */

public class Country implements Serializable{

    String premission = null;
    String userName = null;
    boolean selected = false;

    public Country(String premission, String userName, boolean selected) {
        super();
        this.premission = premission;
        this.userName = userName;
        this.selected = selected;
    }

    public String getpremission() {
        return premission;
    }
    public void setpremission(String premission) {
        this.premission = premission;
    }
    public String getuserName() {
        return userName;
    }
    public void setuserName(String userName) {
        this.userName = userName;
    }

    public boolean isSelected() {
        return selected;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

}