package com.example.howard.myapplication.eventloop;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import com.example.howard.myapplication.LogUtils;

/**
 * Created by howard on 17-9-4.
 */
public class PostHandler extends Handler {

    private final Queue queue;
    private int maxMillisInsideHandleMessage;
    private final int defaultInsideHandleMessage;
    private boolean handlerActive;
    private final IEventHandler eventHandler;

    public PostHandler(IEventHandler eventHandler, Looper looper, int maxMillisInsideHandleMessage) {
        super(looper);
        this.eventHandler = eventHandler;
        this.defaultInsideHandleMessage = this.maxMillisInsideHandleMessage = maxMillisInsideHandleMessage;
        queue = Queue.getDefault();
    }

    public void enqueue(Object subscription) {
        synchronized (this) {
            //入队列
            queue.enqueue(subscription);
            if (!handlerActive) {
                handlerActive = true;
                if (!sendMessage(obtainMessage())) {
                    throw new RuntimeException("could not add to queue");
                }
            }
        }
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        long started = SystemClock.uptimeMillis();
        while (true) {
            Object bean = queue.poll();
            if (bean == null) {
                synchronized (this) {
                    bean = queue.poll();
                    if (bean == null) {
                        handlerActive = false;
                        LogUtils.hLog().i("stop loop wait for new message");
                        return;
                    }
                }
            }
            eventHandler.handlerEvent(bean);
            long timeMethod = SystemClock.uptimeMillis() - started;
            LogUtils.hLog().d("handle event takes " + timeMethod + " ms");
            if (timeMethod < maxMillisInsideHandleMessage) {
                sendEmptyMessageDelayed(0, maxMillisInsideHandleMessage - timeMethod);
                return;
            }
        }
    }
}
