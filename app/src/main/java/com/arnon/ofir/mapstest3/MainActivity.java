package com.arnon.ofir.mapstest3;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Button signIn;
    private EditText userName;
    private FirebaseDatabase database;
    private int flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner dropdown = (Spinner)findViewById(R.id.permiChoose);
        String[] items = new String[]{"User", "Admin"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(this);

        database = FirebaseDatabase.getInstance();
        signIn = (Button) findViewById(R.id.SignInBtn);
        userName = (EditText) findViewById(R.id.userNameEt);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long l) {
        Log.d("TAG","parent.getItemAtPosition(pos): "+parent.getItemAtPosition(pos)+ " , userName.getText().toString(): "+userName.getText().toString());
        if(parent.getItemAtPosition(pos) == "Admin"){
            signIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatabaseReference ref = database.getReference("users").child(userName.getText().toString()).child(("permissions"));

                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String permission = dataSnapshot.getValue(String.class);
                            if(permission!= null){
                                Log.d("admin??" ,permission);

                                if(permission.equals("admin")){
                                    Intent userSignIn = new Intent(MainActivity.this, AdminActivity.class);
                                    userSignIn.putExtra("admin",userName.getText().toString());
                                    startActivity(userSignIn);
                                }
                            }else{
                                Toast.makeText(MainActivity.this, "Not an Admin!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            });
        }else{
            userChoose();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        userChoose();
    }

    private void userChoose() {
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent=new Intent(MainActivity.this,MyLocationDemoActivity.class);
                signInIntent.putExtra("user",userName.getText().toString());
                startActivity(signInIntent);

            }
        });

    }



//    private int checkIfAdmin(String userName) {
//        flag=0;
//
//        Log.d("userName ", userName);
//        DatabaseReference ref = database.getReference("users").child(userName).child(("permissions"));
//
//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                String permission = dataSnapshot.getValue(String.class);
//                Log.d("permi ", permission);
//
//                if(permission == "admin"){
//                    flag = 1;
//                }else{
//                    flag = -1;
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//        Log.d("ADMINORMOT", String.valueOf(flag));
//
//        return flag;
//    }


}
