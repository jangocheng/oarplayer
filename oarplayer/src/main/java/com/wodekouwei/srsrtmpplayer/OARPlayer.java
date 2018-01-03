/*
 The MIT License (MIT)

Copyright (c) 2017-2020 oarplayer(qingkouwei)

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.wodekouwei.srsrtmpplayer;

import android.os.Build;
import android.os.PowerManager;
import android.view.Surface;

import java.io.IOException;

/**
 * Created by qingkouwei on 2017/12/15.
 */

public class OARPlayer {
    private final static String TAG = OARPlayer.class.getName();

    private PowerManager.WakeLock mWakeLock = null;
    private boolean mScreenOnWhilePlaying;
    private boolean mStayAwake;

    private String mDataSource;

    private static volatile boolean mIsNativeInitialized = false;
    private static void initNativeOnce() {
        synchronized (OARPlayer.class) {
            if (!mIsNativeInitialized) {
                native_init(Build.VERSION.SDK_INT, 44100);
                mIsNativeInitialized = true;
            }
        }
    }

    public OARPlayer() {
        initPlayer();
    }
    private void initPlayer() {
        System.loadLibrary("oarp-lib");
        initNativeOnce();
        /*
        Looper looper;
        if ((looper = Looper.myLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else if ((looper = Looper.getMainLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else {
            mEventHandler = null;
        }*/

        /*
         * Native setup requires a weak reference to our object. It's easier to
         * create it here than in C++.
         */
//        native_setup(new WeakReference<OARPlayer>(this));
    }
    private static native void native_init(int run_android_version, int best_samplerate);

    private native void _setDataSource(String path, String[] keys, String[] values)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    public native void _prepareAsync() throws IllegalStateException;
    private native void _start() throws IllegalStateException;

    public void setDataSource(String path)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mDataSource = path;
        _setDataSource(path, null, null);
    }

    public void prepareAsync() throws IllegalStateException {
        _prepareAsync();
    }
    public void start() throws IllegalStateException {
        stayAwake(true);
        _start();
    }

    public void stop() throws IllegalStateException {
        stayAwake(false);
        _stop();
    }
    public void setSurface(Surface surface){
        _setVideoSurface(surface);
    }

    private native void _stop() throws IllegalStateException;

    /*
         * Update the IjkMediaPlayer SurfaceTexture. Call after setting a new
         * display surface.
         */
    private native void _setVideoSurface(Surface surface);

    private void stayAwake(boolean awake) {
        if (mWakeLock != null) {
            if (awake && !mWakeLock.isHeld()) {
                mWakeLock.acquire();
            } else if (!awake && mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
        mStayAwake = awake;
        updateSurfaceScreenOn();
    }
    private void updateSurfaceScreenOn() {
        /*if (mSurfaceHolder != null) {
            mSurfaceHolder.setKeepScreenOn(mScreenOnWhilePlaying && mStayAwake);
        }*/
    }

    @SuppressWarnings("unused")
    void onPlayStatusChanged(int status) {

    }

    /**
     * native 调用的错误码回调
     *
     * @param error error code from native
     */
    void onPlayError(int error) {

    }
}
