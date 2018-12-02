package com.green.auri;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FavoriteDetail extends AppCompatActivity implements OnMapReadyCallback {

    String Place_id;
    Double Lat, Lng;
    TextView txt_resName;
    TextView txt_resRating;
    TextView txt_resPrice;
    TextView txt_resPhone;
    TextView txt_resWeb;

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
        txt_resRating = (TextView) findViewById(R.id.txt_resRating);
        txt_resPrice = (TextView) findViewById(R.id.txt_resPrice);
        txt_resPhone = (TextView) findViewById(R.id.txt_resPhone);
        txt_resWeb = (TextView) findViewById(R.id.txt_resWeb);

        list_review = (ListView) findViewById(R.id.list_resReview);
        review_Adapter = new ResReviewAdapter(this.getBaseContext(), this);
        list_review.setAdapter(review_Adapter);


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

                    txt_resName.setText(myPlace.getName().toString());
                    txt_resRating.setText(Float.toString(myPlace.getRating()));

                    String price_lev = "";
                    for(int i = 0; i<myPlace.getPriceLevel(); i++){
                        price_lev += "$";
                    }
                    txt_resPrice.setText(price_lev);
                    try{
                        txt_resPhone.setText(myPlace.getPhoneNumber().toString());
                    } catch (Exception e){
                        txt_resPhone.setText("No phone number available!");
                    }

                    try{
                        txt_resWeb.setText(myPlace.getWebsiteUri().toString());
                    } catch (Exception e){
                        txt_resWeb.setText("No website available!");
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


