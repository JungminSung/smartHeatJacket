package com.example.android.bluetoothchat;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

/**
 * Created by SangEun on 2016-03-22.
 */
public class PartionSelectionFragment extends Fragment {

    private FrameLayout selectImageLayout,partionSelectLayout;

    private ImageButton bodyImageButton, leftArmImageButton, rightArmImageButton,partionOkButton;
    private int first , bodyClicked,armClicked;  //버튼 여러개 누를때 .
    private final static int mainFragmentSelect=0;



    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ViewGroup rootView=(ViewGroup) inflater.inflate(R.layout.partion_selection,container,false);

        first=0;bodyClicked=0;armClicked=0;


        return rootView;
    }
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        selectImageLayout=(FrameLayout)view.findViewById(R.id.selectImageLayout);
        partionSelectLayout = (FrameLayout)view.findViewById(R.id.partionSelectLayout);
        bodyImageButton = (ImageButton)view.findViewById(R.id.bodyImageButton);
        leftArmImageButton = (ImageButton)view.findViewById(R.id.leftArmImageButton);
        rightArmImageButton = (ImageButton)view.findViewById(R.id.rightArmImageButton);
        partionOkButton = (ImageButton)view.findViewById(R.id.partionOkButton);
    }

    public void onStart(){
        super.onStart();
        bodyImageButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if(bodyClicked==1){
                    bodyClicked = 0;first--;
                }
                else{
                    bodyClicked = 1;first++;
                }
                showWhichPartionSelected(first,armClicked,bodyClicked);
            }
        });
        leftArmImageButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if(armClicked==1){
                    armClicked = 0; first--;
                }
                else{
                    armClicked = 1;first++;
                }
                showWhichPartionSelected(first,armClicked,bodyClicked);
            }
        });
        rightArmImageButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if(armClicked==1){
                    armClicked = 0; first--;
                }
                else{
                    armClicked = 1; first++;
                }
                showWhichPartionSelected(first,armClicked,bodyClicked);
            }
        });
        partionOkButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                if ((armClicked==1)&&(bodyClicked==0)) {
                    mainActivity.onPartionSelected(3);
                }else if ((bodyClicked==1)&&(armClicked==0)) {
                    mainActivity.onPartionSelected(4);
                }else if((armClicked==1)&&(bodyClicked==1)){
                    mainActivity.onPartionSelected(5);
                }
                mainActivity.onFragmentChanged(mainFragmentSelect);
            }
        });
    }



    public void showWhichPartionSelected(int first, int armClicked, int bodyClicked)
    {
//        if(first==1){
//            if(armClicked==1) {
//                selectImageLayout.setBackgroundResource(R.drawable.partionfragment_arm);
//            }
//            else {
//                selectImageLayout.setBackgroundResource(R.drawable.partionfragment_body);
//            }
//
//        }
//        if(first==2){
//            selectImageLayout.setBackgroundResource(R.drawable.partionfragment_upperbody);
//        }
        if((armClicked==1)&&(bodyClicked==1)){
            selectImageLayout.setBackgroundResource(R.drawable.partionfragment_upperbody);
        }
        else if((armClicked==0)&&(bodyClicked==1)){
            selectImageLayout.setBackgroundResource(R.drawable.partionfragment_body);
        }
        else if((armClicked==1)&&(bodyClicked==0)){
            selectImageLayout.setBackgroundResource(R.drawable.partionfragment_arm);
        }else if((armClicked==0)&&(bodyClicked==0)){
            selectImageLayout.setBackgroundResource(R.drawable.partionfragment_default);
        }
    }

}