package cn.edu.sustech.cs209.chatting.common;

import javafx.scene.control.Alert;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class Util {
    public static String getKey(String sender, String receiver){
        String key;
        if(sender.compareTo(receiver) > 0){
            key = sender + "&" + receiver;
        }
        else {
            key = receiver + "&" + sender;
        }
        return key;
    }

    public static String getLocalFile(String path){
        try {
            File file = new File(path);
            return file.toURI().toURL().toString();
        }catch (Exception e) {
            return "";
        }
    }

    public static String time2String(Long time){
        DateTimeFormatter ftf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));
    }

    public static String time2String(){
        DateTimeFormatter ftf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.systemDefault()));
    }


    public static int getRandomInteger(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public static void systemAlert(String title, String info) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(info);
        alert.showAndWait();
    }
}
