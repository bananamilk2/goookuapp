package com.example.howard.myapplication;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

/**
 * Created by Howard on 2017/8/28.
 */
public class MyToast {
    private Toast toast;
    private LinearLayout toastView;
    public MyToast(){

    }
    public MyToast(Context context, View view, int duration, int positionX, int positionY, int width, int height){
        toast = new Toast(context);
        toast.setView(view);
        toast.setDuration(duration);
    }

    public MyToast(Context context, View view, int duration){
        toast = new Toast(context);
        toast.setView(view);
        toast.setDuration(duration);
    }

    public MyToast addView(View view, int position){
        toastView = (LinearLayout) toast.getView();
        toastView.addView(view, position);
        return this;
    }

    public MyToast setToastColor(int messageColor, int backgroundColor){
        View view = toast.getView();
        if(null != view){
            TextView message = view.findViewById(android.R.id.message);
            message.setBackgroundColor(backgroundColor);
            message.setTextColor(messageColor);
        }
        return this;
    }

    public MyToast Long(Context context, int message){
        if(toast == null || (toastView != null && toastView.getChildCount() > 1)){
            toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
            toastView = null;
        }else{
            toast.setText(message);
            toast.setDuration(Toast.LENGTH_LONG);
        }
        return this;
    }

    public MyToast setImageUrl(String url){
        if(null != toast.getView()){
            SimpleDraweeView image = toast.getView().findViewById(R.id.image);
            if(null != image){
                image.setImageURI(Uri.parse(url));
            }
        }
        return this;
    }

    public MyToast show(){
        toast.show();
        return this;
    }

    public Toast getToast(){
        return toast;
    }


}
