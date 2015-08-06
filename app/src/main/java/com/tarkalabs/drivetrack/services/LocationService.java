package com.tarkalabs.drivetrack.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_CODE = 1001;
    private boolean isResolving = false;
    private GoogleApiClient googleApiClient;
    public static Boolean IS_LISTENING=false;
    private MyLocationListener myLocationListener;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        int result = super.onStartCommand(intent, flags, startId);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        if(!isResolving) {
            googleApiClient.connect();
        }
        return result;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        isResolving=false;
        startListening();

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, myLocationListener);
                IS_LISTENING=false;
                stopSelf();
            }
        }, new IntentFilter("stop_listening"));

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                startListening();
                IS_LISTENING=false;
            }
        }, new IntentFilter("start_listening"));
    }


    private void startListening() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setFastestInterval(100)
                .setInterval(300)
                .setSmallestDisplacement(1f);
        if(myLocationListener == null) {
            myLocationListener = new MyLocationListener(this);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, myLocationListener);
        IS_LISTENING = true;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
    private class MyLocationListener implements LocationListener {

        private final Service context;

        public MyLocationListener(Service context) {
            this.context = context;
        }
        @Override
        public void onLocationChanged(Location location) {
            Intent intent = new Intent();
            intent.setAction("location");
            intent.putExtra("lat", location.getLatitude());
            intent.putExtra("lng", location.getLongitude());
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }
}
