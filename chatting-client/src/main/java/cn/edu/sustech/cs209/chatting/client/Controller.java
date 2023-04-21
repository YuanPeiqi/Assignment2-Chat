package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.Util;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Controller implements Initializable {
    @FXML
    public ListView<Message> chatContentList;
    public ListView<ChatItem> chatList;
    public TextArea inputArea;
    public Label currentUsername;
    public Label currentOnlineCnt;
    public Label currentChatTitle;
    private PrintWriter out;
    private String username;
    private String chatTitle;
    private final HashMap<String, String> privateAvatarMap = new HashMap<>();
    private String groupLogoPath = "C:\\Users\\Administrator\\Desktop\\Assignment2-Chat\\chatting-client\\src\\main\\resources\\cn\\edu\\sustech\\cs209\\chatting\\client\\groupAvatar\\group.png";
    private boolean isGroup;
    private final String HOST = "localhost";
    private final int PORT = 8888;
    private List<String> clientsList = new ArrayList<>();

    public List<String> getClientList() {
        return clientsList;
    }

    public void setClientList(List<String> clients) {
        this.clientsList = clients;
    }

    private void sendToServer(Message message) {
        out.println(message.toString());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        Dialog<String> dialog = new TextInputDialog();
        dialog.setTitle("Login");
        dialog.setHeaderText(null);
        dialog.setContentText("Username:");

        Optional<String> input = dialog.showAndWait();
        if (input.isPresent() && !input.get().isEmpty()) {
            this.username = input.get();
            this.currentUsername.setText(String.format("Current User: %s", this.username));
            this.inputArea.setWrapText(true);
            this.chatTitle = "";
            try {
                Socket client = new Socket(HOST, PORT);
                // Create input and output streams
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out = new PrintWriter(client.getOutputStream(), true);
                // Send the username to the server
                Message requestJoinMsg = new Message(Message.REQUEST_TO_JOIN, this.username, "SERVER", System.currentTimeMillis(), Message.REQUEST_TO_JOIN);
                sendToServer(requestJoinMsg);
                String msg_str;
                boolean allowToJoin = true;
                while ((msg_str = in.readLine()) != null) {
                    if (msg_str.startsWith(Message.ERROR_DUPLICATE_USERNAME)) {
                        Util.systemAlert("提示", "用户名重复, 请重新进入聊天室");
                        System.out.println("Duplicate username: " + this.username + ", exiting");
                        allowToJoin = false;
                        in.close();
                        out.close();
                        client.close();
                        Platform.exit();
                        break;
                    }
                    else if (msg_str.startsWith(Message.ALLOW_TO_JOIN)) {
                        break;
                    }
                }
                if (allowToJoin) { new Thread(new Controller.MessageHandler(client)).start(); }
                if (allowToJoin) { chatContentList.setCellFactory(new MessageCellFactory()); }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            Util.systemAlert("提示", "用户名不能为空!");
            System.out.println("Invalid username " + input + ", exiting");
            Platform.exit();
        }
    }

    private void requestPrivateChat(String title){
        Message requestPrivateChatMessage = new Message(Message.REQUEST_PRIVATE_CHAT, this.username, this.chatTitle, System.currentTimeMillis(), title);
        sendToServer(requestPrivateChatMessage);
    }

    @FXML
    public void createPrivateChat() {
        AtomicReference<String> user = new AtomicReference<>();
        Stage stage = new Stage();
        stage.setTitle("Choose a user to chat!");
        stage.setWidth(400.0);
        ComboBox<String> userSel = new ComboBox<>();
        userSel.setPrefWidth(200.0);
        // FIXME: get the user list from server, the current user's name should be filtered out
        synchronized (this) {
            userSel.getItems().addAll(getClientList());
        }
        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            user.set(userSel.getSelectionModel().getSelectedItem());
            stage.close();
        });
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(userSel, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();
        // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name
        // Get the selected user name
        String selectedUser = user.get();
        if(selectedUser != null && !selectedUser.equals("")){
            this.chatTitle = selectedUser;
            this.currentChatTitle.setText(selectedUser);
            this.isGroup = false;
            boolean chatItemExists = false;
            for (Object item : chatList.getItems()) {
                if (item instanceof ChatItem && ((ChatItem) item).getTitle().equals(selectedUser)) {
                    chatItemExists = true;
                    break;
                }
            }
            if (!chatItemExists) {
                ChatItem newChatItem = new ChatItem(selectedUser, Util.time2String(), this.privateAvatarMap.get(selectedUser), false);
                newChatItem.setOnMouseClicked(event -> {
                    System.out.println("点击事件");
//                    if (event.getClickCount() == 1) {
//                        System.out.println("点击1次1");
//                    }
                    Controller.this.isGroup = false;
                    Controller.this.chatTitle = newChatItem.getTitle();
                    requestPrivateChat(Controller.this.chatTitle);
                });
                this.chatList.getItems().add(newChatItem);
            }
            requestPrivateChat(this.chatTitle);
        }

    }

    private void requestGroupChat(String usersAndTitle){
        Message requestGroupChatMessage = new Message(Message.REQUEST_GROUP_CHAT, this.username, this.chatTitle, System.currentTimeMillis(), usersAndTitle);
        sendToServer(requestGroupChatMessage);
    }

    /**
     * A new dialog should contain a multi-select list, showing all user's name.
     * You can select several users that will be joined in the group chat, including yourself.
     * <p>
     * The naming rule for group chats is similar to WeChat:
     * If there are > 3 users: display the first three usernames, sorted in lexicographic order, then use ellipsis with the number of users, for example:
     * UserA, UserB, UserC... (10)
     * If there are <= 3 users: do not display the ellipsis, for example:
     * UserA, UserB (2)
     */
    @FXML
    public void createGroupChat() {
        String usersAndTitle = showGroupChatConfigDialog();
        if (usersAndTitle != null) {
            String groupTitle = usersAndTitle.split("@")[1];
            String userListStr = usersAndTitle.split("@")[0];
            this.chatTitle = groupTitle;
            this.isGroup = true;
            this.currentChatTitle.setText(groupTitle + "(Group Chat)");
            boolean chatItemExists = false;
            for (Object item : chatList.getItems()) {
                if (item instanceof ChatItem && ((ChatItem) item).getTitle().equals(groupTitle + "(Group Chat)")) {
                    chatItemExists = true;
                    break;
                }
            }
            if (!chatItemExists) {
                ChatItem newChatItem = new ChatItem(groupTitle + "(Group Chat)", Util.time2String(), this.groupLogoPath, true);
                newChatItem.setOnMouseClicked(event -> {
                    Controller.this.isGroup = true;
                    Controller.this.chatTitle = newChatItem.getTitle().replace("(Group Chat)", "");
                    requestPrivateChat(Controller.this.chatTitle);
                });
                this.chatList.getItems().add(newChatItem);
            }
            requestGroupChat(userListStr);
        }
    }

    private String showGroupChatConfigDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Create Group Chat");
        dialog.setHeaderText("Please enter a group chat name and select users");

        ButtonType createButtonType = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        TextField groupTitleField = new TextField();

        CheckBox[] userCheckBoxes = new CheckBox[this.clientsList.size()];
        for (int i = 0; i < this.clientsList.size(); i++) {
            userCheckBoxes[i] = new CheckBox(this.clientsList.get(i));
        }
        GridPane contentPane = new GridPane();
        contentPane.setHgap(10);
        contentPane.setVgap(10);
        contentPane.setPadding(new Insets(20));
        contentPane.addRow(0, new Label("Chat title:"), groupTitleField);
        contentPane.addRow(2, new Label("Select users:"));
        for (int i = 0; i < userCheckBoxes.length; i++) {
            contentPane.addRow(i + 3, userCheckBoxes[i]);
        }
        dialog.getDialogPane().setContent(contentPane);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType && groupTitleField.getText() != null && !groupTitleField.getText().equals("")) {
                StringBuilder selectedUsers = new StringBuilder(this.username);
                for (CheckBox checkBox : userCheckBoxes) {
                    if (checkBox.isSelected()) {
                        selectedUsers.append(",").append(checkBox.getText());
                    }
                }
                return selectedUsers + "@" + groupTitleField.getText();
            }
            return null;
        });
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    /**
     * Sends the message to the currently selected chat.
     * Blank messages are not allowed.
     * After sending the message, you should clear the text input field.
     */
    @FXML
    public void doSendMessage() {
        String content = inputArea.getText();
        if(!content.replaceAll("(?!\\r)\\n", "").equals("")){
            content = content.replaceAll("(?!\\r)\\n", Message.DELIMITER_FOR_NEW_LINE);
            inputArea.setText("");
            String header = this.isGroup? Message.SEND_GROUP_MESSAGE:Message.SEND_PRIVATE_MESSAGE;
            if (content.length() > 0){
                Message msg = new Message(header, this.username, this.chatTitle, System.currentTimeMillis(), content);
                sendToServer(msg);
            }
        }
        else {
            Util.systemAlert("提示", "不能发送空消息!");
        }
    }

    /**
     * You may change the cell factory if you changed the design of {@code Message} model.
     * Hint: you may also define a cell factory for the chats displayed in the left panel, or simply override the toString method.
     */
    private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
        @Override
        public ListCell<Message> call(ListView<Message> param) {
            return new ListCell<Message>() {
                @Override
                public void updateItem(Message msg, boolean empty) {
                    super.updateItem(msg, empty);
                    if (empty || Objects.isNull(msg)) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }
                    MessageItem wrapper = new MessageItem(Controller.this.privateAvatarMap.get(msg.getSender()),
                            msg.getSender() + " " + Util.time2String(msg.getTimestamp()),
                            msg.getContent(), !msg.getSender().equals(Controller.this.username));
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }

    public void shutdown() {
        Message leaveMessage = new Message(Message.REQUEST_TO_LEAVE, this.username, "SERVER", System.currentTimeMillis(), "");
        sendToServer(leaveMessage);
    }

    private class MessageHandler implements Runnable {
        private final Socket client;

        public MessageHandler(Socket client) {
            this.client = client;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith(Message.ALLOW_TO_LEAVE)) {
                        break;
                    }
                    else if (message.startsWith(Message.UPDATE_CLIENT_LIST)) {
                        List<String> tmp = new ArrayList<>(Arrays.asList(Message.parse(message).getContent().split(",")));
                        List<String> usernameTmpList = new ArrayList<>();
                        int currentUserCnt = tmp.size();
                        Platform.runLater(() -> Controller.this.currentOnlineCnt.setText("Online: " + currentUserCnt));
                        for (String s: tmp) {
                            String name = s.split(":")[0];
                            String avatar = s.split(":")[1];
                            Controller.this.privateAvatarMap.put(name, "C:\\Users\\Administrator\\Desktop\\Assignment2-Chat\\chatting-client\\src\\main\\resources\\cn\\edu\\sustech\\cs209\\chatting\\client\\privateAvatar\\" + avatar + ".png");
                            usernameTmpList.add(name);
                        }
                        usernameTmpList.remove(Controller.this.username);
                        synchronized (Controller.this) {
                            setClientList(usernameTmpList);
                        }
                    }
                    else if (message.startsWith(Message.RESPONSE_PRIVATE_CHAT)) {
                        List<Message> msgList = Arrays.stream(Message.parseForResponse(message).getContent().split(Message.MSG_DELIMITER)).sequential()
                                .filter(s -> s != null&&!s.equals("")).map(Message::parse).collect(Collectors.toList());
                        if (!isGroup) {
                            String currentChatKey = Util.getKey(Controller.this.username, Controller.this.chatTitle);
                            if (msgList.size() > 0 && currentChatKey.equals(Util.getKey(msgList.get(0).getSender(), msgList.get(0).getReceiver()))) {
                                Platform.runLater(() -> {
                                    Controller.this.chatContentList.getItems().clear();
                                    Controller.this.chatContentList.getItems().addAll(msgList);
                                });
                            }
                            else if (msgList.size() == 0) {
                                Controller.this.chatContentList.getItems().clear();
                            }
                        }
                    }
                    else if (message.startsWith(Message.RESPONSE_GROUP_CHAT)) {
                        List<Message> msgList = Arrays.stream(Message.parseForResponse(message).getContent().split(Message.MSG_DELIMITER)).sequential()
                                .filter(s -> s != null && !s.equals("")).map(Message::parse).collect(Collectors.toList());
                        if (isGroup) {
                            if (msgList.size() > 0 && Controller.this.chatTitle.equals(msgList.get(0).getReceiver())) {
                                Platform.runLater(() -> {
                                    Controller.this.chatContentList.getItems().clear();
                                    Controller.this.chatContentList.getItems().addAll(msgList);
                                });
                            }
                            else if (msgList.size() == 0) {
                                Controller.this.chatContentList.getItems().clear();
                            }
                        }
                    }
                    System.out.println(message);
                }
                in.close();
                System.out.println("Controller closed.");
                System.out.println("MessageHandler closed.");
                Platform.exit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
