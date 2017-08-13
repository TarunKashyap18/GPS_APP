package com.example.tkashyap.gpsapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GeoTask.Geo {

    GoogleMap mMap;
    LatLng currentLocation;
    LatLng finalLocation;
    EditText et;
    TextView tv;
    Button btn;
    Marker marker1;
    Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //*****************************************************
        initialize();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                //String str_from=et.getText().toString();
                try {
                    geoLocate();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String currentp = currentLocation.latitude +","+currentLocation.longitude;
                String finalp = finalLocation.latitude+","+finalLocation.longitude;
                String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + currentp + "&destinations=" + finalp + "&mode=driving&language=fr-FR&avoid=tolls&key=AIzaSyBiNsMIc3nUVC5F0FY9Gnw7k3thIEcZ1xs";
                new GeoTask(MapsActivity.this).execute(url);
//                tv.setText(currentp+" "+finalp);
            }
        });

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(bestProvider);
        if (location != null) {
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(bestProvider, 20000, 0, this);


        //*****************************************************

    }

    public void setDouble(String result) {
        String res[]=result.split(",");
        Double min=Double.parseDouble(res[0])/60;
        int dist=Integer.parseInt(res[1])/1000;
        tv.setText("Duration= " + (int) (min / 60) + " hr " + (int) (min % 60) + " mins"+" Distance= " + dist + " kilometers");
    }
    public void initialize()
    {
        et= (EditText) findViewById(R.id.et);
        btn= (Button) findViewById(R.id.btn);
        tv= (TextView) findViewById(R.id.tv);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                Geocoder gc = new Geocoder(MapsActivity.this);
                LatLng ll = marker.getPosition();
                List<Address> list = null;
                try {
                    list = gc.getFromLocation(ll.latitude, ll.longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Address add = list.get(0);
                marker.setTitle(add.getLocality());
                marker.setSnippet(add.getLatitude() + "," + add.getLongitude());

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {

            }

        });
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
    }
    public void geoLocate() throws IOException {
        String loctaion = et.getText().toString();
        Geocoder gc = new Geocoder(this);
        List<Address> list = gc.getFromLocationName(loctaion, 1);
        Address address = list.get(0);
        String locality = address.getLocality();
        Toast.makeText(this, locality, Toast.LENGTH_LONG).show();
        double lat = address.getLatitude();
        double  lng = address.getLongitude();

        LatLng place = new LatLng(lat,lng);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(place));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place,15.0f));
        finalLocation = new LatLng(lat ,lng);
        setMarker(locality, lat, lng);
    }

    private void setMarker(String locality, double lat, double lng) {
        if(marker != null){
            marker.remove();
        }
        MarkerOptions options = new MarkerOptions()
                .title(locality)
                .draggable(true)
                .position(new LatLng( lat , lng) )
                .snippet( lat + ","+lng);
        marker =  mMap.addMarker(options);

    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        currentLocation = new LatLng(latitude, longitude);
        if (marker1 != null) {
            marker1.remove();
        }
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("Im here");
        marker1 = mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
//        if(marker !=null){
//            try {
//                geoLocate();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            String currentp = currentLocation.latitude +","+currentLocation.longitude;
//            String finalp = finalLocation.latitude+","+finalLocation.longitude;
//            String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + currentp + "&destinations=" + finalp + "&mode=driving&language=fr-FR&avoid=tolls&key=AIzaSyCCWTiezHW0d0oVpwIgtDiANfU1O-SOT-0";
//            new GeoTask(MapsActivity.this).execute(url);
//        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
