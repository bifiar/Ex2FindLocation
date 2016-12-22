package com.arnon.ofir.mapstest3;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button signIn=(Button) findViewById(R.id.SignInBtn);
        final EditText userName= (EditText) findViewById(R.id.userNameEt);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent=new Intent(MainActivity.this,MyLocationDemoActivity.class);
                signInIntent.putExtra("user",userName.getText().toString());
                startActivity(signInIntent);

            }
        });

    }


}
