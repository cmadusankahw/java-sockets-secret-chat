package com.secret.chat;


public class serverMain {
    public static void main(String[] args) {
        int port = 8188;
        Server server = new Server(port);

        server.start();
    }


}
