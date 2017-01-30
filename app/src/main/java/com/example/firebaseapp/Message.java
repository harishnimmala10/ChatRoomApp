package com.example.firebaseapp;

public class Message {
    private String message,imageUrl,time,status;

    public Message(String message, String imageUrl, String time,String status) {
        this.message = message;
        this.imageUrl = imageUrl;
        this.time = time;
        this.status = status;
    }

    public Message() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


}
