package com.example.bewith;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bewith.listclass.CommentData;
import com.example.bewith.listclass.MyAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.w3c.dom.Text;

import java.util.ArrayList;

import javax.security.auth.Subject;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback {
    private GoogleMap mMap;
    private TextView no_data;
    private Button createAR;
    private FloatingActionButton fb_ar;
    private Spinner radius;
    private ArrayList<String> radiusList = new ArrayList<>();
    private CommentData commentData;
    private ListView cList;
    private MyAdapter myAdapter;
    public static ArrayList<CommentData> cData = new ArrayList<>();//comment 정보


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        no_data = findViewById(R.id.no_data);
        createFB();//플로팅버튼 생성(go to ar)
        createSpinner();//스피너 생성
        createComment();//Comment 생성하기

        cList=(ListView)findViewById(R.id.commentContents);//레이아웃 리스트뷰
        myAdapter=new MyAdapter(MainActivity.this,cData);//어뎁터에 어레이리스트를 붙임
        cList.setAdapter(myAdapter);//리스트를 어뎁터에 붙임

        changeList();//리스트뷰에 항목추가
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(cData.isEmpty()){
            no_data.setVisibility(View.VISIBLE);
        }
        else {
            no_data.setVisibility(View.INVISIBLE);
        }
        Toast.makeText(MainActivity.this, "화면이보입니다.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {//지도가 준비되면 실행됨
        mMap = googleMap;//구글맵을 전역변수 저장

        LatLng SEOUL = new LatLng(37.56, 126.97);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SEOUL, 10));

    }
    public void createComment(){
        createAR = findViewById(R.id.createAR);
        createAR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, Popup.class);
                startActivity(intent);

            }
        });
    }
    public void createFB(){
        fb_ar = findViewById(R.id.fb_AR);
        fb_ar.setOnClickListener(new View.OnClickListener() {  //플로팅 버튼 이벤트
            @Override
            public void onClick(View v) {
                int id = v.getId();
                switch (id) {
                    case R.id.fb_AR:
                        Log.d("sds","ar뷰 이동");
                        //Intent intent = new Intent(getActivity(), UnityPlayerActivity.class);
                        // startActivity(intent);
                        break;
                }
            }});
    }
    public void createSpinner(){
        radius = (Spinner)findViewById(R.id.radius);//스피너 선언
        radiusList.add("30m");
        radiusList.add("100m");
        radiusList.add("300m");
        radiusList.add("500m");
        radiusList.add("1km");
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, radiusList);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        radius.setAdapter(adapter1);
    }
    public void changeList(){//리스트뷰에 항목추가
        commentData = new CommentData(0,0,"아ㅋㅋ 개꿀딱ㅎㅎㅎㅎㅎㅎggggggggggggggggggggggggggg","35","127");
        cData.add(commentData);
        commentData = new CommentData(0,1,"아ㅋㅋ 개꿀딱ㅎㅎㅎㅎㅎㅎgggggggdddddddddddgggggggggggggggggggg","35","127");
        cData.add(commentData);
        commentData = new CommentData(0,1,"아ㅋㅋ 개꿀딱ㅎㅎㅎㅎㅎㅎgggggggggggddddddddddddgggggggggggggggg","35","127");
        cData.add(commentData);
        commentData = new CommentData(0,2,"아ㅋㅋ 개꿀딱ㅎㅎㅎㅎㅎㅎggggggggggdddddddddddggggggggggggggggg","35","127");
        cData.add(commentData);
        commentData = new CommentData(0,1,"아ㅋㅋ 개꿀딱ㅎㅎㅎㅎㅎㅎgggggggggggddddddddddddgggggggggggggggg","35","127");
        cData.add(commentData);
        commentData = new CommentData(0,1,"아ㅋㅋ 개꿀딱ㅎㅎㅎㅎㅎㅎgggggggggggddddddddddddgggggggggggggggg","35","127");
        cData.add(commentData);
        myAdapter.notifyDataSetChanged();//어뎁터 갱신
    }
}