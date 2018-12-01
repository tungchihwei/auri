package com.green.auri;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;


/**
 * A simple {@link Fragment} subclass.
 */
public class Card extends Fragment {
    String rname;
    String raddress;
    String Place_id;
    String accountName;
    String res_photo;
    float rating;
    private TextView txt_rname;
    private TextView txt_raddress;
    private RatingBar rb;

    private ToggleButton btn_fav;
    FirebaseDatabase fav_database;
    DatabaseReference favRef;
    DatabaseReference NameRef;
    DatabaseReference PhotoRef;
    public int isFav;
    int checked;

    public Card() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_card, container, false);
        txt_rname = view.findViewById(R.id.txt_rname);
        txt_raddress = view.findViewById(R.id.txt_raddress);
        rb = view.findViewById(R.id.rb);

        txt_rname.setText(rname);
        txt_raddress.setText(raddress);
        rb.setRating(rating);

        btn_fav = view.findViewById(R.id.btn_fav);
        checked = 0;

        // Check if restaurant is in database (favorite)
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
//                            btn_fav.setBackgroundResource(R.drawable.fav_on);
                            break;
                        }
                    }
                    if (isFav == 0){
                        btn_fav.setChecked(false);
//                        btn_fav.setBackgroundResource(R.drawable.fav_off);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        Log.i("isFav", "onCreateView");

//        if (isFav == 1){
////            checked = 1;
//            Log.i("isFav", Integer.toString(isFav));
//            btn_fav.setChecked(true);
//            btn_fav.setBackgroundResource(R.drawable.fav_on);
//        } else{
////            checked = 1;
//            Log.i("isFav", Integer.toString(isFav));
//            btn_fav.setChecked(false);
//            btn_fav.setBackgroundResource(R.drawable.fav_off);
//        }

        btn_fav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    Log.i("isFav", "onCheckedChange to on");
                    checked = 1;
                    // Save restaurant to database
                    fav_database = FirebaseDatabase.getInstance();
                    favRef = fav_database.getReference(accountName);
                    favRef = favRef.child(Place_id);
                    favRef = fav_database.getReference(accountName + "/" + Place_id);
                    NameRef = favRef.child("Name");
                    NameRef.setValue(rname);

                    favRef = FirebaseDatabase.getInstance().getReference(accountName + "/" + Place_id);
                    PhotoRef = favRef.child("Photo");
                    PhotoRef.setValue(res_photo);
                    btn_fav.setBackgroundResource(R.drawable.fav_on);
                } else{
                    Log.i("isFav", "onCheckedChange to off");
                    // Delete restaurant from database
                    checked = 1;
                    fav_database = FirebaseDatabase.getInstance();
                    fav_database.getReference(accountName).child(Place_id).removeValue();
                    btn_fav.setBackgroundResource(R.drawable.fav_off);
                }
            }
        });
        return view;
    }

    public void setInfo(String[] info, String id, String account, String photo, Integer fav){
        rname = info[0];
        raddress = info[1];

        if (info.length > 2) {
            rating = Float.parseFloat(info[2]);
        }
        Place_id = id;
        accountName = account;
        res_photo = photo;
        isFav = fav;
    }
}
