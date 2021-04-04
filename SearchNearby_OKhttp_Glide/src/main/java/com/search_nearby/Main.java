package com.search_nearby;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class Main extends AppCompatActivity implements OnMapReadyCallback, LocationListener, View.OnClickListener {
    //------permission所需要申請的權限數組--------------------------
    private static final String[][] permissionsArray = new String[][]{
            {Manifest.permission.ACCESS_FINE_LOCATION, "僅GPS定位"},
            {Manifest.permission.ACCESS_COARSE_LOCATION, "一般定位"},
            {Manifest.permission.INTERNET, "網路"},
    };
    private List<String> permissionsList = new ArrayList<String>();
    //申請權限後的返回碼
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 101;
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 1;
    //------------------------------------------------------------------
    //------多久更新一次位置資訊
    private long minTime = 5000;// ms
    private float minDist = 30.0f;// meter


    private String loc_type = "food";//類型
    private int loc_range = 500;//範圍
    private float Anchor_x = 0.7f;
    private float Anchor_y = 0.6f;

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LatLng VGPS = new LatLng(24.137622102040563, 120.68662252293544);
    private float mapzoom = 16;
    private String provider; //提供資料

    private ArrayList<Map<String, String>> mList;
    private String m_location = "24.137622102040563, 120.68662252293544";
    private Spinner loc_choose;
    private TextView range_text;
    private String[] loc_Text;
    private String[] loc_Value;

    private boolean searchOn = false;

    private Button btn_SearchOn;
    private Button btn_SearchOff;
    private int i;
    private int position;
    private Spinner spinner;


    //------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkRequiredPermission(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //------------設定MapFragment-----------------------------------
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setupViewComponent();
    }

    private void setupViewComponent() {
        if (getString(R.string.google_maps_key).equals("Your Key"))
            Toast.makeText(getApplicationContext(), getString(R.string.noKey), Toast.LENGTH_SHORT).show();
        loc_Text = getResources().getStringArray(R.array.map_locText);
        loc_Value = getResources().getStringArray(R.array.map_locValue);
        btn_SearchOn = (Button) findViewById(R.id.SearchOn);
        btn_SearchOff = (Button) findViewById(R.id.SearchOff);
        spinner=(Spinner)findViewById(R.id.spinner);

        ArrayAdapter<CharSequence> arrayAdapter=ArrayAdapter.createFromResource(this,R.array.map_locText,
                android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(spinner01);

        btn_SearchOn.setOnClickListener(this);
        btn_SearchOff.setOnClickListener(this);

    }
    private AdapterView.OnItemSelectedListener spinner01= new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                if (!m_location.equals("24.137622102040563, 120.68662252293544")) {
                    if (getString(R.string.google_maps_key).equals("Your Key")) {
                        Toast.makeText(getApplicationContext(), getString(R.string.noKey), Toast.LENGTH_SHORT).show();
                    } else {
//                        btn_SearchOff.setVisibility(View.VISIBLE);
                        btn_SearchOn.setVisibility(View.GONE);


                        if (loc_Text.length == loc_Value.length) {
                            loc_type = loc_Value[position];
                        } else{
                            loc_type = "food";
                        }


                        u_importopendata();

                    }
                    searchOn = true;
                } else
                    Toast.makeText(getApplicationContext(), getString(R.string.noGPS), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.GPSnoStart), Toast.LENGTH_LONG).show();
                checkRequiredPermission(Main.this);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.SearchOn:
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    if (!m_location.equals("24.137622102040563, 120.68662252293544")) {
                        if (getString(R.string.google_maps_key).equals("Your Key")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.noKey), Toast.LENGTH_SHORT).show();
                        } else {
//                            btn_SearchOff.setVisibility(View.VISIBLE);
                            btn_SearchOn.setVisibility(View.GONE);
                            u_importopendata();

                        }
                        searchOn = true;
                    } else
                        Toast.makeText(getApplicationContext(), getString(R.string.noGPS), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.GPSnoStart), Toast.LENGTH_LONG).show();
                    checkRequiredPermission(this);
                }

                break;
            case R.id.SearchOff:
                btn_SearchOff.setVisibility(View.GONE);
                btn_SearchOn.setVisibility(View.GONE);
                mMap.clear();
                searchOn = false;
                break;
        }
    }


    private void u_checkgps() {    // 檢查定位是否成功
        try {
            if (initLocationProvider()) {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    nowaddress();
                } else {
                }
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), getString(R.string.noGPS), Toast.LENGTH_LONG).show();
            //對話方塊啟用GPS
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("GPS未開啟")
                    .setMessage("GPS目前狀態尚未啟用.\n" + "請先開啟定位!,再次執行APP!")
                    .setPositiveButton("離開再次執行", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //使用Intent物件啟動設定程式來更改GPS設定
                            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(i);
                            finish();
                        }
                    }).setNegativeButton("不啟用", null).create().show();
            return;
        }
    }

    private void nowaddress() {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(provider);
/******************************************************** */
            updateWithNewLocation(location); //*****開啟GPS定位
/********************************************************* */
            return;
        }
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Location location = null;
        if (!(isGPSEnabled || isNetworkEnabled))
            Toast.makeText(getApplicationContext(), "GPS 未開啟", Toast.LENGTH_SHORT).show();
        else {
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        minTime, minDist, this);
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//                tmsg.setText("使用網路GPS");
            }
            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        minTime, minDist, this);
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                tmsg.setText("使用精確GPS");
            }
        }
    }

    private boolean initLocationProvider() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
            return true;
        } else {
            return false;
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //開啟GoogleMap 拖曳功能
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        //右下角導覽及開啟GoogleMap 功能
        mMap.getUiSettings().setMapToolbarEnabled(true);
        //左上角顯示指北針，需旋轉畫面才會出現
        mMap.getUiSettings().setCompassEnabled(true);
        //右下角顯示縮放按鈕的放大縮小功能
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //map.addMarker(new MarkerOptions().position(VGPS).title("中區職訓局"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(VGPS, mapzoom));
        //----------取得定位許可-----------------------
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //----顯示我的位置ICO-------
            mMap.setMyLocationEnabled(true);
        } else {
//            Toast.makeText(getApplicationContext(), "GPS定位權限未允許", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        updateWithNewLocation(location);
    }

    private void updateWithNewLocation(Location location) {
        String where = "";
        if (location != null) {
            double lat = location.getLatitude();// 緯度
            double lng = location.getLongitude();// 經度
//            float speed = location.getSpeed();// 速度
//            long time = location.getTime();// 時間
//            double altitude = location.getAltitude();//海拔
            VGPS = new LatLng(lat, lng);
            m_location = lat + "," + lng;
            cameraFocusOnMe(lat, lng);

        } else {
            where = "*位置訊號消失*";
        }

    }

    private void cameraFocusOnMe(double lat, double lng) {
        CameraPosition camPosition = new CameraPosition.Builder()
                .target(new LatLng(lat, lng))
                .zoom(mMap.getCameraPosition().zoom)
                .build();
        /* 移動地圖鏡頭 */
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPosition));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
//                    tmsg.setText("Out of Service");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
//                    tmsg.setText("Temporarily Unavailable");
                break;
            case LocationProvider.AVAILABLE:
//                    tmsg.setText("Available");
                break;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        updateWithNewLocation(null);
    }

    //------載入opendata(Place Api)------
    private void u_importopendata() { //下載Opendata
        String google_place_key = getString(R.string.googke_place_key);
        String aa = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + m_location + "&radius=" + loc_range + "&types=" + loc_type + "&sensor=true&key=" + google_place_key;


        OkHttpClient client = new OkHttpClient();
        String url
                = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + m_location + "&radius=" + loc_range + "&types=" + loc_type + "&sensor=true&key=" + google_place_key;
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {//如果成功
                    try {
                        String m_Response = response.body().string();
                        //-------解析 json   帶有多層結構-------------

                        mList = new ArrayList<Map<String, String>>();


                        JSONObject jsonObject = new JSONObject(m_Response);
                        JSONArray info = jsonObject.getJSONArray("results");
                        int a = 0;
                        for (int i = 0; i < info.length(); i++) {
                            Map<String, String> item = new HashMap<String, String>();
                            String Name = info.getJSONObject(i).getString("name").trim();
                            String Icon_url = info.getJSONObject(i).getString("icon");
                            if (Icon_url == null || Icon_url == "" || Icon_url.length() < 1) { //若icon找不到
                                Icon_url = "";
                            }
                            JSONObject geometry = info.getJSONObject(i).getJSONObject("geometry");
                            JSONObject location = geometry.getJSONObject("location");
                            String lat = location.getString("lat");
                            String lng = location.getString("lng");


                            item.put("Name", Name);
                            item.put("Icon_url", Icon_url);
                            item.put("lat", lat);
                            item.put("lng", lng);
                            mList.add(item);
//-------------------
                        }
                        handler.sendEmptyMessage(1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {

                }
            }
        });


//----------SwipeLayout 結束 --------
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    showloc();
                    break;
                default:
                    //其他想做的事情
                    break;
            }
        }
    };

    private void showloc() {
        if (mList == null || mList.size() == 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.noLoc), Toast.LENGTH_SHORT).show();
            btn_SearchOn.setVisibility(View.GONE);
            btn_SearchOff.setVisibility(View.GONE);
            return;
        }

        if (mMap != null) mMap.clear();
        i = 0;
        handler.post(updata_map);
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());//外圓內方

    }

    private Runnable updata_map = new Runnable() {
        @Override
        public void run() {
            //將所有景點位置顯示

            String vtitle = mList.get(i).get("Name");
            double dLat = Double.parseDouble(mList.get(i).get("lat"));    // 南北緯
            double dLon = Double.parseDouble(mList.get(i).get("lng"));    // 東西經
            String ans_Url = mList.get(i).get("Icon_url");

            //設置圓形
            RequestOptions options = new RequestOptions()
                    .transform(new MultiTransformation(new CenterCrop(), new RoundedCorners(50)));
            Glide.with(Main.this)
                    .asBitmap()
                    .load(ans_Url)
                    .override(100, 100)
                    .error(R.drawable.ic_baseline_settings_24)
                    .apply(options)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            VGPS = new LatLng(dLat, dLon);// 更新成欲顯示的地圖座標
                            mMap.addMarker(new MarkerOptions()
                                    .position(VGPS)
                                    .alpha(0.9f)
                                    .title(vtitle)
                                    .snippet("座標:" + dLat + "," + dLon)
                                    .infoWindowAnchor(Anchor_x, Anchor_y)
                                    .icon(BitmapDescriptorFactory.fromBitmap(resource)) // 顯示圖標文字
                            );
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }


                    });
            i++;
            if (i < mList.size())
                handler.postDelayed(updata_map, 10);
            else
                handler.removeCallbacks(updata_map);


        }
    };

    private void show_settings() {
        //------設定自訂義Dialog------
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Main.this);
        View dailog = getLayoutInflater().inflate(R.layout.settings, null); //取得自訂義layout
        alertDialog.setView(dailog);
        Button btn_OK = dailog.findViewById(R.id.btn_ok); //確認按鈕
        Button btn_Cancel = dailog.findViewById(R.id.btn_cancel); //取消按鈕
        loc_choose = (Spinner) dailog.findViewById(R.id.spn_loc); //選擇設施
        //設定Adapter
        ArrayAdapter loc = new ArrayAdapter(this, android.R.layout.simple_spinner_item);
        if (loc_Text.length == loc_Value.length) {
            for (int i = 0; i < loc_Text.length; i++) loc.add(loc_Text[i]);
        } else {
            loc.add(getString(R.string.default_type));
        }
        loc_choose.setAdapter(loc);
        range_text = (TextView) dailog.findViewById(R.id.bar_text); //搜尋範圍
        SeekBar bar_range = dailog.findViewById(R.id.bar_range);
        bar_range.setOnSeekBarChangeListener(barCL);
        final AlertDialog dialog_create = alertDialog.create();
        dialog_create.setCancelable(false);
        dialog_create.show();
        dialog_create.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //dlg背景全透明
        //------------------------------
        btn_OK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loc_Text.length == loc_Value.length) {
                    loc_type = loc_Value[spinner.getSelectedItemPosition()];
                } else{
                    loc_type = "food";
                }
                loc_range = bar_range.getProgress();
                dialog_create.cancel();
                if (searchOn) {
                    u_importopendata();
                }
            }
        });
        btn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_create.cancel();
            }
        });
    }

    private SeekBar.OnSeekBarChangeListener barCL = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            range_text.setText(progress + "m");
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private void checkRequiredPermission(Main mapsActivity) {
        //        String permission_check= String[i][0] permission;
        for (int i = 0; i < permissionsArray.length; i++) {
            if (ContextCompat.checkSelfPermission(mapsActivity, permissionsArray[i][0]) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permissionsArray[i][0]);
            }
        }
        if (permissionsList.size() != 0) {
            ActivityCompat.requestPermissions(mapsActivity, permissionsList.toArray(new
                    String[permissionsList.size()]), REQUEST_CODE_ASK_PERMISSIONS);
        }
    }
    //------生命週期------

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        u_checkgps();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        locationManager.removeUpdates(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.m_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                show_settings();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //==========自訂一window副程式
    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        @Override
        public View getInfoWindow(Marker marker) {
            View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_content, null);
            infoWindow.setAlpha(1.0f);


            for(int i=0;i<mList.size();i++)
            {
                LatLng test = new LatLng(Double.parseDouble(mList.get(i).get("lat")),Double.parseDouble(mList.get(i).get("lng")));

                LatLng test01 = marker.getPosition();
                if(test.equals(test01))
                {
                    position=i;
                    break;
                }
            }
            TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
            TextView title = ((TextView) infoWindow.findViewById(R.id.title));
            ImageView InfoWindow_img = (ImageView) infoWindow.findViewById(R.id.content_ico);
            ImageView Layout_img = (ImageView) findViewById(R.id.Layout_img);


            title.setText(marker.getTitle());
            snippet.setText(marker.getSnippet());


            String ans_Url = mList.get(position).get("Icon_url");

            Glide.with(Main.this)
                    .asBitmap()
                    .load(ans_Url)
                    .error(R.drawable.error_img)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            Layout_img.setImageBitmap(resource);
                            Layout_img.buildDrawingCache();
                        }
                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) { }
                    });

            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Glide.with(Main.this)
                    .asBitmap()
                    .load(ans_Url)
                    .error(R.drawable.error_img)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            InfoWindow_img.setImageBitmap(resource);
                            InfoWindow_img.buildDrawingCache();
                        }
                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }

                    });
            return infoWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            Toast.makeText(getApplicationContext(), "getInfoContents", Toast.LENGTH_LONG).show();
            return null;
        }
    }
}