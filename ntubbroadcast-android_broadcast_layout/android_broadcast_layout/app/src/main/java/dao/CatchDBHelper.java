package dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by SilentWolf on 2015/5/20.
 * Version 6 by Fan Chun Hao    on 2015/10/22
 * Version 3 by SilentWolf      on 2015/5/20
 */
public class CatchDBHelper extends SQLiteOpenHelper {//?t?d??????w?B????

    public static final String DATABASE_NAME = "Catch!.db";
    public static final int VERSION =6;
    private static SQLiteDatabase database;

    public CatchDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }
    public static SQLiteDatabase getDatabase(Context context) {
        if (database == null || !database.isOpen()) {
            database = new CatchDBHelper(context, DATABASE_NAME,null, VERSION).getWritableDatabase();
        }
        return database;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {//Create All Table
        sqLiteDatabase.execSQL(MessageDAO.CREATE_TABLE);
        sqLiteDatabase.execSQL(MemberDAO.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {//Drop All Table
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MessageDAO.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MemberDAO.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
