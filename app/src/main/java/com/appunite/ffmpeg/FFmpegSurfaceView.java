/*
 * FFmpegSurfaceView.java
 * Copyright (c) 2012 Jacek Marchwicki
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
 *
 */

package com.appunite.ffmpeg;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class FFmpegSurfaceView extends SurfaceView implements FFmpegDisplay,
		SurfaceHolder.Callback {

	public static enum ScaleType {
		CENTER_CROP, CENTER_INSIDE, FIT_XY
	}
    private String resolution = "0";
    static final private String TAG = "FFmpegSurfaceView";
	private FFmpegPlayer mMpegPlayer = null;
	private boolean mCreated = false;

	public FFmpegSurfaceView(Context context) {
		this(context, null, 0);
	}

	public FFmpegSurfaceView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FFmpegSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		SurfaceHolder holder = getHolder();
		holder.setFormat(PixelFormat.RGBA_8888);
		holder.addCallback(this);
	}

	@Override
	public void setMpegPlayer(FFmpegPlayer fFmpegPlayer, String resolution) {
		if (mMpegPlayer != null)
			throw new RuntimeException(
					"setMpegPlayer could not be called twice");
		this.mMpegPlayer = fFmpegPlayer;
        this.resolution = resolution;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d(TAG, "surfaceChanged: width:" + String.valueOf(width) + " ,height:" + String.valueOf(height));
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (mCreated  == true) {
			surfaceDestroyed(holder);
		}
		Surface surface = holder.getSurface();
        holder.setFixedSize(300, 300);
		mMpegPlayer.render(surface);
        Log.d(TAG, "surfaceCreated: ");
        mCreated = true;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		this.mMpegPlayer.renderFrameStop();
		mCreated = false;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float ratio = 1.34f; //VGA & QVGA
        if (resolution.compareTo("2") == 0 || resolution.compareTo("3") == 0){// 360p & 720p & 1080p
            ratio = 1.78f;
        }
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);

		float width = (MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight());
		float height = (MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom());
        Log.d(TAG, "before onMeasure: " + resolution + " width:" + String.valueOf(width) + " height: " + String.valueOf(height));
        if (height < (width/ratio)){
            width = height*ratio;
        }else{
            height = (width / ratio);
        }
        widthMeasureSpec = MeasureSpec.makeMeasureSpec((int)width, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec((int)height, MeasureSpec.EXACTLY);
//        getHolder().setFixedSize((int)width, (int)height);
        Log.d(TAG, "after  onMeasure: " + resolution + " width:" + String.valueOf(width) + " height: " + String.valueOf(height));
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

    public void setResolution(String resolution){
        this.resolution = resolution;
    }
}
