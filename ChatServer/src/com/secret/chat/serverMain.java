package com.secret.chat;


public class serverMain {
    public static void main(String[] args) {
        int port = 7800;
        Server server = new Server(port);

        server.start();
    }


}
