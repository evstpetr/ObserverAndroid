package ru.pes.observer.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    //Структура таблицы
    public static final String TABLE_NAME = "sensor_ids";
    public static final String COLUMN_ID = "_ID";
    public static final String COLUMN_SENSOR_ID = "SENSOR_ID";

    //БД
    private static final String DATABASE_NAME = "local.db";
    private static final int DATABASE_VERSION = 1;

    //Создание таблицы
    private String CREATE_TABLE = "CREATE TABLE "
            + TABLE_NAME + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_SENSOR_ID + " TEXT);";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
