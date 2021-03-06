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
    public static ArrayList<CommentData> mData = new ArrayList<>();//?????? ?????? commnet ??????
    public static ArrayList<CommentData> rData = new ArrayList<>();//?????? ?????? ?????? commnet ??????
    public static ArrayList<CommentData> cData;//comment ??????
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
        Intent data = getIntent();//???????????? ?????????
        myLatitude = data.getDoubleExtra("latitude", 0);
        myLogitude = data.getDoubleExtra("longitude", 0);

        no_data = findViewById(R.id.no_data);

        cData=((GlobalList) getApplication() ).getcData();

        createFB();//??????????????? ??????(go to ar)

        createSpinner();//????????? ??????
        createComment();//Commen?????? ?????? ????????????
        listview = (SwipeMenuListView) findViewById(R.id.myCommentContents);//?????? ??? ?????? ????????? ???
        cList = (ListView) findViewById(R.id.commentContents);//???????????? ????????????
        //?????? ??? ?????? ???????????? ??????
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
        listview.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {//???????????????
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                DeleteComment deleteComment = new DeleteComment();
                deleteComment.execute( "http://" + IP_ADDRESS + "/deleteComment.php", Integer.toString(mData.get(position)._id));

                return true;
            }
        });

        cList.setVisibility(View.GONE);
        myAdapter = new MyAdapter(MainActivity.this, mData);//???????????? ????????????????????? ??????
        listview.setAdapter(myAdapter);//???????????? ???????????? ??????

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

        @Override// list ????????? ?????? ?????????
        public void create(SwipeMenu menu) {
            // create "?????????" item
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
    public void onMapReady(final GoogleMap googleMap) {//????????? ???????????? ?????????
        mMap = googleMap;//???????????? ???????????? ??????
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //?????? ????????? ??????
            return;
        }
        mMap.setMyLocationEnabled(true);
        LatLng myLocation = new LatLng(myLatitude, myLogitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 18));

    }

    public void createComment() {//AR ????????? ?????? ??????
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

    public void createFB() {//??????????????? ??????
        fb_ar = findViewById(R.id.fb_AR);
        fb_reload = findViewById(R.id.fb_reload);
        fb_ar.setOnClickListener(new View.OnClickListener() {//AR????????? ?????? ??????
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UnityPlayerActivity.class);
                 startActivity(intent);
            }
        });
        fb_reload.setOnClickListener(new View.OnClickListener() {//???????????? ??????
            @Override
            public void onClick(View v) {
                getMyLocation();
                UpdateComment updateComment = new UpdateComment();
                updateComment.execute( "http://" + IP_ADDRESS + "/getComment.php", "");
            }
        });
    }

    public void createSpinner() {
        radius = (Spinner) findViewById(R.id.radius);//????????? ??????
        radiusList.add("My Comment");
        radiusList.add("30m");
        radiusList.add("100m");
        radiusList.add("300m");
        radiusList.add("500m");
        radiusList.add("1km");
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, radiusList);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        radius.setAdapter(adapter1);
        radius.setSelection(radiusIndex);//?????????
        radius.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {//???????????? ??????????????? ???
                radiusIndex = position;
                if (position == 0) {//?????? ???????????? My Comment???
                    if (mData.isEmpty()) {
                        no_data.setVisibility(View.VISIBLE);
                    } else {
                        no_data.setVisibility(View.INVISIBLE);
                    }
                    listview.setVisibility(View.VISIBLE);//?????? ??? ?????? ???????????? ?????????
                    cList.setVisibility(View.GONE);//?????? ???????????? ????????????
                    myAdapter = new MyAdapter(MainActivity.this, mData);//???????????? ????????????????????? ??????
                    listview.setAdapter(myAdapter);//?????? ??? ?????? ???????????? ???????????? ??????
                } else {//????????? ????????????
                   changeRadiusData();

                    myAdapter = new MyAdapter(MainActivity.this, rData);//???????????? ????????????????????? ??????
                    cList.setAdapter(myAdapter);//?????? ???????????? ???????????? ??????
                    }
                myAdapter.notifyDataSetChanged();//????????? ??????
                }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {//???????????????(?????? ?????? ??????????????? ???)
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

            Location locationA = new Location("point A");//?????????
            locationA.setLatitude(myLatitude);
            locationA.setLongitude(myLogitude);

            for(int i = 0; i<cData.size();i++){//????????? ?????? ?????? ????????? ?????? ????????? ????????? ??????
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
            cList.setVisibility(View.VISIBLE);//?????????????????? ?????????
            listview.setVisibility(View.GONE);//?????? ??? ?????? ???????????? ????????????

    }

    public void changeList() {//??????????????? ????????????
        mData.clear();
        for (int i = 0; i < cData.size(); i++) {
            if (cData.get(i).UUID.equals(UUID)) {
                mData.add(cData.get(i));
            }
        }
        myAdapter.notifyDataSetChanged();//????????? ??????
    }

    public void getMyLocation() {//????????? ????????????
        if (locationRequest == null) {
            locationRequest = LocationRequest.create();
            locationRequest.setPriority(DEFAULT_LOCATION_REQUEST_PRIORITY);
            locationRequest.setInterval(DEFAULT_LOCATION_REQUEST_INTERVAL);
            locationRequest.setFastestInterval(DEFAULT_LOCATION_REQUEST_FAST_INTERVAL);
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //????????? ????????? ??????
            return;
        }//???????????? ??????
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            myLatitude = locationResult.getLastLocation().getLatitude();
            myLogitude = locationResult.getLastLocation().getLongitude();
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);

            Log.d("?????? ??????", myLatitude + ", " + myLogitude);
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
                        case "??????":
                            category = 0;
                            break;
                        case "??????":
                            category = 1;
                            break;
                        case "??????":
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

            if(radiusIndex != 0){//???????????? ???????????? ?????????
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

                Location locationA = new Location("point A");//?????????
                locationA.setLatitude(myLatitude);
                locationA.setLongitude(myLogitude);

                for(int i = 0; i<cData.size();i++){//????????? ?????? ?????? ????????? ?????? ????????? ????????? ??????
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
                cList.setVisibility(View.VISIBLE);//?????????????????? ?????????
                listview.setVisibility(View.GONE);//?????? ??? ?????? ???????????? ????????????
                myAdapter = new MyAdapter(MainActivity.this, rData);//???????????? ????????????????????? ??????
                cList.setAdapter(myAdapter);
            }
            else{//??? Comment??? ???????????? ?????????
                mData.clear();
                for (int i = 0; i < cData.size(); i++) {//My Comment array??????
                    if (cData.get(i).UUID.equals(UUID)) {
                        mData.add(cData.get(i));
                    }
                }
                if (mData.isEmpty()) {
                    no_data.setVisibility(View.VISIBLE);
                } else {
                    no_data.setVisibility(View.INVISIBLE);
                }
                listview.setVisibility(View.VISIBLE);//?????? ??? ?????? ???????????? ?????????
                cList.setVisibility(View.GONE);//?????????????????? ?????????
                myAdapter = new MyAdapter(MainActivity.this, mData);//???????????? ????????????????????? ??????
                listview.setAdapter(myAdapter);
            }
            myAdapter.notifyDataSetChanged();//????????? ??????
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