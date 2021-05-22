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
import com.google.android.gms.common.ConnectionResult;
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

    /*@Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        if (mLocationPermissionGranted) {
            //getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            init();
        }
    }


    private static final String TAG = "MapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    final AtomicInteger requestsCounter = new AtomicInteger(0);
    //final FirebaseFirestore db = FirebaseFirestore.getInstance();

    //widgets
    private String mSearchText;
    private ImageView mGps, mInfo;
    private Button mAddToRoute;

    //var
    private FirebaseAuth mAuth;
    private Boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Marker mMarker;
    private PlaceInfo mPlace;
    private RequestQueue queue;
    private JSONObject responseJson;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String route_name;
    private FirebaseUser user;
    private String location_name;
    private ConstraintLayout searchBar;
    private int order;
    private List<Polyline> polylines=null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routing);
        //mSearchText = (EditText) findViewById(R.id.input_search);
        mAuth = FirebaseAuth.getInstance();
        mGps = (ImageView) findViewById(R.id.ic_gps);
        queue = Volley.newRequestQueue(getApplicationContext());
        route_name = getIntent().getStringExtra("route_name");
        Log.d(TAG, "onCreate: route_name" + route_name);
        user = mAuth.getCurrentUser();
        getLocationPermission();

        //initAutoComplete();
        getDirection();
        location_name = getIntent().getStringExtra("location_name");
        if(location_name != null && !location_name.equals("")){
            mAddToRoute.setVisibility(View.INVISIBLE);
            //ReadFirestore();
        }
    }

    private void initAutoComplete(){

        String apiKey = getString(R.string.google_maps_API_key);
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                StringRequest placeInfoRequest = new StringRequest(Request.Method.POST, "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + place.getId() + "&key=" + apiKey+ "&fields=name,geometry,opening_hours,formatted_address,formatted_phone_number,rating", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse: " + response);
                        try {
                            responseJson =  new JSONObject(response).getJSONObject("result");
                            mPlace = new PlaceInfo();

                            mPlace.setInRoute(getIntent().getStringExtra("route_name"));

                            if(responseJson.has("name")){
                                mPlace.setName(responseJson.getString("name"));
                            }
                            if(responseJson.has("geometry")){
                                mPlace.setLatitude(responseJson.getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
                                mPlace.setLongitude(responseJson.getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
                            }
                            if(responseJson.has("opening_hours")){
                                mPlace.setOpenNow(responseJson.getJSONObject("opening_hours").getBoolean("open_now"));
                            }
                            if(responseJson.has("formatted_address")){
                                mPlace.setAddress(responseJson.getString("formatted_address"));
                            }
                            if(responseJson.has("formatted_phone_number")){
                                mPlace.setPhoneNumber(responseJson.getString("formatted_phone_number"));
                            }
                            if(responseJson.has("rating")){
                                mPlace.setRating((float) responseJson.getDouble("rating"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, error -> Log.d(TAG, "onPlaceSelected: That didn't work!"));

                // Add the request to the RequestQueue.
                requestsCounter.incrementAndGet();
                queue.add(placeInfoRequest);

                queue.addRequestFinishedListener(request -> {
                    Log.d(TAG, "onPlaceSelected: Request Finished");
                    requestsCounter.decrementAndGet();

                    if (requestsCounter.get() == 0) {
                        //all request are done
                        moveCamera(mPlace, DEFAULT_ZOOM);
                    }
                });


            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    private void init(){
        Log.d(TAG, "init: initializing");

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked gps icon");
                getDeviceLocation();
            }
        });
    }

    private void getDirection() {
        String apiKey = getString(R.string.google_maps_API_key);
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);


        StringRequest placeInfoRequest = new StringRequest(Request.Method.POST,
                "https://maps.googleapis.com/maps/api/directions/json?origin=Disneyland&destination=Universal+Studios+Hollywood&key=" + apiKey, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "onResponse: " + response);

//                        try {
//                            responseJson =  new JSONObject(response).getJSONObject("result");
//                            mPlace = new PlaceInfo();
//
//                            mPlace.setInRoute(getIntent().getStringExtra("route_name"));
//
//                            if(responseJson.has("name")){
//                                mPlace.setName(responseJson.getString("name"));
//                            }
//                            if(responseJson.has("geometry")){
//                                mPlace.setLatitude(responseJson.getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
//                                mPlace.setLongitude(responseJson.getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
//                            }
//                            if(responseJson.has("opening_hours")){
//                                mPlace.setOpenNow(responseJson.getJSONObject("opening_hours").getBoolean("open_now"));
//                            }
//                            if(responseJson.has("formatted_address")){
//                                mPlace.setAddress(responseJson.getString("formatted_address"));
//                            }
//                            if(responseJson.has("formatted_phone_number")){
//                                mPlace.setPhoneNumber(responseJson.getString("formatted_phone_number"));
//                            }
//                            if(responseJson.has("rating")){
//                                mPlace.setRating((float) responseJson.getDouble("rating"));
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }

            }
        }, error -> Log.d(TAG, "onPlaceSelected: That didn't work!"));

        // Add the request to the RequestQueue.
        requestsCounter.incrementAndGet();
        queue.add(placeInfoRequest);

        queue.addRequestFinishedListener(request -> {
            Log.d(TAG, "onPlaceSelected: Request Finished");
            requestsCounter.decrementAndGet();

            if (requestsCounter.get() == 0) {
                //all request are done
                //moveCamera(mPlace, DEFAULT_ZOOM);
            }
        });

    }

    public void Findroutes(LatLng Start, LatLng End)
    {
        if(Start==null || End==null) {
            Toast.makeText(RoutingActivity.this,"Unable to get location",Toast.LENGTH_LONG).show();
        }
        else
        {

            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(true)
                    .waypoints(Start, End)
                    .key("Your Api Key")  //also define your api key here.
                    .build();
            routing.execute();
        }
    }

    //Routing call back functions.
    @Override
    public void onRoutingFailure(RouteException e) {
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar= Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG);
        snackbar.show();
//        Findroutes(start,end);
    }

    @Override
    public void onRoutingStart() {
        Toast.makeText(RoutingActivity.this,"Finding Route...",Toast.LENGTH_LONG).show();
    }

    //If Route finding success..
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        CameraUpdate center = CameraUpdateFactory.newLatLng(start);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
        if(polylines!=null) {
            polylines.clear();
        }
        PolylineOptions polyOptions = new PolylineOptions();
        LatLng polylineStartLatLng=null;
        LatLng polylineEndLatLng=null;


        polylines = new ArrayList<>();
        //add route(s) to the map using polyline
        for (int i = 0; i <route.size(); i++) {

            if(i==shortestRouteIndex)
            {
                polyOptions.color(getResources().getColor(R.color.colorPrimary));
                polyOptions.width(7);
                polyOptions.addAll(route.get(shortestRouteIndex).getPoints());
                Polyline polyline = mMap.addPolyline(polyOptions);
                polylineStartLatLng=polyline.getPoints().get(0);
                int k=polyline.getPoints().size();
                polylineEndLatLng=polyline.getPoints().get(k-1);
                polylines.add(polyline);

            }
            else {

            }

        }

        //Add Marker on route starting position
        MarkerOptions startMarker = new MarkerOptions();
        startMarker.position(polylineStartLatLng);
        startMarker.title("My Location");
        mMap.addMarker(startMarker);

        //Add Marker on route ending position
        MarkerOptions endMarker = new MarkerOptions();
        endMarker.position(polylineEndLatLng);
        endMarker.title("Destination");
        mMap.addMarker(endMarker);
    }

    @Override
    public void onRoutingCancelled() {
        Findroutes(start,end);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Findroutes(start,end);

    }

    private void CountRoutes() {
        order = -1;
        db
                .collection("locations")
                .whereEqualTo("inRoute", route_name)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                order++;

                            }
                            ;
                            mPlace.setOrder(++order);
                            WriteFirestore();
                        } else {
                            Log.d(TAG, "onComplete: Task failed.");
                        }
                    }
                });
    }



    private void WriteFirestore() {
        db
                .collection("locations")
                .whereEqualTo("inRoute", route_name)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            boolean hasDocument = false;

                            for(QueryDocumentSnapshot document : task.getResult()){
                                Log.d(TAG, "onComplete: null document: " + document);
                                if(document.get("name").toString().equals(mPlace.getName()))
                                    hasDocument = true;
                            }
                            if (!hasDocument) {
                                db.
                                        collection("locations")
                                        .document(mPlace.getName())
                                        .set(mPlace)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(RoutingActivity.this, "Location successfully added!", Toast.LENGTH_SHORT).show();
                                                    BackToRouteActivity();
                                                }
                                            }
                                        });
                            }else{
                                Toast.makeText(RoutingActivity.this, "Location already exist in current route.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void BackToRouteActivity(){
        finish();
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
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, mLocationCallback, null);

        LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                //TODO: UI updates.
                Log.d(TAG, "onComplete: found location!");
                Location currentLocation = location;
                Log.d(TAG, "onComplete: " + currentLocation);
                if (currentLocation != null) {
                    moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM, "My Location");
                }
            }
        });
    }

    private void moveCamera(PlaceInfo place, float zoom) {
        hideSoftKeyboard();
        LatLng latlng = new LatLng(place.getLatitude(), place.getLongitude());
        Log.d(TAG, "moveCamera: moving the camera to lat: " + latlng.latitude + ", lng: " + latlng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));

        mMap.clear();

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(RoutingActivity.this));

        // init place info
        String placeInfo = "";
        if (place.getOpenNow() != null) {
            boolean mOpenNow = place.getOpenNow();
            if (mOpenNow) {
                placeInfo += "Open Now: Yes" + "\n";
            } else {
                placeInfo += "Open Now: No" + "\n";
            }
            Log.d(TAG, "moveCamera: " + place.getOpenNow());
        }
        if (place.getAddress() != null) {
            placeInfo += "Address: " + place.getAddress() + "\n";
            Log.d(TAG, "moveCamera: " + place.getAddress());
        }
        if (place.getPhoneNumber() != null) {
            placeInfo += "Phone Number: " + place.getPhoneNumber() + "\n";
            Log.d(TAG, "moveCamera: " + place.getPhoneNumber());
        }

        if (place.getRating() != 0.0) {
            placeInfo += "Rating: " + place.getRating() + "\n";
            Log.d(TAG, "moveCamera: " + place.getRating());
        }
        Log.d(TAG, "moveCamera: " + placeInfo);
        // add marker
        MarkerOptions options = new MarkerOptions()
                .position(latlng)
                .title(place.getName())
                .snippet(placeInfo);

        mMarker = mMap.addMarker(options);



    }

    private void moveCamera(LatLng latlng, float zoom, String title){
        Log.d(TAG, "moveCamera: moving the camera to lat: " + latlng.latitude + ", lng: " + latlng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));

        if(!title.equals("My Location")){
            MarkerOptions options = new MarkerOptions()
                    .position(latlng)
                    .title(title);
            mMap.addMarker(options);
        }

        hideSoftKeyboard();
    }

    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(RoutingActivity.this);
    }

    private void getLocationPermission(){
        Log.d(TAG,"getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionGranted = true;
                    //init map
                    initMap();
                }
            }
        }
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }*/
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
            Toast.makeText(RoutingActivity.this,"Unable to get location",Toast.LENGTH_LONG).show();
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
        Toast.makeText(RoutingActivity.this, "Routing Fail.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRoutingStart() {
        //Toast.makeText(RoutingActivity.this,"Finding Route...",Toast.LENGTH_LONG).show();
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
            Toast.makeText(RoutingActivity.this, "Routing Success!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingCancelled() {
        //Findroutes(start,end);
    }


    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //Findroutes(start,end);

    }
}
