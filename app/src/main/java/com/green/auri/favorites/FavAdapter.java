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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
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

    public void removeItem(int position) {
        // also need to remove from firebase
        main.fav_datail.remove(position);
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
