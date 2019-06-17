package com.example.geohospital;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.math.RoundingMode;

public class MainActivity extends AppCompatActivity {


    private LocationManager locManager;
    private Location loc;
    private TextView lblNombre;
    private ImageView imageViewUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        lblNombre= findViewById(R.id.lblNombre);
        imageViewUser= findViewById(R.id.imageViewUser);

        setSupportActionBar(toolbar);

        Intent recibir = getIntent();
        String nombre  = recibir.getStringExtra("nombre");
        Uri foto  =  recibir.getData();

        lblNombre.setText(""+nombre);
        if (foto != null) {

            Picasso.with(getApplicationContext()).load(foto).resize(400,400).transform(new ImageTrans_CircleTransform()).into(imageViewUser);

        }

        imageViewUser.setImageURI(foto);


        Permiso();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Permiso();

            }
        });
    }


    public  void Permiso()
    {


        solicitarPermiso();

        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(locManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {

            Intent intent=new Intent(getApplicationContext(),MapsActivity.class );
            startActivity(intent);

        }
        else
        {

            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);


        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private  void solicitarPermiso()
    {
        int permiso=ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int estado=PackageManager.PERMISSION_GRANTED;

        if (permiso != estado )
        {

            ActivityCompat.requestPermissions(MainActivity.this ,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1 );

        }

    }
}
