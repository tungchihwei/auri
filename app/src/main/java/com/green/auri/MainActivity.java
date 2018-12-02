package com.green.auri;


import android.Manifest;
import android.content.Context;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenu;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gigamole.infinitecycleviewpager.HorizontalInfiniteCycleViewPager;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;


import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import com.green.auri.arview.ARActivity;
import com.green.auri.utils.LocationListener;
import com.green.auri.utils.LocationUtils;
import com.green.auri.utils.PlaceSearchListener;
import com.green.auri.utils.PlaceSearchUtils;
import io.github.yavski.fabspeeddial.FabSpeedDial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, PlaceSearchListener, LocationListener {

    SupportMapFragment mapFragment;
    private Boolean mLocation = false;
    private static final String TAG = "MapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleMap mMap;
    double latitude;
    double longitude;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";


    private Button auri_mode;
    private SharedPreferences sp;
    String accountName;
    String email;

    protected GeoDataClient mGeoDataClient;
    String photo_toString;
    int isFav;


    private FragmentManager fm;
    private FragmentTransaction transaction;
    private boolean cardHidden = true;

    List<List<String>> lstResInfo = new ArrayList<>();

    FirebaseDatabase fav_database;
    DatabaseReference favRef;

    List<String> lstResName = new ArrayList<>();

    String marker_placeId;

    PlaceAutocompleteFragment autocompleteFragment;
    String placeSearch_id;
    int mode; // 1: nearby restaurant 2: search


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        if (mLocation) {

            // Check for permission in manifest
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            // Enable current Location on the google map and the map settings
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);

//            getDeviceLocation(); // get the Location of device
            LocationUtils.getCurrentLocation(MainActivity.this, this);

            // Reference to the button to find nearby restaurants
//            Button btnRestaurant = (Button) findViewById(R.id.restu);
//            btnRestaurant.setOnClickListener(new View.OnClickListener() {
//                String Restaurant = "restaurant";
//                @Override
//                public void onClick(View v) {
//                    Log.d("onClick", "Button is Clicked");
//
//                    mMap.clear();
//                    String url = PlaceSearchUtils.getUrl(latitude, longitude, Restaurant); // get the url of nearby restaurant
//                    Log.d("onClick", url);
//                    new GetNearbyPlacesTask().execute(url, (PlaceSearchListener) MainActivity.this);
//                    Toast.makeText(MainActivity.this,"Nearby Restaurants", Toast.LENGTH_LONG).show();
//                }
//            });

            mMap.setOnMarkerClickListener((GoogleMap.OnMarkerClickListener) this);


        }
    }


//    private void getDeviceLocation() { // get the device location
//        Log.d(TAG, "getDeviceLocation: getting the devices current location");
//        // use the location service
//        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
//
//        try{
//            if(mLocation){ // if allow to find device location
//                Task location = mFusedLocationProviderClient.getLastLocation();
//                location.addOnCompleteListener(new OnCompleteListener() { // @onLocationReturned and LocationListener
//                    // Perform the location listener
//                    @Override
//                    public void onComplete(@NonNull Task task) {
//                        if(task.isSuccessful()){
//                            Log.d(TAG, "onComplete: found location!");
//                            mLastLocation = (Location) task.getResult();
//                            // move map camera
//                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                                    new LatLng(mLastLocation.getLatitude(),
//                                            mLastLocation.getLongitude()), DEFAULT_ZOOM));
//
//                            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
//                            latitude = mLastLocation.getLatitude();
//                            longitude = mLastLocation.getLongitude();
//
//                            Toast.makeText(MainActivity.this, "Your Current Location", Toast.LENGTH_LONG).show();
//                            Log.d("onLocationChanged", String.format("latitude:%.3f longitude:%.3f",latitude,longitude));
//
//                        }else{ // Error of finding the current location
//                            Log.d(TAG, "onComplete: current location is null");
//                            Toast.makeText(MainActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//            }
//        }catch (SecurityException e){
//            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
//        }
//    }

//    private void moveCamera(LatLng latLng, float zoom){ //
//        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // hide status bar
        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);

        mGeoDataClient = Places.getGeoDataClient(this);

        sp = getSharedPreferences("login",MODE_PRIVATE);
        // ask device for location permission
        getLocationPermission();

        // get the account name
        accountName = sp.getString("account", "NA");
        email = sp.getString("email", "NA");
        Log.i("!!!email", email);

//        // Reference to Auri Mode Button
//        Button auri_mode = (Button) findViewById(R.id.AuriMode);
//        auri_mode.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d("Aurimode", "Button is Clicked");
//                Intent auri_intent = new Intent(MainActivity.this, IndexActivity.class);
//                startActivity(auri_intent);
//            }
//        });
//
//        // Reference to Normal Mode Button (PlaceAPI)
//        Button normal_mode = (Button) findViewById(R.id.NormalMode);
//        normal_mode.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.d("Normalmode", "Button is Clicked");
//                Intent normal_intent = new Intent(MainActivity.this, PlaceAPI.class);
//                startActivity(normal_intent);
//            }
//        });
//
//        // Reference to Camdir Mode Button (Camdir activity)
//        Button camdir = (Button) findViewById(R.id.camdir);
//        camdir.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.d("Camdir", "Button is Clicked");
//                Intent camdir_intent = new Intent(MainActivity.this, Camdir.class);
//                startActivity(camdir_intent);
//            }
//        });
//
//        // Reference to Camdir Mode Button (Camdir activity)
//        Button fav_list = (Button) findViewById(R.id.btn_fav);
//        fav_list.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.d("Favorite", "Button is Clicked");
//                Intent fav_intent = new Intent(MainActivity.this, FavoriteCheck.class);
//                startActivity(fav_intent);
//            }
//        });

//        txt_rname = (TextView) findViewById(R.id.txt_rname);
//        txt_raddress = (TextView) findViewById(R.id.txt_raddress);
//        rb = (RatingBar) findViewById(R.id.rb);
//
//        txt_rname.setText(rname);
//        txt_raddress.setText(raddress);
//        rb.setRating(rating);
//        fm = getSupportFragmentManager();
//        transaction = fm.beginTransaction();


        FabSpeedDial fab2 = (FabSpeedDial) findViewById(R.id.fab2);
        fab2.setMenuListener(new FabSpeedDial.MenuListener() {
            @Override
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                return true; // false: don't show menu
            }

            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                String title = menuItem.getTitle().toString();
                Log.i("!!!",title);
                if (title.equals("Auri Mode")){
                    goToAuriMode();
                } else if (title.equals("Settings")){
                    Toast.makeText(MainActivity.this, "settings", Toast.LENGTH_LONG).show();
                    Intent settings_intent = new Intent(MainActivity.this, SettingsActivity.class);
                    settings_intent.putExtra("email", email);
                    settings_intent.putExtra("accountName", accountName);
                    startActivity(settings_intent);
                } else if (title.equals("Nearby Restaurant")){
                    // Reference to the button to find nearby restaurants
                    String Restaurant = "restaurant";
                    Log.d("onClick", "Button is Clicked");
                    mMap.clear();
                    lstResInfo.clear();
                    mode = 1;
                    String url = PlaceSearchUtils.getUrl(latitude, longitude, Restaurant); // get the url of nearby restaurant
                    Log.d("onClick", url);
                    new GetNearbyPlacesTask().execute(url, (PlaceSearchListener) MainActivity.this);
                    Toast.makeText(MainActivity.this,"Nearby Restaurants", Toast.LENGTH_LONG).show();

                    // get nearby restaurant information and set cardview
//                    initData();
//                    Log.i("lst", String.valueOf(lstResName.isEmpty()));
//                    setCardCycle();
                } else if (title.equals("Favorites")) {
                    Log.d("Favorite", "Button is Clicked");
                    Intent fav_intent = new Intent(MainActivity.this, FavoriteCheck.class);
                    startActivity(fav_intent);
                }
                return true;
            }

            @Override
            public void onMenuClosed() {

            }
        });

        autocompleteFragment = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // Get place id after selecting the place from the fragment
                placeSearch_id = place.getId();

                // Get Geo Data by using place id
                mGeoDataClient.getPlaceById(placeSearch_id).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                        if (task.isSuccessful()) {
                            mode = 2;
                            mMap.clear();
                            lstResInfo.clear();
                            // Get the response of the place
                            PlaceBufferResponse places = task.getResult();
                            // Get Place information
                            Place myPlace = places.get(0);

                            List<String> cur = new ArrayList<>();
                            cur.add(myPlace.getName().toString());
                            cur.add(myPlace.getAddress().toString());
                            cur.add(Double.toString(myPlace.getRating()));
                            cur.add(myPlace.getId());
                            cur.add(accountName);

                            Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(myPlace.getId());
                            photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
                                @Override
                                public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                                    // Get the list of photos.
                                    PlacePhotoMetadataResponse photos = task.getResult();
                                    // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
                                    PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                                    // Get the first photo in the list.

                                    try {
                                        PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(0);
                                        // Get the attribution text.
                                        CharSequence attribution = photoMetadata.getAttributions();
                                        // Get a full-size bitmap for the photo.
                                        Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
                                        photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                                            @Override
                                            public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                                                PlacePhotoResponse photo = task.getResult();
                                                Bitmap res_Photo = photo.getBitmap();

                                                // change photo bitmap to string
                                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                                res_Photo.compress(Bitmap.CompressFormat.PNG, 100, baos);
                                                byte[] b = baos.toByteArray();
                                                photo_toString = Base64.encodeToString(b, Base64.DEFAULT);
                                                cur.add(photo_toString);
                                                lstResInfo.add(cur);
                                                setCardCycle();
//                                    curCard.setInfo(info, id, accountName, photo_toString, isFav);
                                            }
                                        });
                                    } catch (Exception e){
                                        // Set default photo and change photo bitmap to string
                                        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.na);
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        icon.compress(Bitmap.CompressFormat.PNG, 100, baos);
                                        byte[] b = baos.toByteArray();
                                        photo_toString = Base64.encodeToString(b, Base64.DEFAULT);
                                        cur.add(photo_toString);
                                        lstResInfo.add(cur);
                                        setCardCycle();
//                            curCard.setInfo(info, id, accountName, photo_toString, isFav);
                                    }
                                }
                            });

                            MarkerOptions markerOptions = new MarkerOptions();
//                            LatLng latLng = new LatLng(lat, lng);
                            markerOptions.position(myPlace.getLatLng());
                            markerOptions.title(myPlace.getName().toString() + " : " + myPlace.getAddress().toString() + " : " + Double.toString(myPlace.getRating()) + " :" + myPlace.getId());
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            mMap.addMarker(markerOptions);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace.getLatLng(), 15));
//                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));




//                            String url = PlaceSearchUtils.getUrl(myPlace.getLatLng().latitude, myPlace.getLatLng().longitude, myPlace.getName().toString()); // get the url of nearby restaurant
//                            Log.d("onClick", url);
//                            new GetNearbyPlacesTask().execute(url, (PlaceSearchListener) MainActivity.this);

                            places.release();
                        } else {

                        }
                    }
                });
            }
            @Override
            public void onError(Status status) {
                Log.i("Error", "An error of autocomplete occurred: " + status);
            }
        });

    }

//    private void initData() {
//        lstResName.add("Name1");
//        lstResName.add("Name2");
//        lstResName.add("Name3");
//        lstResName.add("Name4");
//    }

    public void setCardCycle(){

        Integer currCard = 0;
        try {
            for (int i = 0; i < lstResInfo.size(); i++){
                if (marker_placeId.equals(lstResInfo.get(i).get(3))){
                    currCard = i;
                }
            }
        } catch(Exception e) {
            Log.i("setCardCycle", "Mark not yet clicked");
        }
        HorizontalInfiniteCycleViewPager pager = (HorizontalInfiniteCycleViewPager) findViewById(R.id.horizontal_cycle);
//        CardAdapter adapter = new CardAdapter(lstResName,getBaseContext());
        CardAdapter adapter = new CardAdapter(lstResInfo,getBaseContext());
        pager.setAdapter(adapter);
        pager.setCurrentItem(currCard);
    }

    private void goToAuriMode(){
        Log.d("Aurimode", "Button is Clicked");
        Intent auri_intent = new Intent(MainActivity.this, ARActivity.class);
        startActivity(auri_intent);
    }

    private void getLocationPermission() {
        // ask for device permission to find location
        Log.d(TAG, "getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocation = true;
                initMap(); // initiate the map if permission allowed
            }else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }

        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    private void initMap() { // map initialization function
        Log.d(TAG, "initMap: initializing map");
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) { // menu inflator for the sign out textview
        // Inflate the Log Out menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // selecting Log Out from the menu
        // If click logout, go to logout class
//        int id = item.getItemId();
//        if (id == R.id.action_nearbyRest) {
//            logout(); // if clicked, signs the user out
//            return true;
//        }else if (id == R.id.action_auri){
//            goToAuriMode();
//            return true;
//        }else if (id == R.id.action_settings){
//            Log.d("Settings", "Button is Clicked");
//            // to do
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

     private void logout(){
        // sign out of this user and go to the log in page
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        sp.edit().putString("account", "").apply();
        sp.edit().putBoolean("logged",false).apply();
        finish();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // request permission result function
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocation = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocation = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocation = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putDouble(KEY_LATITUDE, latitude);
            outState.putDouble(KEY_LONGITUDE, longitude);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        String title = marker.getTitle();
        LatLng ll = marker.getPosition();
        String lat = String.valueOf(ll.latitude);
        String lng = String.valueOf(ll.longitude);


//        // Set default photo
//        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.na);
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        icon.compress(Bitmap.CompressFormat.PNG, 100, baos);
//        byte[] b = baos.toByteArray();
//        photo_toString = Base64.encodeToString(b, Base64.DEFAULT);

        // info[0] is restaurant name; info[1] is address; info[2] is rating; info[3] is Place_id
        String[] info = title.split(":");
        String id = info[3].replace(" ", "");
        marker_placeId = id;

//        Card curCard = new Card();
//
//        fm = getSupportFragmentManager();
//        transaction = fm.beginTransaction();
//
//        // Get Place photo
//        Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(id);
//        photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
//            @Override
//            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
//                // Get the list of photos.
//                PlacePhotoMetadataResponse photos = task.getResult();
//                // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
//                PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
//                // Get the first photo in the list.
//
//                try {
//                    PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(0);
//                    // Get the attribution text.
//                    CharSequence attribution = photoMetadata.getAttributions();
//                    // Get a full-size bitmap for the photo.
//                    Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
//                    photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
//                        @Override
//                        public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
//                            PlacePhotoResponse photo = task.getResult();
//                            Bitmap res_Photo = photo.getBitmap();
//
//                            // change photo bitmap to string
//                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                            res_Photo.compress(Bitmap.CompressFormat.PNG, 100, baos);
//                            byte[] b = baos.toByteArray();
//                            photo_toString = Base64.encodeToString(b, Base64.DEFAULT);
//                            curCard.setInfo(info, id, accountName, photo_toString, isFav);
//                        }
//                    });
//                } catch (Exception e){
//                    // Set default photo and change photo bitmap to string
//                    Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.na);
//                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                    icon.compress(Bitmap.CompressFormat.PNG, 100, baos);
//                    byte[] b = baos.toByteArray();
//                    photo_toString = Base64.encodeToString(b, Base64.DEFAULT);
//                    curCard.setInfo(info, id, accountName, photo_toString, isFav);
//                }
//            }
//        });
//
//        curCard.setInfo(info, id, accountName, photo_toString, isFav);
//
//        if (cardHidden) {
//            cardHidden = false;
//            TextView tv = (TextView) findViewById(R.id.defaultTxt);
//            tv.setText("");
//        }
//
//        transaction.replace(R.id.fragment, curCard);
//        transaction.commit();

//        for (int i = 0; i < lstResInfo.size(); i++){
////            Log.i("place_id", lstResInfo.get(i).toString());
//            if (marker_placeId.equals(lstResInfo.get(i).get(3))){
//                Log.i("place_id", Integer.toString(i));
//                Log.i("place_id", marker_placeId);
//                Log.i("place_id", lstResInfo.get(i).get(3));
//            }
//        }
//        Log.i("place_id", lstResInfo.get(i).toString());

//        setCardCycle();

        setCardCycle();

        return false;
    }

    @Override
    public void onPlaceSearchComplete(List<HashMap<String, String>> nearbyPlacesList) {
        // Iterate through the nearby places that were returned
        // and place a marker for each.
        for (int i = 0; i < nearbyPlacesList.size(); i++) {
            Log.d("onPostExecute","Entered into showing locations");

            try {
                MarkerOptions markerOptions = new MarkerOptions();
                HashMap<String, String> googlePlace = nearbyPlacesList.get(i);
                double lat = Double.parseDouble(googlePlace.get("lat"));
                double lng = Double.parseDouble(googlePlace.get("lng"));
                String placeName = googlePlace.get("place_name");
                Log.i("placesNames", placeName);
//                lstResName.add(placeName);
                String vicinity = googlePlace.get("vicinity");
                String rating = googlePlace.get("rating");
                String Place_id = googlePlace.get("place_id");

                // store info in lstResInfo
                List<String> cur = new ArrayList<>();
                cur.add(placeName);
                cur.add(vicinity);
                cur.add(rating);
                cur.add(Place_id);
                cur.add(accountName);


                Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(Place_id);
                photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                        // Get the list of photos.
                        PlacePhotoMetadataResponse photos = task.getResult();
                        // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
                        PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                        // Get the first photo in the list.

                        try {
                            PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(0);
                            // Get the attribution text.
                            CharSequence attribution = photoMetadata.getAttributions();
                            // Get a full-size bitmap for the photo.
                            Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
                            photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                                @Override
                                public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                                    PlacePhotoResponse photo = task.getResult();
                                    Bitmap res_Photo = photo.getBitmap();

                                    // change photo bitmap to string
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    res_Photo.compress(Bitmap.CompressFormat.PNG, 100, baos);
                                    byte[] b = baos.toByteArray();
                                    photo_toString = Base64.encodeToString(b, Base64.DEFAULT);
                                    cur.add(photo_toString);
                                    lstResInfo.add(cur);
                                    setCardCycle();
//                                    curCard.setInfo(info, id, accountName, photo_toString, isFav);
                                }
                            });
                        } catch (Exception e){
                            // Set default photo and change photo bitmap to string
                            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.na);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            icon.compress(Bitmap.CompressFormat.PNG, 100, baos);
                            byte[] b = baos.toByteArray();
                            photo_toString = Base64.encodeToString(b, Base64.DEFAULT);
                            cur.add(photo_toString);
                            lstResInfo.add(cur);
                            setCardCycle();
//                            curCard.setInfo(info, id, accountName, photo_toString, isFav);
                        }
                    }
                });

                // Set up the marker:
                LatLng latLng = new LatLng(lat, lng);
                markerOptions.position(latLng);
                markerOptions.title(placeName + " : " + vicinity + " : " + rating + " :" + Place_id);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                // Add the marker and move the camera to make it visible.
                mMap.addMarker(markerOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            } catch (NullPointerException e) {
                continue;
            }
        }
//        setCardCycle();
        Log.i("lst", String.valueOf(lstResInfo.isEmpty()));
//        Log.i("lst", String.valueOf(lstResName.isEmpty()));
//        for (int j = 0; j < lstResName.size(); j++) {
////            String tmp = lstResName.get(j);
////            Log.i("lstREE", tmp);
////        }

    }

    @Override
    public void onLocationUpdated(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(latitude,
                        longitude), DEFAULT_ZOOM));
    }
}
