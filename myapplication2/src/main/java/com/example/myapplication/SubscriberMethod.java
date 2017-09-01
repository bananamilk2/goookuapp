package com.example.myapplication;

import java.lang.reflect.Method;

/**
 * Created by Howard on 2017/8/31.
 */
public class SubscriberMethod {
    LogUtils log = LogUtils.hLog();
    final Method method;
    final ThreadMode threadMode;
    final Class<?> eventType;
    final int priority;
    final boolean sticky;
    String methodString;

    public SubscriberMethod(Method method, Class<?> eventType, ThreadMode threadMode, int priority, boolean sticky){
        this.method = method;
        this.eventType = eventType;
        this.threadMode = threadMode;
        this.priority = priority;
        this.sticky = sticky;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this){
            return true;
        }else if(obj instanceof SubscriberMethod){
            checkMethodString();
            SubscriberMethod otherSubscriberMethod = (SubscriberMethod) obj;
            otherSubscriberMethod.checkMethodString();
            return methodString.equals(otherSubscriberMethod.methodString);
        }else{
            return false;
        }
    }

    private synchronized void checkMethodString() {
        if(methodString == null){
            StringBuilder builder = new StringBuilder(64);
            builder.append(method.getDeclaringClass().getName());
            builder.append('#').append(method.getName());
            builder.append('(').append(eventType.getName());
            methodString = builder.toString();
        }
    }
}
