package com.cyno.alarm.models;

/**
 * Created by hp on 21-01-2016.
 */
public class CustomRingtone {
    private String displayName;
    private String id;

    public CustomRingtone(String displayName, String id) {
        this.displayName = displayName;
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
