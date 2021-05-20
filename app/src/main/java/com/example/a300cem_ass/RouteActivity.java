package com.example.a300cem_ass;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RouteActivity extends AppCompatActivity {
    private static final String TAG = "RouteActivity";
    private RecyclerView mRecycleView;
    private CustomAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Button mAddLocation, mStartRoute;
    ArrayList<Custom_Item> itemList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private EditText route_name;
    private boolean hasSameRoute;
    private String intent;
    private String[][] temp_itemList;

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

        intent = getIntent().getStringExtra("route_name");
        Log.d(TAG, "onStart: " + (intent != null));
        if(intent != null || !route_name.getText().toString().equals("")){
            route_name.setEnabled(false);
            hideSoftKeyboard();
            inittemp_Itemlist();
        }
    }

    private void init(){
        mAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HasSameRoute();
            }
        });
    }

    private void inittemp_Itemlist(){
        route_name.setText(intent);
        db
                .collection("locations")
                .whereEqualTo("inRoute", route_name.getText().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: " + task.getResult().size());
                            temp_itemList = new String[task.getResult().size()][1];
                            for(QueryDocumentSnapshot document:task.getResult()){
                                temp_itemList[Integer.parseInt(document.get("order").toString())][0] = document.get("name").toString();
                            }
                            ReadLocations();
                        }
                        else{ Log.d(TAG, "onComplete: Task failed."); }
                    }
                });
    }

    private void ReadLocations(){

        itemList.clear();
        db
                .collection("locations")
                .whereEqualTo("inRoute", route_name.getText().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for(QueryDocumentSnapshot document:task.getResult()) {
                                for(String[] s :temp_itemList){
                                    if(document.get("name").toString().equals(s[0])){
                                        itemList.add(new Custom_Item(R.drawable.ic_route, document.get("name").toString(), document.get("address").toString()));
                                    }
                                }
                            }
                            UpdateRecycleView();
                        }
                        else{ Log.d(TAG, "onComplete: Task failed."); }
                    }
                });
    }

    private void HasSameRoute(){
        hasSameRoute = false;
        db
                .collection("routes")
                .whereEqualTo("route_name", route_name.getText().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, "onComplete: " + document);
                                hasSameRoute = true;
                            };
                            AddLocation();
                        }
                        else{ Log.d(TAG, "onComplete: Task failed."); }
                    }
                });
    }

    private void UpdateRecycleView(){

        mRecycleView = findViewById(R.id.recyclerView_locations);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new CustomAdapter(itemList);
        mRecycleView.setLayoutManager(mLayoutManager);
        mRecycleView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new CustomAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String location_name = itemList.get(position).getText1();
                EnterMapActivity(location_name);
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(mRecycleView);
    }

    private void AddLocation(){
        // Check route name empty | duplicate
        if(route_name.getText().toString().equals("")){
            // Route name empty
            Toast.makeText(this, "Please enter a route name.", Toast.LENGTH_SHORT).show();
        }else if(route_name.isEnabled()){
            if(hasSameRoute){
            Toast.makeText(this, "Route name already exist.", Toast.LENGTH_SHORT).show();
            }
        }else{
            SetRoute();
        }
    }

    private void SetRoute() {
        Map<String, Object> route = new HashMap<>();
        route.put("uid", mAuth.getCurrentUser().getUid());
        route.put("route_name", route_name.getText().toString());

        db
                .collection("routes")
                .document(route_name.getText().toString())
                .set(route)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Toast.makeText(RouteActivity.this, "Register Successful!", Toast.LENGTH_SHORT).show();
                        EnterMapActivity("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Toast.makeText(RouteActivity.this, "Register Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void EnterMapActivity(String name){
            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra("route_name", route_name.getText().toString());
        intent.putExtra("location_name", name);
            startActivity(intent);
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            // Step 2-2
            int fromPos = viewHolder.getAdapterPosition();
            int toPos = target.getAdapterPosition();
            // move item in `fromPos` to `toPos` in adapter.
            Collections.swap(itemList, fromPos, toPos);
            mAdapter.notifyItemMoved(fromPos, toPos);
            UpdateOrder(fromPos, toPos);
            return true;// true if moved, false otherwise
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            // Step 3-2
            int position = viewHolder.getAdapterPosition();
            itemList.remove(position);
            mAdapter.notifyItemRemoved(position);
        }
    };

    private void UpdateOrder(int from, int to){
        db
                .collection("locations")
                .document(itemList.get(from).getText1())
                .update("order", to)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: from changed" + from+", " + to);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.d(TAG, "onFailure: from failed");
                    }
                });
        db
                .collection("locations")
                .document(itemList.get(to).getText1())
                .update("order", from)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: to changed");
                    }
                });
    }
}
