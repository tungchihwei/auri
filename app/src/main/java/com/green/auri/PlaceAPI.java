package com.green.auri;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PlaceAPI extends Activity {

    protected GeoDataClient mGeoDataClient;
    protected PlaceDetectionClient mPlaceDetectionClient;

    private TextView txt_info;
    PlaceAutocompleteFragment autocompleteFragment;
    public ArrayList<Reviews> place_reviews = new ArrayList<Reviews>();

    public ListView list_review;
    public ListAdapter review_Adapter;

    String Place_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_api);

        txt_info = (TextView) findViewById(R.id.txt_info);
        list_review = (ListView) findViewById(R.id.list_review);
        review_Adapter = new ReviewAdapter(this.getBaseContext(), this);
        list_review.setAdapter(review_Adapter);

        mGeoDataClient = Places.getGeoDataClient(this);
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);
        autocompleteFragment = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        // Fragment for autocomplete
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // Get place id after selecting the place from the fragment
                Place_id = place.getId();

                // Get Geo Data by using place id
                mGeoDataClient.getPlaceById(Place_id).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                        if (task.isSuccessful()) {
                            // Clear the review after changing the place
                            place_reviews.clear();
//                            list_review.invalidateViews();
                            // Get the response of the place
                            PlaceBufferResponse places = task.getResult();

                            // Get Place information
                            Place myPlace = places.get(0);
                            String price_lev = "";
                            for(int i = 0; i<myPlace.getPriceLevel(); i++){
                                price_lev += "$";
                            }
                            txt_info.setText("Name: " + myPlace.getName() + "\nrating: "+ Float.toString(myPlace.getRating())
                                    + "\nPrice Level: " + price_lev + "\nAddress: " + myPlace.getAddress()
                                    + "\nPhone Number: " + myPlace.getPhoneNumber() + "\nWebsite: " + myPlace.getWebsiteUri());
                            // Http request for reviews
                            ReviewRequest(myPlace.getId());
                            // Update the list view
                            list_review.invalidateViews();
                            // Release the places
                            places.release();
                        } else {
                            txt_info.setText("Place not found.");
                            // Clear the review and update the list view
                            place_reviews.clear();
                            list_review.invalidateViews();
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
                Reviews add = new Reviews();
                add.rev_author = review.getString("author_name");
                add.rev_date = review.getString("relative_time_description");
                add.rev_review = review.getString("text");
                place_reviews.add(add);
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

// List view for reviews
class ReviewAdapter extends BaseAdapter {

    TextView txt_author;
    TextView txt_date;
    TextView txt_reviews;
    Context context;
    PlaceAPI main;

    public ReviewAdapter(Context aContext, PlaceAPI main){
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
        txt_author.setText("Author: " + this.main.place_reviews.get(position).rev_author);
        txt_date.setText("Posted Time: " + this.main.place_reviews.get(position).rev_date);
        txt_reviews.setText("Review: \n" + this.main.place_reviews.get(position).rev_review);

        return row;
    }
}