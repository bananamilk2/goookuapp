package com.example.myapplication;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

/**
 * Created by Howard on 2017/8/31.
 */
public class HandlerPoster extends Handler {
    private final PendingPostQueue queue;
    private final int maxMillisInsideHandleMessage;
    private final EventBus eventBus;
    private boolean handlerActive;

    HandlerPoster(EventBus eventBus, Looper looper, int maxMillisInsideHandleMessage){
        super(looper);
        this.maxMillisInsideHandleMessage = maxMillisInsideHandleMessage;
        this.eventBus = eventBus;
        queue = new PendingPostQueue();
    }

    void enqueue(Subscription subscription, Object event){
        PendingPost pendingPost = PendingPost.obtainPendingPost(subscription, event);
        synchronized (this){
            queue.enqueue(pendingPost);
            if(!handlerActive){
                handlerActive = true;
                if(!sendMessage(obtainMessage())){
                    throw new EventBusException("could not send handler message");
                }
            }
        }
    }

    @Override
    public void handleMessage(Message msg) {
        boolean rescheduled = false;
        try{
            long started = SystemClock.uptimeMillis();
            while(true){
                PendingPost pendingPost = queue.poll();
                if(pendingPost == null){
                    synchronized (this){
                        pendingPost = queue.poll();
                        if(pendingPost == null){
                            handlerActive = false;
                            return;
                        }
                    }
                }
                eventBus.invokeSubscriber(pendingPost);
                long timeMethod = SystemClock.uptimeMillis() - started;
                if(timeMethod >= maxMillisInsideHandleMessage){
                    if(!sendMessage(obtainMessage())){
                        throw new EventBusException("Coule not send handelr message");
                    }
                    rescheduled = true;
                    return;
                }
            }
        }finally {
            handlerActive = rescheduled;
        }
    }
}
