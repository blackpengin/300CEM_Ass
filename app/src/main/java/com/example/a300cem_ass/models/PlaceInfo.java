package com.example.a300cem_ass.models;

import com.google.android.gms.maps.model.LatLng;

public class PlaceInfo {

    private String name;
    private LatLng latlng;
    private Boolean openNow;
    private String address;
    private String phoneNumber;
    private float rating;

    public PlaceInfo(){
    }

    public String getName() {
        return name;
    }

    public LatLng getLatlng() {
        return latlng;
    }

    public Boolean getOpenNow() {
        return openNow;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public float getRating() {
        return rating;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLatlng(LatLng latlng) {
        this.latlng = latlng;
    }

    public void setOpenNow(Boolean openNow) {
        this.openNow = openNow;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "PlaceInfo{" +
                "name='" + name + '\'' +
                ", latlng=" + latlng +
                ", openNow=" + openNow +
                ", address='" + address + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", rating=" + rating +
                '}';
    }
}
