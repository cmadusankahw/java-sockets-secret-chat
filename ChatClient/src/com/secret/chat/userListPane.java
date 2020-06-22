package com.secret.chat;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class userListPane extends JPanel implements  UserStatusListener {
    private final chatClient client;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;

    public userListPane(chatClient client) {
        this.client = client;
        this.client.addUserStatusListener(this);

        userListModel =  new DefaultListModel<>();
        userList = new JList<>(userListModel);
        setLayout(new BorderLayout());
        add(new JScrollPane(userList), BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        chatClient client = new chatClient("localhost", 8188);

        userListPane userListPane = new userListPane(client);

        JLabel label = new JLabel("Welcome to Socket Lovers");
        label.setForeground(Color.BLUE);
        JFrame frame = new JFrame(" SocketLovers - Chat List");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.getContentPane().add(userListPane, BorderLayout.CENTER);
        frame.setSize(640, 820);
        frame.setVisible(true);

        if (client.connect()){
            try {
                client.login("boy", "boy");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void online(String userName) {
        userListModel.addElement(userName + " is Online");
    }

    @Override
    public void offline(String userName) {
        userListModel.addElement(userName + " is Offline");
    }
}
