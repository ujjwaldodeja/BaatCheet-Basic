package com.client.myapplication;

public class Session {
    private String recipient;
    private final int LIMIT = 5;
    private int COUNT;

    public Session(String recipient){
        this.recipient = recipient;
        COUNT = 0;
    }
    public void incrementCount(){
        COUNT++;
    }
}
