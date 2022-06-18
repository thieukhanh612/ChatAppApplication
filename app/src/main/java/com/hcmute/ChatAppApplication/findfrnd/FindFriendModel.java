package com.hcmute.ChatAppApplication.findfrnd;
//declare model for find friend
public class FindFriendModel {
    //declare person name
    private String personName;
    //declare file name of photo
    private String photoFileName;
    //declare user id
    private String userId;
    //declare boolean for request has sent or not
    private boolean requestHasSent;
    //initiate model
    public FindFriendModel(String personName, String photoFileName, String userId, boolean requestHasSent) {
        this.personName = personName;
        this.photoFileName = photoFileName;
        this.userId = userId;
        this.requestHasSent = requestHasSent;
    }
    //func to get person name
    public String getPersonName() {
        return personName;
    }
    //func to set person name
    public void setPersonName(String personName) {
        this.personName = personName;
    }
    //func to get file name of photo
    public String getPhotoFileName() {
        return photoFileName;
    }
    //func to set file name of photo
    public void setPhotoFileName(String photoFileName) {
        this.photoFileName = photoFileName;
    }
    //func to get user id
    public String getUserId() {
        return userId;
    }
    //func to set user id
    public void setUserId(String userId) {
        this.userId = userId;
    }
    //func to get request has sent or not
    public boolean isRequestHasSent() {
        return requestHasSent;
    }
    //func to set request has sent or not
    public void setRequestHasSent(boolean requestHasSent) {
        this.requestHasSent = requestHasSent;

    }
}
