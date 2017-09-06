package com.example.howard.myapplication.eventloop;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

import com.example.howard.myapplication.LogUtils;

import java.io.IOException;

/**
 * Created by howard on 17-9-5.
 */
public class VideoPlayerView extends TextureView implements TextureView.SurfaceTextureListener {

    private MediaPlayer mMediaPlayer;
    private Context mContext;


    public VideoPlayerView(Context context) {
        super(context);
        this.mContext = context;
        initView();
    }

    public VideoPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initView();
    }

    public VideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();
    }

    public void stopPlay() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void playFromRaw(AssetFileDescriptor raw) {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();

            }
            mMediaPlayer.setSurface(mSurface);
            try {
                mMediaPlayer.setDataSource(raw.getFileDescriptor());
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMediaPlayer.setLooping(true);
            mMediaPlayer.start();
        }
//        try {
//            mMediaPlayer.setDataSource(raw.getFileDescriptor());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void playFromResId(int id){
        try {
            mMediaPlayer = MediaPlayer.create(mContext, id);
            mMediaPlayer.setSurface(mSurface);
            mMediaPlayer.setLooping(true);
            if(mMediaPlayer.isPlaying()){
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = MediaPlayer.create(mContext, id);
            }

            mMediaPlayer.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        mMediaPlayer.start();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {//设置重复播放
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
//                mMediaPlayer.start();
//                mMediaPlayer.setLooping(true);
            }
        });
    }

    private void initView() {
        setSurfaceTextureListener(this);
    }

    private Surface mSurface;

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
        LogUtils.hLog().d("onSurfaceTextureAvailable");

        mSurface = new Surface(surfaceTexture);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setSurface(mSurface);
//        try {
//            mMediaPlayer = MediaPlayer.create(mContext, R.raw.rose);
//            mMediaPlayer.setSurface(mSurface);
//            mMediaPlayer.setLooping(true);
//            if(mMediaPlayer.isPlaying()){
//                mMediaPlayer.stop();
//                mMediaPlayer.release();
//                mMediaPlayer = MediaPlayer.create(mContext, R.raw.rose);
//            }
//
//            mMediaPlayer.start();
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//        }
//        mMediaPlayer.start();
//        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {//设置重复播放
//            @Override
//            public void onCompletion(MediaPlayer mediaPlayer) {
////                mMediaPlayer.start();
////                mMediaPlayer.setLooping(true);
//            }
//        });
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
        LogUtils.hLog().d("onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        LogUtils.hLog().d("onSurfaceTextureDestroyed");
        mContext = null;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
//        LogUtils.hLog().d("onSurfaceTextureUpdated");
    }
}