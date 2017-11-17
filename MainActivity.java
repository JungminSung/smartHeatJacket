/*
* Copyright 2013 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package com.example.android.bluetoothchat;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.common.activities.SampleActivityBase;
import com.example.android.common.logger.Log;
import com.example.android.common.logger.LogWrapper;
import com.example.android.common.logger.MessageOnlyLogFilter;

/**
 * A simple launcher activity containing a summary sample description, sample log and a custom
 * {@link android.support.v4.app.Fragment} which can display a view.
 * <p>
 * For devices with displays with a width of 720dp or greater, the sample log is always visible,
 * on other devices it's visibility is controlled by an item on the Action Bar.
 */
public class MainActivity extends SampleActivityBase {

    StartFragment startingFragment;
    BluetoothChatFragment mainFragment;
    PartionSelectionFragment partionFragment;
    FragmentManager fragmentManager=getSupportFragmentManager();
    FragmentTransaction transaction=fragmentManager.beginTransaction();


    public static final String TAG = "MainActivity";

    // Whether the Log Fragment is currently shown
    private boolean mLogShown;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startingFragment=new StartFragment();
        mainFragment = new BluetoothChatFragment();
        partionFragment=new PartionSelectionFragment();

        fragmentManager=getSupportFragmentManager();
        transaction = getSupportFragmentManager().beginTransaction();



        if (savedInstanceState == null) {


            transaction.replace(R.id.sample_content_fragment, startingFragment);
//            transaction.addToBackStack(null);
            transaction.commit();
       }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    /** Create a chain of targets that will receive log data */
    @Override
    public void initializeLogging() {

        // Wraps Android's native log framework.
        LogWrapper logWrapper = new LogWrapper();
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.setLogNode(logWrapper);

        // Filter strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);

        Log.i(TAG, "Ready");
    }

    public void onFragmentChanged(int index){
        int mainFragmentSelect=0;
        int partionFragmentSelect=1;
        int startToMain=2;
        fragmentManager=getSupportFragmentManager();
        transaction = getSupportFragmentManager().beginTransaction();

        if(index==mainFragmentSelect){
                transaction.remove(partionFragment);
                transaction.commit();

        }else if(index==partionFragmentSelect){
                transaction.add(R.id.sample_content_fragment, partionFragment);
                transaction.addToBackStack(null);
                transaction.commit();
        }
        else if(index==startToMain){

            transaction.replace(R.id.sample_content_fragment,mainFragment);
            transaction.commit();
        }
    }

    public void onPartionSelected(int selectedP){
        mainFragment.setSendPartionString(selectedP);
    }
}
