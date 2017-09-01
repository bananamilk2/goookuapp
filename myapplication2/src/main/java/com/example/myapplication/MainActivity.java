package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {


    private TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EventBus.getDefault().register(this);

        tv = (TextView) findViewById(R.id.text);



    }

    public void click(View v){
        Intent intent = new Intent(MainActivity.this, SecondActivity.class);
        startActivity(intent);
    }

    @Subscribe
    public void onEvent(String str){
        tv.setText(str);
    }

    @Subscribe
    public void onEventAny(String str){
        LogUtils.hLog().i(str);
    }

    @Subscribe
    public void onEvent(Object obj){

    }

    @Subscribe
    public int onEventAny( Integer i){
        return i;
    }
}
