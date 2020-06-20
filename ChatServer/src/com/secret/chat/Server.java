package com.secret.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread{
    private final int serverPort;

    private ArrayList<serverWorker> workerList = new ArrayList<>();

    public Server(int serverPort) {
        this.serverPort = serverPort;
    }

    public List<serverWorker> getWorkerList() {
        return workerList;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while(true){
                System.out.println("Waiting for a client connection..");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection form clinet " + clientSocket);
                serverWorker worker = new serverWorker(this, clientSocket);
                workerList.add(worker);
                worker.start();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeWorker(serverWorker serverWorker) {
        workerList.remove(serverWorker);
    }
}
