package com.example.myapplication;

import android.util.Log;

/**
 * Created by Howard on 2017/8/31.
 */
public class BackgroundPoster implements Runnable{
    private final PendingPostQueue queue;
    private final EventBus eventBus;

    private volatile boolean executorRunning;

    BackgroundPoster(EventBus eventBus){
        this.eventBus = eventBus;
        queue = new PendingPostQueue();
    }

    public void enqueue(Subscription subscription, Object event){
        PendingPost pendingPost = PendingPost.obtainPendingPost(subscription, event);
        synchronized (this){
            queue.enqueue(pendingPost);
            if(!executorRunning){
                executorRunning = true;
                eventBus.getExecutorService().execute(this);
            }
        }
    }


    @Override
    public void run() {
        try{
            try{
                while(true){
                    PendingPost pendingPost = queue.poll(1000);
                    if(pendingPost == null){
                        synchronized (this){
                            pendingPost = queue.poll();
                            if(pendingPost == null){
                                executorRunning = false;
                                return;
                            }
                        }
                    }
                    eventBus.invokeSubscriber(pendingPost);
                }
            }catch (InterruptedException e){
                LogUtils.hLog().w(Thread.currentThread().getName() + "was interrupted  = " + e);
            }
        }finally {
            executorRunning = false;
        }
    }
}
