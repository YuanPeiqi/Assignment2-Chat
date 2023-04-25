package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Util;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ChatItem extends HBox {
    private Label title;
    private Label time;
    private boolean isGroup;
    public ChatItem(String title, String time, String avatarImagePath, boolean isGroup) {
        super(10); // spacing between children

        Image image = new Image(Util.getLocalFile(avatarImagePath));
        ImageView avatar = new ImageView();
        avatar.setImage(image);
        avatar.setFitWidth(50);
        avatar.setFitHeight(50);

        this.isGroup = isGroup;
        this.title = new Label(title);
        this.title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        this.time = new Label(time);
        this.time.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        this.time.setTextFill(Color.GRAY);

        VBox nameTime = new VBox(5, this.title, this.time);
        nameTime.setAlignment(Pos.CENTER_LEFT);

        this.getChildren().addAll(avatar, nameTime);
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public String getTitle() {
        return this.title.getText();
    }

    public void setTime(String time) {
        this.time.setText(time);
    }

    public String getTime() {
        return time.getText();
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setIsGroup(boolean group) {
        isGroup = group;
    }
}
