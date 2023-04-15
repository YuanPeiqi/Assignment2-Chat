package cn.edu.sustech.cs209.chatting.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final Vector<ChatHandler> handlers = new Vector<>();

    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(8888);
            System.out.println("Server started on port 8888");
            // noinspection InfiniteLoopStatement
            while (true) {
                Socket client = server.accept();
                ChatHandler handler = new ChatHandler(client);
                handlers.add(handler);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcast(String message) {
        synchronized (handlers) {
            for (ChatHandler handler : handlers) {
                handler.sendMessage(message);
            }
        }
    }
}

class ChatHandler extends Thread {
    private final Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private String username;

    public ChatHandler(Socket socket) {
        client = socket;

        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);
            username = in.readLine();
            System.out.println("Client \"" + username + "\" has connected to the server.");
            ChatServer.broadcast("Welcome to the chat room, " + username + "!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void run() {
        String message;
        try {
            while ((message = in.readLine()) != null) {
//                System.out.println("Receive a message");
                if (message.equals("bye")) {
                    break;
                }
                ChatServer.broadcast(username + ": " + message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                client.close();
                ChatServer.broadcast(username + " has left the chat.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}