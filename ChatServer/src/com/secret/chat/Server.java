package com.secret.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread{
    private final int serverPort;

    // arrayList of service workers
    private ArrayList<serverWorker> workerList = new ArrayList<>();

    // setting server port for socket communication
    public Server(int serverPort) {
        this.serverPort = serverPort;
    }

    // getting worker list
    public List<serverWorker> getWorkerList() {
        return workerList;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while(true){
                System.out.println("Waiting for a client connection..");
                Socket clientSocket = serverSocket.accept();  // accepting client socket
                System.out.println("Accepted connection form clinet " + clientSocket);
                serverWorker worker = new serverWorker(this, clientSocket); // creating new Server worker to handle client
                workerList.add(worker); // adding worker to Server Worker list
                worker.start(); // Starting worker in a new Thread

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeWorker(serverWorker serverWorker) {
        workerList.remove(serverWorker); // removing serverWorker once the client is disconnected
    }
}
