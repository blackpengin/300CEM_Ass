package com.example.a300cem_ass.models;

import java.util.List;

public class User {

    private String email;
    private String password;
    private List<List> routes;
    private List<PlaceInfo> placeInfos;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public List<List> getRoutes() {
        return routes;
    }

    public List<PlaceInfo> getPlaceInfos() {
        return placeInfos;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRoutes(List<List> routes) {
        this.routes = routes;
    }

    public void setPlaceInfos(List<PlaceInfo> placeInfos) {
        this.placeInfos = placeInfos;
    }
}