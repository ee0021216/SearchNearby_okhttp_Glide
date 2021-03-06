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
    //------permission??????????????????????????????--------------------------
    private static final String[][] permissionsArray = new String[][]{
            {Manifest.permission.ACCESS_FINE_LOCATION, "???GPS??????"},
            {Manifest.permission.ACCESS_COARSE_LOCATION, "????????????"},
            {Manifest.permission.INTERNET, "??????"},
    };
    private List<String> permissionsList = new ArrayList<String>();
    //???????????????????????????
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 101;
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 1;
    //------------------------------------------------------------------
    //------??????????????????????????????
    private long minTime = 5000;// ms
    private float minDist = 30.0f;// meter


    private String loc_type = "food";//??????
    private int loc_range = 500;//??????
    private float Anchor_x = 0.7f;
    private float Anchor_y = 0.6f;

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LatLng VGPS = new LatLng(24.137622102040563, 120.68662252293544);
    private float mapzoom = 16;
    private String provider; //????????????

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
        //------------??????MapFragment-----------------------------------
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


    private void u_checkgps() {    // ????????????????????????
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
            //??????????????????GPS
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("GPS?????????")
                    .setMessage("GPS????????????????????????.\n" + "??????????????????!,????????????APP!")
                    .setPositiveButton("??????????????????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //??????Intent?????????????????????????????????GPS??????
                            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(i);
                            finish();
                        }
                    }).setNegativeButton("?????????", null).create().show();
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
            updateWithNewLocation(location); //*****??????GPS??????
/********************************************************* */
            return;
        }
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Location location = null;
        if (!(isGPSEnabled || isNetworkEnabled))
            Toast.makeText(getApplicationContext(), "GPS ?????????", Toast.LENGTH_SHORT).show();
        else {
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        minTime, minDist, this);
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//                tmsg.setText("????????????GPS");
            }
            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        minTime, minDist, this);
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                tmsg.setText("????????????GPS");
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
        //??????GoogleMap ????????????
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        //????????????????????????GoogleMap ??????
        mMap.getUiSettings().setMapToolbarEnabled(true);
        //??????????????????????????????????????????????????????
        mMap.getUiSettings().setCompassEnabled(true);
        //????????????????????????????????????????????????
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //map.addMarker(new MarkerOptions().position(VGPS).title("???????????????"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(VGPS, mapzoom));
        //----------??????????????????-----------------------
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //----??????????????????ICO-------
            mMap.setMyLocationEnabled(true);
        } else {
//            Toast.makeText(getApplicationContext(), "GPS?????????????????????", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        updateWithNewLocation(location);
    }

    private void updateWithNewLocation(Location location) {
        String where = "";
        if (location != null) {
            double lat = location.getLatitude();// ??????
            double lng = location.getLongitude();// ??????
//            float speed = location.getSpeed();// ??????
//            long time = location.getTime();// ??????
//            double altitude = location.getAltitude();//??????
            VGPS = new LatLng(lat, lng);
            m_location = lat + "," + lng;
            cameraFocusOnMe(lat, lng);

        } else {
            where = "*??????????????????*";
        }

    }

    private void cameraFocusOnMe(double lat, double lng) {
        CameraPosition camPosition = new CameraPosition.Builder()
                .target(new LatLng(lat, lng))
                .zoom(mMap.getCameraPosition().zoom)
                .build();
        /* ?????????????????? */
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

    //------??????opendata(Place Api)------
    private void u_importopendata() { //??????Opendata
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
                if (response.isSuccessful()) {//????????????
                    try {
                        String m_Response = response.body().string();
                        //-------?????? json   ??????????????????-------------

                        mList = new ArrayList<Map<String, String>>();


                        JSONObject jsonObject = new JSONObject(m_Response);
                        JSONArray info = jsonObject.getJSONArray("results");
                        int a = 0;
                        for (int i = 0; i < info.length(); i++) {
                            Map<String, String> item = new HashMap<String, String>();
                            String Name = info.getJSONObject(i).getString("name").trim();
                            String Icon_url = info.getJSONObject(i).getString("icon");
                            if (Icon_url == null || Icon_url == "" || Icon_url.length() < 1) { //???icon?????????
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


//----------SwipeLayout ?????? --------
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
                    //?????????????????????
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
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());//????????????

    }

    private Runnable updata_map = new Runnable() {
        @Override
        public void run() {
            //???????????????????????????

            String vtitle = mList.get(i).get("Name");
            double dLat = Double.parseDouble(mList.get(i).get("lat"));    // ?????????
            double dLon = Double.parseDouble(mList.get(i).get("lng"));    // ?????????
            String ans_Url = mList.get(i).get("Icon_url");

            //????????????
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
                            VGPS = new LatLng(dLat, dLon);// ?????????????????????????????????
                            mMap.addMarker(new MarkerOptions()
                                    .position(VGPS)
                                    .alpha(0.9f)
                                    .title(vtitle)
                                    .snippet("??????:" + dLat + "," + dLon)
                                    .infoWindowAnchor(Anchor_x, Anchor_y)
                                    .icon(BitmapDescriptorFactory.fromBitmap(resource)) // ??????????????????
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
        //------???????????????Dialog------
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Main.this);
        View dailog = getLayoutInflater().inflate(R.layout.settings, null); //???????????????layout
        alertDialog.setView(dailog);
        Button btn_OK = dailog.findViewById(R.id.btn_ok); //????????????
        Button btn_Cancel = dailog.findViewById(R.id.btn_cancel); //????????????
        loc_choose = (Spinner) dailog.findViewById(R.id.spn_loc); //????????????
        //??????Adapter
        ArrayAdapter loc = new ArrayAdapter(this, android.R.layout.simple_spinner_item);
        if (loc_Text.length == loc_Value.length) {
            for (int i = 0; i < loc_Text.length; i++) loc.add(loc_Text[i]);
        } else {
            loc.add(getString(R.string.default_type));
        }
        loc_choose.setAdapter(loc);
        range_text = (TextView) dailog.findViewById(R.id.bar_text); //????????????
        SeekBar bar_range = dailog.findViewById(R.id.bar_range);
        bar_range.setOnSeekBarChangeListener(barCL);
        final AlertDialog dialog_create = alertDialog.create();
        dialog_create.setCancelable(false);
        dialog_create.show();
        dialog_create.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //dlg???????????????
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
    //------????????????------

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

    //==========?????????window?????????
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