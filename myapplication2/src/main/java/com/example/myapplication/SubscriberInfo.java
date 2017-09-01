package com.example.myapplication;

/**
 * Created by Howard on 2017/8/31.
 */
public interface SubscriberInfo {
    Class<?> getSubscriberClass();
    SubscriberMethod[] getSubscriberMethods();
    SubscriberInfo getSuperSubscriberInfo();

    boolean shouldCheckSuperClass();
}
