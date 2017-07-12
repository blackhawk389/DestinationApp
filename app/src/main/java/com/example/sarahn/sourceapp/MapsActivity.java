package com.example.sarahn.sourceapp;


import android.location.Location;
import android.os.Looper;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import android.os.Handler;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private TextView tvLat;
    private TextView tvLng;
    private DatabaseReference mDatabase;
    Map<String, Double> latlng = new HashMap<>();
    DatabaseReference data;
    private double lat;
    private double lng;

    CameraPosition CurrentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SingletonConnectionClass instence = SingletonConnectionClass.getInstance();
        data = instence.firebaseSetup();
        setContentView(R.layout.activity_maps);
        initView();
        setUpMapIfNeeded();
    }



    private void setLatLng(double lat, double lng){
        latlng.put("lat", lat);
        latlng.put("lng", lng);

        data.setValue(latlng, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.i("MainActivity ","onComplete " + databaseError);
            }
        });
    }

    private void initView(){
        tvLat = (TextView) findViewById(R.id.tv_lat);
        tvLng = (TextView) findViewById(R.id.tv_lng);
    }

    @Override
    protected void onResume() {
        super.onResume();
     //   setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        mapFragment.getMapAsync(this);

    }

    private void getCurrentLocation(){
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(final Location location) {

                        if (location != null) {

                            Log.i("mainactivity ", "current lat " + location.getLatitude());
                            Log.i("mainactivity ", "current lng " + location.getLongitude());

                            CurrentLocation = new CameraPosition.Builder().target(new LatLng(location.getLatitude(),
                                    location.getLongitude()))
                                    .zoom(15.5f)
                                    .bearing(0)
                                    .tilt(25)
                                    .build();

                            changeCameraPosition(CameraUpdateFactory.newCameraPosition(CurrentLocation)
                                    , new GoogleMap.CancelableCallback() {
                                @Override
                                public void onFinish() {
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),
                                            location.getLongitude())).title("Marker"));
                                }

                                @Override
                                public void onCancel() {
                                }
                            });

                        }
                    }
                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        getCurrentLocation();
        buildClient();
    }

    private void changeCameraPosition(CameraUpdate update, GoogleMap.CancelableCallback callback){
        mMap.animateCamera(update, 5, callback);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i("mainactivity ", "on connected");

        mFusedLocationClient.requestLocationUpdates(requestLocation(), new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (final Location location : locationResult.getLocations()) {


                    Log.i("mainactivity ", "on connected lat " + location.getLatitude());
                    Log.i("mainactivity ", "on connected lng " + location.getLongitude());

                    setLatLng(location.getLatitude(), location.getLongitude());

                    tvLat.setText(Double.toString(location.getLatitude()));
                    tvLng.setText(Double.toString(location.getLongitude()));

                    lat = location.getLatitude();
                    lng = location.getLongitude();

                    changeCameraPosition(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(), location.getLongitude()),
                            16), new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),
                                    location.getLongitude())).title("Marker"));

                        }

                        @Override
                        public void onCancel() {

                        }
                    });

                }
            }
        }, Looper.myLooper());
    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.i("mainactivity ", " on connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.i("mainactivity ", " on connection failed");
    }

    private void buildClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private LocationRequest requestLocation(){
        LocationRequest mLocationRequest = new LocationRequest();

        mLocationRequest.setSmallestDisplacement(3.81f);

//        mLocationRequest.setInterval(120000);
//        mLocationRequest.setFastestInterval(120000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putDouble("lat", lat);
        outState.putDouble("lng", lng);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        tvLat.setText(Double.toString(savedInstanceState.getDouble("lat")));
        tvLng.setText(Double.toString(savedInstanceState.getDouble("lng")));
    }
}
