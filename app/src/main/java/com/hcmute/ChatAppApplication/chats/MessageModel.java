package com.hcmute.ChatAppApplication.chats;

public class MessageModel {


    private String from;
    private String message;
    private String message_id;
    private String message_type;

    private long time;

    public MessageModel() {
    }

    public MessageModel(String from, String message, String message_id, String message_type, long time) {
        this.from = from;
        this.message = message;
        this.message_id = message_id;
        this.message_type = message_type;
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getMessage_type() {
        return message_type;
    }

    public void setMessage_type(String message_type) {
        this.message_type = message_type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
