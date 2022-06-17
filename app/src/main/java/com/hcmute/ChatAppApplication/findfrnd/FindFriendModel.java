package com.hcmute.ChatAppApplication.findfrnd;

public class FindFriendModel {

    private String personName;
    private String photoFileName;
    private String userId;
    private boolean requestHasSent;

    public FindFriendModel(String personName, String photoFileName, String userId, boolean requestHasSent) {
        this.personName = personName;
        this.photoFileName = photoFileName;
        this.userId = userId;
        this.requestHasSent = requestHasSent;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getPhotoFileName() {
        return photoFileName;
    }

    public void setPhotoFileName(String photoFileName) {
        this.photoFileName = photoFileName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isRequestHasSent() {
        return requestHasSent;
    }

    public void setRequestHasSent(boolean requestHasSent) {
        this.requestHasSent = requestHasSent;

    }
}
