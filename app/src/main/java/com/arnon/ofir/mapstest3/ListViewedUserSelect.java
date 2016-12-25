package com.arnon.ofir.mapstest3;

/**
 * Created by Ofir on 12/20/2016.
 */

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ListViewedUserSelect extends Activity {
    private DatabaseReference databaseRef;
    private String user;
    MyCustomAdapter dataAdapter = null;
    private  ArrayList<userDetails> userDetailsList;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        databaseRef=FirebaseDatabase.getInstance().getReference();
        userDetailsList = ( ArrayList<userDetails>)this.getIntent().getExtras().getSerializable("users");
        user=this.getIntent().getExtras().getString("user");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        displayListView(); //Generate list View from ArrayList
        checkButtonClick();
    }
    private void displayListView(){
        //create an ArrayAdaptar from the String Array
        dataAdapter = new MyCustomAdapter(this,
                R.layout.country_info, userDetailsList);
        ListView listView = (ListView) findViewById(R.id.listView1);
        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);


        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // When clicked, show a toast with the TextView text
                userDetails userDetails = (userDetails) parent.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(),
                         userDetails.getuserName()+"Was selected" ,
                        Toast.LENGTH_LONG).show();
            }
        });
    }
    private class MyCustomAdapter extends ArrayAdapter<userDetails> {

        private ArrayList<userDetails> userDetailsList;

        public MyCustomAdapter(Context context, int textViewResourceId,
                               ArrayList<userDetails> userDetailsList) {
            super(context, textViewResourceId, userDetailsList);
            this.userDetailsList = new ArrayList<userDetails>();
            this.userDetailsList.addAll(userDetailsList);
        }

        private class ViewHolder {
            TextView code;
            CheckBox name;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d("1","getView");
            ViewHolder holder = null;
            Log.v("ConvertView", String.valueOf(position));

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.country_info, null);

                holder = new ViewHolder();
                holder.code = (TextView) convertView.findViewById(R.id.code);
                holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
                convertView.setTag(holder);

                holder.name.setOnClickListener( new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v ;
                        userDetails userDetails = (userDetails) cb.getTag();
                        userDetails.setSelected(cb.isChecked());
                    }
                });
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            userDetails userDetails = userDetailsList.get(position);
            holder.code.setText(" (" +  userDetails.getpremission() + ")");
            holder.name.setText(userDetails.getuserName());
            holder.name.setChecked(userDetails.isSelected());
            holder.name.setTag(userDetails);
            return convertView;
        }
    }
    private void checkButtonClick() {

        Button myButton = (Button) findViewById(R.id.findSelected);
        myButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                StringBuffer responseText = new StringBuffer();


                ArrayList<userDetails> userDetailsList = dataAdapter.userDetailsList;
                ArrayList<userDetails> userSelected=new ArrayList<userDetails>();
                for(int i = 0; i< userDetailsList.size(); i++){
                    userDetails userDetails = userDetailsList.get(i);
                    if(userDetails.isSelected()){
                        userSelected.add(userDetails);
                    }
                }

                Intent signInIntent=new Intent(ListViewedUserSelect.this,UserActivity.class);
                signInIntent.putExtra("user",user);
                signInIntent.putExtra("users", userDetailsList);// pass the users to location activity
                startActivity(signInIntent);
            }
        });

    }

}