package broadcastReceiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import blueToothDevices.BlueToothConnect;
import blueToothDevices.DevicesManager;
import service.CatchService;

/**
 * Created by SilentWolf on 2015/5/6.
 */
public class BlueToothBroadcastReceiver extends BroadcastReceiver {
    private ArrayList<BluetoothDevice> discoveryDeviceList=new ArrayList();
    public ArrayList<BluetoothDevice> memberMacDevice =new ArrayList();



    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            final BluetoothDevice device =intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String action =intent.getAction();
            switch (action){
                case BluetoothAdapter.ACTION_STATE_CHANGED:  //bluetooth 啟動 or 關閉
                    BlueToothConnect.isConnecting=false;
                    BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
                    int i=0;
                    while (!BluetoothAdapter.getDefaultAdapter().getName().startsWith("Catch") &&  (i<100)){
                        BluetoothAdapter.getDefaultAdapter().setName("Catch"+bluetoothAdapter.getDefaultAdapter().getName());
                        i=i+1;
                    }
                    //Log.i("Catch!",BluetoothAdapter.getDefaultAdapter().getName());
                    if(BluetoothAdapter.getDefaultAdapter().isEnabled()){
                        CatchService.startScheduleDiscover();//啟動自動搜尋裝置
                    }else {
                        CatchService.cancelScheduleDiscover();//停止自動搜尋裝置
                    }
                    break;
                case BluetoothDevice.ACTION_PAIRING_REQUEST:  //配對請求
                    try {

                        //自動接收配對
                        //device.getClass().getMethod("cancelPairingUserInput", boolean.class).invoke(device);
                        Log.i("Catch!", "PAIRING_REQUEST "+device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, true));
                    }catch (Exception e){
                        e.printStackTrace();
                        // Log.i("Catch!", "PAIRING_REQUEST error:" + e.getMessage());
                    }
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    //Log.i("Catch!","find "+ device.getName());

                    if( device.getName().startsWith("Catch") ||device.getName().startsWith("SupCatch")){
                        BluetoothDevice tempDevice=device;
                        if(discoveryDeviceList.size()<1){
                            discoveryDeviceList.add(tempDevice);
                        }else{
                            boolean isExist=false;
                            for(int x = 0; x<discoveryDeviceList.size();x++){
                                if(tempDevice.getAddress().equals(discoveryDeviceList.get(x).getAddress())) {
                                    isExist=true;
                                    x=discoveryDeviceList.size()+1;
                                }
                            }
                            if(!isExist){
                                discoveryDeviceList.add(tempDevice);
                            }
                        }
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    memberMacDevice.clear();
                    memberMacDevice.addAll(discoveryDeviceList);
                    discoveryDeviceList.clear();
                    break;
//            case BluetoothDevice.ACTION_ACL_CONNECTED://連線 Listener
//
//                break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED://斷線 Listener
                    //Log.i("Catch!", "lose connect  " + device.getName());
                    DevicesManager.removeDevice(device.getAddress());//清除失去連線的裝置
                    CatchService.checkConnectionNumber();
                    device.getClass().getMethod("removeBond", (Class[]) null).invoke(device, (Object[]) null);  //移除配對
                    Bundle b = new Bundle();
                    b.putInt("stateBar", BlueToothConnect.STATE_LOST_CONNECTED);
                    b.putString("deviceAddress", device.getAddress());
                    Message m=CatchService.serviceHandler.obtainMessage();
                    m.setData(b);
                    CatchService.serviceHandler.sendMessage(m);
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
