package com.green.auri;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FavoriteCheck extends AppCompatActivity {

    protected GeoDataClient mGeoDataClient;
    FirebaseDatabase database;
    DatabaseReference myRef;
    public ArrayList<fav_val> fav_datail = new ArrayList<fav_val>();

    public ListView list_fav;
    public ListAdapter review_Adapter;

    private SharedPreferences sp;
    String accountName;

    String[] favorite_list;
    int fav_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // hide status bar
        getSupportActionBar().hide();

        setContentView(R.layout.activity_favoritecheck);

        mGeoDataClient = Places.getGeoDataClient(this);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        myRef.getDatabase();

//        final fav_val favorite = new fav_val();
        list_fav = (ListView) findViewById(R.id.list_fav);
        review_Adapter = new FavoriteAdapter(this.getBaseContext(), this);
        list_fav.setAdapter(review_Adapter);

        sp = getSharedPreferences("login",MODE_PRIVATE);
        accountName = sp.getString("account", "NA");



        myRef.child(accountName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                    Log.i("checkdata", dataSnapshot.getValue().toString());
                fav_count = (int)dataSnapshot.getChildrenCount();
                favorite_list = new String[fav_count];
                int k = 0;
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    String key=childSnapshot.getKey();
                    favorite_list[k] = key;
                    Log.i("checkdata", key);
                    fav_val add = new fav_val();
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
                list_fav.invalidateViews();
//               fav_datail.clear();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("Favorite Database Errore", databaseError.toString());
            }
        });


        list_fav.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent fav_detail = new Intent(FavoriteCheck.this, FavoriteDetail.class);
                fav_detail.putExtra("place_id", favorite_list[i]);
                startActivity(fav_detail);
            }
        });


    }
}

class fav_val {
    String fav_resName;
    Bitmap fav_bitmap;
}

// List view for favorites
class FavoriteAdapter extends BaseAdapter {

    ImageView img_res;
    TextView txt_resName;
    Context context;
    FavoriteCheck main;

    public FavoriteAdapter(Context aContext, FavoriteCheck main){
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
        return main.fav_datail.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row;

        // Indicates the first time creating this row.
        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.favoritelist, parent, false);
        } else {
            row = convertView;
        }

        txt_resName = (TextView) row.findViewById(R.id.txt_resName);
        img_res = (ImageView) row.findViewById(R.id.img_res);

        try {
            txt_resName.setText(this.main.fav_datail.get(position).fav_resName);
            Bitmap bm = this.main.fav_datail.get(position).fav_bitmap;
            img_res.setImageBitmap(bm);
        } catch (Exception e) {
            Log.i("favorite check", "database error");
        }

        return row;
    }
}

