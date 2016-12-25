package com.arnon.ofir.mapstest3;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Set;

public class BleActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener{
    private TextView mStatusTv;
    private Button mActivateBtn;
    private Button mPairedBtn;
    private Button mScanBtn;
    private Button addDevice;
    protected GoogleApiClient mGoogleApiClient;
    private GoogleApiClient client;
    private FirebaseDatabase database;
    private String userPermission;
    private String userName;
    private BluetoothDevice bluD;


    private ProgressDialog mProgressDlg;

    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();

    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ble);

        userPermission = this.getIntent().getExtras().getString("permission");
        userName = this.getIntent().getExtras().getString("name");

        mStatusTv = (TextView) findViewById(R.id.tv_status);
        mActivateBtn = (Button) findViewById(R.id.btn_enable);
        mPairedBtn = (Button) findViewById(R.id.btn_view_paired);
        mScanBtn = (Button) findViewById(R.id.btn_scan);

        database = FirebaseDatabase.getInstance();
        if(userPermission.equals("admin")){
            mScanBtn.setText("Pair or Unpair Devices");
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mProgressDlg = new ProgressDialog(this);

        mProgressDlg.setMessage("Scanning...");
        mProgressDlg.setCancelable(false);
        mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                mBluetoothAdapter.cancelDiscovery();
            }
        });

        if (mBluetoothAdapter == null) {
            showUnsupported();
        } else {
            mPairedBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

                    if (pairedDevices == null || pairedDevices.size() == 0) {
                        showToast("No Located Devices Found");
                    } else {
                        final ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();
                        DatabaseReference reference = database.getReference("Ble");

                        for (final BluetoothDevice blD : pairedDevices) {
                            Log.d("GGGGG","count");
                            if(blD.getName().contains("JBL GO")){
                                Log.d("GBL", blD.getAddress());
                            }
                            reference.child(blD.getAddress()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Log.d("somone in", "!!!!!!");
//                                    Log.d("Before the list" ,dataSnapshot.getValue(LocationOnMap.class).getLatitude());
                                    LocationOnMap loc = dataSnapshot.getValue(LocationOnMap.class);
                                    if (loc != null) {
                                        list.add(blD);
                                        Log.d("Print from list" , blD.getAddress());
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
//                            Log.d("reference.child(blD.getAddress()):",reference.child(blD.getAddress()).toString());
                        }
//                            reference.addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(DataSnapshot dataSnapshot) {
//                                    LocationOnMap location = dataSnapshot.getValue(LocationOnMap.class);
//                                    int bles= (int)dataSnapshot.getChildrenCount();
//
//                                    List<DataSnapshot> databaseBle = (List<DataSnapshot>) dataSnapshot.getChildren();
//                                    for (DataSnapshot blePoint : databaseBle){
//
//                                    }
//
//                                    for (int i = 0; i < bles; i++) {
//                                        LocationOnMap loc = dataSnapshot.child(String.valueOf(i)).getValue(LocationOnMap.class);
//                                        if(blD.getAddress().equals(loc.getPermissions())){
//                                            showToast("Pairing...");
//                                            list.add(blD);
//                                            i=bles;
//                                        }
//                                    }
//                                }
//
//                                @Override
//                                public void onCancelled(DatabaseError databaseError) {
//                                    Log.d("DatabaseError","reference.addListenerForSingleValueEvent canceld");
//                                }
//                            });

//                        list.addAll(pairedDevices);
                        if (list.size() == 0) {
                            showToast("No Located Devices Found");
                        }

                        Intent intent = new Intent(BleActivity.this, DeviceListActivity.class);

                        intent.putParcelableArrayListExtra("device.list", list);

                        startActivity(intent);
                    }
                }
            });

            mScanBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    mBluetoothAdapter.startDiscovery();
                }
            });

            mActivateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mBluetoothAdapter.isEnabled()) {
                        mBluetoothAdapter.disable();

                        showDisabled();
                    } else {
                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

                        startActivityForResult(intent, 1000);
                    }
                }
            });

            if (mBluetoothAdapter.isEnabled()) {
                showEnabled();
            } else {
                showDisabled();
            }
        }

        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mReceiver, filter);


        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        if(mGoogleApiClient==null)

        {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


    }

    @Override
    public void onPause() {
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
        }

        super.onPause();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);

        super.onDestroy();
    }

    private void showEnabled() {
        mStatusTv.setText("Bluetooth is On");
        mStatusTv.setTextColor(Color.BLUE);

        mActivateBtn.setText("Disable");
        mActivateBtn.setEnabled(true);

        mPairedBtn.setEnabled(true);
        mScanBtn.setEnabled(true);
    }

    private void showDisabled() {
        mStatusTv.setText("Bluetooth is Off");
        mStatusTv.setTextColor(Color.RED);

        mActivateBtn.setText("Enable");
        mActivateBtn.setEnabled(true);

        mPairedBtn.setEnabled(false);
        mScanBtn.setEnabled(false);
    }

    private void showUnsupported() {
        mStatusTv.setText("Bluetooth is unsupported by this device");

        mActivateBtn.setText("Enable");
        mActivateBtn.setEnabled(false);

        mPairedBtn.setEnabled(false);
        mScanBtn.setEnabled(false);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_ON) {
                    showToast("Enabled");

                    showEnabled();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mDeviceList = new ArrayList<BluetoothDevice>();

                mProgressDlg.show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mProgressDlg.dismiss();

                Intent newIntent = new Intent(BleActivity.this, DeviceListActivity.class);
                newIntent.putExtra("permissions", userPermission);
                newIntent.putParcelableArrayListExtra("device.list", mDeviceList);

                startActivity(newIntent);
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                mDeviceList.add(device);

                showToast("Found device " + device.getName());
            }
        }
    };

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Ble Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
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