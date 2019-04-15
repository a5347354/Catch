package vo;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

/**
 * Created by SilentWolf on 2015/5/11.
 * 對應到資料庫的欄位
 */
public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String macAddress;
    private String deviceName;  // 原發送者
    private String messageClass; //訊息類別
    private String text;  //文字
    private byte[] imageBytes={0};  //圖片　
    private byte[] supImageBytes={0};  //廠商圖示
    private byte[] recordingBytes={0};//語音
    private String dateTime;
    private int sendNumber=5;// 被發送次數  當數值等於0 不再發送

    public ChatMessage() {
        this.id="";
        this.deviceName="";
        this.text="";
        this.imageBytes=new byte[0];
        this.recordingBytes = new byte[0];
    }

    public void setImageBitmap(Bitmap bitmap){
        if(bitmap!=null){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100,baos);
            byte[] imageBytes = baos.toByteArray();
            this.imageBytes = imageBytes;
        }else{
            this.imageBytes=null;
        }
    }

    public void setSupImageBitmap(Bitmap bitmap){
        if(bitmap!=null){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100,baos);
            byte[] supImageBytes = baos.toByteArray();
            this.supImageBytes = supImageBytes;
        }else{
            this.supImageBytes=null;
        }
    }


    public String getId() {
        return id;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessageClass() {
        return messageClass;
    }

    public void setMessageClass(String messageClass) {
        this.messageClass = messageClass;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }

    public void setImageBytes(byte[] imageBytes) {
        if(imageBytes==null){
            this.imageBytes=new byte[0];
        }
        this.imageBytes = imageBytes;
    }

    public byte[] getRecordingBytes() {
        return recordingBytes;
    }

    public void setRecordingBytes(byte[] recordingBytes) {
        if(recordingBytes==null){
            this.recordingBytes = new byte[0];
        }
        this.recordingBytes = recordingBytes;
    }


    public byte[] getSupImageBytes() {
        return supImageBytes;
    }

    public void setSupImageBytes(byte[] supImageBytes) {
        if(supImageBytes==null){
            this.supImageBytes=new byte[0];
        }
        this.supImageBytes = supImageBytes;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public int getSendNumber() {
        return sendNumber;
    }

    public void setSendNumber(int sendNumber) {
        this.sendNumber = sendNumber;
    }
}
