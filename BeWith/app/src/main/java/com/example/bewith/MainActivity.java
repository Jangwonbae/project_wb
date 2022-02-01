package com.example.bewith;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.bewith.javaclass.Constants;
import com.example.bewith.javaclass.GlobalList;
import com.example.bewith.javaclass.UUIDClass;
import com.example.bewith.listclass.CommentData;
import com.example.bewith.listclass.MyAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.unity3d.player.UnityPlayerActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.Subject;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback {
    private GoogleMap mMap;
    private TextView no_data;
    private Button createAR;
    private FloatingActionButton fb_ar;
    private FloatingActionButton fb_reload;
    private Spinner radius;
    private ArrayList<String> radiusList = new ArrayList<>();
    private CommentData commentData;
    private SwipeMenuListView listview;
    private ListView cList;
    private MyAdapter myAdapter;
    public static ArrayList<CommentData> mData = new ArrayList<>();//내가 만든 commnet 정보
    public static ArrayList<CommentData> rData = new ArrayList<>();//반경 안에 있는 commnet 정보
    public static ArrayList<CommentData> cData;//comment 정보
    private double myLatitude;
    private double myLogitude;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    public static final int DEFAULT_LOCATION_REQUEST_PRIORITY = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
    public static final long DEFAULT_LOCATION_REQUEST_INTERVAL = 2000L;
    public static final long DEFAULT_LOCATION_REQUEST_FAST_INTERVAL = 2000L;
    public String UUID;
    public int radiusIndex;
    private static String IP_ADDRESS;
    private Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        radiusIndex = 0;
        IP_ADDRESS= Constants.IP_ADDRESS;
        UUID = UUIDClass.GetDeviceUUID(getApplicationContext());
        Intent data = getIntent();//시작위치 받아옴
        myLatitude = data.getDoubleExtra("latitude", 0);
        myLogitude = data.getDoubleExtra("longitude", 0);

        no_data = findViewById(R.id.no_data);

        cData=((GlobalList) getApplication() ).getcData();

        createFB();//플로팅버튼 생성(go to ar)

        createSpinner();//스피너 생성
        createComment();//Commen생성 버튼 생성하기
        listview = (SwipeMenuListView) findViewById(R.id.myCommentContents);//떙길 수 잇는 리스트 뷰
        cList = (ListView) findViewById(R.id.commentContents);//레이아웃 리스트뷰
        //땡길 수 있는 리스트뷰 설정
        listview.setMenuCreator(creator);
        listview.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {
            @Override
            public void onSwipeStart(int position) {
                // swipe start
                listview.smoothOpenMenu(position);
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
                listview.smoothOpenMenu(position);
            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
        listview.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {//삭제메소드
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                DeleteComment deleteComment = new DeleteComment();
                deleteComment.execute( "http://" + IP_ADDRESS + "/deleteComment.php", Integer.toString(mData.get(position)._id));

                return true;
            }
        });

        cList.setVisibility(View.GONE);
        myAdapter = new MyAdapter(MainActivity.this, mData);//어뎁터에 어레이리스트를 붙임
        listview.setAdapter(myAdapter);//리스트를 어뎁터에 붙임

    }

    @Override
    protected void onStart() {
        super.onStart();
        getMyLocation();
        UpdateComment updateComment = new UpdateComment();
        updateComment.execute( "http://" + IP_ADDRESS + "/getComment.php", "");

    }

    public int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    SwipeMenuCreator creator = new SwipeMenuCreator() {

        @Override// list 땡가는 메뉴 만들기
        public void create(SwipeMenu menu) {
            // create "첫번째" item
            SwipeMenuItem openItem = new SwipeMenuItem(
                    MainActivity.this);

            // create "delete" item
            SwipeMenuItem deleteItem = new SwipeMenuItem(
                    MainActivity.this);
            // set item background
            deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                    0x3F, 0x25)));
            // set item width
            deleteItem.setWidth(dpToPx(90));
            // set a icon
            deleteItem.setIcon(R.drawable.ic_baseline_delete_forever_24);
            // add to menu
            menu.addMenuItem(deleteItem);
        }
    };


    @Override
    public void onMapReady(final GoogleMap googleMap) {//지도가 준비되면 실행됨
        mMap = googleMap;//구글맵을 전역변수 저장
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //권한 없으면 리턴
            return;
        }
        mMap.setMyLocationEnabled(true);
        LatLng myLocation = new LatLng(myLatitude, myLogitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 18));

    }

    public void createComment() {//AR 코멘트 생성 버튼
        createAR = findViewById(R.id.createAR);
        createAR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMyLocation();
                Intent intent = new Intent(MainActivity.this, Popup.class);
                intent.putExtra("UUID", UUID);
                intent.putExtra("latitude", myLatitude);
                intent.putExtra("longitude", myLogitude);
                startActivity(intent);
            }
        });
    }

    public void createFB() {//플로팅버튼 생성
        fb_ar = findViewById(R.id.fb_AR);
        fb_reload = findViewById(R.id.fb_reload);
        fb_ar.setOnClickListener(new View.OnClickListener() {//AR카메라 이동 버튼
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UnityPlayerActivity.class);
                 startActivity(intent);
            }
        });
        fb_reload.setOnClickListener(new View.OnClickListener() {//새로고침 버튼
            @Override
            public void onClick(View v) {
                getMyLocation();
                UpdateComment updateComment = new UpdateComment();
                updateComment.execute( "http://" + IP_ADDRESS + "/getComment.php", "");
            }
        });
    }

    public void createSpinner() {
        radius = (Spinner) findViewById(R.id.radius);//스피너 선언
        radiusList.add("My Comment");
        radiusList.add("30m");
        radiusList.add("100m");
        radiusList.add("300m");
        radiusList.add("500m");
        radiusList.add("1km");
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, radiusList);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        radius.setAdapter(adapter1);
        radius.setSelection(radiusIndex);//초기값
        radius.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {//스피너가 선택되었을 때
                radiusIndex = position;
                if (position == 0) {//반경 리스트가 My Comment면
                    if (mData.isEmpty()) {
                        no_data.setVisibility(View.VISIBLE);
                    } else {
                        no_data.setVisibility(View.INVISIBLE);
                    }
                    listview.setVisibility(View.VISIBLE);//땡길 수 있는 리스트를 보이게
                    cList.setVisibility(View.GONE);//일반 리스트를 안보이게
                    myAdapter = new MyAdapter(MainActivity.this, mData);//어뎁터에 어레이리스트를 붙임
                    listview.setAdapter(myAdapter);//땡길 수 있는 리스트를 어뎁터에 붙임
                } else {//다른게 선택되면
                   changeRadiusData();

                    myAdapter = new MyAdapter(MainActivity.this, rData);//어뎁터에 어레이리스트를 붙임
                    cList.setAdapter(myAdapter);//일반 리스트를 어뎁터에 붙임
                    }
                myAdapter.notifyDataSetChanged();//어뎁터 갱신
                }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {//무시하면됨(아무 것도 선택안됬을 때)
            }
        });
    }

    public void changeRadiusData(){

            int m=0;
            rData.clear();
            switch (radiusIndex){
                case 1:
                    m=30;
                    break;
                case 2:
                    m=100;
                    break;
                case 3:
                    m=300;
                    break;
                case 4:
                    m=500;
                    break;
                case 5:
                    m=1000;
                    break;
            }

            Location locationA = new Location("point A");//내위치
            locationA.setLatitude(myLatitude);
            locationA.setLongitude(myLogitude);

            for(int i = 0; i<cData.size();i++){//선택된 반경 안에 있는지 거리 검사후 리스트 삽입
                Location locationB = new Location("point B");

                locationB.setLatitude(Double.parseDouble(cData.get(i).latitude));
                locationB.setLongitude(Double.parseDouble(cData.get(i).logitude));

                double distance = locationA.distanceTo(locationB);
                if(distance<m){
                    rData.add(cData.get(i));
                }
            }
            if (rData.isEmpty()) {
                no_data.setVisibility(View.VISIBLE);
            } else {
                no_data.setVisibility(View.INVISIBLE);
            }
            cList.setVisibility(View.VISIBLE);//일반리스트를 보이게
            listview.setVisibility(View.GONE);//땡길 수 있는 리스트를 안보이게

    }

    public void changeList() {//리스트뷰에 항목추가
        mData.clear();
        for (int i = 0; i < cData.size(); i++) {
            if (cData.get(i).UUID.equals(UUID)) {
                mData.add(cData.get(i));
            }
        }
        myAdapter.notifyDataSetChanged();//어뎁터 갱신
    }

    public void getMyLocation() {//내위치 갱신하기
        if (locationRequest == null) {
            locationRequest = LocationRequest.create();
            locationRequest.setPriority(DEFAULT_LOCATION_REQUEST_PRIORITY);
            locationRequest.setInterval(DEFAULT_LOCATION_REQUEST_INTERVAL);
            locationRequest.setFastestInterval(DEFAULT_LOCATION_REQUEST_FAST_INTERVAL);
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //권한이 없으면 리턴
            return;
        }//위치정보 요청
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            myLatitude = locationResult.getLastLocation().getLatitude();
            myLogitude = locationResult.getLastLocation().getLongitude();
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);

            Log.d("현재 위치", myLatitude + ", " + myLogitude);
        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);

        }
    };
    public class UpdateComment extends AsyncTask<String, Void, String> {
        String errorString = null;
        private String mJsonString;
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(MainActivity.this,"Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null){
            }
            else {
                mJsonString = result;
                showResult();
            }

        }
        private void showResult(){

            String TAG_JSON="comment";
            String TAG_ID = "id";
            String TAG_UUID = "UUID";
            String TAG_category = "category";
            String TAG_text = "text";
            String TAG_STR_LATITUDE = "str_latitude";
            String TAG_STR_LONGITUDE ="str_longitude";

            ((GlobalList) getApplication() ).cleancData();

            try {
                JSONObject jsonObject = new JSONObject(mJsonString);
                JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

                for(int i=0;i<jsonArray.length();i++){

                    JSONObject item = jsonArray.getJSONObject(i);
                    int id = item.getInt(TAG_ID);
                    String UUID = item.getString(TAG_UUID);
                    int category=0;
                    switch (item.getString(TAG_category)){
                        case "리뷰":
                            category = 0;
                            break;
                        case "꿀팁":
                            category = 1;
                            break;
                        case "기록":
                            category = 2;
                            break;

                    }
                    String text = item.getString(TAG_text);
                    String str_latitude = item.getString(TAG_STR_LATITUDE);
                    String str_longitude = item.getString(TAG_STR_LONGITUDE);

                    ((GlobalList) getApplication() ).setcData(new CommentData(id,UUID,category,text,str_latitude,str_longitude));
                }

            } catch (JSONException e) {
            }
            //cData.clear();
            //???????????????????????????????????????????????????????????????????????????????????????????????????????
            cData=((GlobalList) getApplication() ).getcData();

            if(radiusIndex != 0){//반경으로 설정되어 있으면
                int m=0;
                rData.clear();
                switch (radiusIndex){
                    case 1:
                        m=30;
                        break;
                    case 2:
                        m=100;
                        break;
                    case 3:
                        m=300;
                        break;
                    case 4:
                        m=500;
                        break;
                    case 5:
                        m=1000;
                        break;
                }

                Location locationA = new Location("point A");//내위치
                locationA.setLatitude(myLatitude);
                locationA.setLongitude(myLogitude);

                for(int i = 0; i<cData.size();i++){//선택된 반경 안에 있는지 거리 검사후 리스트 삽입
                    Location locationB = new Location("point B");

                    locationB.setLatitude(Double.parseDouble(cData.get(i).latitude));
                    locationB.setLongitude(Double.parseDouble(cData.get(i).logitude));
                    double distance = locationA.distanceTo(locationB);
                    if(distance<m){
                        rData.add(cData.get(i));
                    }
                }
                if (rData.isEmpty()) {
                    no_data.setVisibility(View.VISIBLE);
                } else {
                    no_data.setVisibility(View.INVISIBLE);
                }
                cList.setVisibility(View.VISIBLE);//일반리스트를 보이게
                listview.setVisibility(View.GONE);//땡길 수 있는 리스트를 안보이게
                myAdapter = new MyAdapter(MainActivity.this, rData);//어뎁터에 어레이리스트를 붙임
                cList.setAdapter(myAdapter);
            }
            else{//내 Comment로 설정되어 있으면
                mData.clear();
                for (int i = 0; i < cData.size(); i++) {//My Comment array갱신
                    if (cData.get(i).UUID.equals(UUID)) {
                        mData.add(cData.get(i));
                    }
                }
                if (mData.isEmpty()) {
                    no_data.setVisibility(View.VISIBLE);
                } else {
                    no_data.setVisibility(View.INVISIBLE);
                }
                listview.setVisibility(View.VISIBLE);//땡길 수 있는 리스트를 보이게
                cList.setVisibility(View.GONE);//일반리스트를 보이게
                myAdapter = new MyAdapter(MainActivity.this, mData);//어뎁터에 어레이리스트를 붙임
                listview.setAdapter(myAdapter);
            }
            myAdapter.notifyDataSetChanged();//어뎁터 갱신
            progressDialog.dismiss();
        }

        @Override
        protected String doInBackground(String... params) {

            String serverURL = params[0];
            String postParameters = params[1];

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString().trim();


            } catch (Exception e) {

                errorString = e.toString();

                return null;
            }

        }
    }
    public class DeleteComment  extends AsyncTask<String, Void, String> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            
            getMyLocation();
            UpdateComment updateComment = new UpdateComment();
            updateComment.execute( "http://" + IP_ADDRESS + "/getComment.php", "");
        }

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... params) {
            String result;
            String id = (String)params[1];

            String serverURL = (String)params[0];
            String postParameters = "id=" + id  ;

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();

                return sb.toString();

            } catch (Exception e) {
                return new String("Error: " + e.getMessage());
            }

        }
    }
}