package com.example.bewith.listclass;

public class CommentData {
    public int _id;
    public String UUID;
    public int category;
    public String text;
    public String latitude;
    public String logitude;

    public CommentData(int _id,String UUID, int category,String text,String latitude,String logitude){
        this._id=_id;
        this.UUID=UUID;
        this.category=category;
        this.text=text;
        this.latitude=latitude;
        this.logitude=logitude;

    }
}