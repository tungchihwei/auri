package com.green.auri;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class Card extends Fragment {
    String rname;
    String raddress;
    float rating;
    private TextView txt_rname;
    private TextView txt_raddress;
    private RatingBar rb;

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

        return view;
    }

    public void setInfo(String[] info){
        rname = info[0];
        raddress = info[1];
        if (info.length > 2) {
            rating = Float.parseFloat(info[2]);
        }
    }

}
