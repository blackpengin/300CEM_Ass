package com.example.a300cem_ass;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a300cem_ass.models.PlaceInfo;
import com.example.a300cem_ass.models.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int ERROR_DIALOG_REQUEST = 9001;

    private FirebaseAuth mAuth;
    private Button addRouteBtn;
    private RecyclerView mRecycleView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<PlaceInfo> routes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        addRouteBtn = (Button) findViewById(R.id.addRouteBtn);


        init();

        if(isServicesOK()){
            initMap();
        }
    }

    private void init(){

        ArrayList<Custom_Item> itemList = new ArrayList<>();
        ReadFirestore();
        itemList.add(new Custom_Item(R.drawable.ic_location, "Line 1", "Line 2"));
        itemList.add(new Custom_Item(R.drawable.ic_location, "Line 3", "Line 4"));
        itemList.add(new Custom_Item(R.drawable.ic_location, "Line 5", "Line 6"));

        mRecycleView = findViewById(R.id.recyclerView_routes);
        mRecycleView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new CustomAdapter(itemList);

        mRecycleView.setLayoutManager(mLayoutManager);
        mRecycleView.setAdapter(mAdapter);
    }

    private void ReadFirestore(){

        DocumentReference documentReference = db
                .collection("users")
                .document(User.getUid());

            documentReference.get().addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(Task task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = (DocumentSnapshot) task.getResult();
                    if(documentSnapshot != null){
                        String routes_firestore = documentSnapshot.getString("routes");
                        Log.d(TAG, "onComplete: Routes: " + routes_firestore);
                        if(routes_firestore != null){
                            //TODO: Put data into routes List
                        }
                    }else{ Log.d(TAG, "onComplete: Document null."); }
                }else{ Log.d(TAG, "onComplete: Task failed."); }
            }
        });
    }



    private void initMap(){

        addRouteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EnterRouteActivity();
            }
        });
    }

    private void EnterRouteActivity(){
        Intent intent = new Intent(this, RouteActivity.class);
        startActivity(intent);
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occurred but we can resolve it
            Log.d(TAG, "isServicesOK : an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}