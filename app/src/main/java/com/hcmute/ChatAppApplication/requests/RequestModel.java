package com.hcmute.ChatAppApplication.requests;


//declare class for request model
public class RequestModel {
    //declare user id
    private String userId;
    //declare user name
    private String userName;
    //declare photo file
    private String photoFile;
    //initiate model
    public RequestModel(String userId, String userName, String photoFile) {
        this.userId = userId;
        this.userName = userName;
        this.photoFile = photoFile;
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
    //func to set username
    public void setUserName(String userName) {
        this.userName = userName;
    }
    //func to get photo file
    public String getPhotoFile() {
        return photoFile;
    }
    //func to set photo file
    public void setPhotoFile(String photoFile) {
        this.photoFile = photoFile;
    }
}
