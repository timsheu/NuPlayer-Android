package com.nuvoton.nuplayer;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.view.View.OnClickListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.Handler;
import android.widget.Toast;

import com.appunite.ffmpeg.FFmpegError;
import com.appunite.ffmpeg.FFmpegPlayer;
import com.appunite.ffmpeg.FFmpegListener;
import com.appunite.ffmpeg.FFmpegDisplay;
import com.appunite.ffmpeg.FFmpegStreamInfo;
import com.appunite.ffmpeg.FFmpegSurfaceView;
import com.appunite.ffmpeg.NotPlayingException;
import com.longevitysoft.android.xml.plist.domain.False;
import com.longevitysoft.android.xml.plist.domain.PList;
import com.longevitysoft.android.xml.plist.domain.PListObject;
import com.longevitysoft.android.xml.plist.domain.sString;
import com.nuvoton.socketmanager.ReadConfigure;
import com.nuvoton.socketmanager.SocketInterface;
import com.nuvoton.socketmanager.SocketManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class LiveFragment extends Fragment implements OnClickListener, OnSeekBarChangeListener, FFmpegListener, SocketInterface, View.OnTouchListener, TwoWayTalking.TwoWayTalkingInterface {
    private boolean isDuplex = true;
    private TwoWayTalking mTwoWayTalking;
    private boolean isRestart = false, isPolling = false, isRedDot = false, isRepeatCheck = false;
    private boolean isTCP = false;
    private Handler handler = new Handler();
    private static int counter = 0;
    private Timer redDotTimer, checkTimer, pollingTimer;
    private boolean flashOn = true;
    private String localURL;
    private SocketManager socketManager;
    private ReadConfigure configure;
    private int orientation;
    private String plarform, cameraSerial;
    private ProgressBar progressBar;
    private TextView onlineText;
    private ImageView redDot;
    private boolean isPlaying = false, isTracking = false;
    private int mCurrentTimeS;
    private View thisView;
    private FFmpegPlayer mMpegPlayer;
    private FFmpegSurfaceView mVideoView;
    private SeekBar seekBar;
    private ImageButton snapshotButton, playButton, expandButton, microPhoneButton;
    private int mAudioStreamNo = FFmpegPlayer.UNKNOWN_STREAM;
    private int mSubtitleStreamNo = FFmpegPlayer.NO_STREAM;
    private static final String TAG = "LiveFragment";

    private boolean isHide = false;
    OnHideBottomBarListener mCallback;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (isRestart){
            isRestart = false;
            if (mMpegPlayer != null){
                mMpegPlayer.stop();
            }
            repeatPolling(false);
            repeatRedDot(false);
            if (!isRepeatCheck) repeatCheck(true);
        }
        if (hidden){
            repeatPolling(false);
            repeatRedDot(false);
            repeatCheck(false);
            TwoWayTalking mTwoWay = TwoWayTalking.getInstance();
            mTwoWay.setInterface(this);
            mTwoWay.stopRecording();
            microPhoneButton.setImageResource(R.drawable.microphone_mute);
        }else{
            if (!isPolling) repeatPolling(true);
            if (!isRedDot) repeatRedDot(true);
        }
        isAudioDuplex();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.snapshotButton:
                Log.d(TAG, "onClick: snapshot");
                break;
            case R.id.playButton:
                Log.d(TAG, "onClick: play");
                setButtons(false);
                if (isPlaying == false){
                    isPlaying = true;
                    if (!isRepeatCheck) repeatCheck(true);
                }else {
                    isPlaying = false;
                    mMpegPlayer.stop();
                }
                break;
            case R.id.expandButton:
                Log.d(TAG, "onClick: expand");
                if (orientation == Configuration.ORIENTATION_LANDSCAPE){
                    orientation = Configuration.ORIENTATION_PORTRAIT;
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }else {
                    orientation = Configuration.ORIENTATION_LANDSCAPE;
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
                break;
            case R.id.microphoneButton:
                Log.d(TAG, "onClick: microphone");
                if (isDuplex){
                    mTwoWayTalking = TwoWayTalking.getInstance();
                    mTwoWayTalking.setInterface(this);

                    if (mTwoWayTalking.isRecording){
                        mTwoWayTalking.stopRecording();
                        microPhoneButton.setImageResource(R.drawable.microphone_mute);
                    }else{
                        mTwoWayTalking.startRecording();
                        mTwoWayTalking.pokeClient(getDeviceURL(), "tcp");
                        microPhoneButton.setImageResource(R.drawable.microphone);
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Log.d(TAG, "onProgressChanged:");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isTracking = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isTracking = false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch ( event.getAction() ) {
            case MotionEvent.ACTION_DOWN:
                if (!isDuplex){
                    mTwoWayTalking = TwoWayTalking.getInstance();
                    mTwoWayTalking.setInterface(this);

                    if (!mTwoWayTalking.isRecording){
                        mTwoWayTalking.startRecording();
                        mTwoWayTalking.pokeClient(getDeviceURL(), "tcp");
                        microPhoneButton.setImageResource(R.drawable.microphone);
                    }
                }
                Log.d(TAG, "onTouch: down");
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "onTouch: up");
                if (!isDuplex){
                    mTwoWayTalking = TwoWayTalking.getInstance();
                    mTwoWayTalking.setInterface(this);

                    if (mTwoWayTalking.isRecording){
                        mTwoWayTalking.stopRecording();
                        microPhoneButton.setImageResource(R.drawable.microphone_mute);
                    }
                }
                break;
        }
        return true;
    }

    @Override
    public void showToast(final String message) {
        if (isAdded()){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    public interface OnHideBottomBarListener
    {
        public void onHideBottomBar(boolean isHide);
    }


    public LiveFragment() {
        // Required empty public constructor
    }

    public static LiveFragment newInstance(Bundle b){
        LiveFragment fragment = new LiveFragment();
        fragment.setArguments(b);
        return fragment;
    }

    public void registerUI(){
        snapshotButton = (ImageButton) thisView.findViewById(R.id.snapshotButton);
        snapshotButton.setOnClickListener(this);
        snapshotButton.setEnabled(false);

        playButton = (ImageButton) thisView.findViewById(R.id.playButton);
        playButton.setOnClickListener(this);
        playButton.setEnabled(false);

        expandButton = (ImageButton) thisView.findViewById(R.id.expandButton);
        expandButton.setOnClickListener(this);
        expandButton.setEnabled(false);

        seekBar = (SeekBar) thisView.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setEnabled(false);

        microPhoneButton = (ImageButton) thisView.findViewById(R.id.microphoneButton);

        microPhoneButton.setOnClickListener(this);
        microPhoneButton.setOnTouchListener(this);
        microPhoneButton.setEnabled(false);

        onlineText = (TextView) thisView.findViewById(R.id.onlineText);
        progressBar = (ProgressBar) thisView.findViewById(R.id.progressBar);
        redDot = (ImageView) thisView.findViewById(R.id.redDot);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        thisView = inflater.inflate(R.layout.fragment_live, container, false);
        registerUI();
        determineOrientation();
        if (socketManager == null){
            socketManager = new SocketManager();
        }
        socketManager.setSocketInterface(this);
        // Inflate the layout for this fragment
        return thisView;
    }

    @Override
    public void onPause() {
        super.onPause();
        repeatCheck(false);
        repeatRedDot(false);
        repeatPolling(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        mVideoView = (FFmpegSurfaceView) getActivity().findViewById(R.id.videoView);
        mVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "onTouch: ");
                if (!isHide){
                    isHide = true;
                }else {
                    isHide = false;
                }
                mCallback.onHideBottomBar(isHide);
                return false;
            }
        });
        mMpegPlayer = new FFmpegPlayer((FFmpegDisplay) mVideoView, this);
        configure = ReadConfigure.getInstance(getActivity().getApplicationContext(), false);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged: live fragment");
        super.onConfigurationChanged(newConfig);
    }

    private void populateViewForOrientation(LayoutInflater inflater, ViewGroup viewGroup) {
        viewGroup.removeAllViewsInLayout();
        View subview = inflater.inflate(R.layout.fragment_live, viewGroup);

        // Find your buttons in subview, set up onclicks, set up callbacks to your parent fragment or activity here.
        // You can create ViewHolder or separate method for that.
        // example of accessing views: TextView textViewExample = (TextView) view.findViewById(R.id.text_view_example);
        // textViewExample.setText("example");
    }

    @Override
    public void onStart() {
        super.onStart();
        try{
            mCallback = (OnHideBottomBarListener) getActivity();
        }catch (ClassCastException e){
            throw new ClassCastException(getActivity().toString() + " must implement onHideBottomBarListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle bundle = getArguments();
        if (bundle != null){
            plarform = getArguments().getString("Platform");
            cameraSerial = getArguments().getString("CameraSerial");
        }
        if (!isRepeatCheck) repeatCheck(true);
        isAudioDuplex();
    }

    public void determineOrientation(){
        orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE){
            isHide = true;
        }else {
            isHide = false;
        }
    }
    private class TimerSetDataSource extends TimerTask{
        public void run(){
            Log.d(TAG, "run: timer set data source");
            sendCheckStorage();
        }
    }

    Runnable timerSetRedDot = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "run: timer set red dot handler" + String.valueOf(flashOn));
            if (flashOn == true){
                redDot.setImageResource(R.drawable.recordflashoff);
                flashOn = false;
            }else {
                redDot.setImageResource(R.drawable.recordflashon);
                flashOn = true;
            }
            handler.postDelayed(this, 1000);
        }
    };

    private class TimerSetRedDot extends TimerTask{
        public void run(){
            Log.d(TAG, "run: timer set red dot" + String.valueOf(flashOn));
            if (flashOn == true){
                redDot.setImageResource(R.drawable.recordflashoff);
                flashOn = false;
            }else {
                redDot.setImageResource(R.drawable.recordflashon);
                flashOn = true;
            }
        }
    }

    private class TimerPollingCheck extends TimerTask{
        public void run(){
            Log.d(TAG, "run: timer polling check " + String.valueOf(counter));
            if (counter >= 5){
//                onlineText.setText(R.string.offline);
                repeatCheck(true);
                repeatRedDot(false);
                repeatPolling(false);
            }
            counter++;
        }
    }

    private void repeatCheck(boolean option){
        Log.d(TAG, "repeatCheck: " + String.valueOf(option));
        isRepeatCheck = option;
        if (option == true){
            checkTimer = new Timer(true);
            checkTimer.schedule(new TimerSetDataSource(), 0, 5000);
        }else {
            if (checkTimer != null){
                checkTimer.cancel();
            }

        }
    }

    private void repeatRedDot(boolean option){
        Log.d(TAG, "repeatRedDot: " + String.valueOf(option));
        isRedDot = option;
        if (option == true){
            handler.post(timerSetRedDot);
//            redDotTimer = new Timer(true);
//            redDotTimer.schedule(new TimerSetRedDot(), 0, 1000);
        }else if (option == false){
//            redDotTimer.cancel();
            handler.removeCallbacks(timerSetRedDot);
        }
    }

    private void repeatPolling(boolean option){
        Log.d(TAG, "repeatPolling: " + String.valueOf(option));
        isPolling = option;
        if (option == true){
            pollingTimer = new Timer(true);
            pollingTimer.schedule(new TimerPollingCheck(), 0, 10000);
        }else {
            if (pollingTimer != null){
                pollingTimer.cancel();
            }
        }
    }


    private void setDataSource() {
        String cameraName = "Setup Camera " + cameraSerial;
        SharedPreferences preference = getActivity().getSharedPreferences(cameraName, Context.MODE_PRIVATE);
        String resolution = preference.getString("Resolution", "0");
        mVideoView.setResolution(resolution);

        progressBar.post(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        HashMap<String, String> params = new HashMap<String, String>();
        // set font for ass
        File assFont = new File(Environment.getExternalStorageDirectory(),
                "DroidSansFallback.ttf");
        params.put("ass_default_font_path", assFont.getAbsolutePath());
        params.put("probesize", "5120");
        params.put("max_delay", "0");
        params.put("fflags", "nobuffer");
        params.put("flush_packets", "1");
        if (isTCP){
            params.put("rtsp_transport", "tcp");
        }
        mMpegPlayer.setMpegListener(this);
        mMpegPlayer.setDataSource(localURL, params, FFmpegPlayer.UNKNOWN_STREAM, mAudioStreamNo,
                mSubtitleStreamNo, resolution);
    }

    // FFMPEG interface implementation

    public void onFFDataSourceLoaded(FFmpegError err, FFmpegStreamInfo[] streams){
        if (err != null){
            String format = "Could not open stream";
            Log.d(TAG, "onFFDataSourceLoaded: " + format);
            progressBar.setVisibility(View.VISIBLE);
            onlineText.setText(R.string.offline);
            onlineText.setTextColor(0xFFFFFF);
        }
        Log.d(TAG, "onFFDataSourceLoaded: loaded");
        progressBar.setVisibility(View.GONE);
//        mMpegPlayer.pause();
        mMpegPlayer.resume();
        repeatCheck(false);
    }

    public void onFFResume(NotPlayingException result){
        isPlaying = true;
        Log.d(TAG, "onFFResume: ");
        playButton.setImageResource(R.drawable.pause);
        playButton.setEnabled(true);
        if (!isRedDot) repeatRedDot(true);
    }

    public void onFFPause(NotPlayingException err){
        isPlaying = false;
        Log.d(TAG, "onFFPause: ");
        playButton.setImageResource(R.drawable.play);
        repeatRedDot(false);
    }

    public void onFFStop(){
        isPlaying = false;
        Log.d(TAG, "onFFStop: ");
        playButton.setImageResource(R.drawable.play);
        repeatRedDot(false);
        setButtons(true);
    }

    public void onFFUpdateTime(long currentTimeUs, long videoDurationUs, boolean isFinished){
        Log.d(TAG, "onFFUpdateTime: ");
        counter = 0;
        if ( isTracking == false){
            mCurrentTimeS = (int)(currentTimeUs / 1000000);
            int videoDurationS = (int)(videoDurationUs / 1000000);
            seekBar.setMax(videoDurationS);
            seekBar.setProgress(mCurrentTimeS);
        }
        if (isFinished == true){
            playButton.setImageResource(R.drawable.play);
            isPlaying = false;
        }
    }

    public void onFFSeeked(NotPlayingException result){
        Log.d(TAG, "onFFSeeked: ");
    }

    //socket manager delegate
    @Override
    public void showToastMessage(String message) {

    }

    @Override
    public void updateFileList(ArrayList<FileContent> fileList) {

    }

    @Override
    public void deviceIsAlive() {
        onlineText.setText(R.string.online);
//        onlineText.setTextColor(0x000000);
        repeatCheck(false);
        repeatPolling(true);
        setDataSource();
        setButtons(true);
    }

    @Override
    public void updateSettingContent(String category, String value) {

    }

    private String getDeviceURL(){
        String cameraName = "Setup Camera " + cameraSerial;
        SharedPreferences preference = getActivity().getSharedPreferences(cameraName, Context.MODE_PRIVATE);
        String urlString = preference.getString("URL", "DEFAULT");
        isTCP = preference.getBoolean("Transmission", false);
        localURL = new String(urlString);
        String [] ipCut = urlString.split("/");
        String ip = ipCut[2];
        String port = preference.getString("Camera Port", "80");
        String url = "http://" + ip + ":" + port +"/";
        return url;
    }

    private void sendCheckStorage(){
        String command = getDeviceURL();
        sString baseCommand, action;
        ArrayList<Map> fileCommandSet = configure.infoCommandSet;
        Map<String, PListObject> targetCommand = fileCommandSet.get(0);
        baseCommand = (sString) targetCommand.get("Base Command");
        action = (sString) targetCommand.get("Sub Command");
        command = command + baseCommand.getValue() + "?" + action.getValue();
        if (socketManager != null){
            socketManager.executeSendGetTask(command, SocketManager.CMDCHECK_STORAGE);
        }
    }

    public void restartStream(){
        isRestart = true;
        if (mMpegPlayer != null) {
            mMpegPlayer.stop();
        }
    }

    private void setButtons(boolean enable){
        playButton.setEnabled(enable);
        snapshotButton.setEnabled(enable);
        expandButton.setEnabled(enable);
        microPhoneButton.setEnabled(enable);
    }
    private void isAudioDuplex(){
        String cameraName = "Setup Camera " + cameraSerial;
        SharedPreferences preference = getActivity().getSharedPreferences(cameraName, Context.MODE_PRIVATE);
        isDuplex = preference.getBoolean("Audio Duplex", true);
        if (isDuplex){
            microPhoneButton.setOnClickListener(this);
            microPhoneButton.setOnTouchListener(null);
        }else {
            microPhoneButton.setOnClickListener(null);
            microPhoneButton.setOnTouchListener(this);
        }
    }

    public void setResolution(String resolution){
        mVideoView.setResolution(resolution);
    }

}

