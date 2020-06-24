package com.secret.chat;


public class serverMain {
    public static void main(String[] args) {
        int port = 7800; // defining port for localhost
        Server server = new Server(port); // creating a new localhost chat server

        server.start(); // starting server thread
    }


}
