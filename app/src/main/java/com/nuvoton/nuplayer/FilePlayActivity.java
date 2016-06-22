package com.nuvoton.nuplayer;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.nuvoton.socketmanager.ReadConfigure;
import com.nuvoton.socketmanager.SocketManager;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class FilePlayActivity extends AppCompatActivity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, IVLCVout.Callback, MediaPlayer.EventListener, SurfaceHolder.Callback{
    private int seekTime, totalTime, currentTime;
    private Timer pollingTimer;
    private int counter = 0;
    private boolean isPlaying = false, isTracked = false, isEnded = false;
    private String fileURL;
    private static String TAG = "FilePlayActivity";
    private boolean isHide = false;
    private LibVLC mLibVLC;
    private Media mMedia;
    private SurfaceView mVideoView;
    private MediaPlayer mMediaPlayer;
    private SurfaceHolder mSurfaceHolder;
    private int mVideoHeight, mVideoWidth, mVideoVisibleHeight, mVideoVisibleWidth, mSarNum, mSarDen;
    private SeekBar seekBar;
    private ImageButton snapshotButton, playButton, expandButton;
    private SocketManager socketManager;
    private ReadConfigure configure;
    private int orientation;
    private String plarform, cameraSerial;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_play);
        configure = ReadConfigure.getInstance(getApplicationContext());
        registerUI();
        determineOrientation();
        Log.d(TAG, "onCreate: " + fileURL);
        setDataSource();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    public void registerUI(){
        mVideoView = (SurfaceView) this.findViewById(R.id.fileVideoView);
        mSurfaceHolder = mVideoView.getHolder();
        mSurfaceHolder.setFormat(PixelFormat.RGBX_8888);
        mSurfaceHolder.addCallback(this);
        fileURL = getIntent().getStringExtra("FileURL");
//        fileURL = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov";

        playButton = (ImageButton) this.findViewById(R.id.filePlayButton);
        playButton.setOnClickListener(this);
        playButton.setEnabled(false);

        expandButton = (ImageButton) this.findViewById(R.id.fileExpandButton);
        expandButton.setOnClickListener(this);
        expandButton.setEnabled(false);

        seekBar = (SeekBar) this.findViewById(R.id.fileSeekBar);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setEnabled(false);

        progressBar = (ProgressBar) this.findViewById(R.id.fileProgressBar);
    }

    //button delegate
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.filePlayButton:
                Log.d(TAG, "onClick: play");
                playButton.setEnabled(false);
                if (mMediaPlayer.isPlaying() == false){
                    isPlaying = mMediaPlayer.isPlaying();
                    setDataSource();
                }else {
                    isPlaying = false;
                    mMediaPlayer.pause();
                }
                break;
            case R.id.fileExpandButton:
                Log.d(TAG, "onClick: expand");
                if (orientation == Configuration.ORIENTATION_LANDSCAPE){
                    orientation = Configuration.ORIENTATION_PORTRAIT;
                    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }else {
                    orientation = Configuration.ORIENTATION_LANDSCAPE;
                    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
                break;
            default:
                break;
        }

    }

    //seek bar delegate
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        playButton.setEnabled(false);
        expandButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        mMediaPlayer.pause();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        this.seekBar.setEnabled(false);
        playButton.setEnabled(false);
        expandButton.setEnabled(false);
        isTracked = true;
        seekTime = seekBar.getProgress() * 1000;
        mMediaPlayer.setTime((long)seekTime);
        if (isEnded == true){
            setDataSource();
        }else {
            mMediaPlayer.play();
        }
    }

    public void determineOrientation(){
        orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE){
//            mVideoView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            isHide = true;
        }else {
//            mVideoView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 240));
            isHide = false;
        }
    }

    private void setDataSource() {
        expandButton.setEnabled(false);
        playButton.setEnabled(false);
        progressBar.post(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
        createPlayer(fileURL);
    }

    private void createPlayer(String media){
        releasePlayer();
        try {
            ArrayList<String> options = new ArrayList<>();
            options.add("-vvv");
            options.add("--rtsp-tcp");
            mLibVLC = new LibVLC(options);
            mSurfaceHolder = mVideoView.getHolder();
            mSurfaceHolder.setKeepScreenOn(true);

            mMediaPlayer = new MediaPlayer(mLibVLC);
            mMediaPlayer.setEventListener(this);

            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.setVideoView(mVideoView);
            vout.addCallback(this);
            vout.attachViews();

            mMedia = new Media(mLibVLC, Uri.parse(media));
            mMediaPlayer.setMedia(mMedia);
            mMediaPlayer.play();

        }catch (Exception e){
            Toast.makeText(this, "Error creating player!", Toast.LENGTH_LONG).show();
        }
    }

    private void releasePlayer(){
        if (mLibVLC == null){
            return;
        }
        mMediaPlayer.stop();
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        vout.removeCallback(this);
        vout.detachViews();
        mSurfaceHolder = null;
        mLibVLC.release();
        mLibVLC = null;
        mVideoHeight = 0;
        mVideoWidth = 0;
    }

    @Override
    public void onNewLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        if (width*height == 0) return;

        mVideoWidth = width;
        mVideoHeight = height;
        setSize(mVideoWidth, mVideoHeight);
    }

    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {

    }

    @Override
    public void onHardwareAccelerationError(IVLCVout vlcVout) {

    }

    @Override
    public void onEvent(MediaPlayer.Event event) {
        Log.d(TAG, "onEvent: " + String.valueOf(event.type));
        switch (event.type){
            case MediaPlayer.Event.EndReached:
                Log.d(TAG, "onEvent: end reached");
                playButton.setImageResource(R.drawable.play);
                isEnded = true;
                break;
            case MediaPlayer.Event.Playing:
                isEnded = false;
                if (mMediaPlayer.isSeekable() == false){
                    seekBar.setEnabled(false);
                }
                playButton.setEnabled(true);
                expandButton.setEnabled(true);
                seekBar.setEnabled(true);
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                totalTime = (int)mMediaPlayer.getLength() / 1000;
                seekBar.setMax(totalTime);
                playButton.setImageResource(R.drawable.pause);
                break;
            case MediaPlayer.Event.Paused:
                playButton.setImageResource(R.drawable.play);
                Log.d(TAG, "onEvent: paused");
                break;
            case MediaPlayer.Event.Stopped:
                playButton.setImageResource(R.drawable.play);
                Log.d(TAG, "onEvent: stopped");
                break;
            case MediaPlayer.Event.TimeChanged:
                Log.d(TAG, "onEvent: time changed " + String.valueOf(mMediaPlayer.getTime()));
                currentTime = (int) mMediaPlayer.getTime() / 1000;
                seekBar.setProgress(currentTime);
                if (isTracked){
                    mMediaPlayer.setTime((long)seekTime);
                    Log.d(TAG, "onEvent: tracked");
                    isTracked = false;
                }
                break;
            default:
                break;
        }
    }

    private void setSize(int width, int height){
        mVideoHeight = height;
        mVideoWidth = width;
        if (mVideoWidth * mVideoHeight <= -1) return;
        if (mSurfaceHolder == null || mVideoView == null) return;

        int w = getWindow().getDecorView().getWidth();
        int h = getWindow().getDecorView().getHeight();

        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (w>h && isPortrait || w<h && !isPortrait){
            int i=w;
            w=h;
            h=i;
        }

        float videoAR = (float) mVideoWidth / (float) mVideoHeight;
        float screenAR = (float) w / (float) h;

        if (screenAR < videoAR){
            h = (int) (w/videoAR);
        }else{
            w = (int) (h*videoAR);
        }
        mSurfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);

        ViewGroup.LayoutParams lp = mVideoView.getLayoutParams();
        lp.width = w;
        lp.height = h;
        mVideoView.setLayoutParams(lp);
        mVideoView.invalidate();


    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private class TimerPollingCheck extends TimerTask {
        public void run(){
            Log.d(TAG, "run: timer polling check " + String.valueOf(counter));
            if (counter >= 5){
                repeatPolling(false);
            }
            counter++;
        }
    }

    private void repeatPolling(boolean option){
        Log.d(TAG, "repeatPolling: " + String.valueOf(option));
        if (option == true){
            pollingTimer = new Timer(true);
            pollingTimer.schedule(new TimerPollingCheck(), 0, 10000);
        }else {
            if (pollingTimer != null){
                pollingTimer.cancel();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged: live fragment");
        super.onConfigurationChanged(newConfig);
        setSize(mVideoWidth, mVideoHeight);
    }

}
