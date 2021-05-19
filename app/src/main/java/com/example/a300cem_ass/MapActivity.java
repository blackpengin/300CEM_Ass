package com.example.a300cem_ass;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.a300cem_ass.models.PlaceInfo;
import com.example.a300cem_ass.models.User;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        if (mLocationPermissionGranted) {
            getDeviceLocation();

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
    final FirebaseFirestore db = FirebaseFirestore.getInstance();

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
    RequestQueue queue;
    JSONObject responseJson;
    private List<PlaceInfo> routes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        //mSearchText = (EditText) findViewById(R.id.input_search);
        mAuth = FirebaseAuth.getInstance();
        mGps = (ImageView) findViewById(R.id.ic_gps);
        mInfo = (ImageView) findViewById(R.id.place_info);
        mAddToRoute = (Button) findViewById(R.id.add_route);
        queue = Volley.newRequestQueue(getApplicationContext());
        routes = new ArrayList<>();

        getLocationPermission();

        initAutoComplete();

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
                // TODO: Get info about the selected place.
                StringRequest placeInfoRequest = new StringRequest(Request.Method.POST, "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + place.getId() + "&key=" + apiKey+ "&fields=name,geometry,opening_hours,formatted_address,formatted_phone_number,rating", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse: " + response);
                        try {
                            responseJson =  new JSONObject(response).getJSONObject("result");
                            mPlace = new PlaceInfo();

                            if(responseJson.has("name")){
                                mPlace.setName(responseJson.getString("name"));
                            }
                            if(responseJson.has("geometry")){
                                mPlace.setLatlng(new LatLng(responseJson.getJSONObject("geometry").getJSONObject("location").getDouble("lat"), responseJson.getJSONObject("geometry").getJSONObject("location").getDouble("lng")));
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

        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked place info");
                try{
                    if(mMarker.isInfoWindowShown()){
                        mMarker.hideInfoWindow();
                    }else{
                        Log.d(TAG, "onClick: place info: " + mPlace);
                        mMarker.showInfoWindow();
                    }
                    
                }catch(NullPointerException e){
                    Log.e(TAG, "onClick: NullPointerException" + e.getMessage() );
                }
            }
        });

        mAddToRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked add to route");
                AddToRoute();
            }
        });

        hideSoftKeyboard();
    }

    private void AddToRoute(){
        WriteFirestore();

        Intent intent = new Intent(this, RouteActivity.class);
        startActivity(intent);
    }

    private void WriteFirestore() {
        routes.add(mPlace);

        db
                .collection("users")
                .document(User.getUid())
                .update("routes", routes)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: Routes: " + routes);
                Toast.makeText(MapActivity.this, "Location successfully added!", Toast.LENGTH_SHORT).show();
                EnterRouteActivity();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MapActivity.this, "Error adding location.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void EnterRouteActivity(){
        Intent intent = new Intent(this, RouteActivity.class);
        startActivity(intent);
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
        Log.d(TAG, "moveCamera: moving the camera to lat: " + place.getLatlng().latitude + ", lng: " + place.getLatlng().longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatlng(), zoom));

        mMap.clear();

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapActivity.this));

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
                .position(place.getLatlng())
                .title(place.getName())
                .snippet(placeInfo);

        mMarker = mMap.addMarker(options);


        hideSoftKeyboard();
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

        mapFragment.getMapAsync(MapActivity.this);
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
    }
}
