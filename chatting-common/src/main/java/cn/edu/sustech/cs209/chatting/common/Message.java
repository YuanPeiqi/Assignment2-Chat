package cn.edu.sustech.cs209.chatting.common;

import java.util.Arrays;

public class Message {
    private final Long timestamp;
    private final String sender;
    private final String receiver;
    private final String content;
    private final String command;
    public static final String DELIMITER = "%DELIMITER%";
    public static final String DELIMITER_FOR_RESPONSE = "%DELIMITER_FOR_RESPONSE%";
    public static final String MSG_DELIMITER = "%MSG_DELIMITER%";
    public static final String DELIMITER_FOR_NEW_LINE = "%NEW_LINE%";
    public static final String UPDATE_CLIENT_LIST = "UPDATE_CLIENT_LIST";
    public static final String LEAVE = "LEAVE";
    public static final String SYSTEM_INFO = "SYSTEM";
    public static final String REQUEST_PRIVATE_CHAT = "REQUEST_PRIVATE_CHAT";
    public static final String REQUEST_GROUP_CHAT = "REQUEST_GROUP_CHAT";
    public static final String RESPONSE_PRIVATE_CHAT = "RESPONSE_PRIVATE_CHAT";
    public static final String RESPONSE_GROUP_CHAT = "RESPONSE_GROUP_CHAT";
    public static final String SEND_PRIVATE_MESSAGE = "SEND_PRIVATE_MESSAGE";
    public static final String SEND_GROUP_MESSAGE = "SEND_GROUP_MESSAGE";

    public static final String ERROR_DUPLICATE_USERNAME = "DUPLICATE_USERNAME";
    public static final String REQUEST_TO_JOIN = "REQUEST_TO_JOIN";
    public static final String ALLOW_TO_JOIN = "ALLOW_TO_JOIN";
    public Message(String command, String sender, String receiver, Long timestamp, String content) {
        this.timestamp = timestamp;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getContent() {
        return content;
    }

    public String toString() {
        return command + DELIMITER + sender + DELIMITER + receiver
                + DELIMITER + timestamp.toString() + DELIMITER + content;
    }

    public String toStringForResponse() {
        return command + DELIMITER_FOR_RESPONSE + sender + DELIMITER_FOR_RESPONSE + receiver
                + DELIMITER_FOR_RESPONSE + timestamp.toString() + DELIMITER_FOR_RESPONSE + content;
    }

    public static Message parse(String message) {
        return getMessage(message, DELIMITER);
    }

    public static Message parseForResponse(String message) {
        return getMessage(message, DELIMITER_FOR_RESPONSE);
    }

    private static Message getMessage(String message, String delimiter) {
        System.out.println("Message中接收到的的参数: " + message);
        String[] parts = message.split(delimiter);
        System.out.println(Arrays.toString(parts));
        String command = parts[0];
        String sender = parts[1];
        String receiver = parts[2];
        Long timestamp = Long.parseLong(parts[3]);
        String content = "";
        if (parts.length > 4) {
            content = parts[4];
        }
        return new Message(command, sender, receiver, timestamp, content);
    }
}
