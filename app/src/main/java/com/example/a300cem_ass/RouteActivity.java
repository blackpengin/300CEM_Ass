package com.example.a300cem_ass;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class RouteActivity extends AppCompatActivity {
    private static final String TAG = "RouteActivity";
    private RecyclerView mRecycleView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Button mAddLocation, mStartRoute;
    ArrayList<Custom_Item> itemList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private EditText route_name;
    QuerySnapshot snapShot;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        mAuth = FirebaseAuth.getInstance();
        mAddLocation = (Button) findViewById(R.id.add_location);
        route_name = (EditText) findViewById(R.id.route_name);

        init();
    }

    @Override
    protected void onStart() {
        super.onStart();

        ReadFirestore();
        UpdateRecycleView();
    }

    private void init(){
        mAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddLocation();
            }
        });
    }

    private void ReadFirestore(){
        itemList.clear();
        FirebaseUser user = mAuth.getCurrentUser();
        CollectionReference reference = db
                .collection("users")
                .document(user.getUid())
                .collection("routes");

        reference.get().addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(Task task) {
                if (task.isSuccessful()) {
                    snapShot = (QuerySnapshot) task.getResult();
                    snapShot.forEach(queryDocumentSnapshot -> {
                        Log.d(TAG, "onComplete: " + queryDocumentSnapshot);
                        String name = queryDocumentSnapshot.get("name").toString();
                        String address = queryDocumentSnapshot.get("address").toString();
                        itemList.add(new Custom_Item(R.drawable.ic_location, name, address));
                    });
                }
                else{ Log.d(TAG, "onComplete: Task failed."); }
            }
        });
    }

    private void UpdateRecycleView(){

        mRecycleView = findViewById(R.id.recyclerView_locations);
        mRecycleView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new CustomAdapter(itemList);
        mRecycleView.setLayoutManager(mLayoutManager);
        mRecycleView.setAdapter(mAdapter);
    }

    private void AddLocation(){
        // Check route name empty | duplicate
        if(route_name.getText().toString().equals("")){
            // Route name empty
            Toast.makeText(this, "Please enter a route name.", Toast.LENGTH_SHORT).show();
        }else if(HasDuplicate()){
            Toast.makeText(this, "Route name already exist.", Toast.LENGTH_SHORT).show();
        }else{
            WriteFirestore();
        }
    }

    private void WriteFirestore() {
        db
                .collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("routes")
                .add(route_name.getText())
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        //Toast.makeText(RouteActivity.this, "Register Successful!", Toast.LENGTH_SHORT).show();
                        EnterMapActivity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Toast.makeText(RouteActivity.this, "Register Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void EnterMapActivity(){
            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra("route_name", route_name.getText().toString());
            startActivity(intent);
    }

    private boolean HasDuplicate(){
        for (Custom_Item ci : itemList) {
            if(ci.getText1().equals(route_name.getText().toString())){
                return true;
            }
        }
        return false;
    }
}
