package com.example.android.bluetoothchat;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

/**
 * Created by SangEun on 2016-03-24.
 */
public class StartFragment extends Fragment {

    private ImageButton startImageButton;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ViewGroup rootView=(ViewGroup) inflater.inflate(R.layout.start_view,container,false);


        return rootView;
    }
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

       startImageButton=(ImageButton)view.findViewById(R.id.startImageButton);

    }

    public void onStart(){
        super.onStart();
        startImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.onFragmentChanged(2);
            }
        });

    }

}
