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
            public void onMessage(String fromUser, String msgBody) {
                client.decrypt(msgBody);
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
                    System.out.println(" Login successful!");
                    break;
                } else {
                    System.out.println(" Login unsuccessful! Please try Again");
                }

            }
            // client.logOut();
        } else {
            System.err.println("Connection Failed!");
        }
    }

    public String encrypt(String plain) {
        String b64encoded = Base64.getEncoder().encodeToString(plain.getBytes());

        // Reverse the string
        String reverse = new StringBuffer(b64encoded).reverse().toString();

        StringBuilder tmp = new StringBuilder();
        final int OFFSET = 4;
        for (int i = 0; i < reverse.length(); i++) {
            tmp.append((char)(reverse.charAt(i) + OFFSET));
        }
        return tmp.toString();
    }

    public String decrypt(String secret) {
        StringBuilder tmp = new StringBuilder();
        final int OFFSET = 4;
        for (int i = 0; i < secret.length(); i++) {
            tmp.append((char)(secret.charAt(i) - OFFSET));
        }

        String reversed = new StringBuffer(tmp.toString()).reverse().toString();
        return new String(Base64.getDecoder().decode(reversed));
    }

    public void logOut() throws IOException {
        String cmd = "logout\n";
        serverOut.write(cmd.getBytes());
    }

    public boolean login(String userName, String password) throws IOException {
        this.user = userName;
        String cmd = "login "+ userName + " " + password+ "\n";
        serverOut.write(cmd.getBytes());

        String response = bufferIn.readLine();
        System.out.println( response);

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
                    Scanner in = new Scanner(System.in);
                    while ((line = in.nextLine()) != null) {
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


        };
        t.start();
    }

    public void Msg( String msg) throws IOException {
        // String sendMsg = "Message from " + user + ": " + msg + "\n";
        serverOut.write(msg.getBytes());
        String response = bufferIn.readLine();
        System.out.println(response);
    }

    public void directMsg(String sendTo, String msgBody) throws IOException {
        String cmd = "pm " + sendTo + " " + msgBody + "\n";
        serverOut.write(cmd.getBytes());
        String response = bufferIn.readLine();
        System.out.println(response);
    }

    private void handleBroadcastMsg(String[] tokens) throws IOException {
        String msgBody = ""; // use encryption
        for (String token: tokens){
            msgBody = msgBody + " " + token;
        }
        for (MessageListener listener: messageListeners) {
            listener.onMessage(user, msgBody);
        }
        msgBody = encrypt(msgBody);
        Msg(msgBody);
    }

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
        msgBody = encrypt(msgBody);
        directMsg(sendTo,msgBody);
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

    public boolean connect() {
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
}
