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

public class FavAdapter extends RecyclerView.Adapter<FavAdapter.ViewHolder>
{
    FavoriteView main;
    Context context;

    public FavAdapter(FavoriteView main, Context aContext)
    {
        this.main = main;
        context = aContext;
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
        holder.txt_Name.setText(this.main.fav_detail.get(position).fav_resName);
        Bitmap bm = this.main.fav_detail.get(position).fav_bitmap;
        holder.img_Photo.setImageBitmap(bm);

        // Set item to be clickable
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent fav_detail = new Intent(context, FavoriteDetail.class);
                fav_detail.putExtra("place_id", FavAdapter.this.main.fav_detail.get(position).Place_id);
                fav_detail.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(fav_detail);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return main.fav_detail.size();
    }

    public void removeItem(int position) {
        // also need to remove from firebase
        main.fav_detail.remove(position);
        notifyItemRemoved(position);
    }

    // restore, can be done in the future.

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
