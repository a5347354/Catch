package service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.broadcastone.broadcast.android_broadcast.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import blueToothDevices.BlueToothConnect;
import blueToothDevices.DeviceInfo;
import blueToothDevices.DevicesManager;
import broadcastReceiver.BlueToothBroadcastReceiver;
import chatLayout.ChatActivity;
import dao.MessageDAO;
import dao.MemberDAO;
import preferencesSeting.PreferencesSeting;
import threadExceptionCatch.ThreadUncaughtException;
import vo.ChatMessage;


/**
 * Created by SilentWolf on 2015/5/17.
 */
public class CatchService extends Service {
    private static final int MAX_CONNECTION_NUMBER=5;//連線最大數量
    public static MessageDAO messageDAO;
    public static MemberDAO memberDAO;
    public static CatchService catchService;
    public static Map<String,DeviceInfo> currentDevicesInfo = new HashMap<String, DeviceInfo>();//紀錄目前連線的裝置
    private static Timer scheduleDiscover,connectTimer;
    private BlueToothBroadcastReceiver blueToothBroadcastReceiver=new BlueToothBroadcastReceiver();
    private IBinder mBinder = new LocalBinder();
    public static Handler serviceHandler;

    private static BlueToothConnect blueToothConnect;
    final static BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
    private ArrayList<String> tempMacAddress =new ArrayList();
    @Override
    public void onCreate() {
        //  Log.i("Catch!","onCreate Catch!");
        blueToothConnect=new BlueToothConnect();
        messageDAO=new MessageDAO(this);
        memberDAO =new MemberDAO(this);


        int i=0;
        while (!BluetoothAdapter.getDefaultAdapter().getName().startsWith("Catch") &&  (i<100)){
            BluetoothAdapter.getDefaultAdapter().setName("Catch"+bluetoothAdapter.getDefaultAdapter().getName());
            i=i+1;
        }

        startScheduleDiscover();//  啟動自動搜尋裝置

        catchService =this;
        PreferencesSeting.initPreferencesSeting();

        if (blueToothConnect != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (blueToothConnect.getState() == BlueToothConnect.STATE_NONE) {
                // Start the Bluetooth chat services
                blueToothConnect.start();//   接收 其他裝置來連線
            }
        }

        //註冊監聽器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(blueToothBroadcastReceiver, filter);



        serviceHandler=new Handler(){
            public void handleMessage(Message msg) {
                //Log.i("Catch!",""+(msg.getData()!=null && msg.getData().getString("deviceAddress")!=null));
                if(msg.getData()!=null && msg.getData().getString("connectDeviceAddress")!=null){
                    String address= msg.getData().getString("connectDeviceAddress");
                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                    //Log.i("Catch!", "start connect " + device.getName());
                    if(!currentDevicesInfo.containsKey(device.getAddress()) && ( device.getName().startsWith("Catch") || device.getName().startsWith("SupCatch")  )){
                        blueToothConnect.connect(device);
                    }
                }
                if(msg.getData()!=null && msg.getData().getString("delTempMacAddress")!=null){
                    String address= msg.getData().getString("delTempMacAddress");
                    if(tempMacAddress.remove(address)){
                        //Log.i("Catch!","tempMacAddress remvoe "+address);
                    }
                }
                if(msg.getData().getInt("stateBar")==BlueToothConnect.STATE_LOST_CONNECTED){
                    String address=msg.getData().getString("deviceAddress");
                    if(msg.getData().getString("deviceAddress")!=null){
                        if(tempMacAddress.remove(address)){
                            //Log.i("Catch!","tempMacAddress remvoe "+device.getAddress());
                        }
                        BluetoothDevice device=BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
                        blueToothConnect.setState(msg.getData().getInt("stateBar"), device);
                    }
                }
            }
        };


        if(connectTimer==null){
            connectTimer=new Timer();
            connectTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    //Log.i("Catch!","connectTimer run");
                    if(!BlueToothConnect.isConnecting && !bluetoothAdapter.isDiscovering()){
                        if(!blueToothConnect.isConnecting && blueToothBroadcastReceiver.memberMacDevice.size()>0){
                            for(BluetoothDevice device:blueToothBroadcastReceiver.memberMacDevice){
                                if(!CatchService.currentDevicesInfo.containsKey(device.getAddress()) && !Arrays.asList(tempMacAddress).contains(device.getAddress())){
                                    tempMacAddress.add(device.getAddress());
                                    blueToothConnect.connect(device);
                                    break;
                                }
                            }
                        }
                    }
                }
            }, 30000,5000);
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("*** Service onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        DevicesManager.removeAll();
        unregisterReceiver(blueToothBroadcastReceiver);//註銷監聽器
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    public class LocalBinder extends Binder {
        public CatchService getServerInstance() {
            return CatchService.this;
        }
    }


    public static void checkConnectionNumber(){//判斷 連線數量 是否 >=最大值
        if(currentDevicesInfo.size()>=MAX_CONNECTION_NUMBER){
            //  Log.i("Catch!", "over max number stop scheduleDiscover");
            cancelScheduleDiscover();//停止自動搜尋裝置
        }else{
            //  Log.i("Catch!", "not over max number start scheduleDiscover");
            startScheduleDiscover();//啟動自動搜尋裝置
        }
    }




    public static void startScheduleDiscover(){  //啟動排程搜尋
        class ScheduleDiscoverBuleToothDevice extends TimerTask { //排程任務
            @Override
            public void run() {
                if(!bluetoothAdapter.isDiscovering() && !blueToothConnect.isConnecting){
                    //Log.i("Catch!", "Discover  blueToothConnect.getState=" + blueToothConnect.getState()+blueToothConnect.isConnecting);
                    blueToothConnect.setState(BlueToothConnect.STATE_DISCOVERING,null);
                    bluetoothAdapter.startDiscovery();
                }
            }
        }


        if(BluetoothAdapter.getDefaultAdapter().isEnabled()){//若bluetooth  isEnabled  true   就  啟動排成
            bluetoothAdapter.startDiscovery();
            if(scheduleDiscover==null){
                scheduleDiscover= new Timer();
                scheduleDiscover.schedule(new ScheduleDiscoverBuleToothDevice(), 0, 60000);// 自動搜尋裝置
            }
        }

        //    Log.i("Catch!","startScheduleDiscover");
    }

    public static void cancelScheduleDiscover(){ //取消排程搜尋
        if(scheduleDiscover!=null){
            scheduleDiscover.cancel();
            scheduleDiscover=null;
        }
        // Log.i("Catch!","cancelScheduleDiscover");
    }

    public static  void showNotification(ChatMessage chatMessage) {//設定通知 訊息
        Intent intent ;
        if(!chatMessage.getMessageClass().equals("personal")){
            intent=new Intent(catchService, ChatActivity.class);// for 廣告
            intent.putExtra("type","ad");
            intent.putExtra("macAddress",chatMessage.getMacAddress());
            intent.putExtra("supName",chatMessage.getDeviceName());
        }else{
            intent=new Intent(catchService, ChatActivity.class);  //for 私人訊息
        }

        NotificationManager mNM= (NotificationManager) catchService.getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.mipmap.ic_launcher, "accept message from "+ chatMessage.getDeviceName(),System.currentTimeMillis());
        PendingIntent contentIntent = PendingIntent.getActivity(catchService, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setLatestEventInfo(catchService,"Title", chatMessage.getDeviceName()+":"+ chatMessage.getText(), contentIntent);

        notification.ledARGB = 0xff00ff00;
        notification.ledOnMS = 300;
        notification.ledOffMS = 1000;
        notification.flags|= Notification.FLAG_SHOW_LIGHTS;
        notification.flags|=Notification.FLAG_AUTO_CANCEL;

        mNM.notify(1, notification);
    }





}
