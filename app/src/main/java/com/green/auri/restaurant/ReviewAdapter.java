package com.green.auri.restaurant;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.green.auri.R;

// List view for reviews
public class ReviewAdapter extends BaseAdapter {

    private TextView txt_author;
    private TextView txt_date;
    private TextView txt_reviews;
    private Context context;
    private RestaurantActivity main;

    public ReviewAdapter(Context aContext, RestaurantActivity main){
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
        return main.place_reviews.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row;

        // Indicates the first time creating this row.
        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.reviewlist, parent, false);
        } else {
            row = convertView;
        }

        txt_author = row.findViewById(R.id.txt_author);
        txt_date = row.findViewById(R.id.txt_date);
        txt_reviews = row.findViewById(R.id.txt_reviews);

        // Set reviews
        txt_author.setText(this.main.place_reviews.get(position).getAuthor());
        txt_date.setText(this.main.place_reviews.get(position).getDate());
        txt_reviews.setText(this.main.place_reviews.get(position).getReview());

        return row;
    }
}
