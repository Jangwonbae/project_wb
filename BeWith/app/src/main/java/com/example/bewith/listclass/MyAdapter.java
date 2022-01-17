package com.example.bewith.listclass;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.bewith.R;

import java.util.ArrayList;

public class MyAdapter extends BaseAdapter {
    private Context ctx;
    private ArrayList<CommentData> data;//원본
    private String text;

    public MyAdapter(Context ctx,ArrayList<CommentData> data){
        this.ctx=ctx;
        this.data=data;
    }
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view ==null){
            LayoutInflater inflater = LayoutInflater.from(ctx);
            view = inflater.inflate(R.layout.commentlist,viewGroup,false);
        }

        TextView text1 = (TextView)view.findViewById(R.id.categoryText);
        TextView text2 = (TextView)view.findViewById(R.id.contentsText);
        switch (data.get(i).category) {
            case 0:
                text = "리뷰";
                break;
            case 1:
                text = "꿀팁";
                break;
            case 2:
                text = "기록";
                break;
        }
        text1.setText(text);
        text2.setText(data.get(i).text);

        return view;
    }
}