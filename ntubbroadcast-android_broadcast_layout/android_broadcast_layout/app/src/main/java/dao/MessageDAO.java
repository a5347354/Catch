package dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import vo.ChatMessage;
import vo.Member;

/**
 * Created by SilentWolf on 2015/5/20.
 * 定義 資料表、資料表CRUD操作
 */
public class MessageDAO {

    public static final String TABLE_NAME="Message";

    private static final String ID="id";
    private static final String MAC_ADDRESS="macAddress";
    private static final String DEVICE_Name="deviceName";
    private static final String MESSAGE_CLASS="messageClass";
    private static final String TEXT="text";  //文字
    private static final String IMAGE="image";
    private static final String RECORDING ="recording";
    private static final String DATETIME="dateTime";

    public static final String CREATE_TABLE=
            "CREATE TABLE " + TABLE_NAME +
             " (" +
                ID + " TEXT  PRIMARY KEY , " +
                MAC_ADDRESS + " TEXT NOT NULL, " +
                DEVICE_Name + " TEXT NOT NULL, " +
                MESSAGE_CLASS + " TEXT NOT NULL, " +
                TEXT + " TEXT," +
                IMAGE+ " Blob,"+
                RECORDING +" Blob," +
                DATETIME +" TEXT  "+
             ")" ;

    private SQLiteDatabase db;

    public MessageDAO(Context context){
        db = CatchDBHelper.getDatabase(context);
    }


    /**
     * 檢查 訊息是否重複
     * @param messageID MessageID
     * @return return True/False
     * */
    public boolean checkExists(String messageID){
        String query = "SELECT  * FROM " + TABLE_NAME +" WHERE "+ ID +" = ?";
        String condition[]=new String[]{messageID};
        Cursor cursor = db.rawQuery(query,condition);
        int cnt = cursor.getCount();
        if(cursor.getCount() <= 0){
            return false;
        }
        return true;
    }

    public void addMessage(ChatMessage member){
        ContentValues cv = new ContentValues();
        cv.put(ID, member.getId());
        cv.put(MAC_ADDRESS, member.getMacAddress());
        cv.put(DEVICE_Name, member.getDeviceName());
        cv.put(MESSAGE_CLASS, member.getMessageClass());
        cv.put(TEXT, member.getText());
        cv.put(IMAGE, member.getImageBytes());
        cv.put(RECORDING, member.getRecordingBytes());
        cv.put(DATETIME,member.getDateTime());
        db.insert(TABLE_NAME,null,cv);
    }

    public void delMessage(String messageID){
        String where =ID+ "=?";
        String condition[]=new String[]{messageID};
        db.delete(TABLE_NAME, where, condition);
    }


    /**
     * 刪除過期資料
     * */
    public void cleanOutdateHisotry(){
        //刪除訊息
        List<ChatMessage> chatMessagesList=getAllMessage();
        for(ChatMessage chatMessage:chatMessagesList){
            try {
                Calendar now = Calendar.getInstance();
                Calendar date= Calendar.getInstance();
                date.setTime(DateFormat.getDateTimeInstance().parse(chatMessage.getDateTime()));
                long diff =now.getTimeInMillis()-date.getTimeInMillis();
                //long diffDays = diff / (24 * 60 * 60 * 1000);  //單位:天數
                long diffMin = diff / (60 * 1000); //單位:分鐘
                if(diffMin>5){
                    delMessage(chatMessage.getId());
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public long getCountByAddress(String address){
        return DatabaseUtils.queryNumEntries(db, TABLE_NAME, MAC_ADDRESS + "=? ", new String[]{address});
    }

    public List<ChatMessage> getAllPersonalMessage(){//取得所有私人訊息
        String query = "SELECT  * FROM " + TABLE_NAME +" WHERE "+MESSAGE_CLASS +" = ?";
        String condition[]=new String[]{"personal"};
        Cursor cursor = db.rawQuery(query,condition);
        ChatMessage member =null;
        List<ChatMessage> tempList=new ArrayList<ChatMessage>();
        if (cursor.moveToFirst()) {
            do {//從資料表 撈出來的資料  封裝成 Message 物件
                member = new ChatMessage();
                member.setId(cursor.getString(0));
                member.setMacAddress(cursor.getString(1));
                member.setDeviceName(cursor.getString(2));
                member.setMessageClass(cursor.getString(3));
                member.setText(cursor.getString(4));
                member.setImageBytes(cursor.getBlob(5));
                member.setRecordingBytes(cursor.getBlob(6));
                member.setDateTime(cursor.getString(7));
                tempList.add(member);
            } while (cursor.moveToNext());
        }
        return tempList;
    }


    public ChatMessage getTheNewAdMessage(String macAddress){ //取得指定廠商  最新廣告
        String query = "SELECT  * FROM " + TABLE_NAME +" WHERE "+ MESSAGE_CLASS +" != ?  and  "+MAC_ADDRESS+" ==?   ORDER BY "+ID+" DESC LIMIT 1 ";
        String condition[]=new String[]{"personal",macAddress};
        Cursor cursor = db.rawQuery(query,condition);
        if(cursor.getCount()>0){
            cursor.moveToLast();
            ChatMessage message =new ChatMessage();
            message.setId(cursor.getString(0));
            message.setMacAddress(cursor.getString(1));
            message.setDeviceName(cursor.getString(2));
            message.setMessageClass(cursor.getString(3));
            message.setText(cursor.getString(4));
            message.setImageBytes(cursor.getBlob(5));
            message.setRecordingBytes(cursor.getBlob(6));
            message.setDateTime(cursor.getString(7));
            return  message;
        }
        return  null;

    }

    public List<ChatMessage> getAllAdMessage(String macAddress){ //取得指定廠商所有廣告訊息
        String query = "SELECT  * FROM " + TABLE_NAME +" WHERE "+ MESSAGE_CLASS +" != ?  and  "+MAC_ADDRESS+" ==?  ";
        String condition[]=new String[]{"personal",macAddress};
        Cursor cursor = db.rawQuery(query,condition);
        ChatMessage message =null;
        List<ChatMessage> tempList=new ArrayList<ChatMessage>();
        if (cursor.moveToFirst()) {
            do {//從資料表 撈出來的資料  封裝成 Message 物件
                message = new ChatMessage();
                message.setId(cursor.getString(0));
                message.setMacAddress(cursor.getString(1));
                message.setDeviceName(cursor.getString(2));
                message.setMessageClass(cursor.getString(3));
                message.setText(cursor.getString(4));
                message.setImageBytes(cursor.getBlob(5));
                message.setRecordingBytes(cursor.getBlob(6));
                message.setDateTime(cursor.getString(7));
                tempList.add(message);
            } while (cursor.moveToNext());
        }
        return tempList;
    }


    public List<ChatMessage>getAllMessage(){
        String query = "SELECT  * FROM " + TABLE_NAME ;
        Cursor cursor = db.rawQuery(query,null);
        ChatMessage message =null;
        List<ChatMessage> tempList=new ArrayList<ChatMessage>();
        if (cursor.moveToFirst()) {
            do {//從資料表 撈出來的資料  封裝成 Message 物件
                message = new ChatMessage();
                message.setId(cursor.getString(0));
                message.setMacAddress(cursor.getString(1));
                message.setDeviceName(cursor.getString(2));
                message.setMessageClass(cursor.getString(3));
                message.setText(cursor.getString(4));
                message.setImageBytes(cursor.getBlob(5));
                message.setRecordingBytes(cursor.getBlob(6));
                message.setDateTime(cursor.getString(7));
                tempList.add(message);
            } while (cursor.moveToNext());
        }
        return tempList;
    }


    public ChatMessage getMessageById(String id){
        String query = "SELECT  * FROM " + TABLE_NAME +" WHERE "+ ID +" = ?";
        String condition[]=new String[]{id};

        Cursor cursor = db.rawQuery(query,condition);
        ChatMessage message=null;
        if(cursor.moveToFirst()){
            message = new ChatMessage();
            message.setId(cursor.getString(0));
            message.setMacAddress(cursor.getString(1));
            message.setDeviceName(cursor.getString(2));
            message.setMessageClass(cursor.getString(3));
            message.setText(cursor.getString(4));
            message.setImageBytes(cursor.getBlob(5));
            message.setRecordingBytes(cursor.getBlob(6));
            message.setDateTime(cursor.getString(7));
        }
        return message;
    }

}
