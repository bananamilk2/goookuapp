package com.example.howard.myapplication;

/**
 * Created by Howard on 2017/8/24.
 */
public class MessageBean {
    private String userImg;         //用户头像
    private String userName;        //用户昵称
    private String occupyUserName;  //打赏对象
    private String occupyUserImg;   //打赏对象头像
    private int userGender;         //用户性别
    private int occupyUserGender;   //被打赏用户性别
    private int type = 0;           //消息类型，默认为text
    private int time;               //霸屏时间
    private int giftType;           //礼物类型
    private String giftUser;        //
    private String image;           //用户发送的图片
    private String text;            //用户发送的消息
    private String timestamp;       //消息时间戳

    public MessageBean() {

    }

    @Override
    public String toString() {
        return  "userName = "+userName + "    " + "userImg = "+userImg + "    " +
                "userGender = "+userGender + "    " + "messageType = "+type + "    " +
                "image = " +image + "   " + "text = " +text;
    }

    public String getUserImg() {
        return userImg;
    }

    public void setUserImg(String userImg) {
        this.userImg = userImg;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getOccupyUserName() {
        return occupyUserName;
    }

    public void setOccupyUserName(String occupyUserName) {
        this.occupyUserName = occupyUserName;
    }

    public String getOccupyUserImg() {
        return occupyUserImg;
    }

    public void setOccupyUserImg(String occupyUserImg) {
        this.occupyUserImg = occupyUserImg;
    }

    public int getUserGender() {
        return userGender;
    }

    public void setUserGender(int userGender) {
        this.userGender = userGender;
    }

    public int getOccupyUserGender() {
        return occupyUserGender;
    }

    public void setOccupyUserGender(int occupyUserGender) {
        this.occupyUserGender = occupyUserGender;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getGiftType() {
        return giftType;
    }

    public void setGiftType(int giftType) {
        this.giftType = giftType;
    }

    public String getGiftUser() {
        return giftUser;
    }

    public void setGiftUser(String giftUser) {
        this.giftUser = giftUser;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
