package com.green.auri.onboarding;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.green.auri.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class instruction1 extends Fragment {


    public instruction1() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_intro_page_1, container, false);
    }

}
