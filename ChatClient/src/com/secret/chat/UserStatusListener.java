package com.secret.chat;

public interface UserStatusListener {
    public void online(String userName);
    public void offline(String userName);
}
