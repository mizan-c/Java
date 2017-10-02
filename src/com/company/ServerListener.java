package com.company;

/**
 * Created by Mizanur Rahman
 */
public interface ServerListener {
    public void onNewClientJoin(int id, String name);
    public void onClientLeft(int id, String name);
    public void onNewMessage(int id, String s);

}
