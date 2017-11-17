/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothchat;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.common.logger.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * This fragment controls Bluetooth to communicate with other devices.
 */

public class BluetoothChatFragment extends Fragment {

    private static final String TAG = "BluetoothChatFragment";

    //saving files.
    private ArmBodySaving savingClass;
//    private ArmBodySaving armBodySaving;
    private int checkPartion;
    private static final int upperBodySelected=2;
    private static final int armAndBodySelected=1;
    private String filename="masterfile.txt";

    //send String
    private String sendModeString;
    private String sendPartionString;
    private String sendModeOrTempString;
    private static final String EndOfSend="~";

    //
    private static final int RECMODE=1;
    private static final int USERMODE=2;

    private static final int ARM=3;
    private static final int BODY=4;
    private static final int UPPERBODY=5;

    private static final int POWERMODE=70;
    private static final int WARMMODE=80;
    private static final int SAVEMODE=90;
    private int tempCelsius;


    //button clicked background image
    private static final int rec_default=0;
    private static final int rec_power=1;
    private static final int rec_warm=2;
    private static final int rec_save=3;
    private static final int user_default=4;



    //set wear state
    private static final char wearState='1';
    private static final char unWearState='2';

    //set Recommand or User Mode
    private static final int RECSelection=1;
    private static final int USRSelection=2;

    //read Message
    private StringBuilder readMessages=new StringBuilder();
    private static final String EndOfArmTemperature="*";




    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Layout Views

    private LinearLayout masterLayout,wearandCurrentTempLayout,partionAndControlLayout,
                        modeControlLayout;
    private FrameLayout currentTempLayout,controlFrameLayout,recommendControlLayout,
                        userControlLayout,voiceCommandLayout;
    private ImageView wearImageView,partionImageView;
    private TextView currentArmTempTextView,currentBodyTempTextView,userTempTextView,voiceResultTextView;
    private SeekBar tempSeekBar;
    private ImageButton powerImageButton, warmImageButton, saveImageButton,
                        recommendImageButton, userImageButton, voiceCommandImageButton;
    private BitmapDrawable OnWearBitmap,OffWearBitmap;

    //final numbers for background change
    private static final int recommendImageButtonClicked=1;
    private static final int userImageButtonClicked=2;

    /* Voice Recognization*/

    private ArrayList<String> voiceResult;
    private SpeechRecognizer voiceRecognizer;

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;


    /**
     * String buffer for outgoing messages
     */


    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        sendModeOrTempString="";
        sendModeString="1";
        sendPartionString="5";

        savingClass=new ArmBodySaving();


        savingClass.setCheckPartion(upperBodySelected);

        fileRead();


        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }

    }


    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


        if (mChatService != null) {
            mChatService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ViewGroup rootView=(ViewGroup) inflater.inflate(R.layout.fragment_bluetooth_chat,container,false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        masterLayout=(LinearLayout)view.findViewById(R.id.masterLayout);

        //upper part
        wearandCurrentTempLayout=(LinearLayout)view.findViewById(R.id.wearAndCurrentTempLayout);
        wearImageView=(ImageView)view.findViewById(R.id.wearImageView);
        Resources wearRes=getResources();
        OnWearBitmap=(BitmapDrawable)wearRes.getDrawable(R.drawable.on);
        OffWearBitmap=(BitmapDrawable)wearRes.getDrawable(R.drawable.off);
        currentTempLayout=(FrameLayout)view.findViewById(R.id.currentTempLayout);
        currentArmTempTextView=(TextView)view.findViewById(R.id.currentArmTempTextView);
        currentBodyTempTextView=(TextView)view.findViewById(R.id.currentBodyTempTextView);

        //middle part
        partionAndControlLayout=(LinearLayout)view.findViewById(R.id.partionAndControlLayout);
        partionImageView=(ImageView)view.findViewById(R.id.partionImageView);

        controlFrameLayout=(FrameLayout)view.findViewById(R.id.controlFrameLayout);

        recommendControlLayout=(FrameLayout)view.findViewById(R.id.recommendControlLayout);
        powerImageButton=(ImageButton)view.findViewById(R.id.powerImageButton);
        warmImageButton=(ImageButton)view.findViewById(R.id.warmImageButton);
        saveImageButton=(ImageButton)view.findViewById(R.id.saveImageButton);

        userControlLayout=(FrameLayout)view.findViewById(R.id.userControlLayout);
        tempSeekBar=(SeekBar)view.findViewById(R.id.tempSeekBar);
        userTempTextView=(TextView)view.findViewById(R.id.userTempTextView);

        modeControlLayout=(LinearLayout)view.findViewById(R.id.modeControlLayout);
        recommendImageButton=(ImageButton)view.findViewById(R.id.recommendImageButton);
        userImageButton=(ImageButton)view.findViewById(R.id.userImageButton);

        //lower part
        voiceCommandLayout=(FrameLayout)view.findViewById(R.id.voiceCommandLayout);
        voiceCommandImageButton=(ImageButton)view.findViewById(R.id.voiceCommandImageButton);
        voiceResultTextView=(TextView)view.findViewById(R.id.voiceResultTextView);

    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        Log.d(TAG, "setupChat()");




        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(getActivity(), mHandler);

        // Initialize the buffer for outgoing messages

        powerImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showWhichRecommandSelected(POWERMODE);
                sendModeString="1";
                sendModeOrTempString="70";
                orderToArduino();
            }
        });
        warmImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showWhichRecommandSelected(WARMMODE);
                sendModeString="1";
                sendModeOrTempString="80";
                orderToArduino();
            }
        });
        saveImageButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                showWhichRecommandSelected(SAVEMODE);
                sendModeString="1";
                sendModeOrTempString="90";
                orderToArduino();
            }
        });

        tempSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int showProgress = (int) (progress * 0.3) + 20;
                userTempTextView.setText(showProgress + "");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {       }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                String selectedUserTemp = userTempTextView.getText().toString();
                sendModeOrTempString=selectedUserTemp;
                sendModeString="2";
                orderToArduino();
            }
        });



        recommendImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                modeControlLayout.setBackgroundResource(R.drawable.recommendmode);
                controlFrameLayout.setBackgroundResource(R.drawable.recdefault);
                showModeChange(RECSelection);
                sendModeString="1";
//                showScreenDependOnPartion();




            }
        });
        userImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                modeControlLayout.setBackgroundResource(R.drawable.usermode);
//                recommendControlLayout.setBackgroundResource(R.drawable.recdefault);
                showModeChange(USRSelection);
                sendModeString="2";
                controlFrameLayout.setBackgroundResource(R.drawable.userdefault);
            }
        });

        voiceCommandImageButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                Intent voiceIntent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                voiceIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getClass().getPackage().getName());
                voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
                voiceIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "말을 하세요");

                voiceRecognizer=SpeechRecognizer.createSpeechRecognizer(getContext());
                voiceRecognizer.setRecognitionListener(voiceListener);
                voiceRecognizer.startListening(voiceIntent);

            }
        });

        partionImageView.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                int mainFragmentSelect=0;
                int partionFragmentSelect=1;

                onPause();
                MainActivity activity=(MainActivity) getActivity();
                activity.onFragmentChanged(partionFragmentSelect);

            }
        });


    }

    /**
     * Makes this device discoverable.
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     *
     */


    private void orderToArduino() {
        // Check that we're actually connected before trying anything
        String totalMessage=sendModeString+sendPartionString+sendModeOrTempString+EndOfSend;
//        saveStates();

//        currentArmTempTextView.setText(totalMessage);

        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }


        // Check that there's actually something to send
        if (isRightOrder()) {
            saveStates();
            fileSave();
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = totalMessage.getBytes();
            mChatService.write(send);


        }
    }

    public boolean isRightOrder(){
        if((sendModeString.equals("1")||sendModeString.equals("2"))
                && (sendPartionString.equals("3")||sendPartionString.equals("4")||sendPartionString.equals("5"))
                && (sendModeOrTempString.length()==2)){
            return true;
        }else{
            return false;
        }
    }
    /**
     * The action listener for the EditText widget, to listen for the return key
     */
//    private TextView.OnEditorActionListener mWriteListener
//            = new TextView.OnEditorActionListener() {
//        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
//            // If the action is a key-up event on the return key, send the message
//            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
//                String message = view.getText().toString();
//                sendMessage(message);
//            }
//            return true;
//        }
//    };

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {

                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));

                            BTCONNECTVIEW();
                            firstShow();



                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);

                            BTUNCONNECTVIEW();
//                            BTCONNECTVIEW();

                            break;
                    }
                    break;
//                case Constants.MESSAGE_WRITE:
//                    byte[] writeBuf = (byte[]) msg.obj;
//                    // construct a string from the buffer
//                    String writeMessage = new String(writeBuf);
//                   // mConversationArrayAdapter.add("Me:  " + writeMessage);
//                    break;

                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf,0, msg.arg1);

                     readMessages.append(readMessage);

                     if(readMessage.contains(EndOfSend)) {
                         showWearState();
                         showCurrentTempState();
                         readMessages.delete(0,readMessages.length());
                     }

                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                        // Bluetooth is now enabled, so set up a chat session
                        setupChat();
                    } else {
                        // User did not enable Bluetooth or an error occurred
                        Log.d(TAG, "BT not enabled");
                        Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                                Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                    }
                    break;




        }
    }

    /**
     * Establish connection with other divice
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bluetooth_chat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.secure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.insecure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            }
            case R.id.discoverable: {
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
            }
        }
        return false;
    }

    public void fileSave(){
        try{
            File myFile=new File(getContext().getFilesDir(),filename);
            FileOutputStream fout=new FileOutputStream(myFile);
            ObjectOutputStream oout=new ObjectOutputStream(fout);
            oout.writeObject(savingClass);
            oout.close();
            fout.close();

        }catch(Exception e){

            e.printStackTrace();
        }

    }
    public void fileRead(){
        try {
            File myFile = new File(getContext().getFilesDir(),filename);
            FileInputStream fin = new FileInputStream(myFile);
            ObjectInputStream oin=new ObjectInputStream(fin);

            savingClass=(ArmBodySaving)oin.readObject();

            oin.close();
            fin.close();
        }catch(Exception e){
            e.printStackTrace();
            System.out.print(e.toString());
        }
    }

    public void BTCONNECTVIEW(){
        wearandCurrentTempLayout.setVisibility(View.VISIBLE);
        partionAndControlLayout.setVisibility(View.VISIBLE);
        voiceCommandLayout.setVisibility(View.VISIBLE);
    }
    public void BTUNCONNECTVIEW(){
        wearandCurrentTempLayout.setVisibility(View.INVISIBLE);
        partionAndControlLayout.setVisibility(View.INVISIBLE);
        voiceCommandLayout.setVisibility(View.INVISIBLE);

    }


    public void changeVoiceCommandToSendMessage(String voiceResult){

            char partionCharacter = voiceResult.charAt(0);

            if (partionCharacter == '몸') {
                sendPartionString = "4";
                savingClass.setCheckPartion(armAndBodySelected);

            } else if (partionCharacter == '팔' || partionCharacter == '8') {
                sendPartionString = "3";
                savingClass.setCheckPartion(armAndBodySelected);

            } else if (partionCharacter == '전') {
                sendPartionString = "5";
                savingClass.setCheckPartion(upperBodySelected);
            }

            int endIndex=voiceResult.indexOf("도");

            if(voiceResult.indexOf("도")!=(-1)){
                sendModeString = "2";
                String degreeToString = voiceResult.substring((endIndex - 2), endIndex);
                sendModeOrTempString = degreeToString;
            }else{
                sendModeString="1";
                if(voiceResult.contains("파워")){
                    sendModeOrTempString="70";

                }else if(voiceResult.contains("따뜻")){
                    sendModeOrTempString="80";
                }else if(voiceResult.contains("절전")){
                    sendModeOrTempString="90";
                }

            }

    }

    public void showWearState(){
        char checkwear = readMessages.charAt(0);

        if(checkwear==wearState){
            wearImageView.setImageDrawable(OnWearBitmap);
            partionImageView.setVisibility(View.VISIBLE);
            controlFrameLayout.setVisibility(View.VISIBLE);
            voiceCommandLayout.setVisibility(View.VISIBLE);
            currentTempLayout.setVisibility(View.VISIBLE);
            partionAndControlLayout.setBackgroundResource(R.drawable.message1);

        }else if(checkwear==unWearState){
            wearImageView.setImageDrawable(OffWearBitmap);
            partionImageView.setVisibility(View.INVISIBLE);
            controlFrameLayout.setVisibility(View.INVISIBLE);
            voiceCommandLayout.setVisibility(View.INVISIBLE);
            currentTempLayout.setVisibility(View.INVISIBLE);
            partionAndControlLayout.setBackgroundResource(R.drawable.message);
        }


    }

    public void showCurrentTempState(){
        int armPart=readMessages.indexOf(EndOfArmTemperature);
        int indexofEnd=readMessages.indexOf(EndOfSend);

        Integer armTemperature=new Integer(readMessages.substring(1,armPart));
        Integer bodyTemperature=new Integer(readMessages.substring(armPart+1,indexofEnd));

        armTemperature-=20;
        bodyTemperature-=20;

        currentArmTempTextView.setText(armTemperature.toString()+"ºC");
        currentBodyTempTextView.setText(bodyTemperature.toString()+"ºC");

    }

    private void showModeChange(int selectedMode){

        if(selectedMode==RECSelection){
            userControlLayout.setVisibility(View.INVISIBLE);
            recommendControlLayout.setVisibility(View.VISIBLE);

        }else if(selectedMode==USRSelection){
            userControlLayout.setVisibility(View.VISIBLE);
            recommendControlLayout.setVisibility(View.INVISIBLE);

        }
    }

    public void showWhichRecommandSelected(int selectedMode){
        if(selectedMode==POWERMODE){
            controlFrameLayout.setBackgroundResource(R.drawable.powermode);

        }else if(selectedMode==WARMMODE){
            controlFrameLayout.setBackgroundResource(R.drawable.warmmode);

        }else if(selectedMode==SAVEMODE){
            controlFrameLayout.setBackgroundResource(R.drawable.sleepmode);
        }else if(selectedMode==rec_default){
            controlFrameLayout.setBackgroundResource(R.drawable.recdefault);

        }
    }

    public void setSendPartionString(int selectedP){
        if(selectedP==ARM){
            sendPartionString="3";
            partionImageView.setImageResource(R.drawable.main_arm_selected);

        }else if(selectedP==BODY){
            sendPartionString="4";
            partionImageView.setImageResource(R.drawable.main_body_selected);

        }else if(selectedP==UPPERBODY){
            sendPartionString="5";
            partionImageView.setImageResource(R.drawable.main_all_selected);
        }
        showScreenDependOnPartion();
    }

    public RecognitionListener voiceListener=new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) { voiceResultTextView.setText("듣는중..."); }
        @Override
        public void onBeginningOfSpeech() {  }
        @Override
        public void onRmsChanged(float rmsdB) {  }
        @Override
        public void onBufferReceived(byte[] buffer) {  }
        @Override
        public void onEndOfSpeech() {   }
        @Override
        public void onError(int error) {    }
        @Override
        public void onResults(Bundle results) {

            String key="";
            key=SpeechRecognizer.RESULTS_RECOGNITION;
            voiceResult=results.getStringArrayList(key);
            String[] resultArray=new String[voiceResult.size()];
            voiceResult.toArray(resultArray);

            voiceResultTextView.setText(resultArray[0]);
            String resultSTring=resultArray[0];


            if(isVoiceResultStringCorrect(resultSTring)) {
                changeVoiceCommandToSendMessage(resultSTring);
                saveStates();
                setSendPartionString(new Integer(sendPartionString));
                orderToArduino();
            }else{
                voiceResultTextView.setText("다시말씀해주세요");
            }


        }
        @Override
        public void onPartialResults(Bundle partialResults) {        }
        @Override
        public void onEvent(int eventType, Bundle params) {       }
    };

    public boolean isVoiceResultStringCorrect(String outputVoice){
        if(outputVoice.length()<3&&outputVoice.length()>7){
            return false;
        }
        if(outputVoice.contains("도")){
           int indexDo=outputVoice.indexOf("도");
            try {
                Integer outputVoiceDegree = new Integer(outputVoice.substring((indexDo - 2), indexDo));
            }catch (Exception e){
                return false;
            }
        }else{
            if(outputVoice.contains("파워")||outputVoice.contains("따뜻")||outputVoice.contains("절전")){

            }else{
                return false;
            }
        }
        if((outputVoice.charAt(0)=='전')||(outputVoice.charAt(0)=='몸')||(outputVoice.charAt(0)=='8')||(outputVoice.charAt(0)=='팔')){
        }else{
            return false;
        }


        return true;

    }



    public void saveStates(){
        Integer part=new Integer(sendPartionString);
        Integer modeInt=new Integer(sendModeString);
        Integer temperatureInt=new Integer(sendModeOrTempString);

        if(part==ARM){
            savingClass.setCheckPartion(armAndBodySelected);
            savingClass.getArmSavings().setRecOrUserMode(modeInt);
            savingClass.getArmSavings().setModeOrTemp(temperatureInt);
        }else if(part==BODY){
            savingClass.setCheckPartion(armAndBodySelected);
            savingClass.getBodySavings().setRecOrUserMode(modeInt);
            savingClass.getBodySavings().setModeOrTemp(temperatureInt);
        }else if(part==UPPERBODY){
            savingClass.setCheckPartion(upperBodySelected);
            savingClass.getUpperbodySavings().setRecOrUserMode(modeInt);
            savingClass.getUpperbodySavings().setModeOrTemp(temperatureInt);

        }
    }

    public void showScreenDependOnPartion(){

       if(sendPartionString.equals("3")){
           int armRUMode=savingClass.getArmSavings().getRecOrUserMode();
           int armTEMode=savingClass.getArmSavings().getModeOrTemp();
           showModeChange(armRUMode);
           if(armRUMode==RECSelection){
//               recommendImageButton.performClick();
               showWhichRecommandSelected(armTEMode);
               makeUsrModeClear();
           }else{
               userImageButton.performClick();
               setUserControl(armTEMode);
//               makeRecModeClear();
           }

       }
        else if(sendPartionString.equals("4")){
           int bodyRUMode=savingClass.getBodySavings().getRecOrUserMode();
           int bodyTEMode=savingClass.getBodySavings().getModeOrTemp();
           showModeChange(bodyRUMode);
           if(bodyRUMode==RECSelection){
//               recommendImageButton.performClick();
               showWhichRecommandSelected(bodyTEMode);
               makeUsrModeClear();
           }else {
               userImageButton.performClick();
               setUserControl(bodyTEMode);
//               makeRecModeClear();
           }
       }
        else if(sendPartionString.equals("5")){
           int upperbodyRUMode=savingClass.getUpperbodySavings().getRecOrUserMode();
           int upperbodyTEMode=savingClass.getUpperbodySavings().getModeOrTemp();
           showModeChange(upperbodyRUMode);
           if(upperbodyRUMode==RECSelection){
//               recommendImageButton.performClick();
               showWhichRecommandSelected(upperbodyTEMode);
               makeUsrModeClear();
           }else{
               userImageButton.performClick();
               setUserControl(upperbodyTEMode);
//               makeRecModeClear();
           }
       }

    }//showsCreen


//    public void makeRecModeClear(){
//        recommendControlLayout.setBackgroundResource(R.drawable.recdefault);
//    }

    public void makeUsrModeClear(){
        tempSeekBar.setProgress(0);
        userTempTextView.setText("온도");
    }

    public void setUserControl(int tempState){
        int seekBarProgress=(tempState-20)*10/3;
        tempSeekBar.setProgress(seekBarProgress);
        userTempTextView.setText(tempState+"");
    }
//    public int checkingWhickMode(int checkingMode){
//        if((checkingMode==POWERMODE)||(checkingMode==WARMMODE)||(checkingMode==SAVEMODE)){
//            return RECSelection;
//        }else{
//            return USRSelection;
//        }
//    }
    public void firstShow(){
//        currentArmTempTextView.setText(savingClass.getCheckPartion()+"");
        if(savingClass.getCheckPartion()==armAndBodySelected){
            setSendPartionString(ARM);

        }else if(savingClass.getCheckPartion()==upperBodySelected){
            setSendPartionString(UPPERBODY);
        }

    }

}
