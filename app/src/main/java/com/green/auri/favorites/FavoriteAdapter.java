package com.green.auri.favorites;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.green.auri.R;
import com.green.auri.restaurant.RestaurantActivity;

/**
 * The recycler view adapter for the favorites activity.
 */
public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder>
{
    private FavoriteView main;
    private Context context;

    public FavoriteAdapter(FavoriteView main, Context context)
    {
        this.main = main;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        // Inflate item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favoritelist, parent, false);
        ViewHolder viewholder = new ViewHolder(view);
        return viewholder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        holder.txt_Name.setText(this.main.favoriteList.get(position).getRestaurantName());
        holder.img_Photo.setImageBitmap(this.main.favoriteList.get(position).getRestaurantImage());

        // Set item to be clickable
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent restaurantIntent = new Intent(context, RestaurantActivity.class);
                restaurantIntent.putExtra("place_id", FavoriteAdapter.this.main.favoriteList.get(position).getPlaceId());
                restaurantIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(restaurantIntent);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return main.favoriteList.size();
    }

    public void removeItem(int position) {
        main.favoriteList.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * The structure for a row item in the recycler view.
     */
    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView txt_Name;
        public ImageView img_Photo;
        public RelativeLayout view_background, view_foreground;

        public ViewHolder(View itemView)
        {
            super(itemView);
            txt_Name = (TextView) itemView.findViewById(R.id.txt_resName);
            img_Photo = (ImageView) itemView.findViewById(R.id.img_res);
            view_background = (RelativeLayout) itemView.findViewById(R.id.view_background);
            view_foreground = (RelativeLayout) itemView.findViewById(R.id.view_foreground);
        }
    }
}
