package com.kennexcorp.cleanup;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class DirtReport extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CHECK_SETTINGS = 33;
    private ImageView imageUpload;
    private EditText description;
    private Button submit;
    private Uri photoURI;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private DirtModel dirtModel;
    private DatabaseReference myRef;
    private StorageReference mStorageReference;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private Uri file;
    private File photoFile;
    private ProgressDialog progressDialog;
    private StorageReference dirtRef;
    private Uri dirtUri;
    // Location objects
    private SettingsClient msSettingsClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private LocationSettingsRequest mLocationSettingsRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dirt_report);
        dirtModel = new DirtModel();
        description = findViewById(R.id.description);
        submit = findViewById(R.id.submit);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending Report...");

        msSettingsClient = LocationServices.getSettingsClient(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child("Recycler");
        mAuth = FirebaseAuth.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        dirtRef = mStorageReference.child("Dirts_dir");

        // Initializing location
        createLocationRequest();
        createLocationCallback();
        buildLocationSettingsRequest();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser == null) {
                    startActivity(new Intent(DirtReport.this, Welcome.class));
                    finish();
                }
            }
        };

        imageUpload = findViewById(R.id.imageupload);
        imageUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                capturePhoto();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String desc = description.getText().toString().trim();
                final DatabaseReference dRef = myRef.push();
                if (file != null) {
                    progressDialog.show();
                    StorageReference dirtRef = mStorageReference.child("Dirts_dir").child(file.getLastPathSegment());
                    dirtRef.putFile(file)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    dirtUri = taskSnapshot.getDownloadUrl();
                                    dirtModel.setImageUrl(dirtUri.toString());
                                    dirtModel.setDescription(desc);
                                    dirtModel.setId(dRef.getKey());
                                    dRef.setValue(dirtModel);
                                    Toast.makeText(getApplicationContext(), "Dirt report Posted", Toast.LENGTH_SHORT).show();
                                    description.setText("");
                                    imageUpload.setImageDrawable(getResources().getDrawable(R.drawable.up));
                                    progressDialog.dismiss();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Error posting image", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            });

                } else {
                    Toast.makeText(getApplicationContext(), "No Image Available", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
               /* for (Location location: locationResult.getLocations()) {
                    Toast.makeText(DirtReport.this, "Lat" + location.getLatitude() + "Long " + location.getLongitude(), Toast.LENGTH_LONG).show();
                }*/
                mCurrentLocation = locationResult.getLastLocation();
                dirtModel.setLatitude(mCurrentLocation.getLatitude());
                dirtModel.setLongitude(mCurrentLocation.getLongitude());
                //Toast.makeText(DirtReport.this, "Lat" + mCurrentLocation.getLatitude() + "mCurrentLocation: Long " + mCurrentLocation.getLongitude(), Toast.LENGTH_LONG).show();
            }
        };
    }

    private void stopLocationUpdates() {
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates() {
        Task<LocationSettingsResponse> task = msSettingsClient.checkLocationSettings(mLocationSettingsRequest);

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                //startLocationUpdates();
                mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                createLocationCallback();
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(DirtReport.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                            Log.e("error", sendEx.getMessage());
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        String errorMessage = "Location settings are inadequate, and cannot be " +
                                "fixed here. Fix in Settings.";
                        Log.e("error", errorMessage);
                        Toast.makeText(DirtReport.this, errorMessage, Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void capturePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null){

            photoFile = createImageFile();

            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this, "com.kennexcorp.cleanup", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                Log.e("PHOTO URI", photoFile.toString());
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                //galleryAddPic();
            }
        }
    }

    private File createImageFile() {
        Long ts = System.currentTimeMillis()/1000;
        String timeStamp = ts.toString();
        String imageFileName = "CLEANUP_" + timeStamp + "_";
        File image = null;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        try {
            image = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        //String mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            file = Uri.fromFile(new File(photoFile.toString()));;
            imageUpload.setImageURI(photoURI);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //mAuth.signOut();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();

    }

}
