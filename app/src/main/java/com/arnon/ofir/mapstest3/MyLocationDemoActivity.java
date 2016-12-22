package com.arnon.ofir.mapstest3;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Ofir on 12/16/2016.
 */

public class MyLocationDemoActivity extends AppCompatActivity
        implements
        GoogleMap.OnMyLocationButtonClickListener, OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback
,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener {

    private FirebaseDatabase database;
    protected Location mLastLocation;
    protected GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private String user;
    private ArrayList<userDetails> userDetailsList;
    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 5000; /* 2 sec */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;// * Request code for location permission request.
    protected static final String TAG = "GetLocation";
    private boolean mPermissionDenied = false;// * Flag indicating whether a requested permission has been denied after returning in
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        database = FirebaseDatabase.getInstance();
        checkDBupdate();
        user = this.getIntent().getExtras().getString("user");
        creatUserOnDb();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_location_demo);
        buttonsListener();

        buildGoogleApiClient();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    private void buttonsListener(){
        Button frienndsLocations = (Button) findViewById(R.id.friendslocationsBtn);
        frienndsLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent frienndsLocationsIntent = new Intent(MyLocationDemoActivity.this, ListViewedUserSelect.class);
                frienndsLocationsIntent.putExtra("user", user);
                frienndsLocationsIntent.putExtra("users", userDetailsList);
                startActivity(frienndsLocationsIntent);//userDetailsList

            }
        });
        Button showBtn = (Button) findViewById(R.id.showBtn);
        showBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectedUserMarks();
            }
        });
        Button refreshBtb = (Button) findViewById(R.id.refreshBtb);
        refreshBtb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GetUserData();
            }
        });
    }
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    private void checkDBupdate(){
        if (this.getIntent().getExtras().getSerializable("users") == null) {
            GetUserData();
        } else {
            userDetailsList = (ArrayList<userDetails>) this.getIntent().getExtras().getSerializable("users");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation saved on database", Toast.LENGTH_SHORT).show();
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    private void creatUserOnDb() {
        DatabaseReference myRef = database.getReference("users");
        myRef.child(user).setValue(new MyLocation("0", "0", "user"));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Get last known recent location.
        Location mCurrentLocation = enableGetMyLocation();
        // Note that this can be NULL if last location isn't already known.
        if (mCurrentLocation != null) {
            // Print current location if not null
            Log.d("DEBUG", "current location: " + mCurrentLocation.toString());
            updateLocationOnDb(mCurrentLocation);
        }
        // Begin polling for new location updates.
        startLocationUpdates();
    }

    // Trigger new location updates at interval
    protected void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        // Request location updates
        enableUpdateMyLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {

        DatabaseReference myRef = database.getReference("users");
        myRef.child(user).setValue(new MyLocation(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), "user"));
    }

    private void GetUserData() {
        database.getReference("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("1","********get users DB************");
                userDetailsList = new ArrayList<userDetails>();
                Map<String, Map<String, String>> map = (Map<String, Map<String, String>>) dataSnapshot.getValue();
                for (Map.Entry<String, Map<String, String>> entry : map.entrySet()) {
                    userDetails userDetails = new userDetails(entry.getValue().get("permissions"), entry.getKey(), false, entry.getValue().get("latitude"), entry.getValue().get("longitude"));
                    userDetailsList.add(userDetails);
                }

            }
            @Override
            public void onCancelled(DatabaseError DbError) {
                Log.d("1", "Database Error: " + DbError.getMessage());
            }
        });

    }
    private Location enableGetMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mGoogleApiClient != null) {
            // Access to the location has been granted to the app.
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            // LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
            //mLocationRequest, this);
        }
        return mLastLocation;
    }

    private void updateLocationOnDb(Location location) {
        DatabaseReference myRef = database.getReference("users");
        myRef.child(user).setValue(new MyLocation(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), "user"));
    }

    private void enableUpdateMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mGoogleApiClient != null) {
            // Access to the location has been granted to the app.
            // mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    /**
     * Enables the My MyLocation layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    protected void showSelectedUserMarks() {
        IconGenerator iconFactory = new IconGenerator(this);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(32.1041943, 35.2050993), 10));
        for (userDetails userDetails : userDetailsList) {
            if (userDetails.isSelected()) {
                int randStyle = (int)( Math.random() * 3);
                switch (randStyle) {
                    case 0:
                        iconFactory.setStyle(IconGenerator.STYLE_RED);
                        break;
                    case 1:
                        iconFactory.setStyle(IconGenerator.STYLE_PURPLE);
                        break;
                    case 2:
                        iconFactory.setStyle(IconGenerator.STYLE_GREEN);
                        break;

                }
                addIcon(iconFactory, userDetails.getuserName(), new LatLng(Double.parseDouble(userDetails.getLatitude()), Double.parseDouble(userDetails.getLongitude())));
            }
        }
    }
    private void addIcon(IconGenerator iconFactory, CharSequence text, LatLng position) {
        MarkerOptions markerOptions = new MarkerOptions().
                icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(text))).
                position(position).
                anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

        mMap.addMarker(markerOptions);
    }

}

