package preferencesSeting;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.broadcastone.broadcast.android_broadcast.R;

import service.CatchService;

/**
 * Created by SilentWolf on 2015/5/16.
 */
public class PreferencesSeting{
    public static final String YOUR_Name="YourName";
    public static final String FIRST_RUN = "first";//判斷 是否第一次執行
    public  static final String FOOD ="foodClass"; //食物類
    public  static final String CLOTHES ="clothesClass";//衣服類
    public static final String ENTERTAINMENT="entertainmentClass";//娛樂類

    public static void initPreferencesSeting(){
        SharedPreferences sp=CatchService.catchService.getSharedPreferences("Catch!",0);
        SharedPreferences.Editor editor=sp.edit();
        boolean isFirst= sp.getBoolean(FIRST_RUN,true);//若 FIRST_RUN 沒有值  預設為 true;
        if(isFirst){//第一次執行
            editor.putString(YOUR_Name, BluetoothAdapter.getDefaultAdapter().getName());
            editor.putBoolean(FOOD,true);// 給予 FOOD 值 true
            editor.putBoolean(CLOTHES,true);//給予 CLOTHES 值 true
            editor.putBoolean(ENTERTAINMENT,true);//給予 ENTERTAINMENT 值 true
            editor.putBoolean(FIRST_RUN, false);//給予 FIRST_RUN 值 false
            editor.commit();
        }
    }

    public static void setPreferences(String messageClass,boolean b){
        SharedPreferences sp =CatchService.catchService.getApplication().getSharedPreferences("Catch!", 0);
        SharedPreferences.Editor editor=sp.edit();
        editor.putBoolean(messageClass,b);//將指定的訊息類別 值  改成 true or false
        editor.commit();
    }
    public static boolean getPreferences(String messageClass){
        SharedPreferences sp =CatchService.catchService.getApplication().getSharedPreferences("Catch!", 0);
        return sp.getBoolean(messageClass,true); // 若 messageClass  沒有值  返回預設值 true
    }

    public static boolean checkAcceptClass(String messageClass){// 判斷 接收到的訊息 是否符合 偏好
        if(messageClass.equals("personal")){  //私人訊息　　
            return true;
        }else{
            SharedPreferences sp =CatchService.catchService.getApplication().getSharedPreferences("Catch!", 0);
            return sp.getBoolean(messageClass,true); // 若 messageClass  沒有值  返回預設值 true
        }
    }

    public String getYouerName(){
        SharedPreferences sp =CatchService.catchService.getApplication().getSharedPreferences("Catch!", 0);
        return sp.getString(YOUR_Name,"輸入手機名稱");
    }

    public void setYourName(String youerName){
        SharedPreferences sp=CatchService.catchService.getApplication().getSharedPreferences("Catch!", 0);
        SharedPreferences.Editor editor=sp.edit();
        editor.putString(YOUR_Name,youerName);
        editor.commit();
    }

}
