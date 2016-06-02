package com.nuvoton.skyeye;


import android.app.Activity;
import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.view.View.OnClickListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ProgressBar;

import com.appunite.ffmpeg.FFmpegError;
import com.appunite.ffmpeg.FFmpegPlayer;
import com.appunite.ffmpeg.FFmpegListener;
import com.appunite.ffmpeg.FFmpegDisplay;
import com.appunite.ffmpeg.FFmpegStreamInfo;
import com.appunite.ffmpeg.NotPlayingException;

import java.io.File;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class LiveFragment extends Fragment implements OnClickListener, OnSeekBarChangeListener, FFmpegListener{
    private Timer checkAliveTimer;
    private int orientation;
    private String cameraName = "DVR";
    private ProgressBar progressBar;
    private boolean isPlaying = false, isTracking = false;
    private int mCurrentTimeS;
    private View thisView;
    private FFmpegPlayer mMpegPlayer;
    private SurfaceView mVideoView;
    private SeekBar seekBar;
    private ImageButton snapshotButton, playButton, expandButton;
    private int mAudioStreamNo = FFmpegPlayer.UNKNOWN_STREAM;
    private int mSubtitleStreamNo = FFmpegPlayer.NO_STREAM;
    private static final String TAG = "ffmpegAndroid";

    private boolean isHide = false;
    OnHideBottomBarListener mCallback;

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.snapshotButton:
                Log.d(TAG, "onClick: snapshot");
                break;
            case R.id.playButton:
                Log.d(TAG, "onClick: play");
                playButton.setEnabled(false);
                if (isPlaying == false){
                    isPlaying = true;
                    mMpegPlayer.resume();
                }else {
                    isPlaying = false;
                    mMpegPlayer.pause();
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

    public interface OnHideBottomBarListener{
        public void onHideBottomBar(boolean isHide);
    }


    public LiveFragment() {
        // Required empty public constructor
    }

    public LiveFragment newInstance(){
        LiveFragment fragment = new LiveFragment();
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

        progressBar = (ProgressBar) thisView.findViewById(R.id.progressBar);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        thisView = inflater.inflate(R.layout.fragment_live, container, false);
        registerUI();
        determineOrientation();

        // Inflate the layout for this fragment

        return thisView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        mVideoView = (SurfaceView) getActivity().findViewById(R.id.videoView);
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
        setDataSource();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged: live fragment");
        super.onConfigurationChanged(newConfig);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        populateViewForOrientation(inflater, (ViewGroup) getView());
        registerUI();
        determineOrientation();
        mVideoView = (SurfaceView) getView().findViewById(R.id.videoView);
        mVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
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
        setDataSource();
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
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try{
            mCallback = (OnHideBottomBarListener) activity;
        }catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + " must implement onHideBottomBarListener");
        }
    }

    public void determineOrientation(){
        orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE){
            isHide = true;
        }else {
            isHide = false;
        }
    }

    public class TimerSetDataSource extends TimerTask{
        public void run(){
            setDataSource();
        }
    };

    private void setDataSource() {
        progressBar.setVisibility(View.VISIBLE);

        HashMap<String, String> params = new HashMap<String, String>();
        // set font for ass
        File assFont = new File(Environment.getExternalStorageDirectory(),
                "DroidSansFallback.ttf");
        params.put("ass_default_font_path", assFont.getAbsolutePath());
        params.put("fflags", "nobuffer");
        params.put("probesize", "5120");
        String url="rtsp://192.168.100.1/cam1/h264";
        mMpegPlayer.setDataSource(url, params, FFmpegPlayer.UNKNOWN_STREAM, mAudioStreamNo,
                mSubtitleStreamNo);
        mMpegPlayer.setMpegListener(this);
        mMpegPlayer.pause();
        mMpegPlayer.resume();
    }

    // FFMPEG interface implementation

    public void onFFDataSourceLoaded(FFmpegError err, FFmpegStreamInfo[] streams){
        checkAliveTimer.cancel();
        checkAliveTimer = new Timer();
        if (err != null){
            String format = "Could not open stream";
            Log.d(TAG, "onFFDataSourceLoaded: " + format);
            progressBar.setVisibility(View.VISIBLE);
            checkAliveTimer.schedule(new TimerSetDataSource(), 5000);
        }
        Log.d(TAG, "onFFDataSourceLoaded: loaded");
        progressBar.setVisibility(View.GONE);
    }

    public void onFFResume(NotPlayingException result){
        isPlaying = true;
        Log.d(TAG, "onFFResume: ");
        playButton.setImageResource(R.drawable.pause);
        playButton.setEnabled(true);
    }

    public void onFFPause(NotPlayingException err){
        isPlaying = false;
        Log.d(TAG, "onFFPause: ");
        playButton.setImageResource(R.drawable.play);
        playButton.setEnabled(true);
    }

    public void onFFStop(){
        isPlaying = false;
        Log.d(TAG, "onFFStop: ");
        playButton.setImageResource(R.drawable.play);
        playButton.setEnabled(true);
    }

    public void onFFUpdateTime(long currentTimeUs, long videoDurationUs, boolean isFinished){
        Log.d(TAG, "onFFUpdateTime: ");
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

}

