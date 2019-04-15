package vo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

import utils.BitmapTools;

/**
 * Created by SilentWolf on 2015/8/13.
 */
public class Photo {
    private String macAddress;
    private byte[] photo = {0};

    public Photo(){
        this.macAddress = "";
        this.photo = new byte[0];
    }

    public Photo(String macAddress,byte[] photo){
        this.macAddress = macAddress;
        this.photo = photo;
    }

    public Photo(String macAddress, Bitmap photo){
        this.macAddress = macAddress;
        this.setPhoto(photo);
    }
    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public Bitmap getPhotoBitMap(){
        Bitmap bitmap = BitmapFactory.decodeByteArray(photo, 0, photo.length);
        return bitmap;
    }

    public void setPhoto(byte[] photo) {
        if(photo==null){
            this.photo=new byte[0];
        }
        this.photo= photo;
    }

    public void setPhoto(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        setPhoto(byteArray);
    }

}
