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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
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
    private Custom_Item[] temp_itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        mRecycleView = findViewById(R.id.recyclerView_locations);
        mAuth = FirebaseAuth.getInstance();
        mAddLocation = (Button) findViewById(R.id.add_location);
        route_name = (EditText) findViewById(R.id.route_name);
        mStartRoute = (Button) findViewById(R.id.view_map);
        intent = getIntent().getStringExtra("route_name");
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();


        Log.d(TAG, "onStart: " + (intent != null));
        if (intent != null || !route_name.getText().toString().equals("")) {
            route_name.setEnabled(false);
            hideSoftKeyboard();
            route_name.setText(intent);
            Reload();
        }
    }

    private void init() {
        mAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isEmptyOrDulplicate()){SetRoute();}
            }
        });

        mStartRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Reload();
                    if(temp_itemList!=null && temp_itemList.length > 1){
                        EnterRoutingActivity();
                    }
                    else{
                        Toast.makeText(RouteActivity.this, getString(R.string.please_add_two_locations), Toast.LENGTH_SHORT).show();
                    }

            }

        });
    }


    private void Reload() {
        Log.d(TAG, "Reload: Reload");
        itemList.clear();

        db
                .collection("locations")
                .whereEqualTo("inRoute", route_name.getText().toString())
                .whereEqualTo("uid", mAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: " + task.getResult().size());
                            temp_itemList = new Custom_Item[task.getResult().size()];
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                temp_itemList[Integer.parseInt(document.get("order").toString())]
                                        = new Custom_Item(R.drawable.ic_route, document.get("name").toString(),  document.get("address").toString());
                            }
                            itemList.addAll(Arrays.asList(temp_itemList));
                            Log.d(TAG, "onComplete: "+ itemList);
                            UpdateRecycleView();
                        } else {
                            Log.d(TAG, "onComplete: Task failed.");
                        }
                    }
                });
    }


    private void UpdateRecycleView() {


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

    private boolean isEmptyOrDulplicate() {
        if (route_name.isEnabled()) {
            // Check route name empty | duplicate
            if (route_name.getText().toString().equals("")) {
                // Route name empty
                Toast.makeText(this, getString(R.string.please_enter_route_name), Toast.LENGTH_SHORT).show();
                return true;
            } else {
                CheckDuplicate();
                Log.d(TAG, "isEmptyOrDulplicate: "+hasSameRoute);
                if (hasSameRoute) {
                    Toast.makeText(RouteActivity.this, getString(R.string.route_exist), Toast.LENGTH_SHORT).show();
                    return true;
                }


            }
            return false;
        }
        return false;
    }


    public void CheckDuplicate(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        hasSameRoute = false;
        db
                .collection("routes")
                .whereEqualTo("route_name", route_name.getText().toString())
                .whereEqualTo("uid", mAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, "onComplete: " + document);
                                hasSameRoute = true;
                            }
                            ;

                        } else {
                            Log.d(TAG, "onComplete: Task failed.");
                        }
                    }
                });
    }


    private void SetRoute() {
        if(route_name.isEnabled()){
            intent = route_name.getText().toString();
        }
        Map<String, Object> route = new HashMap<>();
        route.put("uid", mAuth.getCurrentUser().getUid());
        route.put("route_name", route_name.getText().toString());

        db
                .collection("routes")
                .document(route_name.getText().toString()+","+mAuth.getCurrentUser().getUid())
                .set(route)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        EnterMapActivity("");
                    }
                });
    }

    private void EnterMapActivity(String name) {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("route_name", route_name.getText().toString());
        intent.putExtra("location_name", name);
        startActivity(intent);
    }

    private void EnterRoutingActivity() {

            Log.d(TAG, "EnterRoutingActivity: "+temp_itemList[0].getText1());
            Intent intent = new Intent(this, RoutingActivity.class);
            intent.putExtra("item_list", temp_itemList);
            intent.putExtra("route_name", route_name.getText().toString());
            startActivity(intent);


    }

    private void hideSoftKeyboard() {
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
            int fromPos = viewHolder.getAdapterPosition();
            int toPos = target.getAdapterPosition();
            // move item in `fromPos` to `toPos` in adapter.
            UpdateFSOrder(fromPos, toPos);
            Collections.swap(itemList, fromPos, toPos);
            Custom_Item temp = temp_itemList[fromPos];
            temp_itemList[fromPos] = temp_itemList[toPos];
            temp_itemList[toPos] = temp;
            mAdapter.notifyItemMoved(fromPos, toPos);
            Log.d(TAG, "onMove: from: "+ fromPos +", To: "+toPos);

            return true;// true if moved, false otherwise
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            RemoveFSData(position);
            itemList.remove(position);
            mAdapter.notifyItemRemoved(position);
        }
    };

    private void UpdateFSOrder(int from, int to) {
        db
                .collection("locations")
                .document((itemList.get(from).getText1()+","+route_name.getText().toString()))
                .update("order", to)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: from changed" + from + ", " + to);
                    }
                });

        db
                .collection("locations")
                .document(itemList.get(to).getText1()+","+route_name.getText().toString())
                .update("order", from)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: to changed");
                    }
                });
    }

    private void RemoveFSData(int position) {
        db
                .collection("locations")
                .document(itemList.get(position).getText1()+","+route_name.getText().toString())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onComplete: Removed");

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
                .whereEqualTo("inRoute", route_name.getText().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                int order = Integer.parseInt(document.get("order").toString());
                                if (order > position){
                                    document.getReference().update("order", order - 1);
                                }
                            }
                            Reload();

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.d(TAG, "onFailure: from failed");
                    }
                });

    }


}
