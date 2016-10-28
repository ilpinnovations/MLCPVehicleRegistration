package com.example.magicuhf;

import android.app.Activity;
import android.content.Intent;
import android.hardware.uhf.magic.reader;
import android.os.Bundle;
import android.util.Log;

public class MagicMainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Init();
        Intent intent = new Intent(MagicMainActivity.this, MainActivity.class);
        startActivity(intent);
        finish();

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        android.hardware.uhf.magic.reader.Close();
    }

    void Init() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                android.hardware.uhf.magic.reader.init("/dev/ttyMT1");
                android.hardware.uhf.magic.reader.Open("/dev/ttyMT1");
                Log.e("7777777777", "111111111111111111111111111111111111");
                if (reader.SetTransmissionPower(2100) != 0x11) {
                    if (reader.SetTransmissionPower(2100) != 0x11) {
                        reader.SetTransmissionPower(2100);
                    }
                }
            }
        });
        thread.start();
    }

}
