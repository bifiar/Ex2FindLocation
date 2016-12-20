package com.arnon.ofir.mapstest3;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button googleMapsBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        googleMapsBtn=(Button) findViewById(R.id.GoogleMapsBtn);
        googleMapsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent anythingintent=new Intent(MainActivity.this,MyLocationDemoActivity.class);
                startActivity(anythingintent);
            }
        });
        //MyLocationBtn
        Button MyLocationBtn=(Button) findViewById(R.id.MyLocationBtn);
        MyLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent anythingintent=new Intent(MainActivity.this,GetLocation.class);
                startActivity(anythingintent);
            }
        });
    }


}
