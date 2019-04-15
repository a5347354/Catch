package utils;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import service.CatchService;

/**
 * Created by SilentWolf on 2015/7/5.
 */
public class FileTools {
//    private MeetingActivity meetingActivity;
    public static final String DIR=Environment.getExternalStorageDirectory().toString()+File.separator+"catchFile"+File.separator;// 檔案儲存路徑


    public FileTools(){
        File file =new File(DIR);
        if(!file.exists())file.mkdirs();
    }

//    public FileTools(MeetingActivity meetingActivity){
////        this.meetingActivity=meetingActivity;
//    }

    public  byte[] getBytesFromFile(File f){//將指定檔案 轉成 bytes
        if(f!=null && f.exists()){
            try {
                FileInputStream stream = new FileInputStream(f);
                ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
                byte[] b = new byte[1000];
                int n;
                while ((n = stream.read(b)) != -1)
                    out.write(b, 0, n);
                stream.close();
                out.close();
                return out.toByteArray();
            } catch (IOException e){
                return null;
            }
        }
        return null;
    }

    public  File saveFile(String fileName,byte[] fileByte){//儲存檔案
        try {
            File file = new File(DIR+fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(fileByte);
            fos.close();
            Log.i("Catch!", "saveFile " + DIR + fileName);
            return file;
        }catch (Exception e){
            Log.i("Catch!", "saveFile error " + e.getMessage());
            e.printStackTrace();
            return  null;
        }

    }


    /**
     * @param part 檔名
     * @param ext 副檔名
     * @return file 
     */
    public File saveTempFile(  String part, String ext ){//儲存暫存檔
        try {
            File tempFile =new File(DIR+"/.temp/");
            if(!tempFile.exists())tempFile.mkdirs();
            return File.createTempFile(part, ext, tempFile);
        }catch (Exception e){
           // Log.i("Catch!", "saveTempFile error "+e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


    /**  ============   進行處裡  android 4.4.4 版本   uri 格式不一樣  無法取得檔案絕對路徑  ============**/
    /**
     *
     * @param context
     * @param uri
     * @return String RealPath
     * 進行處裡  android 4.4.4 版本以上   uri 格式不一樣  無法取得檔案絕對路徑
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public String getRealPathFromURI(Context context, Uri uri) {
        //check here to KITKAT or new version
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId( Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if ("com.google.android.apps.photos.content".equals(uri.getAuthority()))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
    public static String getDataColumn(Context context, Uri uri, String selection,String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
    /**  ============  進行處裡  android 4.4.4 版本以上  uri 格式不一樣  無法取得檔案絕對路徑  ============**/
}
