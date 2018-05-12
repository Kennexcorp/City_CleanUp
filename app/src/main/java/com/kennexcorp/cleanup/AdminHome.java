package com.kennexcorp.cleanup;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.Locale;

public class AdminHome extends Activity {

    private FloatingActionButton logoutButton;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private RecyclerView dirtView;
    private DatabaseReference databaseReference;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Recycler");
        databaseReference.keepSynced(true);
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(AdminHome.this, Welcome.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };
        dirtView = findViewById(R.id.dirt_list);
        dirtView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        dirtView.setLayoutManager(linearLayoutManager);

        logoutButton = findViewById(R.id.floatingActionButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(AdminHome.this, "logged out", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);

        FirebaseRecyclerAdapter<DirtModel, DirtViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<DirtModel, DirtViewHolder>(
                DirtModel.class,
                R.layout.dirt_row,
                DirtViewHolder.class,
                databaseReference
        ) {
            @Override
            protected void populateViewHolder(DirtViewHolder viewHolder, final DirtModel model, final int position) {
                viewHolder.setDescription(model.getDescription());
                viewHolder.setImage(getApplicationContext(), model.getImageUrl());
                viewHolder.setLatitude(model.getLatitude());
                viewHolder.setLongitude(model.getLongitude());
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bundle = new Bundle();
                        //bundle.putDoubleArray("location",location);
                        double[] location = { model.getLatitude(), model.getLongitude()};
                        Log.e("Loc", model.getLatitude()+"  :  "+model.getLongitude());
                        bundle.putDoubleArray("location", location);
                        //intent.putExtras(loc);
                        Intent intent = new Intent(AdminHome.this, MapsActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                });
                viewHolder.deleteReport.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (model.getId() == null){
                            Log.e("hello", "no delete");
                        }else {
                            databaseReference.child(model.getId()).removeValue();
                        }
                    }
                });
            }
        };
        dirtView.setAdapter(firebaseRecyclerAdapter);
    }

    public static class DirtViewHolder extends RecyclerView.ViewHolder {

        View mView;
        ImageView deleteReport;

        public DirtViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            deleteReport = itemView.findViewById(R.id.deleteReport);
        }

        void setDescription(String description) {
            TextView desc = mView.findViewById(R.id.dirtDescription);
            desc.setText(description);
        }

        void setImage(Context context, String image) {
            ImageView postView = mView.findViewById(R.id.dirtImage);
            Picasso.with(context).load(image).into(postView);
        }

        void setLatitude(double latitude) {
            TextView lat = mView.findViewById(R.id.latitude);
            lat.setText(latitude+"");
        }

        void setLongitude(double longitude) {
            TextView lon = mView.findViewById(R.id.longitude);
            lon.setText(longitude+"");
        }

    }
}
