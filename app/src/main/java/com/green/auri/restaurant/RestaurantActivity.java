package com.green.auri.restaurant;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.green.auri.MainActivity;
import com.green.auri.R;
import com.green.auri.utils.place.PlaceSearchUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * The activity for displaying the indepth info of the restaurant.
 * Includes the reviews, phone number, and website.
 */
public class RestaurantActivity extends AppCompatActivity implements OnMapReadyCallback {
    /* Static Constants */
    private static final int MAX_REVIEW_COUNT = 5;

    /* Instance Constants */
    private String accountName;
    private String phone_number;
    private String resName;
    private String resPhoto;
    private String Place_id;
    private Double Lat, Lng;
    private Integer isFav;
    private Uri web;

    public ArrayList<Review> place_reviews = new ArrayList<Review>();
    public ListView list_review;
    public ListAdapter review_Adapter;

    /* Components */
    private TextView txt_resName;
    private TextView txt_resRating;
    private TextView txt_resPrice;
    private ImageButton btn_call;
    private ImageButton btn_web;
    private ToggleButton btn_detailFav;
    private RatingBar resRating;

    /* Firebase */
    private FirebaseDatabase fav_database;
    private DatabaseReference favRef;
    private DatabaseReference NameRef;
    private DatabaseReference PhotoRef;
    protected GeoDataClient mGeoDataClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // hide status bar
        getSupportActionBar().hide();

        setContentView(R.layout.activity_favoritedetail);

        // Set the map
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map_resDetail);
        mapFragment.getMapAsync(this);

        accountName = MainActivity.accountName;

        Lat = 0.;
        Lng = 0.;

        // Get Place id
        Place_id = getIntent().getStringExtra("place_id");

        txt_resName = findViewById(R.id.txt_resName);
        resRating = findViewById(R.id.resRating);
        txt_resRating = findViewById(R.id.txt_resRating);
        txt_resPrice = findViewById(R.id.txt_resPrice);

        // Set adapter for restaurant reviews
        list_review = findViewById(R.id.list_resReview);
        review_Adapter = new ReviewAdapter(this.getBaseContext(), this);
        list_review.setAdapter(review_Adapter);

        btn_call = findViewById(R.id.btn_call);
        btn_web = findViewById(R.id.btn_web);
        btn_detailFav = findViewById(R.id.btn_detailAddFav);

        // Call the restaurant
        btn_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (phone_number == "") {
                    Toast.makeText(RestaurantActivity.this, "No phone number available", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RestaurantActivity.this, "Phone number available", Toast.LENGTH_SHORT).show();
                    Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
                    phone_number = phone_number.replace("-", "").replace(" ", "");
                    phoneIntent.setData(Uri.parse("tel:" + phone_number));
                    startActivity(phoneIntent);
                }
            }
        });

        // Link too restaurant website
        btn_web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (web == null) {
                    Toast.makeText(RestaurantActivity.this, "No website available", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RestaurantActivity.this, "Website available", Toast.LENGTH_SHORT).show();
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, web);
                    startActivity(browserIntent);
                }
            }
        });

        // Showing/deleting/adding restaurant from/to favorites(database)
        btn_detailFav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    // Save restaurant to database
                    fav_database = FirebaseDatabase.getInstance();
                    favRef = fav_database.getReference(accountName);
                    favRef = favRef.child(Place_id);
                    favRef = fav_database.getReference(accountName + "/" + Place_id);
                    NameRef = favRef.child("Name");
                    NameRef.setValue(resName);

                    favRef = FirebaseDatabase.getInstance().getReference(accountName + "/" + Place_id);
                    PhotoRef = favRef.child("Photo");
                    PhotoRef.setValue(resPhoto);

                    btn_detailFav.setBackgroundResource(R.drawable.fav_on);
                } else {
                    // Delete restaurant from database
                    fav_database = FirebaseDatabase.getInstance();
                    fav_database.getReference(accountName).child(Place_id).removeValue();

                    btn_detailFav.setBackgroundResource(R.drawable.fav_off);
                }
            }
        });

        mGeoDataClient = Places.getGeoDataClient(this);
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
                            resPhoto = Base64.encodeToString(b, Base64.DEFAULT);

                            // Check if restaurant is user's favorite
                            isFavorite();
                        }
                    });
                } catch (Exception e) {
                    // Set default photo and change photo bitmap to string
                    Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.na);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    icon.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] b = baos.toByteArray();
                    resPhoto = Base64.encodeToString(b, Base64.DEFAULT);

                    // Check if restaurant is user's favorite
                    isFavorite();
                }
            }
        });
    }

    // Check if restaurant is user's favorite
    public void isFavorite() {
        if (accountName != null) {
            fav_database = FirebaseDatabase.getInstance();
            favRef = fav_database.getReference();
            favRef.getDatabase();
            isFav = 0;
            favRef.child(accountName).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String key = childSnapshot.getKey();
                        if (key.equals(Place_id)) {
                            isFav = 1;
                            btn_detailFav.setChecked(true);
                            break;
                        }
                    }
                    if (isFav == 0) {
                        btn_detailFav.setChecked(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {

        GeoDataClient mGeoDataClient = Places.getGeoDataClient(this);
        // Get Geo Data by using place id
        mGeoDataClient.getPlaceById(Place_id).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                if (task.isSuccessful()) {
                    PlaceBufferResponse places = task.getResult();
                    // .get(0) gets this restaurant's details
                    Place myPlace = places.get(0);

                    // set map
                    Lat = myPlace.getLatLng().latitude;
                    Lng = myPlace.getLatLng().longitude;

                    // Set marker on the map by Lat/Lng
                    map.addMarker(new MarkerOptions()
                            .position(new LatLng(Lat, Lng))
                            .title(myPlace.getName().toString()));
                    map.setMinZoomPreference(6.0f);
                    map.setMaxZoomPreference(20.0f);

                    // Set map camera
                    LatLng res_loc = new LatLng(Lat, Lng);
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(res_loc, 15));

                    //Get restaurant detail and set the detail text
                    resName = myPlace.getName().toString();
                    txt_resName.setText(myPlace.getName().toString());
                    txt_resRating.setText(Float.toString(myPlace.getRating()));
                    resRating.setRating(myPlace.getRating());
                    String price_lev = "";
                    for (int i = 0; i < myPlace.getPriceLevel(); i++) {
                        price_lev += "$";
                    }
                    txt_resPrice.setText(price_lev);
                    try {
                        phone_number = myPlace.getPhoneNumber().toString();
                    } catch (Exception e) {
                        phone_number = "";
                    }
                    try {
                        web = myPlace.getWebsiteUri();
                    } catch (Exception e) {
                        web = null;
                    }

                    // get all the reviews
                    ReviewRequest(Place_id);
                    list_review.invalidateViews();
                    // Release the place after finish getting all detail
                    places.release();
                } else {
                    Toast.makeText(RestaurantActivity.this, "Place API error", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void ReviewRequest(String myPlaceId) {
        try {

            String placeApiKey = getResources().getString(R.string.place_api_key);
            URL url = new URL("https://maps.googleapis.com/maps/api/place/details/json?placeid=" + myPlaceId + "&fields=reviews&key=" + placeApiKey);

            String reviewString = PlaceSearchUtils.getSearchResults(url);

            // Format json file for getting the data that we need for the reviews
            JSONArray reviews = new JSONObject(reviewString).getJSONObject("result").getJSONArray("reviews");

            // 5 reviews for each time of the request
            for (int i = 0; i < Math.min(MAX_REVIEW_COUNT, reviews.length()); i++) {
                JSONObject json = new JSONObject(reviews.getString(i));
                Review review = new Review(json.getString("author_name"), json.getString("relative_time_description"), json.getString("text"));
                place_reviews.add(review);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

}


