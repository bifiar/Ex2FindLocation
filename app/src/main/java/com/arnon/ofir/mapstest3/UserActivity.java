package com.arnon.ofir.mapstest3;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
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

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Ofir on 12/16/2016.
 */

public class UserActivity extends AppCompatActivity
        implements
        GoogleMap.OnMyLocationButtonClickListener, OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback
,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener {

    private FirebaseDatabase database;
    protected Location mLastLocation;
    protected GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private String userName;
    private ArrayList<userDetails> userDetailsList;
    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 5000; /* 2 sec */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;// * Request code for location permission request.
    protected static final String TAG = "GetLocation";
    private boolean mPermissionDenied = false;// * Flag indicating whether a requested permission has been denied after returning in
    private GoogleMap mMap;
    private Button QrBtn,BleBtn;
    private LocationOnMap locationOnMap;
    private LatLng latLng ;
    private String QRlocation;
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        database = FirebaseDatabase.getInstance();
        checkDBupdate();
        userName = this.getIntent().getExtras().getString("user");
        creatUserOnDb();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_location_demo);
        buttonsListener();

        buildGoogleApiClient();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        QrBtn = (Button) findViewById(R.id.QrBtn);
        final Activity activity = this;
        buildQR(activity);
        BleBtn = (Button) findViewById(R.id.BleBtn);
        buildBle();
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }
    private void buildQR(final Activity activity) {
        QrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("Scan");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(true);
                integrator.setBarcodeImageEnabled(true);
                integrator.initiateScan();
            }
        });
    }
    private void buttonsListener(){
        Button frienndsLocations = (Button) findViewById(R.id.friendslocationsBtn);
        frienndsLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent frienndsLocationsIntent = new Intent(UserActivity.this, ListViewedUserSelect.class);
                frienndsLocationsIntent.putExtra("user", userName);
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

    }
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();

    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "You cancelled the scanning", Toast.LENGTH_LONG).show();
            } else {
                QRlocation =result.getContents();
                DatabaseReference myRef = database.getReference("QR").child(QRlocation);
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        locationOnMap =  dataSnapshot.getValue(LocationOnMap.class);
                        Log.d("TAG",locationOnMap.toString());

                        latLng=new LatLng(Double.parseDouble(locationOnMap.getLatitude()),Double.parseDouble(locationOnMap.getLongitude()));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,20));
                        mMap.addMarker(new MarkerOptions().position(latLng).title("QR:"+QRlocation+" location"));

                        DatabaseReference myRef = database.getReference("users").child(userName);
                        myRef.setValue(new MyLocation(locationOnMap.getLatitude(),locationOnMap.getLongitude(),"user"));

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    private void buildBle() {
        BleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ble = new Intent(UserActivity.this, BleActivity.class);
                ble.putExtra("permission","user");
                ble.putExtra("name", userName);
                startActivity(ble);

            }
        });
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
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    protected void onStop() {
        super.onStop();

        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        client.disconnect();
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
        myRef.child(userName).setValue(new MyLocation("0", "0", "user"));
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
        myRef.child(userName).setValue(new MyLocation(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), "user"));
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
        myRef.child(userName).setValue(new MyLocation(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), "user"));
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
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("MyLocationDemo Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }
}



