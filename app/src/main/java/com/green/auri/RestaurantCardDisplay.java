package com.green.auri;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RestaurantCardDisplay extends Fragment {
    public static final String RESTAURANT_NAME_KEY = "name";
    public static final String RESTAURANT_ADDRESS_KEY = "address";
    public static final String RESTAURANT_IMAGE_KEY = "images";
    public static final String RESTAURANT_RATING_KEY = "rating";
    public static final float DEFAULT_RATING = 3;

    String restaurantName;
    String restaurantAddress;
    String[] restaurantImages;
    float restaurantRating;

    private TextView tv_restaurantName;
    private TextView tv_restaurantAddress;
    private RatingBar rb_restaurantRating;
    private ImageView img_restaurantLogo;

    public RestaurantCardDisplay() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.restaurant_card, container, false);

        tv_restaurantName = view.findViewById(R.id.tv_restaurant_name);
        tv_restaurantAddress = view.findViewById(R.id.tv_restaurant_address);
        rb_restaurantRating = view.findViewById(R.id.rb_restaurant_rating);

        // Extract the arguments from the Bundle passed in.
        Log.i("RESTAURANT_CARD", "CREATING FRAGMENT");

        Bundle bundle = getArguments();
        if (bundle == null) {
            Log.i("RESTAURANT_CARD", "NO BUNDLE");
            return view;
        }

        restaurantName = bundle.getString(RESTAURANT_NAME_KEY);
        restaurantAddress = bundle.getString(RESTAURANT_ADDRESS_KEY);
        restaurantImages = bundle.getStringArray(RESTAURANT_IMAGE_KEY);
        restaurantRating = bundle.getFloat(RESTAURANT_RATING_KEY, DEFAULT_RATING);

        Log.i("RESTAURANT_CARD", bundle.toString());

        tv_restaurantName.setText(restaurantName);
        tv_restaurantAddress.setText(restaurantAddress);
//        img_restaurantLogo.setImageDrawable();
        rb_restaurantRating.setRating(restaurantRating);

        Log.i("RESTAURANT_CARD", tv_restaurantName.getText() + " " + tv_restaurantAddress.getText());

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
