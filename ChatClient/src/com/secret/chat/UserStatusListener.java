package com.secret.chat;

public interface UserStatusListener { // interface to handle user online, offline status
    public void online(String userName);
    public void offline(String userName);
}
