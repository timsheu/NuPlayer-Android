package com.nuvoton.nuplayer;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
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

public class FilePlayActivity extends AppCompatActivity implements FFmpegListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener{
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_play);
        mVideoView = (SurfaceView) this.findViewById(R.id.fileVideoView);
        mMpegPlayer = new FFmpegPlayer((FFmpegDisplay) mVideoView, this);
        configure = ReadConfigure.getInstance(this);
        registerUI();
        determineOrientation();
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
    @Override
    public void onFFDataSourceLoaded(FFmpegError err, FFmpegStreamInfo[] streams) {

    }

    @Override
    public void onFFResume(NotPlayingException result) {

    }

    @Override
    public void onFFPause(NotPlayingException err) {

    }

    @Override
    public void onFFStop() {

    }

    @Override
    public void onFFUpdateTime(long mCurrentTimeUs, long mVideoDurationUs, boolean isFinished) {

    }

    @Override
    public void onFFSeeked(NotPlayingException result) {

    }

    //button delegate
    @Override
    public void onClick(View v) {

    }

    //seek bar delegate
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void determineOrientation(){
        orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE){
            isHide = true;
        }else {
            isHide = false;
        }
    }
}
