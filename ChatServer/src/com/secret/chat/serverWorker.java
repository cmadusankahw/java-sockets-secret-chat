package com.secret.chat;

import java.io.*;
import java.net.Socket;
import java.util.List;

// encryption & decryption imports


public class serverWorker extends Thread {
    private final Socket clientSocket;
    private final Server server;
    private String user;
    private OutputStream outputStream;


    public serverWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            handleClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // PROTOCOL
    // type 'quit' to quit the client connection
    // type 'login <uname> <pword> to login
    // type 'logout' to log out
    // send private messages 'pm <username> messageBody'

    private void handleClient() throws IOException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ( (line = reader.readLine()) != null) {
            String[] tokens = line.split(" ");
            if (tokens != null && tokens.length >0){
                String cmd = tokens[0];
                if ("logout".equalsIgnoreCase(cmd) || "quit".equalsIgnoreCase(cmd)){
                    handleLogOff();
                    break;
                } else if ("login".equalsIgnoreCase(cmd)){
                    handleLogin(outputStream, tokens);
                }else if ("pm".equalsIgnoreCase(cmd)){
                    handleDirectMsg(tokens);
                } else {
                    if ( user == null) {
                        outputStream.write(("unknown command " + cmd + " Please log in first!\n").getBytes());
                    } else {
                        handleBroadcastMsg(line);
                    }
                }
                //String message = "You typed:" + line + "\n";
                //outputStream.write(message.getBytes());
            }

        }
        clientSocket.close();
    }

    private void handleBroadcastMsg(String msg) {
        List<serverWorker> workerList = server.getWorkerList();
        for (serverWorker worker: workerList) {
                String sendMsg = "Message from " + user + ": " + msg + "\n";
                worker.send(sendMsg);
        }
    }

    // format 'pm username msg'
    private void handleDirectMsg(String[] tokens) {
        String sendTo = tokens [1];
        String msgBody = ""; // use encryption
        int i =0;
        for (String token: tokens){
            if (i>1) {
                msgBody = msgBody + " " + token;
            }
            i++;
        }

        List<serverWorker> workerList = server.getWorkerList();
        for (serverWorker worker: workerList) {
            if (sendTo.equalsIgnoreCase(worker.getUser())) {
                String sendMsg = "Message from " + user + ": " + msgBody + "\n";
                worker.send(sendMsg);
            }
        }
    }

    private void handleLogOff() throws IOException {
        server.removeWorker(this);
        List<serverWorker> workerList = server.getWorkerList();
        // send current user offline status to other users
        for (serverWorker worker: workerList) {
            if (!user.equals(worker.getUser())) {
                if (worker.getUser() != null ){
                    String loginMsg2 = "offline " + user + "\n";
                    worker.send(loginMsg2);
                }
            }
        }
        clientSocket.close();
    }

    public String getUser() {
        return user;
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens.length == 3) {
            String uname = tokens[1];
            String pword = tokens[2];
            if (uname.equals("girl") && pword.equals("girl") || uname.equals("boy") && pword.equals("boy")) {
                this.user = uname;
                System.out.println( "logged " + user );

                String loginMsg = "online " + user + "\n";
                outputStream.write(loginMsg.getBytes());

                List<serverWorker> workerList = server.getWorkerList();

                // send current user online status to other users
                for (serverWorker worker: workerList) {
                    if (!user.equals(worker.getUser())) {
                        if (worker.getUser() != null ){
                            worker.send(loginMsg);
                        }
                    }
                }

            } else {
                try {
                    outputStream.write("Error in Login\n".getBytes());
                    System.out.println("Login attempt failed");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void send(String msg) {
        if ( user != null) {
            try {
                outputStream.write(msg.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
