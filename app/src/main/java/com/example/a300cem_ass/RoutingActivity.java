package com.example.a300cem_ass;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.a300cem_ass.models.PlaceInfo;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class RoutingActivity extends AppCompatActivity implements OnMapReadyCallback, RoutingListener {
    private static final String TAG = "RoutingActivity";
    private static final float DEFAULT_ZOOM = 15f;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    //google map object
    private GoogleMap mMap;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Custom_Item[] itemList;
    private String route_name;
    private PlaceInfo mPlace;
    private LatLng moveTo = null;
    private PlaceInfo[] placeInfos;
    private int placeInfoIndex = 0;
    private ImageView mGps;
    //current and destination location objects
    Location myLocation = null;
    Location destinationLocation = null;
    protected LatLng start = null;
    protected LatLng end = null;
    private FirebaseAuth mAuth;
    //to get location permissions.
    private final static int LOCATION_REQUEST_CODE = 23;
    boolean locationPermission = false;

    //polyline object
    private List<Polyline> polylines = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routing);

        //request location permission.
        getLocationPermission();

        //init google map fragment to show map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        polylines = new ArrayList<>();
        mGps = (ImageView) findViewById(R.id.ic_gps);
        mAuth = FirebaseAuth.getInstance();
        init();
        initRoute();
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationPermission = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(RoutingActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermission = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            locationPermission = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    locationPermission = true;
                    //init map
                    initMap();
                }
            }
        }
    }

    private void init() {
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked gps icon");
                getDeviceLocation();
            }
        });
    }

    private void initRoute() {
        itemList = (Custom_Item[]) getIntent().getSerializableExtra("item_list");
        route_name = getIntent().getStringExtra("route_name");
        final boolean[] isFirst = {true};
        placeInfos = new PlaceInfo[itemList.length];
        for (int i = 0; i < itemList.length; i++) {
            int finalI = i;
            int finalI1 = i;
            db
                    .collection("locations")
                    .whereEqualTo("name", itemList[i].getText1())
                    .whereEqualTo("inRoute", route_name)
                    .whereEqualTo("uid", mAuth.getCurrentUser().getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, "onComplete: " + document);
                                    mPlace = new PlaceInfo();

                                    if (document.get("name") != null) {
                                        mPlace.setName(document.get("name").toString());
                                    }
                                    if (document.get("address") != null) {
                                        mPlace.setAddress(document.get("address").toString());
                                    }
                                    if (document.get("latitude") != null) {
                                        mPlace.setLatitude((Double) document.get("latitude"));
                                    }
                                    if (document.get("longitude") != null) {
                                        mPlace.setLongitude((Double) document.get("longitude"));
                                    }
                                    if (document.get("openNow") != null) {
                                        mPlace.setOpenNow((Boolean) document.get("openNow"));
                                    }
                                    if (document.get("phoneNumber") != null) {
                                        mPlace.setPhoneNumber(document.get("phoneNumber").toString());
                                    }
                                    if (document.get("rating") != null) {
                                        mPlace.setRating((Double) document.get("rating"));
                                    }
                                    placeInfos[finalI1] = mPlace;
                                    start = new LatLng(mPlace.getLatitude(), mPlace.getLongitude());
                                    if (isFirst[0]) {
                                        isFirst[0] = false;
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, DEFAULT_ZOOM));
                                    }
                                }
                                ;
                            } else {
                                Log.d(TAG, "onComplete: Task failed.");
                            }
                        }
                    });

            if (i < itemList.length - 1) {
                finalI++;
                db
                        .collection("locations")
                        .whereEqualTo("name", itemList[finalI].getText1())
                        .whereEqualTo("inRoute", route_name)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (DocumentSnapshot document : task.getResult()) {
                                        Log.d(TAG, "onComplete: " + document);


                                        Double latitude = (Double) document.get("latitude");
                                        Double longitude = (Double) document.get("longitude");
                                        end = new LatLng(latitude, longitude);
                                        Log.d(TAG, "onComplete: Start: " + start + ", End: " + end);
                                        Findroutes(start, end);
                                    }
                                    ;

                                } else {
                                    Log.d(TAG, "onComplete: Task failed.");
                                }
                            }
                        });
            }


        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }

    private void getDeviceLocation() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        //TODO: UI updates.
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, mLocationCallback, null);

        LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                //TODO: UI updates.
                Log.d(TAG, "onSuccess: found location!");
                Location currentLocation = location;
                Log.d(TAG, "onSuccess: "+currentLocation);
                if (currentLocation != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM));
                }
            }
        });
    }

    // function to find Routes.
    public void Findroutes(LatLng Start, LatLng End)
    {
        Log.d(TAG, "Findroutes: "+Start+", " + End);
        if(Start==null || End==null) {
            Toast.makeText(RoutingActivity.this,getString(R.string.get_location_fail),Toast.LENGTH_LONG).show();
        }
        else
        {

            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(true)
                    .waypoints(Start, End)
                    .key(getString(R.string.google_maps_API_key))  //also define your api key here.
                    .build();
            routing.execute();
        }
    }

    //Routing call back functions.
    @Override
    public void onRoutingFailure(RouteException e) {
        Toast.makeText(RoutingActivity.this, getString(R.string.routing_fail), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRoutingStart() {
    }

    //If Route finding success..
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        PolylineOptions polyOptions = new PolylineOptions();

        //add route(s) to the map using polyline
        for (int i = 0; i <route.size(); i++) {

            if(i==shortestRouteIndex)
            {
                //polyOptions.color(getResources().getColor(R.color.colorPrimary));
                polyOptions.width(7);
                polyOptions.addAll(route.get(shortestRouteIndex).getPoints());
                Polyline polyline = mMap.addPolyline(polyOptions);
                polylines.add(polyline);

            }
        }

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(RoutingActivity.this));

        // init place info
        String placeInfo = "";
        if (placeInfos[placeInfoIndex].getOpenNow() != null) {
            boolean mOpenNow = placeInfos[placeInfoIndex].getOpenNow();
            if (mOpenNow) {
                placeInfo += "Open Now: Yes" + "\n";
            } else {
                placeInfo += "Open Now: No" + "\n";
            }
        }
        if (placeInfos[placeInfoIndex].getAddress() != null) {
            placeInfo += "Address: " + placeInfos[placeInfoIndex].getAddress() + "\n";
        }
        if (placeInfos[placeInfoIndex].getPhoneNumber() != null) {
            placeInfo += "Phone Number: " + placeInfos[placeInfoIndex].getPhoneNumber() + "\n";
        }

        if (placeInfos[placeInfoIndex].getRating() != 0.0) {
            placeInfo += "Rating: " + placeInfos[placeInfoIndex].getRating() + "\n";
        }
        // add marker
        MarkerOptions options = new MarkerOptions()
                .position(new LatLng(placeInfos[placeInfoIndex].getLatitude(), placeInfos[placeInfoIndex].getLongitude()))
                .title(placeInfos[placeInfoIndex].getName())
                .snippet(placeInfo);

        mMap.addMarker(options);
        Log.d(TAG, "onRoutingSuccess: Marker Added" + placeInfos[placeInfoIndex].getName());
        placeInfoIndex++;

        if(placeInfoIndex == placeInfos.length - 1){
            mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(RoutingActivity.this));

            // init place info
            placeInfo = "";
            if (placeInfos[placeInfoIndex].getOpenNow() != null) {
                boolean mOpenNow = placeInfos[placeInfoIndex].getOpenNow();
                if (mOpenNow) {
                    placeInfo += "Open Now: Yes" + "\n";
                } else {
                    placeInfo += "Open Now: No" + "\n";
                }
            }
            if (placeInfos[placeInfoIndex].getAddress() != null) {
                placeInfo += "Address: " + placeInfos[placeInfoIndex].getAddress() + "\n";
            }
            if (placeInfos[placeInfoIndex].getPhoneNumber() != null) {
                placeInfo += "Phone Number: " + placeInfos[placeInfoIndex].getPhoneNumber() + "\n";
            }

            if (placeInfos[placeInfoIndex].getRating() != 0.0) {
                placeInfo += "Rating: " + placeInfos[placeInfoIndex].getRating() + "\n";
            }
            // add marker
            options = new MarkerOptions()
                    .position(new LatLng(placeInfos[placeInfoIndex].getLatitude(), placeInfos[placeInfoIndex].getLongitude()))
                    .title(placeInfos[placeInfoIndex].getName())
                    .snippet(placeInfo);

            mMap.addMarker(options);
            Log.d(TAG, "onRoutingSuccess: Last Marker Added"+placeInfos[placeInfoIndex].getName());
            Toast.makeText(RoutingActivity.this, getString(R.string.routing_success), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingCancelled() {
    }
}
