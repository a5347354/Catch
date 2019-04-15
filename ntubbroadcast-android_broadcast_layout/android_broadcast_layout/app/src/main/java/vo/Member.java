package vo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import utils.BitmapTools;

/**
 * Created by ChuLay on 2015/10/11.
 * 廠商資料
 */
public class Member implements Serializable {
    private String id;
    private String name;
    private byte[] photoBytes = {0};  //圖片
    private String macAddress=null;
    private String deviceName=null;
    private String className=null;
    private int image;
    private String newInfo;


    public Member() {
        super();
        this.id = "";
        this.image = 0;
        this.photoBytes = new byte[0];
        this.name = "";
    }
    public Member(String id, int image, String name) {
        super();
        this.id = id;
        this.image = image;
        this.name = name;
    }

    public Member(String macAddress,String userName, Bitmap photo){
        this.macAddress = macAddress;
        this.name = userName;
        this.setPhoto(photo);
    }
    public Member(String macAddress,String userName, byte[] photo){
        this.macAddress = macAddress;
        this.name = userName;
        setPhoto(photo);
    }


    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getImageBytes() {
        return photoBytes;
    }

    public void setImageBytes(byte[] photoBytes) {
        this.photoBytes = photoBytes;
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

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Bitmap getPhotoBitMap(){
        Bitmap bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
        return bitmap;
    }

    public void setPhoto(byte[] photo) {
        if(photo==null){
            this.photoBytes=new byte[0];
        }else{
            this.photoBytes= photo;
        }

    }

    public void setPhoto(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        //100 not compress，30 mean 70% Compression ratio
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        setPhoto(byteArray);
    }

    public String getNewInfo() {
        return newInfo;
    }

    public void setNewInfo(String newInfo) {
        this.newInfo = newInfo;
    }
}
