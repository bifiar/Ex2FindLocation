package com.arnon.ofir.mapstest3;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Device list.
 * 
 * @author Lorensius W. L. T <lorenz@londatiga.net>
 *
 */
public class DeviceListActivity extends Activity {
	private ListView mListView;
	private DeviceListAdapter mAdapter;
	private ArrayList<BluetoothDevice> mDeviceList;
	private String permissions;
	private FirebaseDatabase database;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_paired_devices);
		permissions = this.getIntent().getExtras().getString("permissions");
		
		mDeviceList		= getIntent().getExtras().getParcelableArrayList("device.list");
		
		mListView		= (ListView) findViewById(R.id.lv_paired);
		
		mAdapter		= new DeviceListAdapter(this);
		
		mAdapter.setData(mDeviceList);
		mAdapter.setListener(new DeviceListAdapter.OnPairButtonClickListener() {			
			@Override
			public void onPairButtonClick(int position) {
				final BluetoothDevice device = mDeviceList.get(position);


				DatabaseReference reference = database.getReference("Ble");
				reference.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(DataSnapshot dataSnapshot) {
						int bles= (int)dataSnapshot.getChildrenCount();
						for (int i = 0; i < bles; i++) {
							LocationOnMap loc = dataSnapshot.child(String.valueOf(i)).getValue(LocationOnMap.class);
							if(!device.getAddress().equals(loc.getPermissionsOrMac())){
								unpairDevice(device);
							}else {
								showToast("Pairing...");
								i=bles;
								pairDevice(device);
							}
						}
					}

					@Override
					public void onCancelled(DatabaseError databaseError) {
						Log.d("DatabaseError","reference.addListenerForSingleValueEvent canceld");
					}
				});

			}
		});
		
		mListView.setAdapter(mAdapter);
		
		registerReceiver(mPairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)); 
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(mPairReceiver);
		
		super.onDestroy();
	}
	
	
	private void showToast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}
	
    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        
	        if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {	        	
	        	 final int state 		= intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
	        	 final int prevState	= intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
	        	 
	        	 if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
	        		 showToast("Paired");
	        	 } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
	        		 showToast("Unpaired");
	        	 }
	        	 
	        	 mAdapter.notifyDataSetChanged();
	        }
	    }
	};
}