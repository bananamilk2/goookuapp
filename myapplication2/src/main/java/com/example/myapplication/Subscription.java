package com.example.myapplication;

/**
 * Created by Howard on 2017/8/31.
 */
public class Subscription {
    final Object subscriber;
    final SubscriberMethod subscriberMethod;
    volatile boolean active;

    Subscription(Object subscriber, SubscriberMethod subscriberMethod){
        this.subscriber = subscriber;
        this.subscriberMethod = subscriberMethod;
        active = true;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Subscription){
            Subscription otherSubscription = (Subscription) obj;
            return subscriber == otherSubscription
                    && subscriberMethod.equals(otherSubscription.subscriberMethod);
        }else{
            return false;
        }
    }

    @Override
    public int hashCode() {
        return subscriber.hashCode() + subscriberMethod.methodString.hashCode();
    }
}
