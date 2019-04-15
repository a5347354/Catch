package com.broadcastone.broadcast;

/**
 * Created by LuxProtoss on 2015/4/28.
 * 進入頁面(Logo)
 */
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.*;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.broadcastone.broadcast.android_broadcast.R;

public class EntranceActivity  extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrance);
      //  Log.i("Catch!",BluetoothAdapter.getDefaultAdapter().getName());
        BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        int i=0;
        while (!BluetoothAdapter.getDefaultAdapter().getName().startsWith("Catch") &&  (i<100)){
            BluetoothAdapter.getDefaultAdapter().setName("Catch"+bluetoothAdapter.getDefaultAdapter().getName());
            i=i+1;
        }


     //   Log.i("Catch!", bluetoothAdapter.getName());
       // Log.i("Catch!",BluetoothAdapter.getDefaultAdapter().getName());

        //2秒跳轉
        mHandler.sendEmptyMessageDelayed(GOTO_MAIN_ACTIVITY, 2000);

    }
    private static final int GOTO_MAIN_ACTIVITY = 0;

    //從EntranceActivity->MainActivity
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case GOTO_MAIN_ACTIVITY:
                    Intent intent = new Intent();
                    intent.setClass(EntranceActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    onDestroy();
                    break;
                default:
                    break;
            }

        };

    };



    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindDrawables(findViewById(R.id.enter));
        System.gc();        //呼叫gargabe collection
    }


    /**
     * 回收記憶體判斷圖片
     * @param view 要回收的view
     */

    private void unbindDrawables(View view) {                 //回收記憶體判斷圖片
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }

}
