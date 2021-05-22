package com.example.a300cem_ass.models;

public class PlaceInfo {

    private String uid;
    private String inRoute;
    private String name;
    private Double latitude, longitude;
    private Boolean openNow;
    private String address;
    private String phoneNumber;
    private double rating;
    private int order;

    public PlaceInfo(){
    }

    public String getUid() {
        return uid;
    }

    public String getInRoute() {
        return inRoute;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public int getOrder() {
        return order;
    }

    public String getName() {
        return name;
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

    public double getRating() {
        return rating;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setInRoute(String inRoute) {
        this.inRoute = inRoute;
    }

    public void setName(String name) {
        this.name = name;
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

    public void setOrder(int order) {
        this.order = order;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "PlaceInfo{" +
                "inRoute='" + inRoute + '\'' +
                ", name='" + name + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", openNow=" + openNow +
                ", address='" + address + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", rating=" + rating +
                '}';
    }

}
