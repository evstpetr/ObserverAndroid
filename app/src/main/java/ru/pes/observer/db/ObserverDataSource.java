package ru.pes.observer.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class ObserverDataSource {
    private SQLiteDatabase database;
    private DbHelper dbHelper;
    private String[] sensorsDbColumns = {DbHelper.COLUMN_ID, DbHelper.COLUMN_SENSOR_ID};
    private String[] phonesDbColumns = {DbHelper.P_COLUMN_ID, DbHelper.P_COLUMN_NUMBER, DbHelper.P_COLUMN_USER_ID};

    public ObserverDataSource(Context context) {
        dbHelper = new DbHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // Add
    public void addSensorId(String sensorId) {
        ContentValues values = new ContentValues();
        values.put(DbHelper.COLUMN_SENSOR_ID, sensorId);
        database.insert(DbHelper.TABLE_SENSORS, null, values);
    }

    public void addPhone(int number, int userId) {
        ContentValues values = new ContentValues();
        values.put(DbHelper.P_COLUMN_NUMBER, number);
        values.put(DbHelper.P_COLUMN_USER_ID, userId);
        database.insert(DbHelper.TABLE_PHONES, null, values);
    }

    // Get
    public List<String> getAllSensorIds() {
        List<String> ids = new ArrayList<>();

        Cursor cursor = database.query(DbHelper.TABLE_SENSORS, null, null, null, null, null, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            ids.add(cursor.getString(1));
            cursor.moveToNext();
        }

        cursor.close();

        return ids;
    }

    // Delete
    public void deleteSensorId(String sensorId) {
        database.delete(DbHelper.TABLE_SENSORS, DbHelper.COLUMN_SENSOR_ID + " = " + "\"" + sensorId + "\"", null);
    }

    public void deleteAll() {
        for (String s:getAllSensorIds()) {
            System.out.println(s);
            deleteSensorId(s);
        }
    }
}
