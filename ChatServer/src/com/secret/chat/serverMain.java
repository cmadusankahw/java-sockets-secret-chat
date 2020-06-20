package com.secret.chat;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class serverMain {
    public static void main(String[] args) {
        int port = 8188;
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while(true){
                System.out.println("Waiting for a client connection..");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection form clinet " + clientSocket);
                OutputStream outputStream = clientSocket.getOutputStream();
                outputStream.write("Hello\n".getBytes());
                clientSocket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
