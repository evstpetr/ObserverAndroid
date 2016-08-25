package ru.pes.observer.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    //Структура таблицы датчиков
    public static final String TABLE_SENSORS = "sensor_ids";
    public static final String COLUMN_ID = "_ID";
    public static final String COLUMN_SENSOR_ID = "SENSOR_ID";
    //Структура таблицы пользователей
    public static final String TABLE_USERS = "users";
    public static final String U_COLUMN_ID = "_ID";
    public static final String U_COLUMN_NAME = "LOGIN";
    public static final String U_COLUMN_STATE = "STATE";
    //Структура таблицы телефонов (для рассылки)
    public static final String TABLE_PHONES = "phones";
    public static final String P_COLUMN_ID = "_ID";
    public static final String P_COLUMN_USER_ID = "USER_ID";
    public static final String P_COLUMN_NUMBER = "PHONE";
    //БД
    private static final String DATABASE_NAME = "local.db";
    private static final int DATABASE_VERSION = 1;

    //Создание таблиц
    private String CREATE_TABLE = "CREATE TABLE "
            + TABLE_SENSORS + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_SENSOR_ID + " TEXT);";
    private String CREATE_TABLE_USERS = "CREATE TABLE "
            + TABLE_USERS + " (" + U_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + U_COLUMN_NAME + " TEXT, "
            + U_COLUMN_STATE + " TEXT);";
    private String CREATE_TABLE_PHONES = "CREATE TABLE "
            + TABLE_PHONES + " (" + P_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + P_COLUMN_USER_ID + " (" + " INTEGER, "
            + P_COLUMN_NUMBER + " INTEGER);";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_PHONES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SENSORS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHONES);
        onCreate(db);
    }
}
