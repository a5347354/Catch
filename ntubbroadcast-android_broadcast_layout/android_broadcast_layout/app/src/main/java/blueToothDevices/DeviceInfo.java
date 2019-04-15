package blueToothDevices;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.broadcastone.broadcast.AdActivity;

import org.apache.commons.lang.SerializationUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;

import chatLayout.ChatActivity;
import preferencesSeting.PreferencesSeting;
import service.CatchService;
import vo.ChatMessage;
import vo.Member;

/**
 * Created by SilentWolf on 2015/5/6.
 */
public class DeviceInfo extends  Thread{  //個別裝置的資訊
    private final BluetoothDevice device;
    private  BluetoothSocket bluetoothSocket;
    private  ObjectInputStream inputStream;
    private  OutputStream outputStream;
    private boolean terminate = false;
    public DeviceInfo(BluetoothDevice device, BluetoothSocket bluetoothSocket) {
        this.device = device;
        this.bluetoothSocket = bluetoothSocket;
        OutputStream tempOutputStream=null;
        try {
            tempOutputStream=bluetoothSocket.getOutputStream();
        }catch (Exception e){
            e.printStackTrace();
        }
        this.outputStream=tempOutputStream;
    }


    @Override
    public void run() {
        //Log.i("Catch!", "Start " + device.getName() + "  inputStreamt");

        while (!terminate) { //  接收此裝置所傳送的訊息
            try {
                inputStream = new ObjectInputStream(bluetoothSocket.getInputStream());
                final ChatMessage chatMessage =(ChatMessage) inputStream.readObject();
                //從sqlite 判斷 messageID  、接收的類型 都符合
                if(!CatchService.messageDAO.checkExists(chatMessage.getId()) && PreferencesSeting.checkAcceptClass(chatMessage.getMessageClass()) && !BluetoothAdapter.getDefaultAdapter().getAddress().equals(chatMessage.getMacAddress())){
                   //Log.i("Catch!", "accept message from :" + chatMessage.getMacAddress());

                    if(chatMessage.getMessageClass().equals("personal")){
                        CatchService.messageDAO.addMessage(chatMessage);//將訊息 存到資料庫
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if(ChatActivity.chatActivityHandler!=null && ChatActivity.chatActivityHandler.obtainMessage()!=null){
                                    Bundle b = new Bundle();
                                    b.putSerializable("chatMessage", SerializationUtils.serialize(chatMessage));
                                    Message msgObj = ChatActivity.chatActivityHandler.obtainMessage();
                                    msgObj.setData(b);
                                    ChatActivity.chatActivityHandler.sendMessage(msgObj);
                                }
                            }
                        });
                        t.start();
                    }else{  //這裡進行 廣告畫面的控制
                        if(PreferencesSeting.checkAcceptClass(chatMessage.getMessageClass())){
                           // Log.i("Catch!", "允許接收訊息 :" + chatMessage.getMessageClass());
                            Member member=new Member(chatMessage.getMacAddress(),chatMessage.getDeviceName(),chatMessage.getSupImageBytes());
                            CatchService.messageDAO.addMessage(chatMessage);//將訊息 存到資料庫
                            CatchService.memberDAO.addMember(member);
                            if(chatMessage.getText()=="" && (chatMessage.getImageBytes().length>0)){
                                member.setNewInfo("收到廣告圖片");
                            }else{
                                member.setNewInfo(chatMessage.getText());
                            }
                                if(AdActivity.adActivityHandler!=null){ // 廠商List
                                    Bundle b = new Bundle();
                                    b.putSerializable("member", SerializationUtils.serialize(member));
                                    Message msgObj= AdActivity.adActivityHandler.obtainMessage();
                                    msgObj.setData(b);
                                    AdActivity.adActivityHandler.sendMessage(msgObj);
                                }
                                if(ChatActivity.chatActivityHandler!=null){ //進入指定廠商
                                    Bundle b = new Bundle();
                                    b.putSerializable("type", "ad");
                                    b.putSerializable("adMessage", SerializationUtils.serialize(chatMessage));
                                    Message msgObj= ChatActivity.chatActivityHandler.obtainMessage();
                                    msgObj.setData(b);
                                    ChatActivity.chatActivityHandler.sendMessage(msgObj);
                                }
                        }else{
                          //  Log.i("Catch!", "拒絕接收訊息 :" + chatMessage.getMessageClass());
                        }


                    }
                    CatchService.showNotification(chatMessage);//通知使用者 收到訊息
                    DevicesManager.sendMessageToAll(chatMessage);
                }

            } catch (Exception e) {
               // Log.i("Catch!","deviceInfo accept error  "+e.getMessage());
                e.printStackTrace();
                break;
            }
        }

    }

    public void sendMessage(byte[] message) throws Exception {// 將訊息 傳給此裝置
       // Log.i("Catch!", "start send");
        byte[] data = new byte[1024];
        InputStream is = new ByteArrayInputStream(message);
       // BufferedInputStream bis = new BufferedInputStream(is);
        int len=0;
        
        while ((len =is.read(data)) != -1) {
          //  Log.i("Catch!", "do write");
            outputStream.write(data, 0, len);
          //  Log.i("Catch!", "writed " + len);

            outputStream.flush();
            Bundle b = new Bundle();
            b.putString("type", "increase");
            b.putInt("len", len);
            if(ChatActivity.progressBarHandler!=null){
                Message msgObj = ChatActivity.progressBarHandler.obtainMessage();
                msgObj.setData(b);
                ChatActivity.progressBarHandler.sendMessage(msgObj);
            }

        }
        outputStream.flush();
        Thread.currentThread().sleep(1000);
      //  Log.i("Catch!", "send end");
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public boolean isConnected(){
        return bluetoothSocket.isConnected();
    }

    public boolean isTerminate() {
        return terminate;
    }

    public void setTerminate(boolean terminate) {
        this.terminate = terminate;
    }


    public void resetConnection() {
        if (inputStream != null) {
            try {inputStream.close();} catch (Exception e) {}
            inputStream = null;
        }
        if (outputStream != null) {
            try {outputStream.close();} catch (Exception e) {}
            outputStream = null;
        }
        if (bluetoothSocket != null) {
            try {bluetoothSocket.close();} catch (Exception e) {}
            bluetoothSocket = null;
        }
    }


}
