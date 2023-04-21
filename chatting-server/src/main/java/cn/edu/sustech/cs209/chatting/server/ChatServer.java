package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class ChatServer {
    public static final int PORT = 8888;
    private static final ArrayList<ClientThread> clients = new ArrayList<>();
    private static final HashMap<String, String> avatarMap = new HashMap<>();
    private static final HashMap<String, ArrayList<Message>> privateMessageHistory = new HashMap<>();
    private static final HashMap<String, ArrayList<Message>> groupMessageHistory = new HashMap<>();
    private static final HashMap<String, ArrayList<String>> groupMemberLists = new HashMap<>();
    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);
            // noinspection InfiniteLoopStatement
            while (true) {
                Socket socket = server.accept();
                ChatServer.ClientThread client = new ClientThread(socket);
                boolean uniqueNameCheck = true;
                for (ClientThread it: ChatServer.clients) {
                    if (client.getUsername().equals(it.getUsername())) {
                        uniqueNameCheck = false;
                        break;
                    }
                }
                if (!uniqueNameCheck) {
                    Message errorMsg = new Message(Message.ERROR_DUPLICATE_USERNAME, "SERVER", client.username, System.currentTimeMillis(), Message.ERROR_DUPLICATE_USERNAME);
                    client.sendMessage(errorMsg.toString());
                    continue;
                }

                Message successMsg = new Message(Message.ALLOW_TO_JOIN, "SERVER", client.username, System.currentTimeMillis(), Message.ALLOW_TO_JOIN);
                client.sendMessage(successMsg.toString());
                clients.add(client);
                int a = Util.getRandomInteger(1, 9);
                System.out.println(client.username + " " + a);
                avatarMap.put(client.username, Integer.toString(a));
                client.start();

                // 通知每个用户新增了client, 要求更新client列表
                String usernameListStr = clients.stream().map(ChatServer.ClientThread::getUsername).map(x -> x + ":" + avatarMap.get(x)).collect(Collectors.joining(","));
                broadcast(Message.UPDATE_CLIENT_LIST, usernameListStr);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void broadcast(String command, String content) {
        Long timestamp = System.currentTimeMillis();
        for (ClientThread client : clients) {
            Message message = new Message(command, "SERVER", client.username, timestamp, content);
            client.sendMessage(message.toString());
        }
    }

    public static class ClientThread extends Thread {
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String username;

        public String getUsername() {
            return username;
        }

        public ClientThread(Socket socket) {
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                username = Message.parse(in.readLine()).getSender();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        private synchronized void removeClient(ClientThread clientThread) { clients.remove(clientThread); }

        public void run() {
            System.out.println("Connected: " + socket.getInetAddress() + " - " + username);
            String message;
            try {
                while ((message = in.readLine()) != null) {
                    if (message.startsWith(Message.SEND_PRIVATE_MESSAGE)) {
                        Message tmp = Message.parse(message);
                        String key = Util.getKey(tmp.getSender(), tmp.getReceiver());
                        if (!privateMessageHistory.containsKey(key)) {
                            privateMessageHistory.put(key, new ArrayList<>());
                        }
                        privateMessageHistory.get(key).add(tmp);
                        sendPrivateMessageHistory(tmp, key);
                    }
                    else if (message.startsWith(Message.SEND_GROUP_MESSAGE)) {
                        Message tmp = Message.parse(message);
                        String title = tmp.getReceiver();
                        groupMessageHistory.get(title).add(tmp);
                        sendGroupMessageHistory(title);
                    }
                    else if (message.startsWith(Message.REQUEST_PRIVATE_CHAT)) {
                        Message tmp = Message.parse(message);
                        String key = Util.getKey(tmp.getSender(), tmp.getReceiver());
                        if (!privateMessageHistory.containsKey(key)) {
                            privateMessageHistory.put(key, new ArrayList<>());
                        }
                        sendPrivateMessageHistory(tmp, key);
                    }
                    else if (message.startsWith(Message.REQUEST_GROUP_CHAT)) {
                        Message tmp = Message.parse(message);
                        String title = tmp.getReceiver();
                        String[] userArray = tmp.getContent().split("@")[0].split(",");
                        ArrayList<String> userList = new ArrayList<>(Arrays.asList(userArray));
                        if (!groupMessageHistory.containsKey(title)) {
                            groupMessageHistory.put(title, new ArrayList<>());
                            groupMemberLists.put(title, userList);
                        }
                        sendGroupMessageHistory(title);
                    }
                    System.out.println(message);
                }
            } catch (IOException e) {
                System.out.println("退出");
            } finally {
                try {
                    removeClient(this);
                    ChatServer.broadcast(Message.SYSTEM_INFO, username + " has left the chat room.");
                    in.close();
                    out.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendPrivateMessageHistory(Message tmp, String key) {
            String msgListString = privateMessageHistory.get(key).stream().map(Message::toString).collect(Collectors.joining(Message.MSG_DELIMITER));
            Message responseToMsgSender = new Message(Message.RESPONSE_PRIVATE_CHAT, "SERVER", tmp.getSender(), System.currentTimeMillis(), msgListString);
            this.sendMessage(responseToMsgSender.toStringForResponse());
            for (ClientThread ct: clients) {
                if(ct.getUsername().equals(tmp.getReceiver())){
                    Message responseToMsgReceiver = new Message(Message.RESPONSE_PRIVATE_CHAT, "SERVER", tmp.getReceiver(), System.currentTimeMillis(), msgListString);
                    ct.sendMessage(responseToMsgReceiver.toStringForResponse());
                    break;
                }
            }
        }

        private void sendGroupMessageHistory(String title) {
            String msgListString = groupMessageHistory.get(title).stream().map(Message::toString).collect(Collectors.joining(Message.MSG_DELIMITER));
            ArrayList<String> memberList = groupMemberLists.get(title);
            for (ClientThread ct: clients) {
                if (memberList.contains(ct.getUsername())) {
                    Message responseToMsgReceiver = new Message(Message.RESPONSE_GROUP_CHAT, "SERVER", ct.getUsername(), System.currentTimeMillis(), msgListString);
                    ct.sendMessage(responseToMsgReceiver.toStringForResponse());
                }
            }
        }
    }
}