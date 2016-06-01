package com.nuvoton.skyeye;


import android.app.Activity;
import android.app.Fragment;
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
import android.widget.VideoView;

import com.appunite.ffmpeg.FFmpegPlayer;
import com.appunite.ffmpeg.FFmpegListener;
import com.appunite.ffmpeg.FFmpegDisplay;

import java.io.File;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class LiveFragment extends Fragment {
    private FFmpegPlayer mMpegPlayer;
    private SurfaceView mVideoView;

    private int mAudioStreamNo = FFmpegPlayer.UNKNOWN_STREAM;
    private int mSubtitleStreamNo = FFmpegPlayer.NO_STREAM;
    private static final String TAG = "ffmpegAndroid";


    private boolean isHide = false;
    OnHideBottomBarListener mCallback;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live, container, false);
        determineOrientation();
        ImageButton button = (ImageButton) view.findViewById(R.id.snapshotButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ");
            }
        });
        // Inflate the layout for this fragment



        return view;
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
        super.onConfigurationChanged(newConfig);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        populateViewForOrientation(inflater, (ViewGroup) getView());
        Log.d(TAG, "onConfigurationChanged: live fragment");
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
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE){
            isHide = true;
        }else {
            isHide = false;
        }
    }

    private void setDataSource() {
        HashMap<String, String> params = new HashMap<String, String>();

        // set font for ass
        File assFont = new File(Environment.getExternalStorageDirectory(),
                "DroidSansFallback.ttf");
        params.put("ass_default_font_path", assFont.getAbsolutePath());
        params.put("fflags", "nobuffer");
        params.put("probesize", "5120");
        //naInit("rtsp://192.168.100.1/cam1/mpeg4");
        //naInit("/storage/sdcard1/1.mp4");
        String url="rtsp://192.168.100.1/cam1/h264";
//        String url = "rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov";
        mMpegPlayer.setDataSource(url, params, FFmpegPlayer.UNKNOWN_STREAM, mAudioStreamNo,
                mSubtitleStreamNo);
        mMpegPlayer.pause();
        mMpegPlayer.resume();
    }

}
