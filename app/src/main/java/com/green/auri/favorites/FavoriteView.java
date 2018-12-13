package com.green.auri.favorites;

import android.graphics.BitmapFactory;
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
import com.green.auri.MainActivity;
import com.green.auri.R;
import com.green.auri.utils.recycler.RecyclerItemTouchHelper;
import com.green.auri.utils.recycler.RecyclerItemTouchHelperListener;

import java.util.ArrayList;
import android.graphics.Bitmap;

/**
 * An activity to display user's favorites, using a recycler view.
 */
public class FavoriteView extends AppCompatActivity implements RecyclerItemTouchHelperListener {
    /* Constants */
    int fav_count;
    boolean firstRender;
    private String accountName;
    private String[] favoriteIds;
    public ArrayList<Favorite> favoriteList = new ArrayList<Favorite>();

    /* Components */
    private FavoriteAdapter favoriteAdapter;
    public RecyclerView recyclerView;

    /* Firebase */
    protected GeoDataClient mGeoDataClient;
    private FirebaseDatabase database;
    private DatabaseReference myRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("My Favorites");
        setContentView(R.layout.activity_favoriteview);

        mGeoDataClient = Places.getGeoDataClient(this);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        accountName = MainActivity.accountName;
        favoriteAdapter = new FavoriteAdapter(this, this.getBaseContext());
        recyclerView = (RecyclerView) findViewById(R.id.fav_recyclerView);

        firstRender = true;

        // Read database
        myRef.child(accountName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                fav_count = (int)dataSnapshot.getChildrenCount();
                favoriteIds = new String[fav_count];

                if (!firstRender){
                    favoriteList.clear();
                }

                int k = 0;
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    String restaurantId = childSnapshot.getKey();

                    // Get value from database
                    favoriteIds[k] = restaurantId;
                    Bitmap bitmap;

                    try {
                        // Turn photo string to bitmap
                        String photo = childSnapshot.child("Photo").getValue().toString();
                        byte[] encodeByte = Base64.decode(photo, Base64.DEFAULT);

                        bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                    } catch (Exception e) {

                        // Set default photo
                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.na);
                    }

                    Favorite favorite = new Favorite(childSnapshot.child("Name").getValue().toString(), bitmap, restaurantId);
                    favoriteList.add(favorite);
                    k++;
                }

                firstRender = false;

                // Set RecyclerView
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(FavoriteView.this);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.addItemDecoration(new DividerItemDecoration(FavoriteView.this, DividerItemDecoration.VERTICAL));
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(favoriteAdapter);
                enableSwipe();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("Favorite Database Error", databaseError.toString());
            }
        });
    }

    // enable swiping the item in RecyclerView
    private void enableSwipe(){
        ItemTouchHelper.SimpleCallback itemTouchHelperCallBack
                    = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
                new ItemTouchHelper(itemTouchHelperCallBack).attachToRecyclerView(recyclerView);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof FavoriteAdapter.ViewHolder){
            final int deleteIndex = viewHolder.getAdapterPosition();
            String restaurantId = favoriteList.get(deleteIndex).getPlaceId();
            favoriteAdapter.removeItem(deleteIndex);

            // Delete the unfavorited from firebase
            FirebaseDatabase fav_database;
            fav_database = FirebaseDatabase.getInstance();
            fav_database.getReference(accountName).child(restaurantId).removeValue();
            firstRender = false;
        }
    }
}

