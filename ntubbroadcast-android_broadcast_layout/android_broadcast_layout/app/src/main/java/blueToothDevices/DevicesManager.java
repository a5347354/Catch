package blueToothDevices;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import org.apache.commons.lang.SerializationUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import service.CatchService;
import threadExceptionCatch.ThreadUncaughtException;
import vo.ChatMessage;

/**
 * Created by SilentWolf on 2015/5/7.
 */
public class DevicesManager {


    public static void addDevices(DeviceInfo deviceInfo) {
        synchronized (CatchService.currentDevicesInfo) {
           // Log.i("Catch!", "add device :" + deviceInfo.getDevice().getName());
            CatchService.currentDevicesInfo.put(deviceInfo.getDevice().getAddress(), deviceInfo);
        }
    }

    public static void removeDevice(String address) {//從 currentDevicesInfo中  刪除指定的裝置 並中斷執行緒
        synchronized (CatchService.currentDevicesInfo) {
            Iterator<Map.Entry<String, DeviceInfo>> it = CatchService.currentDevicesInfo.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, DeviceInfo> entry = it.next();
                if (address.equals(entry.getValue().getDevice().getAddress())) {
                  //  Log.i("Catch!", "remvoeDevice :" + entry.getValue().getDevice().getName());
                    entry.getValue().resetConnection();
                    entry.getValue().interrupt();
                    entry.getValue().setTerminate(true);
                    it.remove();
                }
            }
        }
       // Log.i("Catch!", "current Devices number : " + CatchService.currentDevicesInfo.size());
    }


    public static void removeAll() { //中斷所有裝置的執行緒 清除 currentDevicesInfo
        synchronized (CatchService.currentDevicesInfo) {
            Iterator<Map.Entry<String, DeviceInfo>> it = CatchService.currentDevicesInfo.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, DeviceInfo> entry = it.next();
                entry.getValue().resetConnection();
                entry.getValue().interrupt();
                entry.getValue().setTerminate(true);
            }
            CatchService.currentDevicesInfo.clear();
        }
    }

    public synchronized  static int sendMessageToAll(final ChatMessage chatMessage) {
        CatchService.cancelScheduleDiscover();//停止自動搜尋裝置
        byte[] data = SerializationUtils.serialize(chatMessage);
        Iterator<Map.Entry<String, DeviceInfo>> it = CatchService.currentDevicesInfo.entrySet().iterator();
      //  Log.i("Catch!", " data len :" + data.length);
        int hasSend=0;
        if(chatMessage.getSendNumber()>0){
            chatMessage.setSendNumber(chatMessage.getSendNumber()-1);
            while (it.hasNext()) {
                Map.Entry<String, DeviceInfo> entry=null ;
                try {
                    entry =it.next();
                    if(entry.getValue().getDevice().getName().startsWith("Catch")){
                       // Log.i("Catch!", "confirmID_class:" + chatMessage.getId() + "_" + chatMessage.getMessageClass());
                        entry.getValue().sendMessage(data);
                        hasSend=hasSend+1;
                    }
                } catch (Exception e) {//發生錯誤　可以是已失去連線
                    if(entry!=null){
                        if(CatchService.currentDevicesInfo.containsKey(entry.getKey())){
                            it.remove();//  從目前連線的裝置的list 中　刪除指定的裝置
                        }
                      //  Log.i("Catch!", "lose connected : " + entry.getValue().getDevice().getName());
                      //  Log.i("Catch!", "cause : " + e.getMessage());
                       // Log.i("Catch!", "current Devices number : " + CatchService.currentDevicesInfo.size());
                    }
                    e.printStackTrace();
                }
            }
        }
        CatchService.startScheduleDiscover();//啟動自動搜尋裝置
        //return hasSend;
        return 1;//demo 測試用
    }


}
