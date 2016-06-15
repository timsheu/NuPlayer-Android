package com.nuvoton.nuplayer;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.appunite.ffmpeg.FFmpegDisplay;
import com.appunite.ffmpeg.FFmpegError;
import com.appunite.ffmpeg.FFmpegListener;
import com.appunite.ffmpeg.FFmpegPlayer;
import com.appunite.ffmpeg.FFmpegStreamInfo;
import com.appunite.ffmpeg.NotPlayingException;
import com.nuvoton.socketmanager.ReadConfigure;
import com.nuvoton.socketmanager.SocketManager;

import java.io.File;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class FilePlayActivity extends AppCompatActivity implements FFmpegListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener{
    private int mCurrentTimeS, mSeekTimeS;
    private Timer pollingTimer;
    private int counter = 0;
    private boolean isPlaying = false, isTracking = false, isTracked = false;
    private String fileURL;
    private static String TAG = "FilePlayActivity";
    private boolean isHide = false;
    private FFmpegPlayer mMpegPlayer;
    private SurfaceView mVideoView;
    private SeekBar seekBar;
    private ImageButton snapshotButton, playButton, expandButton;
    private SocketManager socketManager;
    private ReadConfigure configure;
    private int orientation;
    private String plarform, cameraSerial;
    private ProgressBar progressBar;
    private int mAudioStreamNo = FFmpegPlayer.UNKNOWN_STREAM;
    private int mSubtitleStreamNo = FFmpegPlayer.NO_STREAM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_play);
        mVideoView = (SurfaceView) this.findViewById(R.id.fileVideoView);
        mMpegPlayer = new FFmpegPlayer((FFmpegDisplay) mVideoView, this);
        configure = ReadConfigure.getInstance(this);
        registerUI();
        determineOrientation();
        fileURL = getIntent().getStringExtra("FileURL");
        Log.d(TAG, "onCreate: " + fileURL);
        setDataSource();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMpegPlayer.stop();
    }

    public void registerUI(){
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

    //FFMPEG delegate
    public void onFFDataSourceLoaded(FFmpegError err, FFmpegStreamInfo[] streams){
        if (err != null){
            String format = "Could not open stream";
            Log.d(TAG, "onFFDataSourceLoaded: " + format);
            progressBar.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "onFFDataSourceLoaded: loaded");
        progressBar.setVisibility(View.GONE);
        seekBar.setEnabled(true);
        mMpegPlayer.pause();
        mMpegPlayer.resume();
    }

    public void onFFResume(NotPlayingException result){
        isPlaying = true;
        Log.d(TAG, "onFFResume: ");
        playButton.setImageResource(R.drawable.pause);
        expandButton.setEnabled(true);
        playButton.setEnabled(true);
        seekBar.setEnabled(true);
    }

    public void onFFPause(NotPlayingException err){
        isPlaying = false;
        Log.d(TAG, "onFFPause: ");
        playButton.setImageResource(R.drawable.play);
        expandButton.setEnabled(true);
        playButton.setEnabled(true);
    }

    public void onFFStop(){
        isPlaying = false;
        Log.d(TAG, "onFFStop: ");
        playButton.setImageResource(R.drawable.play);
        playButton.setEnabled(true);
    }

    public void onFFUpdateTime(long currentTimeUs, long videoDurationUs, boolean isFinished){
        counter = 0;
        if (isTracking == false){
            mCurrentTimeS = (int)(currentTimeUs / 1000000);
            int videoDurationS = (int)(videoDurationUs / 1000000);
            seekBar.setMax(videoDurationS);
            if (isTracked == false){
                seekBar.setProgress(mCurrentTimeS);
            }else if (isTracked == true){
                mCurrentTimeS = mSeekTimeS;
                seekBar.setProgress(mSeekTimeS);
                isTracked = false;
            }
        }
        if (isFinished == true){
            playButton.setImageResource(R.drawable.play);
            isPlaying = false;
        }
        Log.d(TAG, "onFFUpdateTime: " + String.valueOf(currentTimeUs) + "max: " + String.valueOf(videoDurationUs));
    }

    public void onFFSeeked(NotPlayingException result){
        isTracked = true;
        Log.d(TAG, "onFFSeeked: " + String.valueOf(mSeekTimeS));
        seekBar.setEnabled(false);
        playButton.setEnabled(false);
        expandButton.setEnabled(false);
        seekBar.setProgress(mSeekTimeS);
        mMpegPlayer.resume();
    }

    //button delegate
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.filePlayButton:
                Log.d(TAG, "onClick: play");
                playButton.setEnabled(false);
                if (isPlaying == false){
                    setDataSource();
                }else {
                    isPlaying = false;
                    mMpegPlayer.pause();
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
        mMpegPlayer.pause();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        this.seekBar.setEnabled(false);
        playButton.setEnabled(false);
        expandButton.setEnabled(false);
        mSeekTimeS = seekBar.getProgress();
        long mSeekTimeUS = mSeekTimeS * 1000000;
        mMpegPlayer.seek(mSeekTimeUS);
        Log.d(TAG, "onStopTrackingTouch: " + String.valueOf(mSeekTimeUS));
        isTracked = true;
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

        HashMap<String, String> params = new HashMap<String, String>();
        // set font for ass
        File assFont = new File(Environment.getExternalStorageDirectory(),
                "DroidSansFallback.ttf");
        params.put("ass_default_font_path", assFont.getAbsolutePath());
        params.put("fflags", "nobuffer");
        params.put("probesize", "5120");
        params.put("flush_packets", "1");
        mMpegPlayer.setMpegListener(this);

//                mMpegPlayer.setDataSource("rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov", params, FFmpegPlayer.UNKNOWN_STREAM, mAudioStreamNo,
//                mSubtitleStreamNo);

        mMpegPlayer.setDataSource(fileURL, params, FFmpegPlayer.UNKNOWN_STREAM, mAudioStreamNo,
                mSubtitleStreamNo);
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
        switch (newConfig.orientation){
            case Configuration.ORIENTATION_LANDSCAPE:
//                mVideoView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                break;
            case Configuration.ORIENTATION_PORTRAIT:
//                mVideoView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 240));
                break;
        }
    }

}
