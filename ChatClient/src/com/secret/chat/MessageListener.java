package com.secret.chat;

public interface MessageListener {
    public void onMessage(String fromUser, String msgBody); // interface to communicate messages
}
