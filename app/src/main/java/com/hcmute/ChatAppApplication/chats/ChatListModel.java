package com.hcmute.ChatAppApplication.chats;
//declare chat list model
public class ChatListModel {
    // declare user id
    private String userId;
    //declare user name
    private String userName;
    //declare name file of photo
    private String photoFileName;
    //declare last message of user
    private String lastMessage;
    //declare number of unread message
    private String unreadMessageCount;
    //declare time of last message read
    private String lastSeenTime;
    //initiate chat list model
    public ChatListModel(String userId, String userName, String photoFileName, String lastMessage, String unreadMessageCount, String lastSeenTime) {
        this.userId = userId;
        this.userName = userName;
        this.photoFileName = photoFileName;
        this.lastMessage = lastMessage;
        this.unreadMessageCount = unreadMessageCount;
        this.lastSeenTime = lastSeenTime;
    }
    //func to get user id
    public String getUserId() {
        return userId;
    }
    //func to set user id
    public void setUserId(String userId) {
        this.userId = userId;
    }
    //func to get user name
    public String getUserName() {
        return userName;
    }
    //func to set user name
    public void setUserName(String userName) {
        this.userName = userName;
    }
    //func to get name file of user photo
    public String getPhotoFileName() {
        return photoFileName;
    }
    //func to set name file of user photo
    public void setPhotoFileName(String photoFileName) {
        this.photoFileName = photoFileName;
    }
    //func to get last message of user
    public String getLastMessage() {
        return lastMessage;
    }
    //func to set last message of user
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
    //func to get number of unread message
    public String getUnreadMessageCount() {
        return unreadMessageCount;
    }
    //func to set number of unread message
    public void setUnreadMessageCount(String unreadMessageCount) {
        this.unreadMessageCount = unreadMessageCount;
    }
    //func to get last seen message time
    public String getLastSeenTime() {
        return lastSeenTime;
    }
    //func to set last seen message time
    public void setLastSeenTime(String lastSeenTime) {
        this.lastSeenTime = lastSeenTime;
    }
}
