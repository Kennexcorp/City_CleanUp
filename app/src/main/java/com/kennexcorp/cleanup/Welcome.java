package com.kennexcorp.cleanup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class Welcome extends Activity {
    private Button dirtReport;
    private Button admin;
    public static final int RequestPermissionCode = 7;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DirtModel dirtModel;
    private DatabaseReference myRef;
    private ProgressDialog progressDialog;
    private LinearLayout linearLayout;
    private TextView cancel;
    private Button login;
    private TextInputEditText email;
    private TextInputEditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        dirtReport = findViewById(R.id.dirtreport);
        admin = findViewById(R.id.admin);
        dirtModel = new DirtModel();
        //linearLayout = new LinearLayout(this);
        //linearLayout
        linearLayout = findViewById(R.id.loginDetails);
        cancel = findViewById(R.id.cancel);
        login = findViewById(R.id.loginAdmin);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child("Recycler");
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Components");
        //getLocation();
        dirtReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CheckingPermissionIsEnabledOrNot()) {
                    progressDialog.show();
                    mAuth.signInAnonymously()
                            .addOnCompleteListener(Welcome.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("Sign", "signInAnonymously:success");
                                        progressDialog.dismiss();
                                        startActivity(new Intent(getApplicationContext(), DirtReport.class));
                                    }
                                }
                            }).addOnFailureListener(Welcome.this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Welcome.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });

                } else {
                    RequestMultiplePermission();
                }

            }
        });

        admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayout.setVisibility(View.VISIBLE);
                admin.setVisibility(View.GONE);
                dirtReport.setVisibility(View.GONE);
                //startActivity(new Intent(Welcome.this, AdminHome.class));
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayout.setVisibility(View.GONE);
                admin.setVisibility(View.VISIBLE);
                dirtReport.setVisibility(View.VISIBLE);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("Logging in---");
                String mail = email.getText().toString().trim();
                String pass = password.getText().toString().trim();
                if (TextUtils.isEmpty(mail)) {
                    Toast.makeText(Welcome.this, "Insert login mail", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(pass)) {
                    Toast.makeText(Welcome.this, "Insert login password", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressDialog.show();
                mAuth.signInWithEmailAndPassword(mail, pass)
                        .addOnCompleteListener(Welcome.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    startActivity(new Intent(Welcome.this, AdminHome.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                    progressDialog.dismiss();
                                }
                            }
                        }).addOnFailureListener(Welcome.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Welcome.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
            }
        });

    }

    private void RequestMultiplePermission() {
        ActivityCompat.requestPermissions(Welcome.this, new String[]{
                ACCESS_FINE_LOCATION,
                CAMERA,
                INTERNET,
                WRITE_EXTERNAL_STORAGE,
                READ_EXTERNAL_STORAGE,
        }, RequestPermissionCode);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {
                    boolean locationPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraServicePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean internetPermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean writePermission = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    boolean readPermission = grantResults[4] == PackageManager.PERMISSION_GRANTED;

                    if (cameraServicePermission && internetPermission && writePermission && readPermission && locationPermission) {
                        Toast.makeText(Welcome.this, "Permissions granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Welcome.this, "Permission Denied, You have to grant all permissions", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    public boolean CheckingPermissionIsEnabledOrNot() {
        int locationPermission = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int CameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        int InternetPermission = ContextCompat.checkSelfPermission(getApplicationContext(), INTERNET);
        int writePermission = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int readPermission = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);

        return CameraPermission == PackageManager.PERMISSION_GRANTED
                && InternetPermission == PackageManager.PERMISSION_GRANTED
                && writePermission == PackageManager.PERMISSION_GRANTED
                && readPermission == PackageManager.PERMISSION_GRANTED
                && locationPermission == PackageManager.PERMISSION_GRANTED;
    }
}
