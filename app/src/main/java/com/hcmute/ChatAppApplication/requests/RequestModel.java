package com.hcmute.ChatAppApplication.requests;

public class RequestModel {

    private String userId;
    private String userName;
    private String photoFile;

    public RequestModel(String userId, String userName, String photoFile) {
        this.userId = userId;
        this.userName = userName;
        this.photoFile = photoFile;
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

    public String getPhotoFile() {
        return photoFile;
    }

    public void setPhotoFile(String photoFile) {
        this.photoFile = photoFile;
    }
}
