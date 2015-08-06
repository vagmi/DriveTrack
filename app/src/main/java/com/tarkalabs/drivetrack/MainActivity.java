package com.tarkalabs.drivetrack;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tarkalabs.drivetrack.services.LocationService;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapFragment map;
    private GoogleMap googleMap;
    private Marker marker;
    private Button toggleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toggleButton = (Button) findViewById(R.id.toggle_track);
        if(LocationService.IS_LISTENING) {
            toggleButton.setText("Stop Listening");
        } else {
            toggleButton.setText("Start Listening");
        }

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(LocationService.IS_LISTENING) {
                    toggleButton.setText("Start Listening");
                    stopListening();
                } else {
                    toggleButton.setText("Stop Listening");
                    startLocationService();
                }
            }
        });

        map = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        map.getMapAsync(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action == "location") {
                    Log.i("LOCATION", String.valueOf(intent.getExtras().getDouble("lat")));
                    Log.i("LOCATION", String.valueOf(intent.getExtras().getDouble("lng")));
                    double lat = intent.getExtras().getDouble("lat");
                    double lng = intent.getExtras().getDouble("lng");
                    updateLocation(new LatLng(lat, lng));
                }
            }
        }, new IntentFilter("location"));

    }
    private void stopListening() {
        Intent stop_listening = new Intent("stop_listening");
        LocalBroadcastManager.getInstance(this).sendBroadcast(stop_listening);
    }

    private void startLocationService() {
        Intent intent = new Intent(this, LocationService.class);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }
    public void updateLocation(LatLng latLng) {
        if(this.googleMap != null) {
            if(this.marker == null) {
                MarkerOptions position = new MarkerOptions().position(latLng);
                marker = googleMap.addMarker(position);
            } else {
                marker.setPosition(latLng);
            }
            LatLngBounds bounds = LatLngBounds.builder().include(latLng).build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);
            googleMap.animateCamera(cameraUpdate);
        }
    }
}
