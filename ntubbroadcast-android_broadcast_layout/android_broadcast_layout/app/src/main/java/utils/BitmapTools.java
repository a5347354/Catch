package utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.widget.ImageView;

import com.broadcastone.broadcast.android_broadcast.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Created by SilentWolf on 2015/8/14.
 */
public class BitmapTools {

    /*
        * 圖片放大縮小
        * ratio>1 放大
        * ratio<1 縮小
        * */
    public Bitmap reSizeBitmap(Bitmap bitmap,float ratio){
        Matrix matrix = new Matrix();
        matrix.postScale(ratio,ratio);
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        return resizeBmp;
    }

    public Bitmap drawableToBitmap(int drawble,Resources resources){
       // Resources res=resources;
       // Bitmap bmp=BitmapFactory.decodeResource(res, drawble);
        Resources res=resources;
        InputStream is = res.openRawResource(drawble);
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = 2;   //width，hight設為原來的二分一    避免oom
        Bitmap bmp =BitmapFactory.decodeStream(is, null, options);
        return bmp;
    }

    public byte[] bitmapToBytes(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100,baos);
        return baos.toByteArray();
    }

    /** BitmapBytes Compress 圖片位元組壓縮
     * @param bitmapBytes Bitmap Bytes 圖片位元組
     * @param compressRatio Compress Ratio 壓縮比率 100為無壓縮
     * @return return Bitmap Bytes 回傳壓縮後圖片位元組
     */
    public static byte[] bitmapBytesCompress(byte[] bitmapBytes,int compressRatio){
        Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        //100 not compress，30 mean 70% Compression ratio
        bitmap.compress(Bitmap.CompressFormat.PNG,compressRatio, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

}
