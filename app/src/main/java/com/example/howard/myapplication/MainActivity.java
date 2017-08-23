package com.example.howard.myapplication;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import it.gmariotti.recyclerview.adapter.AlphaAnimatorAdapter;
import it.gmariotti.recyclerview.adapter.ScaleInAnimatorAdapter;
import it.gmariotti.recyclerview.itemanimator.SlideScaleInOutRightItemAnimator;


public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private List<String> mDatas;
    private HomeAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        mRecyclerView.setLayoutManager(layout);

        initData();
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        mRecyclerView.setLayoutManager(new ScrollSpeedLinearLayoutManager(this));
//        mRecyclerView.setAdapter(mAdapter = new HomeAdapter());
        mAdapter = new HomeAdapter();
        ScaleInAnimatorAdapter animatorAdapter = new ScaleInAnimatorAdapter(mAdapter, mRecyclerView);
        mRecyclerView.setAdapter(animatorAdapter);
//        mRecyclerView.setItemAnimator(new SlideScaleInOutRightItemAnimator(mRecyclerView));


        mAdapter.setOnItemClickListener(new onItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(MainActivity.this,"onClick"+position,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(View view, int position) {
                Toast.makeText(MainActivity.this,"onLongClick"+position,Toast.LENGTH_SHORT).show();
            }
        });
        
        try {
            WebClient c = new WebClient(new URI("ws://192.168.1.114:4001"), new MySocketListener());
            c.connectBlocking();
            c.connect();
            c.send("handshake");
        }catch (Exception e){
            Log.e("MainActivity", e.toString());
        }

    }

    class MySocketListener implements ISocketListener{

        @Override
        public void onReceiveMessage(String msg) {
            Log.i("MySocketListener", "onReceiveMessage" + msg);
            addData(msg);
        }

        @Override
        public void onSocketClose(int code, String reason, boolean remote) {
            Log.i("MySocketListener", "onSocketClose");
        }

        @Override
        public void onSocketError(Exception ex) {
            Log.i("MySocketListener", "onSocketError");
        }

        @Override
        public void onSocketOpen(ServerHandshake handshakedata) {
            Log.i("MySocketListener", "onSocketOpen");
        }
    }

    public void scroll(View view){
        if(mAdapter.getItemCount() > 0) {
            mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
        }
    }

    public void addData(View view){
//        mRealData.add(mDatas.get(position++));
        mAdapter.addItem(mAdapter.getItemCount(),mDatas.get(position++));
        if(mAdapter.getItemCount() > 0) {
            mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
        }
    }

    private void addData(String data){
//        mRealData.add(mDatas.get(position++));
        mAdapter.addItem(mAdapter.getItemCount(),data);
        if(mAdapter.getItemCount() > 0) {
            mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
        }
    }


    protected void initData() {
        mDatas = new ArrayList();
        mRealData = new ArrayList();
//        for (int i = 'A'; i < 'z'; i++)
//        {
//            mDatas.add("" + (char) i);
//        }
    }

    private List<String> mRealData;
    private int position = 0;

    class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            MyViewHolder holder = new MyViewHolder(LayoutInflater.from(
                    MainActivity.this).inflate(R.layout.item, parent,
                    false));
            return holder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position)
        {
            Log.d("howard","position = " + position);
            holder.tv.setText(mRealData.get(position));
            if(onItemClickListener != null){
                holder.itemView.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View view) {
                        int layoutPosition = holder.getLayoutPosition();
                        onItemClickListener.onItemClick(holder.itemView, layoutPosition);
                        Log.i("onBindViewHolder", "OnItemClick" + "  " + layoutPosition);
                    }
                });
            }

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener(){

                @Override
                public boolean onLongClick(View view) {
                    int layoutPosition = holder.getLayoutPosition();
                    onItemClickListener.onItemLongClick(holder.itemView, layoutPosition);
                    Log.i("onBindViewHolder", "OnItemLongClick" + "  " + layoutPosition);
                    return false;
                }
            });
        }

        public void addItem(int position, String data){
            mRealData.add(position,data);
            notifyItemInserted(position);
        }

        @Override
        public int getItemCount() {
            Log.i("getItemCount", "item COunts = " + mRealData.size());
            return mRealData.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder
        {

            TextView tv;

            public MyViewHolder(View view)
            {
                super(view);
                tv = (TextView) view.findViewById(R.id.id_num);
            }
        }


        private onItemClickListener onItemClickListener;
        public void setOnItemClickListener(onItemClickListener onItemClickListener){
            this.onItemClickListener=onItemClickListener;
        }


    }

    interface onItemClickListener{
        void onItemClick(View view ,int position);
        void  onItemLongClick(View view,int position);
    }

    class ScrollSpeedLinearLayoutManager extends LinearLayoutManager{
        private float MILLISECONDS_PER_INCH = 1f;
        private Context context;
        public ScrollSpeedLinearLayoutManager(Context context) {
            super(context);
            this.context = context;
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
            LinearSmoothScroller linearSmoothScroller =
                    new LinearSmoothScroller(recyclerView.getContext()) {
                        @Override
                        public PointF computeScrollVectorForPosition(int targetPosition) {
                            return ScrollSpeedLinearLayoutManager.this
                                    .computeScrollVectorForPosition(targetPosition);
                        }

                        //This returns the milliseconds it takes to
                        //scroll one pixel.
                        @Override
                        protected float calculateSpeedPerPixel
                        (DisplayMetrics displayMetrics) {
                            return MILLISECONDS_PER_INCH / displayMetrics.density;
                            //返回滑动一个pixel需要多少毫秒
                        }

                    };
            linearSmoothScroller.setTargetPosition(position);
            startSmoothScroll(linearSmoothScroller);
        }

        public void setSpeedSlow() {
            //自己在这里用density去乘，希望不同分辨率设备上滑动速度相同
            //0.3f是自己估摸的一个值，可以根据不同需求自己修改
            MILLISECONDS_PER_INCH = context.getResources().getDisplayMetrics().density * 0.3f;
        }

        public void setSpeedFast() {
            MILLISECONDS_PER_INCH = context.getResources().getDisplayMetrics().density * 0.03f;
        }
    }
}


