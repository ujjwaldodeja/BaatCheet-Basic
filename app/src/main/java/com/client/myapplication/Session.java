package com.client.myapplication;

import javax.crypto.SecretKey;

public class Session {
    private final int LIMIT = 2;

    public synchronized int getCOUNT() {
        return COUNT;
    }

    private int COUNT;

    public Session() {
        COUNT = 0;
    }
    public synchronized void incrementCount() {
        COUNT++;
    }

    public synchronized boolean isOver() {
        return COUNT>=LIMIT;
    }


    public synchronized void reset() {
        COUNT = 0;
    }
}
