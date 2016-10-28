package com.example.magicuhf;

import android.content.DialogInterface;
import android.hardware.uhf.magic.reader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import utilities.AppConstants;
import utilities.ConnectionDetector;
import utilities.JsonHelper;


public class MainActivity extends AppCompatActivity {
    RequestQueue mRequestQueue;
    String m_strresult = "";
    private boolean mFlag1 = false, mFlag2 = false;
    private String mRFID;
    private TextView mRFIDText;
    private Handler mHandler = new MainHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRequestQueue = Volley.newRequestQueue(MainActivity.this);
        reader.m_handler = mHandler;

        mRFIDText = (TextView) findViewById(R.id.input_rfid);
        final EditText mVehicleNumberText = (EditText) findViewById(R.id.input_vehicle_number);
        final EditText mEmployeeNumber = (EditText) findViewById(R.id.input_employee_number);
        final EditText mEmployeeName = (EditText) findViewById(R.id.input_employee_name);

        Button mSubmit = (Button) findViewById(R.id.submitForm);
        Button scanRFID = (Button) findViewById(R.id.scanRfid);

        scanRFID.setOnClickListener(new StartScanningAction());

        mRFIDText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mFlag1 = s.length() > 0;
                Log.d("TAG: mFlag1", String.valueOf(mFlag1));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mVehicleNumberText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mFlag2 = s.length() > 0;
                Log.d("TAG: mFlag2", String.valueOf(mFlag2));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String rFid = mRFIDText.getText().toString().trim();
                String vehicleNum = mVehicleNumberText.getText().toString().trim();

                Log.d("My TAG", vehicleNum);

                if (rFid.length() == 0) {
                    AlertDialog alertDialog = new AlertDialog.Builder(
                            MainActivity.this).create();

                    alertDialog.setTitle("Alert");
                    alertDialog.setMessage(getString(R.string.alert1_text));
                    // Setting OK Button
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    alertDialog.setCancelable(false);
                    alertDialog.show();
                } else if (!mFlag2) {
                    AlertDialog alertDialog = new AlertDialog.Builder(
                            MainActivity.this).create();

                    alertDialog.setTitle("Alert");
                    alertDialog.setMessage(getString(R.string.alert2_text));
                    // Setting OK Button
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    alertDialog.setCancelable(false);
                    alertDialog.show();
                } else if (mEmployeeName.getText().toString().trim().length() == 0) {
                    mEmployeeName.setError("Please enter a valid employee name.");
                } else if (mEmployeeNumber.getText().toString().trim().length() == 0) {
                    mEmployeeNumber.setError("Please enter a valid employee number.");
                } else {
                    String employeeName = mEmployeeName.getText().toString().trim();
                    String employeeNumber = mEmployeeNumber.getText().toString().trim();

                    DBHelper dbHelper = new DBHelper(MainActivity.this);
                    dbHelper.insertVehicle(rFid, vehicleNum, employeeName, employeeNumber);
                    Log.d("My TAG", String.valueOf(dbHelper.numberOfRows()));

                    AlertDialog alertDialog = new AlertDialog.Builder(
                            MainActivity.this).create();

                    alertDialog.setTitle("Alert");
                    alertDialog.setMessage(getString(R.string.alert3_text));
                    // Setting OK Button
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    alertDialog.setCancelable(false);
                    alertDialog.show();

                    mRFIDText.setText("");
                    mVehicleNumberText.setText("");
                    mEmployeeName.setText("");
                    mEmployeeNumber.setText("");
                    mFlag2 = false;

                    ArrayList<RegistrationBean> data = new DBHelper(MainActivity.this).getAllVehicles();
                    JsonDataBean jsonDataBean = new JsonDataBean(data);

                    String toUpload = JsonHelper.serialize(jsonDataBean);
                    Log.d("My JSON: ", toUpload);


                    if (new ConnectionDetector(MainActivity.this).isConnectingToInternet()) {
                        uploadData();
                    }
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        reader.StopLoop();
    }

    private void uploadData() {
        String url = AppConstants.url;
        final String TAG = "MY TAG";

        ArrayList<RegistrationBean> data = new DBHelper(MainActivity.this).getAllVehicles();
        JsonDataBean jsonDataBean = new JsonDataBean(data);

        final String toUpload = JsonHelper.serialize(jsonDataBean);
        Log.d("My JSON: ", toUpload);
        StringRequest strReq = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {


            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);
                if (response.equals("success")) {
                    new DBHelper(MainActivity.this).deleteAll();
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

    private class StartScanningAction implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            reader.InventoryLables();
        }
    }

    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d("My Tag", "Inside Handler");
            if (msg.what != 0) {
                if (m_strresult.indexOf((String) msg.obj) < 0) {
                    //Log.e("8888888888",(String)msg.obj+"\r\n");
                    m_strresult = (String) msg.obj;
                    Log.d("My Tag", m_strresult);
                    m_strresult = m_strresult.substring(4);
                    m_strresult += "\r\n";
                    mRFIDText.setText(m_strresult);

                }

            }

        }
    }

}
