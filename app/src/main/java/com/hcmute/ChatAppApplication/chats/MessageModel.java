package com.hcmute.ChatAppApplication.chats;

public class MessageModel {

    //declare user sent message
    private String from;
    //declare message
    private String message;
    //declare id for message
    private String message_id;
    //declare type for message
    private String message_type;
    //declare time send message
    private long time;
    //initiate message model
    public MessageModel() {
    }
    //initiate message model
    public MessageModel(String from, String message, String message_id, String message_type, long time) {
        this.from = from;
        this.message = message;
        this.message_id = message_id;
        this.message_type = message_type;
        this.time = time;
    }
    //func to get message
    public String getMessage() {
        return message;
    }
    //func to set message
    public void setMessage(String message) {
        this.message = message;
    }
    //func to get id of message
    public String getMessage_id() {
        return message_id;
    }
    //func to set message id
    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }
    //func to get type of message
    public String getMessage_type() {
        return message_type;
    }
    //func to set type of message
    public void setMessage_type(String message_type) {
        this.message_type = message_type;
    }
    //func to get user sent message
    public String getFrom() {
        return from;
    }
    //func to set user sent message
    public void setFrom(String from) {
        this.from = from;
    }
    //func to get time sent message
    public long getTime() {
        return time;
    }
    //func to set time sent message
    public void setTime(long time) {
        this.time = time;
    }
}
