package com.example.codeseasy.com.customerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ListOrders extends AppCompatActivity {

    TextView textViewOrders;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_orders);
        textViewOrders = findViewById(R.id.textOrders);
        sharedPreferences = getSharedPreferences("CustomerApp", MODE_PRIVATE);
        textViewOrders.setMovementMethod(new ScrollingMovementMethod());
        fetchData("http://192.168.1.4/Food-Delivery-App/Food-Delivery-App/food-delivery-restaurant-app/public/api/users/orders/list?email="
                +sharedPreferences.getString("email", ""));
    }

    public void parseJSON(String response){
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject stu = jsonArray.getJSONObject(i);
                String id = stu.getString("id");
                String created_at = stu.getString("created_at");
                String status = stu.getString("status");
                String item_details = stu.getString("item_details");
                JSONArray jsonArrayItem = new JSONArray(item_details);
                textViewOrders.append("Order placed on: " + created_at + "\nStatus: " + status + "\n");
                for (int j = 0; j < jsonArrayItem.length(); j++) {
                    JSONObject jsonObjectItem = jsonArrayItem.getJSONObject(j);
                    textViewOrders.append("Items: \n" + jsonObjectItem.getString("name") + "\nPrice: "
                            + jsonObjectItem.getString("price") + "\n");
                }
                textViewOrders.append("\n\n");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void fetchData(String apiUrl) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, apiUrl,
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