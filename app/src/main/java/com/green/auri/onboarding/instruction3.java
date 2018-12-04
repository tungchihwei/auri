package com.green.auri.onboarding;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.green.auri.LoginActivity;
import com.green.auri.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class instruction3 extends Fragment {
    private Button btn;

    public instruction3() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_intro_page_3, container, false);
        View rootView = inflater.inflate(R.layout.fragment_intro_page_3, container, false);
        btn = rootView.findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), LoginActivity.class);
                startActivity(i);
            }
        });
        return rootView;
    }

}
