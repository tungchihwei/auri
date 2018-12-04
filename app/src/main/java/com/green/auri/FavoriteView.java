package com.green.auri;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.Toast;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FavoriteView extends AppCompatActivity {

    protected GeoDataClient mGeoDataClient;
    FirebaseDatabase database;
    DatabaseReference myRef;
    public ArrayList<fav_data> fav_datail = new ArrayList<fav_data>();

    public RecyclerView fav_recyclerView;
    FavAdapter fav_Adapter;
    LinearLayoutManager layoutManager;

    private SharedPreferences sp;
    String accountName;

    String[] favorite_list;
    int fav_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_favoriteview);

        mGeoDataClient = Places.getGeoDataClient(this);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        myRef.getDatabase();

        sp = getSharedPreferences("login",MODE_PRIVATE);
        accountName = sp.getString("account", "NA");

        fav_Adapter = new FavAdapter(this, this.getBaseContext());
        fav_recyclerView = (RecyclerView) findViewById(R.id.fav_recyclerView);



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
                    fav_data add = new fav_data();
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
                layoutManager = new LinearLayoutManager(FavoriteView.this);
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

                fav_recyclerView.addItemDecoration(new DividerItemDecoration(FavoriteView.this, DividerItemDecoration.VERTICAL));
                fav_recyclerView.setLayoutManager(layoutManager);
                fav_recyclerView.setAdapter(fav_Adapter);
//                fav_recyclerView.invalidateOutline();
//                list_fav.invalidateViews();
//               fav_datail.clear();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("Favorite Database Errore", databaseError.toString());
            }
        });

//        fav_recyclerView.set
    }
}

class fav_data {
    String fav_resName;
    Bitmap fav_bitmap;
    String Place_id;
}

class FavAdapter extends RecyclerView.Adapter<FavAdapter.ViewHolder>
{
//    private List<String> mDataSet;
//    Context context;
    FavoriteView main;
    Context context;
//    private OnItemClickListener mOnItemClickListener = null;

//    public interface OnItemClickListener {
//        void onItemClick(View view, int position);
//    }
//
//    public void setOnItemClickListener(OnItemClickListener listener) {
//        mOnItemClickListener = listener;
//    }

    public FavAdapter(FavoriteView main, Context aContext)
    {
        this.main = main;
        context = aContext;
//        context = aContext;
    }

    @Override
    public FavAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favoritelist, parent, false);
        ViewHolder viewholder = new ViewHolder(view);
//        view.setOnClickListener(this);
        return viewholder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
//        holder.txt_Name.setText(mDataSet.get(position));

        holder.txt_Name.setText(this.main.fav_datail.get(position).fav_resName);
        Bitmap bm = this.main.fav_datail.get(position).fav_bitmap;
        holder.img_Photo.setImageBitmap(bm);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent fav_detail = new Intent(context, FavoriteDetail.class);
                fav_detail.putExtra("place_id", FavAdapter.this.main.fav_datail.get(position).Place_id);
                fav_detail.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(fav_detail);
//                Toast.makeText(context, "onclick " + FavAdapter.this.main.fav_datail.get(position).Place_id, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return main.fav_datail.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView txt_Name;
        public ImageView img_Photo;

        public ViewHolder(View itemView)
        {
            super(itemView);
            txt_Name = (TextView) itemView.findViewById(R.id.txt_resName);
            img_Photo = (ImageView) itemView.findViewById(R.id.img_res);

//            if (view == null) {
//                throw new IllegalArgumentException("itemView may not be null");
//            }
//            view.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                }
//            });

        }
    }
}