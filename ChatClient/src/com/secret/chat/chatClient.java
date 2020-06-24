package com.secret.chat;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;

public class chatClient {
    private final String serverName;
    private final int serverPort;
    private Socket socket;
    private OutputStream serverOut;
    private InputStream serverIn;
    private BufferedReader bufferIn;

    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    private ArrayList<MessageListener> messageListeners = new ArrayList<>();
    private String user;

    public chatClient(String hostName, int port) {
        this.serverName = hostName;
        this.serverPort = port;
    }

    public static void main(String[] args) throws IOException {
        chatClient client = new chatClient("localhost", 7800);
        client.addUserStatusListener(new UserStatusListener() {
            @Override
            public void online(String userName) {
                System.out.println(userName + " is Online" );
            }

            @Override
            public void offline(String userName) {
                System.out.println(userName + " is Offline" );
            }
        });

        client.addMessagesListener(new MessageListener() {
            @Override
            public void onMessage(String fromUser, String msgBody) {
                System.out.println( "Message form " + fromUser+ " : " +msgBody);
            }
        });

        if(client.connect()){
            while(true) {
                System.out.println("Connection successful");
                Scanner in = new Scanner(System.in);
                System.out.println("Welcome to Socketlovers Chat!\n\nEnter UserName: ");
                String un = in.next();

                System.out.println("\nEnter Password: ");
                String pw = in.next();
                if(client.login(un, pw)){
                    System.out.println("Login successful!");
                        break;
                } else {
                    System.out.println(" Login unsuccessful! Please try Again");
                }

            }

        } else {
            System.err.println("Connection Failed!");
        }
    }


    // connecting client socket to server
    public boolean connect() {
        try {
            this.socket = new Socket(serverName, serverPort); // creating new socket connection
            System.out.println("Client port is "+ socket.getLocalPort());
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferIn = new BufferedReader(new InputStreamReader(serverIn)); // buffer reader to read server responses
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // adding and removing User State listeners and Message listeners
    public void addUserStatusListener(UserStatusListener listener) {
        userStatusListeners.add(listener);
    }

    public void addMessagesListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    public void removeMessagesListener(MessageListener listener) {
        messageListeners.remove(listener);
    }

    public void removeUserStatusListener(UserStatusListener listener) {
        userStatusListeners.remove(listener);
    }


    // encrypt messages
    public static String encrypt(String msg){
        String key = "1234dfrghtjkGHJR";
        msg = new StringBuilder(new String(msg)).reverse().toString();
        msg = key + msg;
        System.out.println("Encrypted :" + msg);
        return msg;
    }

    // handle client log out and state updating
    public void logOut() throws IOException {
        String cmd = "logout\n";
        serverOut.write(cmd.getBytes());

        // getting server Worker response and printing
        String response = bufferIn.readLine();
        System.out.println( response);
    }

    // handle client log in and state updating
    public boolean login(String userName, String password) throws IOException {
        this.user = userName;
        String cmd = "login "+ userName + " " + password+ "\n";
        serverOut.write(cmd.getBytes());

        // getting server Worker response and printing
        String response = bufferIn.readLine();
        System.out.println( response);

        if ( (response.split(" ")[1]).equalsIgnoreCase(userName)){
            // start listening to messages if the user has logged in
            startMessageReader();
            return true;
        } else {
            return false;
        }

    }

    // message listener handling
    private void startMessageReader() {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    while(true){
                        // listening for messages and providing responses
                        readMessageLoop();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            private void readMessageLoop() throws IOException {
                try{
                    String line;
                    Scanner in = new Scanner(System.in); // gettig user input with Scanner
                    while ((line = in.nextLine()) != null) {
                        String[] tokens = line.split(" "); // splitting input into tokens
                        if (tokens != null && tokens.length >0) {
                            // extracting protocol command from tokens and perform actions
                            if ("online".equalsIgnoreCase(tokens[0])){
                                handleOnline(tokens); // handle user online state
                            } else if ("offline".equalsIgnoreCase(tokens[0])){
                                handleOffline(tokens); // handle user offline state
                            } else if ("pm".equalsIgnoreCase(tokens[0])){
                                handleDirectMsg(tokens); // handle private messages between two logged users
                            }  else if ("logout".equalsIgnoreCase(tokens[0]) || "quit".equalsIgnoreCase(tokens[0]) ){
                                logOut(); // handle user log out
                            }else {
                                if ( user != null) {
                                    handleBroadcastMsg(tokens); // if protocol commands not specified consider input as a broadcast msg and share it with all logged users
                                } else {
                                    System.out.println("unknown command! Please log in first!\n".getBytes());

                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    socket.close();
                }

            }

        };
        t.start();
    }

    // sending broadcast messages and retriving server response
    public void Msg( String msg) throws IOException {
        // encrypting message at client
        msg = encrypt(msg);
        serverOut.write(msg.getBytes());

        String response = bufferIn.readLine();
        System.out.println(response);

    }

    // sending private messages and retriving server response
    public void directMsg(String sendTo, String msgBody) throws IOException {
        // encrypting message at client
        msgBody = encrypt(msgBody);
        String cmd = "pm " + sendTo + " " + msgBody + "\n";
        serverOut.write(cmd.getBytes());
        if(user.equalsIgnoreCase(sendTo)) {
            String response = bufferIn.readLine();
            System.out.println(response);
        }

    }

    // handling broadcast messages at client
    private void handleBroadcastMsg(String[] tokens) throws IOException {
        String msgBody = ""; // use encryption
        for (String token: tokens){
            msgBody = msgBody + " " + token;
        }
        for (MessageListener listener: messageListeners) {
            listener.onMessage(user, msgBody);
        }
        Msg(msgBody);

    }

    // handling private messages at client
    private void handleDirectMsg(String[] tokens) throws IOException {
        String sendTo = tokens [1];
        String msgBody = ""; // use encryption
        int i =0;
        for (String token: tokens){
            if (i>1) {
                msgBody = msgBody + " " + token;
            }
            i++;
        }

        for (MessageListener listener: messageListeners) {
            listener.onMessage(sendTo,msgBody);
        }
        directMsg(sendTo,msgBody);

    }

    // handling user online state at client
    private void handleOnline(String[] tokens) {
        String userName = tokens[1];
        for(UserStatusListener listener: userStatusListeners){
            listener.online(userName);
        }
    }

    // handling user offline state at client
    private void handleOffline(String[] tokens) {
        String userName = tokens[1];
        for(UserStatusListener listener: userStatusListeners){
            listener.offline(userName);
        }
    }

}
