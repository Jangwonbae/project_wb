package com.example.bewith;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Popup extends Activity {

    private ArrayList<String> categoryList = new ArrayList<>();
    private Spinner category;
    private TextView edit_text;
    private String UUID;
    private double myLatitude;
    private double myLogitude;
    private String contents;
    private String categoryText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_popup);
        createCategorySpinner();//카테고리 스피너 생성
        edit_text = findViewById(R.id.edit_text);
        Intent data = getIntent();
        UUID = data.getStringExtra("UUID");
        myLatitude = data.getDoubleExtra("latitude", 0);
        myLogitude = data.getDoubleExtra("longitude", 0);

    }
    public void createCategorySpinner(){//카테고리 스피너 생성
        category = (Spinner)findViewById(R.id.category);//스피너 선언
        categoryList.add("리뷰");
        categoryList.add("꿀팁");
        categoryList.add("기록");
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category.setAdapter(adapter1);
        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        categoryText = "리뷰";
                        break;
                    case 1:
                        categoryText = "꿀팁";
                        break;
                    case 2:
                        categoryText = "기록";
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    //확인 버튼 클릭
    public void mOnClose(View v){
        int id = v.getId();
        switch (id) {
            case R.id.okBtn:
                contents = edit_text.getText().toString();//텍스트 내용
                Log.d("태그","UUID : "+UUID +" ,카테고리 : "+categoryText+", 내용 : "+contents+", 위도 : "+myLatitude+", 경도 : "+myLogitude);
                /*
                해당 정보들을 서버로 보내고 AR씬으로 이동
                 */
                break;
            case R.id.noBtn:
                break;
        }
        //액티비티(팝업) 닫기
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

}
