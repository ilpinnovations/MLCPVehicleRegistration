package utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import beans.JsonDataBean;
import beans.RegistrationBean;
import db.DBHelper;

/**
 * Created by abhi on 6/30/2016.
 */
public class ConnectionBroadcastReceiver extends BroadcastReceiver {
    RequestQueue mRequestQueue;

    @Override
    public void onReceive(Context context, Intent intent) {
        mRequestQueue = Volley.newRequestQueue(context);
        DBHelper dbHelper = new DBHelper(context);
        int count = dbHelper.numberOfRows();

        if (new ConnectionDetector(context).isConnectingToInternet() && count > 0) {
            uploadData(context);
        }

    }

    private void uploadData(final Context context) {
        String url = AppConstants.url;
        final String TAG = "MY TAG";

        ArrayList<RegistrationBean> registrations = new DBHelper(context).getAllVehicles();
        JsonDataBean jsonDataBean = new JsonDataBean(registrations);
        final String toUpload = JsonHelper.serialize(jsonDataBean);

        Log.d("My JSON: ", toUpload);

        StringRequest strReq = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {


            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);
                if (response.equals("success")) {
                    new DBHelper(context).deleteAll();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }


        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("Registration_Data", toUpload);
                return params;
            }
        };

        mRequestQueue.add(strReq);
    }


}