package blueToothDevices;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.UUID;

import chatLayout.ChatActivity;
import service.CatchService;
import threadExceptionCatch.ThreadUncaughtException;

/**
 * Created by SilentWolf on 2015/5/6.
 */
public class BlueToothConnect {
    private static final String NAME_INSECURE = "BluetoothChatInsecure";


    private final BluetoothAdapter mAdapter;
    private  int mState;
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    public static final int STATE_DISCOVERING=4;
    public static final int STATE_LOST_CONNECTED=5;
    private ArrayList<UUID> mUuids;

    private BlueToothAccept blueToothAccept;
    private BlueToothConnecting blueToothConnecting;
    public static boolean isConnecting=false;

    public BlueToothConnect() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mUuids = new ArrayList<UUID>();

        //mUuids.add(UUID.fromString("b7746a40-c758-4868-aa19-7ac6b3475dfc"));
        //mUuids.add(UUID.fromString("2d64189d-5a2c-4511-a074-77f199fd0834"));
        //mUuids.add(UUID.fromString("e442e09a-51f3-4a7b-91cb-f638491d1412"));
        //mUuids.add(UUID.fromString("a81d6504-4536-49ee-a475-7d96d09439e4"));
        //mUuids.add(UUID.fromString("aa91eab1-d8ad-448e-abdb-95ebba4a9b55"));
        //mUuids.add(UUID.fromString("4d34da73-d0a4-4f40-ac38-917e0a9dee97"));
        //mUuids.add(UUID.fromString("5e14d4df-9c8a-4db7-81e4-c937564c86e0"));
        //mUuids.add(UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66"));
        mUuids.add(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
    }


    public  synchronized void setState(int state,BluetoothDevice device) {
        String str="connected count :"+CatchService.currentDevicesInfo.size();
        mState = state;
        switch (mState){
            case STATE_NONE:
                str=str+", status:  do nothing";
                break;
            case STATE_LISTEN :
                str=str+" , status: listening";
                break;
            case STATE_CONNECTING :
                String deviceName ="";
                if(device!=null)deviceName=device.getName();
                str=str+", status: connecting  "+deviceName ;
                break;
            case STATE_CONNECTED  :
                break;
            case STATE_DISCOVERING:
                str=str+", status:  do discovering";
                break;
            case STATE_LOST_CONNECTED:
                str=str+", status: lose "+device.getName();
                break;
        }

        if(ChatActivity.chatActivityHandler!=null){
            Bundle b = new Bundle();
            b.putString("stateBar",str);
            Message msgObj=ChatActivity.chatActivityHandler.obtainMessage();
            msgObj.setData(b);
            ChatActivity.chatActivityHandler.sendMessage(msgObj);
        }

    }

    public  synchronized int getState() {
        return mState;
    }

    public synchronized void start() {
        // Cancel any thread attempting to make a connection
        if (blueToothConnecting != null) {blueToothConnecting.cancel(); blueToothConnecting = null;}


        // Start the thread to listen on a BluetoothServerSocket
        if (blueToothAccept == null) {
            blueToothAccept = new BlueToothAccept();
            blueToothAccept.setUncaughtExceptionHandler(new ThreadUncaughtException());
            blueToothAccept.start();
        }
        setState(STATE_LISTEN,null);
    }



    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        if(mAdapter.isEnabled() && !mAdapter.isDiscovering()){
            while (!isConnecting){
                isConnecting=true;
            }

            if (blueToothConnecting != null) {blueToothConnecting.cancel(); blueToothConnecting = null;}
            if(!CatchService.currentDevicesInfo.containsKey(device.getAddress()) && ( device.getName().startsWith("Catch") ||device.getName().startsWith("SupCatch")  )){
                // Create a new thread and attempt to connect to each UUID one-by-one.
                for (int i = 0; i < mUuids.size(); i++) {
                    setState(STATE_CONNECTING,device);
                    try {
                        blueToothConnecting = new BlueToothConnecting(device, mUuids.get(i));
                        blueToothConnecting.setUncaughtExceptionHandler(new ThreadUncaughtException());
                        blueToothConnecting.start();
                    } catch (Exception e) {
                    }
                }
            }
        }
    }


    public  class BlueToothConnecting extends Thread{
        private final BluetoothSocket bluetoothSocket;
        private final BluetoothDevice device;
        //private UUID tempUuid;

        public BlueToothConnecting(BluetoothDevice device, UUID uuidToTry) {
            this.device=device;
            BluetoothSocket tmp = null;
            //tempUuid = uuidToTry;
            try {
                //Log.i("Catch!","connecting "+device.getName());
                //tmp =device.createInsecureRfcommSocketToServiceRecord(uuidToTry);
                tmp = device.createRfcommSocketToServiceRecord(uuidToTry);
            } catch (Exception e) {
            }
            bluetoothSocket = tmp;
        }
        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                if(!CatchService.currentDevicesInfo.containsKey(device.getAddress()) && mAdapter.isEnabled() && ( device.getName().startsWith("Catch") || device.getName().startsWith("SupCatch"))){
                    //Log.i("Catch!","connecting "+device.getName());
                    if(mAdapter.isDiscovering()){
                        mAdapter.cancelDiscovery();
                    }
                    boolean isConnectSuccess=false;
                    for(int i=0;i<3;i++){
                        try {
                            //Log.i("Catch!",i+"");
                            bluetoothSocket.connect();
                            isConnectSuccess=true;
                            i=3;
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    if(isConnectSuccess){
                        DeviceInfo deviceInfo = new DeviceInfo(device, bluetoothSocket);
                        deviceInfo.setUncaughtExceptionHandler(new ThreadUncaughtException(deviceInfo));
                        deviceInfo.start();//連線成功  把裝置 交給一個獨立的  執行序  去接收資料
                        DevicesManager.addDevices(deviceInfo);
                        CatchService.checkConnectionNumber();
                        setState(STATE_CONNECTED, device);
                    }else {
                        setState(STATE_LISTEN,null);
                        try {
                            bluetoothSocket.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        BlueToothConnect.this.start();
                        return;
                    }
                    // Reset the ConnectThread because we're done
                    synchronized (BlueToothConnect.this) {
                        blueToothConnecting = null;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                Log.i("Catch!","finally");
                while (isConnecting){
                    isConnecting=false;
                }
                Bundle b = new Bundle();
                b.putString("delTempMacAddress", device.getAddress());
                Message msgObj= CatchService.serviceHandler.obtainMessage();
                msgObj.setData(b);
                CatchService.serviceHandler.sendMessage(msgObj);
            }

        }
    }




    public  class BlueToothAccept extends Thread{
        private  BluetoothServerSocket serverSocket = null;
        private boolean terminate = false;
        private  final BluetoothAdapter mAdapter=BluetoothAdapter.getDefaultAdapter();
        public BlueToothAccept(){
        }

        @Override
        public void run() {
            // Log.i("Catch!", "start accept");

            while (true){
                BluetoothSocket socket = null;
                try{
                    for (int i = 0; i < mUuids.size(); i++) {
                        //serverSocket = mAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME_INSECURE, mUuids.get(i));
                        serverSocket = mAdapter.listenUsingRfcommWithServiceRecord(NAME_INSECURE, mUuids.get(i));
                        socket = serverSocket.accept();
                        if (socket != null) {
                            String address = socket.getRemoteDevice().getAddress();
                            DeviceInfo deviceInfo =null;
                            if(socket!=null){ //接收到 其他裝置的連線  將裝置  存到 DevicesManager.currentDevicesInfo
                                if(socket.getRemoteDevice().getName()!=null && (socket.getRemoteDevice().getName().startsWith("Catch") ||socket.getRemoteDevice().getName().startsWith("SupCatch"))){
                                    //Log.i("Catch!", "accepted device :" + socket.getRemoteDevice().getName());
                                    deviceInfo = new DeviceInfo(socket.getRemoteDevice(),socket);
                                    DevicesManager.addDevices(deviceInfo);
                                    deviceInfo.setUncaughtExceptionHandler(new ThreadUncaughtException(deviceInfo));
                                    deviceInfo.start();
                                    //Log.i("Catch!", "current Devices number : " + CatchService.currentDevicesInfo.size());
                                    CatchService.checkConnectionNumber();
                                    setState(STATE_CONNECTED, socket.getRemoteDevice());
                                }else{
                                    socket.close();
                                }

                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    //Log.i("Catch!", "accept() failed "+e);
                }
            }

        }

    }

}
