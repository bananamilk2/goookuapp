package com.example.myapplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Howard on 2017/8/31.
 */
public class PendingPost {
    private final static List<PendingPost> pendingPostPoll = new ArrayList<>();
    Object event;
    Subscription subscription;
    PendingPost next;
    private PendingPost(Object event, Subscription subscription){
        this.event = event;
        this.subscription = subscription;
    }

    static PendingPost obtainPendingPost(Subscription subscription, Object event){
        synchronized (pendingPostPoll){
            int size = pendingPostPoll.size();
            if(size > 0){
                PendingPost pendingPost = pendingPostPoll.remove(size - 1);
                pendingPost.event = event;
                pendingPost.subscription = subscription;
                pendingPost.next = null;
                return pendingPost;
            }
        }
        return new PendingPost(event, subscription);
    }

    static void releasePendingPost(PendingPost pendingPost){
        pendingPost.event = null;
        pendingPost.subscription = null;
        pendingPost.next = null;
        synchronized (pendingPostPoll){
            if(pendingPostPoll.size() < 10000){
                pendingPostPoll.add(pendingPost);
            }
        }
    }
}
