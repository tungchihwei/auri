package com.green.auri;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.green.auri.favorites.FavoriteDetail;
import com.green.auri.arview.ARActivity;

import java.util.HashMap;
import java.util.List;

public class RestaurantCardAdapter extends PagerAdapter {
    private static int NORMAL_CARD = R.layout.restaurant_card;
    private static int AR_CARD = R.layout.ar_restaurant_card;

    private Context context;
    private LayoutInflater layoutInflater;
    private List<RestaurantResult> restaurantList;
    private int restaurantCardId = NORMAL_CARD;


    public RestaurantCardAdapter(List<RestaurantResult> restaurantList, Context context) {
        this.restaurantList = restaurantList;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    public RestaurantCardAdapter(List<RestaurantResult> restaurantList, Context context, boolean arEnabled) {
        this(restaurantList, context);
        if (arEnabled) {
            restaurantCardId = AR_CARD;
        }
    }

    @Override
    public int getCount() {
        Log.i("CARDS", String.valueOf(restaurantList == null));
        return restaurantList.size();
    }

    @Override
    public int getItemPosition(final Object object) {
        return POSITION_NONE;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view.equals(o);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = layoutInflater.inflate(restaurantCardId, container, false);

        RestaurantResult restaurantInfo = restaurantList.get(position);
        TextView txt_restaurantName = view.findViewById(R.id.txt_restaurant_name);
        txt_restaurantName.setText(String.format("%d. %s", position + 1, restaurantInfo.getRestaurantName()));

        TextView txt_restaurantAddress = view.findViewById(R.id.txt_restaurant_address);
        txt_restaurantAddress.setText(restaurantInfo.getRestaurantAddress());

        RatingBar rb_restaurantRating = view.findViewById(R.id.rb_restaurant_rating);
        rb_restaurantRating.setRating((float) restaurantInfo.getRestaurantRating());

        TextView txt_restaurantRating = view.findViewById(R.id.txt_restaurant_rating);
        txt_restaurantRating.setText(String.valueOf(restaurantInfo.getRestaurantRating()));

        String accountName = MainActivity.accountName;
        String placeId = restaurantInfo.getRestaurantId();
        String restaurantPhoto = restaurantInfo.getRestaurantPhoto();

        if (restaurantCardId == AR_CARD) {
            ImageView restaurantImgView = view.findViewById(R.id.restaurant_image);
            byte[] encodeByte = Base64.decode(restaurantPhoto, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            restaurantImgView.setImageBitmap(bitmap);
        }

        // Set the card to be clickable
        LinearLayout layout_cardInfo = view.findViewById(R.id.ll_card_display);
        layout_cardInfo.setOnClickListener(v -> {

            if (restaurantInfo.getRestaurantId() == null || restaurantInfo.getRestaurantId() == "") {
                return;
            }

            Intent fav_detail = new Intent(context, FavoriteDetail.class);
            fav_detail.putExtra("place_id", placeId);
            fav_detail.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(fav_detail);
        });

        if (accountName == null) {
            container.addView(view);
            return view;
        }

        Log.i("CARDS", "created " + restaurantInfo.getRestaurantName());

        ToggleButton btn_favorite = view.findViewById(R.id.btn_favorite);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference().child(accountName);
        database.goOnline();

        database.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    if (childSnapshot.getKey().equals(placeId)){
                        btn_favorite.setChecked(true);
                        return;
                    }
                }

                btn_favorite.setChecked(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        btn_favorite.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked){
                // Save restaurant to database
                new Thread(() -> {
                    DatabaseReference favoriteReference = FirebaseDatabase.getInstance()
                            .getReference(MainActivity.accountName)
                            .child(placeId);

                    favoriteReference.child("Name").setValue(restaurantList.get(position).getRestaurantName());
                    favoriteReference.child("Photo").setValue(restaurantPhoto);
                    Log.i("DATABASE", "added " + placeId );
                }).start();

                btn_favorite.setBackgroundResource(R.drawable.cardfav_on);
            } else{
                // Delete restaurant from database
                new Thread(() -> {
                    DatabaseReference favoriteReference = FirebaseDatabase.getInstance()
                            .getReference(MainActivity.accountName)
                            .child(placeId);

                    if (favoriteReference.getKey() == null) {
                        return;
                    }

                    favoriteReference.removeValue();
                    Log.i("DATABASE", "deleted " + placeId );
                }).start();
                btn_favorite.setBackgroundResource(R.drawable.cardfav_off);
            }
        });

        container.addView(view);
        return view;
    }


    @Override
    protected void finalize() throws Throwable {
        super.finalize();

//        if (database != null) {
//            database.goOffline();
//        }
    }
}
