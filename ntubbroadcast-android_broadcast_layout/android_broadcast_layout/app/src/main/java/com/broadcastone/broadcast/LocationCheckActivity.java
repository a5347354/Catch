package com.broadcastone.broadcast;

/**
 * Created by ChuLay on 2015/11/28.
 */
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.Image;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.broadcastone.broadcast.android_broadcast.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import chatLayout.ChatActivity;

public class LocationCheckActivity extends ActionBarActivity implements OnMapReadyCallback {
    private final static int REQUEST_CODE_RESOLUTION = 1;
    private GoogleApiClient googleApiClient;
    private GoogleMap map;
    private String myPosition;
    private double latitude;
    private double longitude;
    private ImageButton btPositon;
    private ImageButton btSentPositon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);




        initMap();
    }
    private void initMap() {    //判斷地圖是否被開啟
        if (map == null) {
            map = ((SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fmMap)).getMap();
            onMapReady(map);
            if (map != null) {  //若已開啟、匯入Map內建的屬性
                setUpMap();
            }
        }
    }
    private void setUpMap() {
        map.setMyLocationEnabled(true); //新增定位"我的位置"按鈕
    }
    @Override
    public void onMapReady(GoogleMap map) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(23.582594, 120.585528), 7));

        //自共同記憶區取得傳遞資料，變數名稱:address
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Toast.makeText(LocationCheckActivity.this, extras.getString("address"), Toast.LENGTH_SHORT).show();
            locationNameToMarker(extras.getString("address"));
        }
    }

    private com.google.android.gms.location.LocationListener locationListener =
            new com.google.android.gms.location.LocationListener() {    //建立Location監聽器
                @Override
                public void onLocationChanged(Location location) {  //當GPS位置改變時run此函數
//                    updateLastLocationInfo(location);
                }
            };
//    private void updateLastLocationInfo(Location lastLocation){
//        if (lastLocation == null) {
//            Toast.makeText(this, "LastLocationNotAvailable", Toast.LENGTH_SHORT).show();
//            return;
//        }else {                                             //若有抓取到GPS位置
//            latitude = lastLocation.getLatitude();
//            longitude = lastLocation.getLongitude();
//            LatLng position = new LatLng(latitude, longitude);
//            CameraPosition cameraPosition = new CameraPosition.Builder()
//                    .target(position).zoom(15).build();
//            map.animateCamera(CameraUpdateFactory          //將畫面停在我的位置
//                    .newCameraPosition(cameraPosition));
//            map.clear();                                  //清空原本的mark
//            btPositon.setVisibility(ImageButton.VISIBLE);
//            btSentPositon.setVisibility(ImageButton.VISIBLE);
//            map.addMarker(new MarkerOptions()             //標記(Mark)我的位置
//                    .position(new LatLng(latitude, longitude))
//                    .title("Your position"));
//
//
//
//        }
//    }

    private GoogleApiClient.ConnectionCallbacks connectionCallbacks =
            new GoogleApiClient.ConnectionCallbacks() {     //建立google api服務連結監聽器
                @Override
                public void onConnected(Bundle bundle) {    //連到google api的服務run此函數
                    LocationRequest locationRequest = LocationRequest.create()
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            .setInterval(10000)
                            .setSmallestDisplacement(1000);
                    LocationServices.FusedLocationApi.requestLocationUpdates(
                            googleApiClient, locationRequest, locationListener);
                }
                @Override
                public void onConnectionSuspended(int i) {  //連線暫停時run此函數
                    Toast.makeText(LocationCheckActivity.this, "GoogleApiClientConnectionSuspended", Toast.LENGTH_SHORT).show();
                }
            };

    public void onLocationNameClick(View view) {
        EditText etLocationName = (EditText) findViewById(R.id.etLocationName);
        String locationName = etLocationName.getText().toString().trim();
        if (locationName.length() > 0) {
            locationNameToMarker(locationName);
        } else {
            Toast.makeText(LocationCheckActivity.this, "LocationNameIsEmpty", Toast.LENGTH_SHORT).show();
        }
    }

    private void locationNameToMarker(String locationName) {
        map.clear();
        Geocoder geocoder = new Geocoder(LocationCheckActivity.this);
        List<Address> addressList = null;
        int maxResults = 1;
        try {
            addressList = geocoder
                    .getFromLocationName(locationName, maxResults);
        }
        catch (IOException e) {
            Log.e("IOException", e.toString());
        }

        if (addressList == null || addressList.isEmpty()) {
            Toast.makeText(LocationCheckActivity.this, "Not found, please try again", Toast.LENGTH_SHORT).show();
        } else {
            Address address = addressList.get(0);

            LatLng position = new LatLng(address.getLatitude(),
                    address.getLongitude());

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(position).zoom(15).build();
            map.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
        }
    }



    @Override
    protected  void onStart(){
        super.onStart();

    }
    @Override
    protected void onPause() {
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
        super.onPause();
        finish();
    }
    @Override
    protected void onResume() {
        super.onResume();
        initMap();
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(connectionCallbacks)
                    .build();
        }
        googleApiClient.connect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_RESOLUTION) {
                googleApiClient.connect();
            }
        }
    }
}
