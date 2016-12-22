package com.arnon.ofir.mapstest3;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class AdminActivity extends AppCompatActivity {

    private String admin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        admin =this.getIntent().getExtras().getString("admin");


    }
}
