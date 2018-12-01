package com.green.auri;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.List;

public class CardAdapter extends PagerAdapter {

//    List<String> lstResName; // need include 0:name, 1:address, 2:rating, if favorite, res image?
    List<List<String>> lstResInfo;
    Context context;
    LayoutInflater layoutInflater;

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
        //==
        ImageView imageView = (ImageView) ll.findViewById(R.id.img_favorite);
        // to do: get tag from firebase and set to imageview, 1 is favorite, 0 otherwise
        imageView.setImageResource(R.drawable.ic_favorite_gray_24dp);
        imageView.setTag(0);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int preTag_favorite = (int) v.getTag();
                int curTag_favorite = preTag_favorite == 0 ? 1 : 0;
                v.setTag(curTag_favorite);
                if (curTag_favorite == 1){
                    imageView.setImageResource(R.drawable.ic_favorite_red_24dp);
                }else {
                    imageView.setImageResource(R.drawable.ic_favorite_gray_24dp);
                }
                // to do : store it in firebase
                Log.i("!!!cardview",String.valueOf((int)v.getTag()));
            }
        });
        //==
        container.addView(view);
        return view;
    }
}
