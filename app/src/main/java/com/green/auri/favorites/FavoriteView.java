package com.green.auri.favorites;

import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.green.auri.R;
import com.green.auri.utils.RecyclerItemTouchHelper;
import com.green.auri.utils.RecyclerItemTouchHelperListener;

import java.util.ArrayList;

public class FavoriteView extends AppCompatActivity implements RecyclerItemTouchHelperListener {

    protected GeoDataClient mGeoDataClient;
    FirebaseDatabase database;
    DatabaseReference myRef;
    public ArrayList<FavoriteData> fav_datail = new ArrayList<FavoriteData>();

    public RecyclerView fav_recyclerView;
    FavAdapter fav_Adapter;
//    LinearLayoutManager layoutManager;
    private CoordinatorLayout rootLayout;

    private SharedPreferences sp;
    String accountName;

    String[] favorite_list;
    int fav_count;

    boolean firstRender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("My Favorite");
        setContentView(R.layout.activity_favoriteview);

        mGeoDataClient = Places.getGeoDataClient(this);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        myRef.getDatabase();

        sp = getSharedPreferences("login",MODE_PRIVATE);
        accountName = sp.getString("account", "NA");

        fav_Adapter = new FavAdapter(this, this.getBaseContext());
        fav_recyclerView = (RecyclerView) findViewById(R.id.fav_recyclerView);

        rootLayout = (CoordinatorLayout) findViewById(R.id.rootLayout);

        firstRender = true;


        myRef.child(accountName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                    Log.i("checkdata", dataSnapshot.getValue().toString());
                fav_count = (int)dataSnapshot.getChildrenCount();
                favorite_list = new String[fav_count];
                int k = 0;
                if (!firstRender){
                    fav_datail.clear();
                }
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    String key=childSnapshot.getKey();
                    favorite_list[k] = key;
                    Log.i("checkdata", key);
                    FavoriteData add = new FavoriteData();
                    add.Place_id = key;
//                    Log.i("order", childSnapshot.child("Name").getValue().toString());
                    add.fav_resName = childSnapshot.child("Name").getValue().toString();

                    try {
                        String photo = childSnapshot.child("Photo").getValue().toString();
                        byte[] encodeByte = Base64.decode(photo, Base64.DEFAULT);
                        add.fav_bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                    } catch (Exception e) {
                        add.fav_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.na);
                    }


                    fav_datail.add(add);
//                   Log.i("order", favorite_list[i]);
                    k ++;
                }
                firstRender = false;
//                layoutManager = new LinearLayoutManager(FavoriteView.this);
//                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(FavoriteView.this);
                fav_recyclerView.setLayoutManager(layoutManager);

                fav_recyclerView.addItemDecoration(new DividerItemDecoration(FavoriteView.this, DividerItemDecoration.VERTICAL));
                fav_recyclerView.setItemAnimator(new DefaultItemAnimator());
                fav_recyclerView.setLayoutManager(layoutManager);
                fav_recyclerView.setAdapter(fav_Adapter);
//                fav_recyclerView.invalidateOutline();
//                list_fav.invalidateViews();
//               fav_datail.clear();
                enableSwipe();
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("Favorite Database Errore", databaseError.toString());
            }
        });

//        fav_recyclerView.set
    }

    private void enableSwipe(){
        ItemTouchHelper.SimpleCallback itemTouchHelperCallBack
                    = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
                new ItemTouchHelper(itemTouchHelperCallBack).attachToRecyclerView(fav_recyclerView);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof FavAdapter.ViewHolder){
            // need to delete from firebase
            final int deleteIndex = viewHolder.getAdapterPosition();
            String Place_id = fav_datail.get(deleteIndex).Place_id;
            fav_Adapter.removeItem(deleteIndex);
            Log.i("isFav", "onCheckedChange to off");

            FirebaseDatabase fav_database;
            fav_database = FirebaseDatabase.getInstance();
            fav_database.getReference(accountName).child(Place_id).removeValue();
            firstRender = false;

        }
    }
}

