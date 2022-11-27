package com.ransankul.chatapp.Model;

public class AllUserModel {

    private String userName;
    private String about;
    private String profileImage;
    private String uid;
    private String phoneumber;
    private String uidkey;
    private String token;

    public AllUserModel(String uid) {
        this.uid = uid;
    }


    public AllUserModel() {
    }

    public String getUid() {
        return uid;
    }

    public AllUserModel(String userName, String about, String profileImage, String uid, String phoneumber) {
        this.userName = userName;
        this.about = about;
        this.profileImage = profileImage;
        this.uid = uid;
        this.phoneumber = phoneumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getUid(String key) {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPhoneumber() {
        return phoneumber;
    }

    public void setPhoneumber(String phoneumber) {
        this.phoneumber = phoneumber;
    }

    public String getUidkey() {
        return uidkey;
    }

    public void setUidkey(String uidkey) {
        this.uidkey = uidkey;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}