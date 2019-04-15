package dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import utils.BitmapTools;
import vo.Member;
import vo.Photo;

/**
 * Created by SilentWolf on 2015/8/13.
 */
public class MemberDAO {
    public static final String TABLE_NAME="Member";
    public static final String USERNAME="UserName";
    public static final String PHOTO="Photo";
    public static final String MAC_ADDRESS="Address";
    public static final String DEVICENAME="DeviceName";
    public static final String CLASS="Class";
    public static final String LASTTIME="LastTime";
    private static final int NUM_MAC_ADDRESS=0;
    private static final int NUM_USERNAME=1;
    private static final int NUM_PHOTO=2;
    private static final int NUM_DEVICENAME=3;
    private static final int NUM_CLASS=4;
    private static final int NUM_LASTTIME=5;
    public static final String CREATE_TABLE=
            "CREATE TABLE " + TABLE_NAME +
                    " (" +
                    MAC_ADDRESS + " TEXT PRIMARY KEY," +
                    USERNAME    + " TEXT," +
                    PHOTO       + " BLOB," +
                    DEVICENAME  + " TEXT," +
                    CLASS       + " TEXT," +
                    LASTTIME    + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")" ;

    private SQLiteDatabase db;
    public MemberDAO(Context context) {
        db = CatchDBHelper.getDatabase(context);
    }

    public void addMember(Member member){
        ContentValues cv = new ContentValues();
        cv.put(MAC_ADDRESS, member.getMacAddress());
        cv.put(USERNAME, member.getName());
        if(member.getImageBytes()!=null && member.getImageBytes().length>1){
            cv.put(PHOTO, BitmapTools.bitmapBytesCompress(member.getImageBytes(), 30));//將ImageBytes壓縮後放入資料庫
        }
        cv.put(DEVICENAME, member.getDeviceName());
        cv.put(CLASS, member.getClassName());

        if(getMember(member.getMacAddress())!=null){
            updateMember(member);//address 是PK  若PK存在 進行更新
        }else{
            db.insert(TABLE_NAME,null,cv);
        }
    }

    public Member getMember(String macAddress){
        String query = "SELECT  *  FROM " + TABLE_NAME +"  WHERE "+ MAC_ADDRESS +" = ?";
        String condition[]=new String[]{macAddress};
        Cursor cursor = db.rawQuery(query,condition);
        int cnt = cursor.getCount();
        if(cursor.getCount() > 0){
            if(cursor.moveToFirst()){
                Member member=new Member();
                member.setMacAddress(cursor.getString(NUM_MAC_ADDRESS));
                member.setImageBytes(cursor.getBlob(NUM_PHOTO));
                return  member;
            }
        }
        return null;
    }

    public void updateMember(Member member){
        ContentValues cv = new ContentValues();
        cv.put(PHOTO, member.getImageBytes());
        String where = MAC_ADDRESS + "=\"" + member.getMacAddress()+"\"";
        db.update(TABLE_NAME, cv, where, null);
//        return db.update(TABLE_NAME, cv, where, null) > 0;
    }

    public void deleteMember(String macAddress){
        String where =MAC_ADDRESS+ "=?";
        String condition[]=new String[]{macAddress};
        db.delete(TABLE_NAME, where, condition);
    }

    public void deleteAllMember(){
        String where =" 1 = 1 ";
        db.delete(TABLE_NAME, where, null);
    }

    public boolean checkExists(String macAddress){
        String query = "SELECT  * FROM " + TABLE_NAME +" WHERE "+ MAC_ADDRESS +" = ?";
        String condition[]=new String[]{macAddress};
        Cursor cursor = db.rawQuery(query,condition);
        int cnt = cursor.getCount();
        if(cursor.getCount() <= 0){
            return false;
        }
        return true;
    }

    public List<Member> getAllMember(){//取得廠商資料
        String query = "SELECT  * FROM " + TABLE_NAME + " WHERE ?";
        String condition[]=new String[]{"1 = 1"};//條件

        Cursor cursor = db.rawQuery(query,condition);
       // Log.i("getAllMemberCount", String.valueOf(cursor.getCount()));
        Member member =null;
        List<Member> tempList=new ArrayList<Member>();
        if (cursor.moveToFirst()) {
            do {//從資料表 撈出來的資料  封裝成 Member 物件
                member = new Member();
                member.setMacAddress(cursor.getString(NUM_MAC_ADDRESS));//廠商MAC
                member.setName(cursor.getString(NUM_USERNAME));
                member.setImageBytes(cursor.getBlob(NUM_PHOTO));//Logo
                member.setDeviceName(cursor.getString(NUM_DEVICENAME));
                member.setClassName(cursor.getString(NUM_CLASS));
                tempList.add(member);
            } while (cursor.moveToNext());
        }
        return tempList;
    }

}
