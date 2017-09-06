package com.example.howard.myapplication;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.howard.myapplication.eventloop.VideoPlayerView;
import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by howard on 17-9-5.
 */
public class VideoActivity extends AppCompatActivity{

    private VideoPlayerView mPlayer;
    private ImageView mFromAvator;
    private ImageView mToAvator;
    private TextView mFromUser;
    private TextView mToUser;

    private DisplayImageOptions display;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        mPlayer = (VideoPlayerView) findViewById(R.id.textureView);

        mFromAvator = (ImageView) findViewById(R.id.from_avator);
        mToAvator = (ImageView) findViewById(R.id.to_avator);
        mFromUser = (TextView) findViewById(R.id.from_name);
        mToUser = (TextView) findViewById(R.id.to_name);

        String fromName = getIntent().getStringExtra("fromUser");
        String toName = getIntent().getStringExtra("toUser");

        String fromUrl = "http://wx3.sinaimg.cn/mw600/9ccd8aa6gy1fj8lqa0c3rj20f00ir769.jpg";
        String toUrl = "http://wx4.sinaimg.cn/mw600/9ccd8aa6gy1fj8lqdr5ebj20f00b975h.jpg";

        ImageLoader imageLoader = ImageLoader.getInstance();

        display = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.mipmap.ic_launcher)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .bitmapConfig(Bitmap.Config.ARGB_8888)   //ÉèÖÃÍ¼Æ¬µÄ½âÂëÀàÐÍ
                .displayer(new Displayer(0))
                .build();

        imageLoader.displayImage(toUrl, mToAvator, display);
        imageLoader.displayImage(fromUrl, mFromAvator, display);

        mFromUser.setText(fromName);
        mToUser.setText(toName);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                mPlayer.playFromRaw(getResources().openRawResourceFd(R.raw.fire));
                mPlayer.playFromResId(R.raw.rose1);
            }
        }, 100);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayer.stopPlay();
    }
}