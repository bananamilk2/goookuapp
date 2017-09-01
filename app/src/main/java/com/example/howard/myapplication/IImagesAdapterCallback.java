package com.example.howard.myapplication;

import android.media.Image;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by Howard on 2017/8/28.
 */
public interface IImagesAdapterCallback {
    void onEnterImageDetails(String sharedImageTransitionName, String imageUrl, ImageView image, Image imageModel);
}
