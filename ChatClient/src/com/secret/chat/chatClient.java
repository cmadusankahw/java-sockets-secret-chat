package com.secret.chat;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

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
        chatClient client = new chatClient("localhost", 8188);
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
            public void onMessage(String fromLUser, String msgBody) {
                System.out.println( msgBody);
            }
        });

        if(client.connect()){
            System.out.println("Connection successful");
            if (client.login("boy","boy")) {
                System.out.println("Client Login Successful!");

                // handle direct messages
                client.directMsg("boy","HELLO!");

                // handle broadcast messages
                client.Msg("Test Body");
            }  else {
                System.err.println("Client Login Failed!");
            }
            // client.logOut();
        } else {
            System.err.println("Connection Failed!");
        }
    }

    private void Msg( String msg) throws IOException {
       // String sendMsg = "Message from " + user + ": " + msg + "\n";
        serverOut.write(msg.getBytes());
    }

    private void directMsg(String sendTo, String msgBody) throws IOException {
        String cmd = "pm " + sendTo + " " + msgBody + "\n";
        serverOut.write(cmd.getBytes());
    }

    private void logOut() throws IOException {
        String cmd = "logout\n";
        serverOut.write(cmd.getBytes());
    }

    private boolean login(String userName, String password) throws IOException {
        this.user = userName;
        String cmd = "login "+ userName + " " + password+ "\n";
        serverOut.write(cmd.getBytes());

        String response = bufferIn.readLine();
        System.out.println("Server Response: " + response);

        if ( (response.split(" ")[1]).equalsIgnoreCase(userName)){
            startMessageReader();
            return true;
        } else {
            return false;
        }

    }

    private void startMessageReader() {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    readMessageLoop();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            private void readMessageLoop() throws IOException {
                try{
                    String line;
                    while ((line = bufferIn.readLine()) != null) {
                        String[] tokens = line.split(" ");
                        if (tokens != null && tokens.length >0) {
                            if ("online".equalsIgnoreCase(tokens[0])){
                                handleOnline(tokens);
                            } else if ("offline".equalsIgnoreCase(tokens[0])){
                                handleOffline(tokens);
                            } else if ("pm".equalsIgnoreCase(tokens[0])){
                                handleDirectMsg(tokens);
                            } else {
                                handleBroadcastMsg(tokens);
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    socket.close();
                }

            }

            private void handleBroadcastMsg(String[] tokens) {
                String msgBody = ""; // use encryption
                for (String token: tokens){
                        msgBody = msgBody + " " + token;
                }
                for (MessageListener listener: messageListeners) {
                    listener.onMessage(user, msgBody);
                }
            }

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

                for (MessageListener listener: messageListeners) {
                    listener.onMessage(sendTo,msgBody);
                }
            }

            private void handleOnline(String[] tokens) {
                String userName = tokens[1];
                for(UserStatusListener listener: userStatusListeners){
                    listener.online(userName);
                }
            }

            private void handleOffline(String[] tokens) {
                String userName = tokens[1];
                for(UserStatusListener listener: userStatusListeners){
                    listener.offline(userName);
                }
            }
        };
        t.start();
    }

    private boolean connect() {
        try {
            this.socket = new Socket(serverName, serverPort);
            System.out.println("Client port is "+ socket.getLocalPort());
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void addUserStatusListener(UserStatusListener listener) {
        userStatusListeners.add(listener);
    }

    private void addMessagesListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    private void removeMessagesListener(MessageListener listener) {
        messageListeners.remove(listener);
    }

    private void removeUserStatusListener(UserStatusListener listener) {
        userStatusListeners.remove(listener);
    }
}
