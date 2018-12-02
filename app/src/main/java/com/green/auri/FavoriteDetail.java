package com.green.auri;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;

public class FavoriteDetail extends AppCompatActivity implements OnMapReadyCallback {

    String Place_id;
    Double Lat, Lng;
    TextView txt_resName;
    TextView txt_resRating;
    TextView txt_resPrice;
//    TextView txt_resPhone;
//    TextView txt_resWeb;
    ImageButton btn_call;
    ImageButton btn_web;
    ToggleButton btn_detailFav;
    RatingBar resRating;

    String phone_number;
    Uri web;

    String accountName;
    private SharedPreferences sp;
    FirebaseDatabase fav_database;
    DatabaseReference favRef;
    DatabaseReference NameRef;
    DatabaseReference PhotoRef;
    Integer isFav;
    String resName;
    String resPhoto;
    protected GeoDataClient mGeoDataClient;


    public ArrayList<reviews_detail> place_reviews = new ArrayList<reviews_detail>();
    public ListView list_review;
    public ListAdapter review_Adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favoritedetail);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map_resDetail);
        mapFragment.getMapAsync(this);

        Lat = 0.;
        Lng = 0.;
        Place_id = getIntent().getStringExtra("place_id");

        txt_resName = (TextView) findViewById(R.id.txt_resName);
        resRating = (RatingBar) findViewById(R.id.resRating);
        txt_resRating = (TextView) findViewById(R.id.txt_resRating);
        txt_resPrice = (TextView) findViewById(R.id.txt_resPrice);
//        txt_resPhone = (TextView) findViewById(R.id.txt_resPhone);
//        txt_resWeb = (TextView) findViewById(R.id.txt_resWeb);

        list_review = (ListView) findViewById(R.id.list_resReview);
        review_Adapter = new ResReviewAdapter(this.getBaseContext(), this);
        list_review.setAdapter(review_Adapter);

        btn_call = (ImageButton) findViewById(R.id.btn_call);
        btn_web = (ImageButton) findViewById(R.id.btn_web);
        btn_detailFav = (ToggleButton) findViewById(R.id.btn_detailAddFav);

        btn_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (phone_number == "") {
                    Toast.makeText(FavoriteDetail.this, "No phone number available", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FavoriteDetail.this, "Phone number available", Toast.LENGTH_SHORT).show();
                    Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
                    phone_number = phone_number.replace("-", "").replace(" ", "");
                    phoneIntent.setData(Uri.parse("tel:" + phone_number));
                    startActivity(phoneIntent);
                }
            }
        });

        btn_web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (web == null){
                    Toast.makeText(FavoriteDetail.this, "No website available", Toast.LENGTH_SHORT).show();
                } else {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, web);
                    startActivity(browserIntent);
                }
            }
        });

        sp = getSharedPreferences("login",MODE_PRIVATE);
        accountName = sp.getString("account", "NA");


        btn_detailFav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    Log.i("isFav", "onCheckedChange to on");
//                    checked = 1;
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
                } else{
                    Log.i("isFav", "onCheckedChange to off");
                    // Delete restaurant from database
//                    checked = 1;
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

                            if (accountName != null) {
                                fav_database = FirebaseDatabase.getInstance();
                                favRef = fav_database.getReference();
                                favRef.getDatabase();
                                isFav = 0;
                                favRef.child(accountName).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                                            String key=childSnapshot.getKey();
                                            if (key.equals(Place_id)){
                                                isFav = 1;
                                                btn_detailFav.setChecked(true);
//                            btn_fav.setBackgroundResource(R.drawable.fav_on);
                                                break;
                                            }
                                        }
                                        if (isFav == 0){
                                            btn_detailFav.setChecked(false);
//                        btn_fav.setBackgroundResource(R.drawable.fav_off);
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                    });
                } catch (Exception e){
                    // Set default photo and change photo bitmap to string
                    Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.na);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    icon.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] b = baos.toByteArray();
                    resPhoto = Base64.encodeToString(b, Base64.DEFAULT);

                    if (accountName != null) {
                        fav_database = FirebaseDatabase.getInstance();
                        favRef = fav_database.getReference();
                        favRef.getDatabase();
                        isFav = 0;
                        favRef.child(accountName).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                                    String key=childSnapshot.getKey();
                                    if (key.equals(Place_id)){
                                        isFav = 1;
                                        btn_detailFav.setChecked(true);
//                            btn_fav.setBackgroundResource(R.drawable.fav_on);
                                        break;
                                    }
                                }
                                if (isFav == 0){
                                    btn_detailFav.setChecked(false);
//                        btn_fav.setBackgroundResource(R.drawable.fav_off);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }
        });

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
                    Place myPlace = places.get(0);

                    Lat = myPlace.getLatLng().latitude;
                    Lng = myPlace.getLatLng().longitude;

                    map.addMarker(new MarkerOptions()
                            .position(new LatLng(Lat, Lng))
                            .title(myPlace.getName().toString()));

                    map.setMinZoomPreference(6.0f);
                    map.setMaxZoomPreference(20.0f);

//                    LatLngBounds res = new LatLngBounds(
//                            new LatLng(Lat+0.1, Lng+0.1), new LatLng(Lat, Lng));

                    LatLng res_loc = new LatLng(Lat, Lng);

                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(res_loc, 15));

                    resName = myPlace.getName().toString();

                    txt_resName.setText(myPlace.getName().toString());
                    txt_resRating.setText(Float.toString(myPlace.getRating()));
                    resRating.setRating(myPlace.getRating());

                    String price_lev = "";
                    for(int i = 0; i<myPlace.getPriceLevel(); i++){
                        price_lev += "$";
                    }
                    txt_resPrice.setText(price_lev);
                    try{
                        phone_number = myPlace.getPhoneNumber().toString();
//                        phone_number = phone_number.split(" ")[1].replace("-", "");
//                        Log.i("call_number", phone_number.split(" ")[1].replace("-", ""));
//                        txt_resPhone.setText(myPlace.getPhoneNumber().toString());
                    } catch (Exception e){
                        phone_number = "";
//                        txt_resPhone.setText("No phone number available!");
                    }

                    try{
                        web = myPlace.getWebsiteUri();
//                        txt_resWeb.setText(myPlace.getWebsiteUri().toString());
                    } catch (Exception e){
                        web = null;
//                        txt_resWeb.setText("No website available!");
                    }



                    ReviewRequest(Place_id);

                    list_review.invalidateViews();

                    places.release();
                } else {

                }
            }
        });

    }

    private void ReviewRequest(String myPlaceId){
        try{
            MyHttp myHttp = new MyHttp();
            String rev = myHttp.httpGet(myPlaceId);

            // Format json file for getting the data that we need for the reviews
            JSONObject obj = new JSONObject(rev);
            JSONObject auth = new JSONObject(obj.getString("result"));
            // 5 reviews for each time of the request
            for(int i = 0; i < 5; i ++){
                JSONObject review = new JSONObject(auth.getJSONArray("reviews").getString(i));
                // Update the reviews
                reviews_detail add = new reviews_detail();
                add.rev_author = review.getString("author_name");
                add.rev_date = review.getString("relative_time_description");
                add.rev_review = review.getString("text");
//                Log.i("reviews", add.rev_author);
//                Log.i("reviews", add.rev_date);
//                Log.i("reviews", add.rev_review);
                place_reviews.add(add);
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

}

class reviews_detail {
    String rev_author;
    String rev_date;
    String rev_review;
}

// List view for reviews
class ResReviewAdapter extends BaseAdapter {

    TextView txt_author;
    TextView txt_date;
    TextView txt_reviews;
    Context context;
    FavoriteDetail main;

    public ResReviewAdapter(Context aContext, FavoriteDetail main){
        this.main = main;
        context = aContext;

    }
    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public int getCount() {
        return main.place_reviews.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row;

        // Indicates the first time creating this row.
        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.reviewlist, parent, false);
        } else {
            row = convertView;
        }

        txt_author = (TextView) row.findViewById(R.id.txt_author);
        txt_date = (TextView) row.findViewById(R.id.txt_date);
        txt_reviews = (TextView) row.findViewById(R.id.txt_reviews);

        // Set reviews
        Log.i("reviews adapt", "Author: " + this.main.place_reviews.get(position).rev_author);
        txt_author.setText(this.main.place_reviews.get(position).rev_author);
        txt_date.setText(this.main.place_reviews.get(position).rev_date);
        txt_reviews.setText(this.main.place_reviews.get(position).rev_review);

        return row;
    }
}


