package com.example.a300cem_ass.models;

import android.app.Application;

import java.io.Serializable;
import java.util.List;

public class User extends Application implements Serializable {

    static String uid;
    static List<PlaceInfo> routes;

    public User() {
    }

    public static String getUid() {
        return uid;
    }

    public static List<PlaceInfo> getRoutes() {
        return routes;
    }

    public static void setUid(String s) {
        uid = s;
    }

    public static void setRoutes(List<PlaceInfo> r) {
        routes = r;
    }
}