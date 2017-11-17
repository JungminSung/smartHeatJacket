package com.example.android.bluetoothchat;

import java.io.Serializable;

/**
 * Created by SangEun on 2016-03-23.
 */
public class ArmBodySaving implements Serializable  {

//    private final int armbody=1;
//    private final int upperbody=2;
    private int checkPartion;
    private Savings armSavings;
    private Savings bodySavings;
    private Savings upperbodySavings;



    public ArmBodySaving(){
        checkPartion=2;
        armSavings=new Savings();
        bodySavings=new Savings();
        upperbodySavings=new Savings();

    }

    public Savings getArmSavings() {
        return armSavings;
    }

    public void setArmSavings(Savings armSavings) {
        this.armSavings = armSavings;
    }

    public Savings getBodySavings() {
        return bodySavings;
    }

    public void setBodySavings(Savings bodySavings) {
        this.bodySavings = bodySavings;
    }

    public Savings getUpperbodySavings() {
        return upperbodySavings;
    }

    public void setUpperbodySavings(Savings upperbodySavings) {
        this.upperbodySavings = upperbodySavings;
    }

    public int getCheckPartion() {
        return checkPartion;
    }

    public void setCheckPartion(int checkPartion) {
        this.checkPartion = checkPartion;
    }
}
