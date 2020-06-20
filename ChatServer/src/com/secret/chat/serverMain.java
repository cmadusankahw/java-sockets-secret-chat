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
                Thread t  = new Thread() {
                    @Override
                    public void run() {
                        try {
                            handleClient(clientSocket);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };

                t.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) throws IOException {
        OutputStream outputStream = clientSocket.getOutputStream();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        outputStream.write("Hello\n".getBytes());
        clientSocket.close();
    }
}
