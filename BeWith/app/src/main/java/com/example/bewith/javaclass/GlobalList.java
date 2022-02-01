package com.example.bewith.javaclass;

import android.app.Application;

import com.example.bewith.listclass.CommentData;

import java.util.ArrayList;

public class GlobalList extends Application {
    private ArrayList<CommentData> cData = new ArrayList<>();//comment 정보
    public ArrayList<CommentData> getcData() {
        return cData;
    }

    public void setcData( CommentData commentData ) {
        cData.add(commentData);
    }
    public void cleancData(){
        cData.clear();
    }
}
