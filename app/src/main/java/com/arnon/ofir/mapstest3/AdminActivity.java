package com.arnon.ofir.mapstest3;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AdminActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Button genBtn, downloadBtn,setBle;
    private String adminName;
    private ImageView image;
    private String text2Qr;
    private FirebaseDatabase database;
    private LocationOnMap adminLocation;
    protected GoogleApiClient mGoogleApiClient;
    private Bitmap bitmap;
    private String TAG ="ERROR";
    private DatabaseReference getRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        adminName = this.getIntent().getExtras().getString("permission");
        genBtn = (Button) findViewById(R.id.QrBtn);
        setBle =(Button)findViewById(R.id.setBle);

        downloadBtn = (Button) findViewById(R.id.download);
        downloadBtn.setVisibility(View.INVISIBLE);

        image = (ImageView) findViewById(R.id.imageQR);
        database = FirebaseDatabase.getInstance();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }



        genBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateQR();
            }
        });

        setBle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ble = new Intent(AdminActivity.this, BleActivity.class);
                ble.putExtra("permission", "admin");
                ble.putExtra("name", adminName);
                startActivity(ble);
            }
        });

        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storeImage(bitmap);

            }
        });

    }

    private void generateQR() {
        getRef = database.getReference("Counters").child("Qrs");
        getRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int index = dataSnapshot.getValue(Integer.class);
                text2Qr = String.valueOf(index);

                //generate the QR
                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                try {
                    BitMatrix bitMatrix = multiFormatWriter.encode(text2Qr, BarcodeFormat.QR_CODE, 400, 400);
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    bitmap = barcodeEncoder.createBitmap(bitMatrix);
                    image.setImageBitmap(bitmap);
                } catch (WriterException e) {
                    e.printStackTrace();
                }


                //use google maps API to get the current location
                getAdminLocation();


                //set to the fireBase the new QR location
                DatabaseReference setRef = database.getReference("QR").child(text2Qr);
                setRef.setValue(adminLocation);
                getRef.setValue(Integer.parseInt(text2Qr)+1);

                downloadBtn.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("DatabaseError", databaseError.toString());
            }
        });
    }

    private void storeImage(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            //saving the image to the device
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            Toast.makeText(this, "The path that image saved: " + pictureFile.getPath(), Toast.LENGTH_LONG).show();

            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    /** Create a File for saving an image or video */
    private  File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Android/data/Files";
        File mediaStorageDir = new File(dir);

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="MI_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }


    private void getAdminLocation() {

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            adminLocation = new LocationOnMap(String.valueOf(mLastLocation.getLatitude()),String.valueOf(mLastLocation.getLongitude()));
            Log.d("LOCATION",adminLocation.getLatitude() + ", "+adminLocation.getLongitude() );
        }


        DatabaseReference ref =database.getReference("users").child(adminName);
        ref.setValue(new LocationOnMap(adminLocation.getLatitude(),adminLocation.getLongitude(),"admin"));
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("Connection suspended", "");
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("Connection failed", " ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());

    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

}
