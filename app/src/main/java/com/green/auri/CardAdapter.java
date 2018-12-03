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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class CardAdapter extends PagerAdapter {

//    List<String> lstResName; // need include 0:name, 1:address, 2:rating, if favorite, res image?
    List<List<String>> lstResInfo;
    Context context;
    LayoutInflater layoutInflater;
//    ToggleButton btn_fav;
    FirebaseDatabase fav_database;
    DatabaseReference favRef;
    DatabaseReference NameRef;
    DatabaseReference PhotoRef;
    Integer isFav;
    MainActivity main;

    protected GeoDataClient mGeoDataClient;

    public CardAdapter(List<List<String>> lstResInfo, Context context) {
//        this.lstResName = lstResName;
        this.lstResInfo = lstResInfo;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return lstResInfo.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view.equals(o);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = layoutInflater.inflate(R.layout.card_item, container, false);
        LinearLayout ll = (LinearLayout) view.findViewById(R.id.cycle_card);
        TextView txtResName = (TextView) ll.findViewById(R.id.txtResName);
        txtResName.setText(String.valueOf(position + 1) + ". " + lstResInfo.get(position).get(0));
        TextView txtResAddress = (TextView) ll.findViewById(R.id.txtResAddress);
        txtResAddress.setText(lstResInfo.get(position).get(1));
        TextView txtRb = (TextView) ll.findViewById(R.id.txtRb);
        String rating = lstResInfo.get(position).get(2);
        txtRb.setText(rating);
        RatingBar rbRes = (RatingBar) ll.findViewById(R.id.rbRes);
        rbRes.setRating(Float.parseFloat(rating));

        String Place_id = lstResInfo.get(position).get(3);
        String accountName = lstResInfo.get(position).get(4);
        String res_photo = lstResInfo.get(position).get(5);

        ToggleButton btn_fav = (ToggleButton) ll.findViewById(R.id.btn_favorite);

//        if (position == lstResInfo.size()-1){
//            main.wait = 0;
//        }


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
                            btn_fav.setChecked(true);
//                            main.wait = 1;
//                            btn_fav.setBackgroundResource(R.drawable.fav_on);
                            break;
                        }
                    }
                    if (isFav == 0){
                        btn_fav.setChecked(false);
//                        main.wait = 1;
//                        btn_fav.setBackgroundResource(R.drawable.fav_off);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }



        btn_fav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
                    Log.i("swipebtn", lstResInfo.get(position).get(0));
                    NameRef.setValue(lstResInfo.get(position).get(0));

                    favRef = FirebaseDatabase.getInstance().getReference(accountName + "/" + Place_id);
                    PhotoRef = favRef.child("Photo");
                    PhotoRef.setValue(res_photo);
                    btn_fav.setBackgroundResource(R.drawable.cardfav_on);
//                    main.wait = 1;
                } else{
                    Log.i("isFav", "onCheckedChange to off");
                    // Delete restaurant from database
//                    checked = 1;
                    fav_database = FirebaseDatabase.getInstance();
                    fav_database.getReference(accountName).child(Place_id).removeValue();
                    btn_fav.setBackgroundResource(R.drawable.cardfav_off);
//                    main.wait = 1;
                }
            }
        });

        //==

        LinearLayout layout_cardInfo = (LinearLayout ) ll.findViewById(R.id.layout_cardInfo);

        layout_cardInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fav_detail = new Intent(context, FavoriteDetail.class);
                fav_detail.putExtra("place_id", Place_id);
                fav_detail.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(fav_detail);
            }
        });


        container.addView(view);
        return view;
    }
}
