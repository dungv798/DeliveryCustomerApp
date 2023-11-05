package com.example.codeseasy.deliveryboyapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    TextView textViewAddress, textViewUser;
    String apiUrl = "http://192.168.166.142/Food-Delivery-App/Food-Delivery-App/food-delivery-restaurant-app/public/api/users/delivery?email=";
    SharedPreferences sharedPreferences;
    String order_id, longitude, latitude;
    CardView cardViewDeliveryDetails;
    TextView textViewTitle;
    private GoogleMap mMap;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewAddress = findViewById(R.id.new_delivery);
        textViewUser = findViewById(R.id.userDetails);
        sharedPreferences = getSharedPreferences("DeliveryBoyApp", MODE_PRIVATE);
        cardViewDeliveryDetails = findViewById(R.id.deliveryDetails);
        textViewTitle = findViewById(R.id.textDeliveryTitle);
        textViewTitle.setText("No pending delivery");
        cardViewDeliveryDetails.setVisibility(View.GONE);
        if (sharedPreferences.getString("login", "false").equals("false")) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }
        checkPermissions();
        GPSUtils gpsUtils = new GPSUtils(this);
        gpsUtils.statusCheck();
        textViewUser.setText(sharedPreferences.getString("name", "") + "\n" +
                sharedPreferences.getString("email", ""));
        fetchData();
        Button buttonLocation = findViewById(R.id.startDelivery);
        Button buttonSuccess = findViewById(R.id.btn_success);
        Button buttonFailed = findViewById(R.id.btn_failed);

        buttonSuccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markOrderStatus("http://192.168.166.142/Food-Delivery-App/Food-Delivery-App/food-delivery-restaurant-app/public/api/users/delivery/success?order_id=" + order_id,
                        "success");
            }
        });

        buttonFailed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markOrderStatus("http://192.168.166.142/Food-Delivery-App/Food-Delivery-App/food-delivery-restaurant-app/public/api/users/delivery/failed?order_id=" + order_id,
                        "failed");
            }
        });

        buttonLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ViewLocation.class);
                intent.putExtra("lat", latitude);
                intent.putExtra("long", longitude);
                startActivity(intent);
            }
        });

        // Khởi tạo Google Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Đặt marker lên bản đồ khi có thông tin vị trí
        if (latitude != null && longitude != null) {
            LatLng location = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
            mMap.addMarker(new MarkerOptions().position(location).title("Destination"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        }
    }

    public void checkPermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    public void markOrderStatus(String url, String status) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("success")) {

                            Toast.makeText(MainActivity.this, "Marked: Delivery "
                                    + status, Toast.LENGTH_SHORT).show();
                            cardViewDeliveryDetails.setVisibility(View.GONE);
                            textViewTitle.setText("No pending delivery");

                        } else Toast.makeText(MainActivity.this, "Operation Failed",
                                Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        queue.add(stringRequest);
    }

    public void parseJSON(String data) {
        try {
            JSONArray jsonArray = new JSONArray(data);
            if (jsonArray.length() > 0) {
                cardViewDeliveryDetails.setVisibility(View.VISIBLE);
                textViewTitle.setText("New Delivery Details Found");
            }
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject stu = jsonArray.getJSONObject(i);
                String address = stu.getString("destination_address");
                order_id = stu.getString("id");
                latitude = stu.getString("destination_lat");
                longitude = stu.getString("destination_lon");
                textViewAddress.setText("Address: " + address);

                if (mMap != null && latitude != null && longitude != null) {
                    LatLng location = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                    mMap.addMarker(new MarkerOptions().position(location).title("Destination"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void fetchData() {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                apiUrl + sharedPreferences.getString("email", ""),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseJSON(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        queue.add(stringRequest);
    }
}
