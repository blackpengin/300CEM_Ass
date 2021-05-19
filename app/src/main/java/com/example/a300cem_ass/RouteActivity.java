package com.example.a300cem_ass;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RouteActivity extends AppCompatActivity {

    private RecyclerView mRecycleView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Button mAddRoute, mStartRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        ArrayList<Custom_Item> itemList = new ArrayList<>();
        itemList.add(new Custom_Item(R.drawable.ic_location, "Line 1", "Line 2"));
        itemList.add(new Custom_Item(R.drawable.ic_location, "Line 3", "Line 4"));
        itemList.add(new Custom_Item(R.drawable.ic_location, "Line 5", "Line 6"));

        mRecycleView = findViewById(R.id.recyclerView_locations);
        mRecycleView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new CustomAdapter(itemList);

        mRecycleView.setLayoutManager(mLayoutManager);
        mRecycleView.setAdapter(mAdapter);

        mAddRoute = (Button) findViewById(R.id.add_location);

        init();
    }

    private void init(){
        mAddRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EnterMapActivity();
            }
        });
    }

    private void EnterMapActivity(){
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }
}
