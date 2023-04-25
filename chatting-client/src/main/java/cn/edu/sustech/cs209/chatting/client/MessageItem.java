package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.Util;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

public class MessageItem extends HBox {
    private ImageView avatarImageView;
    private final Label infoLabel;
    private final TextFlow contentTextFlow;

    public MessageItem(String avatarImagePath, String info, String content, boolean isLeft) {
        this.avatarImageView = new ImageView(Util.getLocalFile(avatarImagePath));
        this.avatarImageView.setFitWidth(40);
        this.avatarImageView.setFitHeight(40);

        this.contentTextFlow = new TextFlow(new Text(content.replace(Message.DELIMITER_FOR_NEW_LINE, "\r\n")));
        this.contentTextFlow.setPrefWidth(300);
        this.contentTextFlow.setLineSpacing(5.0);
        this.contentTextFlow.setTextAlignment(TextAlignment.LEFT);

        this.infoLabel = new Label(info);
        this.infoLabel.setFont(new Font(10));
        this.infoLabel.setTextFill(Color.grayRgb(150));

        VBox contentVBox = new VBox();
        contentVBox.setAlignment(isLeft? Pos.CENTER_LEFT:Pos.CENTER_RIGHT);
        contentVBox.getChildren().add(this.contentTextFlow);
        contentVBox.setPadding(new Insets(2, 5, 2, 5));
        if (isLeft) {
            contentVBox.setStyle("-fx-background-color: #E5E5E5; -fx-background-radius: 5px;");
        }
        else {
            contentVBox.setStyle("-fx-background-color: #95EC69; -fx-background-radius: 5px;");
        }

        VBox avatarVBox = new VBox();
        avatarVBox.setAlignment(isLeft? Pos.TOP_LEFT:Pos.TOP_RIGHT);
        avatarVBox.getChildren().add(this.avatarImageView);
        VBox.setVgrow(avatarVBox, Priority.ALWAYS);

        GridPane messagePane = new GridPane();
        messagePane.setAlignment(isLeft? Pos.CENTER_LEFT:Pos.CENTER_RIGHT);
        messagePane.setHgap(10);
        messagePane.setVgap(5);
        messagePane.setPadding(new Insets(5));
        if (isLeft) {
            messagePane.add(avatarVBox, 0, 0, 1, 2);
            messagePane.add(this.infoLabel, 1, 0);
            messagePane.add(contentVBox, 1, 1);
        }
        else {
            messagePane.add(avatarVBox, 1, 0, 1, 2);
            messagePane.add(this.infoLabel, 0, 0);
            messagePane.add(contentVBox, 0, 1);
        }
        this.getChildren().add(messagePane);
    }
}