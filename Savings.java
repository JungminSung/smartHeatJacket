package com.example.android.bluetoothchat;

import java.io.Serializable;

/**
 * Created by SangEun on 2016-02-22.
 */
public class Savings implements Serializable {


    private int recOrUserMode;   // recMode, userMode
    private int modeOrTemp;


    public Savings(){
        recOrUserMode=1;
        modeOrTemp=80;

    }

    public int getRecOrUserMode() {
        return recOrUserMode;
    }

    public void setRecOrUserMode(int recOrUserMode) {
        this.recOrUserMode = recOrUserMode;
    }

    public int getModeOrTemp() {
        return modeOrTemp;
    }

    public void setModeOrTemp(int modeOrTemp) {
        this.modeOrTemp = modeOrTemp;
    }
}
