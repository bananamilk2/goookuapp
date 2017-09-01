package com.example.howard.myapplication;

import android.content.Context;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easyandroidanimations.library.PuffOutAnimation;
import com.facebook.drawee.view.SimpleDraweeView;

import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import it.gmariotti.recyclerview.adapter.ScaleInAnimatorAdapter;

/**
 * Created by Howard on 2017/8/29.
 */
public class SecondActivity extends AppCompatActivity{

    private RecyclerView mRecyclerView;
    private List<String> mDatas;
    private HomeAdapter mAdapter;
    private ImageView mConnectCode;
    private SimpleDraweeView mScaleImage;

    public static final int ITEM_TYPE_TEXT = 0;
    public static final int ITEM_TYPE_IMAGE = 1;
    public static final int ITEM_TYPE_TEXT_IMAGE = 2;
    public static final int SHOW_IMAGE = 3;

    public Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 0){
                mConnectCode.setVisibility(View.VISIBLE);
                mHandler.sendEmptyMessageDelayed(1, 5000);
            }else if(msg.what == 1){
                mConnectCode.setVisibility(View.INVISIBLE);
                mHandler.sendEmptyMessageDelayed(0, 5000);
            }else if(msg.what == SHOW_IMAGE){
                showImage((String)msg.obj, 4);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mScaleImage = (SimpleDraweeView) findViewById(R.id.scale_image);
        initData();
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        mConnectCode = (ImageView)findViewById(R.id.connectcode);

        mRecyclerView.setLayoutManager(new ScrollSpeedLinearLayoutManager(this));
//        mRecyclerView.setAdapter(mAdapter = new HomeAdapter());
        mAdapter = new HomeAdapter();
        ScaleInAnimatorAdapter animatorAdapter = new ScaleInAnimatorAdapter(mAdapter, mRecyclerView);
        mRecyclerView.setAdapter(animatorAdapter);

//        mHandler.sendEmptyMessageDelayed(3, 3000);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    mWebClient = new WebClient(new URI("ws://192.168.1.114:4004"), new MySocketListener());
                    mWebClient.connectBlocking();
                    mWebClient.connect();

                }catch (Exception e){
                    Log.e("MainActivity", e.toString());
                }
            }
        });
    }

    class MySocketListener implements ISocketListener{

        @Override
        public void onReceiveMessage(WechatUserBean msg) {
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
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mWebClient.send("connect");
                }
            }, 2000);
        }
    }

    public void scroll(View view){
        if(mAdapter.getItemCount() > 0) {
            mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
        }
    }

    private void addData(WechatUserBean data){
        MessageBean msg = new MessageBean();
        msg.setType(ITEM_TYPE_TEXT);
        msg.setText(data.text);
        msg.setUserName(data.nickname);
        msg.setUserImg(data.head);
        msg.setTimestamp(getCurrentTime());
        msg.setImage(data.image);
        mAdapter.addItem(mAdapter.getItemCount(),msg);
        if(mAdapter.getItemCount() > 0) {
            mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
        }
        if(data.image != null) {
            Message ms = Message.obtain();
            ms.what = SHOW_IMAGE;
            ms.obj = data.image;
            mHandler.sendMessageDelayed(ms, 1000);
        }
    }

    public void addData(View view){
//        mRealData.add(mDatas.get(position++));
       showImage(null,3);
    }

    private WebClient mWebClient;


    public String TAG = "howard";

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mWebClient != null)
            mWebClient.close();
        mHandler.removeCallbacksAndMessages(null);
    }

    private void showImage(final String url, int duration){
        Log.i(TAG, "show image = " + Thread.currentThread().getName());
        mScaleImage.setImageURI(Uri.parse(url));
        RelativeLayout.LayoutParams pa = (RelativeLayout.LayoutParams) mScaleImage.getLayoutParams();
        pa.height = 530;
        pa.width = 530;
        mScaleImage.setVisibility(View.VISIBLE);
        mScaleImage.setLayoutParams(pa);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                new PuffOutAnimation(mScaleImage).animate();
//                mScaleImage.setVisibility(View.GONE);
            }
        }, duration * 1000);

    }

    protected void initData() {
        mDatas = new ArrayList();
        mRealData = new ArrayList();
        for (int i = 'A'; i < 'z'; i++) {
            mDatas.add("" + (char) i);
        }
    }

    private List<MessageBean> mRealData;
    private int position = 0;

    class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder> {
        private IImagesAdapterCallback mImageCallback;
        public HomeAdapter(IImagesAdapterCallback callback){
            mImageCallback = callback;
        }

        public HomeAdapter(){

        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            Log.d("howard","onCreateViewHolder: position = " + position + " view type = " + viewType);
            MyViewHolder holder = null;
            switch (viewType){
                case ITEM_TYPE_TEXT:
                    holder = new MyViewHolder(LayoutInflater.from(
                            SecondActivity.this).inflate(R.layout.recyclerview_item_text, parent,
                            false));
                    break;
                case ITEM_TYPE_IMAGE:

                    break;
                case ITEM_TYPE_TEXT_IMAGE:

                    break;
                default:

                    break;
            }

            return holder;
        }

        //复写该方法决定返回哪种item类型
        @Override
        public int getItemViewType(int position) {
            Log.d("howard","getItemViewType: position = " + position + " type = " + mRealData.get(position).getType());
            return mRealData.get(position).getType();
        }

        /**
         * 字符串转换unicode
         */
        public String string2Unicode(String string) {
            Log.i("howard", string);
            StringBuffer unicode = new StringBuffer();
            for (int i = 0; i < string.length(); i++) {
                // 取出每一个字符
                char c = string.charAt(i);
                if(c < 256){   //ASC11表中的字符码值不够4位,补00
                    unicode.append("\\u00");
                }
                else {
                    unicode.append("\\u");
                }
                // 转换为unicode
                unicode.append(Integer.toHexString(c));
            }
            Log.i("howard", "unicodetostring = "+unicode.toString());
            return unicode.toString();
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position){
            Log.d("howard","onBindViewHolder: position = " + position);
//            holder.userText.setText(string2Unicode(mRealData.get(position).getText()));

            if(mRealData.get(position).getText() != null) {
                holder.userText.setText(mRealData.get(position).getText());
                holder.userText.setVisibility(View.VISIBLE);
            }

//            if (holder instanceof CommentFirstHolder) {
//
//            } else if (holder instanceof CommentSecondHolder) {
//
//            }
            if(mRealData.get(position).getImage() != null) {
                Uri uri = Uri.parse(mRealData.get(position).getImage());
                holder.image.setImageURI(uri);
                holder.image.setScaleType(ImageView.ScaleType.FIT_START);
                holder.image.setVisibility(View.VISIBLE);
            }

            final Uri uri = Uri.parse(mRealData.get(position).getUserImg());
            holder.userImg.setImageURI(uri);
            holder.timestamp.setText(mRealData.get(position).getTimestamp());
            holder.userName.setText(mRealData.get(position).getUserName());



//            ImageView userImg;
//            TextView userName;
//            TextView timestamp;
//            ImageView occupyImage;

//            LinearLayout.LayoutParams linearParams =(LinearLayout.LayoutParams) textView.getLayoutParams(); //取控件textView当前的布局参数
//            linearParams.height = 20;// 控件的高强制设成20
//
//            linearParams.width = 30

            if(onItemClickListener != null){
                holder.itemView.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View view) {
                        int layoutPosition = holder.getLayoutPosition();
                        onItemClickListener.onItemClick(holder.itemView, layoutPosition);
                        Log.i("howard", "OnItemClick" + "  " + layoutPosition);
//                        ColorDialog cd = new ColorDialog(MainActivity.this);
//                        cd.setTitle("uhduhudheue");
//                        cd.setContentText("呵呵呵呵呵");
//                        cd.setContentImage()
//                        View v = LayoutInflater.from(MainActivity.this).inflate(R.layout.toast_layout, null);
//                        new MyToast(MainActivity.this, v, Toast.LENGTH_LONG).setImageUrl(mRealData.get(position).getImage()).show();

//                        if(mRealData.get(position).getImage() != null) {
//                            mImageCallback.onEnterImageDetails(null,mRealData.get(position).getImage(), holder.image, null);
//                        }



                    }
                });
            }

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener(){

                @Override
                public boolean onLongClick(View view) {
                    int layoutPosition = holder.getLayoutPosition();
                    onItemClickListener.onItemLongClick(holder.itemView, layoutPosition);
                    Log.i("howard", "OnItemLongClick" + "  " + layoutPosition);
                    return false;
                }
            });
        }

        public void addItem(int position, MessageBean data){
            Log.i("howard", "addItem: position =  " + position + "  data = " + data.toString());
            mRealData.add(position,data);
            notifyItemInserted(position);
        }

        @Override
        public int getItemCount() {
            Log.i("howard", "getItemCount: item Counts = " + mRealData.size());
            return mRealData.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView userText;
            SimpleDraweeView userImg;
            TextView userName;
            TextView timestamp;
            ImageView occupyImage;
            SimpleDraweeView image;

            public MyViewHolder(View view) {
                super(view);
                userText = view.findViewById(R.id.usertext);
                userImg = view.findViewById(R.id.userimg);
                userName = view.findViewById(R.id.username);
                timestamp = view.findViewById(R.id.timestamp);
                occupyImage = view.findViewById(R.id.occupyImage);
                image = view.findViewById(R.id.image);
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

    class ScrollSpeedLinearLayoutManager extends LinearLayoutManager {
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

    public String getCurrentTime(){
        long time = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
        String format = sdf.format(time);
        Log.i("'howard", "current time = " + format);
        return format;
    }
}
