package com.example.howard.myapplication.eventloop;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by howard on 17-9-4.
 */
public class PendingPost {
    private final static List<PendingPost> pendingPostPoll = new ArrayList<>();
    Object event;
    Object subscription;
    PendingPost next;
    private PendingPost(Object event, Object subscription){
        this.event = event;
        this.subscription = subscription;
    }

    static PendingPost obtainPendingPost(Object subscription, Object event){
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
