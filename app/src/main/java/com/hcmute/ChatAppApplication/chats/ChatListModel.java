package com.hcmute.ChatAppApplication.chats;

public class ChatListModel {

    private String userId;
    private String userName;
    private String photoFileName;
    private String lastMessage;
    private String unreadMessageCount;
    private String lastSeenTime;

    public ChatListModel(String userId, String userName, String photoFileName, String lastMessage, String unreadMessageCount, String lastSeenTime) {
        this.userId = userId;
        this.userName = userName;
        this.photoFileName = photoFileName;
        this.lastMessage = lastMessage;
        this.unreadMessageCount = unreadMessageCount;
        this.lastSeenTime = lastSeenTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhotoFileName() {
        return photoFileName;
    }

    public void setPhotoFileName(String photoFileName) {
        this.photoFileName = photoFileName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getUnreadMessageCount() {
        return unreadMessageCount;
    }

    public void setUnreadMessageCount(String unreadMessageCount) {
        this.unreadMessageCount = unreadMessageCount;
    }

    public String getLastSeenTime() {
        return lastSeenTime;
    }

    public void setLastSeenTime(String lastSeenTime) {
        this.lastSeenTime = lastSeenTime;
    }
}
