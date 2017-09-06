package com.example.howard.myapplication.eventloop;

import java.util.LinkedList;

/**
 * Created by howard on 17-9-4.
 */
public class Queue {

    private static LinkedList list = new LinkedList();

    static volatile Queue mQueue;

    private Queue(){}

    public static Queue getDefault(){
        if(mQueue == null){
            synchronized (Queue.class){
                if(mQueue == null){
                    mQueue = new Queue();
                }
            }
        }
        return mQueue;
    }

    //销毁队列
    public void clear(){
        list.clear();
    }

    //判断队列是否为空
    public boolean isEmpty() {
        return list.isEmpty();
    }

    //进队
    public void enqueue(Object o) {
        list.addLast(o);

    }

    //出队
    public Object poll() {
        if (!list.isEmpty()) {
            return list.removeFirst();
        }
        return null;
    }

    //获取队列长度
    public int getSize() {
        return list.size();
    }

    //查看队首元素
    public Object QueuePeek() {
        return list.getFirst();
    }

}