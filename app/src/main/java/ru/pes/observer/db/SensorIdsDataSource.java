package ru.pes.observer.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class SensorIdsDataSource {
    private SQLiteDatabase database;
    private DbHelper dbHelper;
    private String[] allColumns = {DbHelper.COLUMN_ID, DbHelper.COLUMN_SENSOR_ID};

    public SensorIdsDataSource(Context context) {
        dbHelper = new DbHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void addSensorId(String sensorId) {
        ContentValues values = new ContentValues();
        values.put(DbHelper.COLUMN_SENSOR_ID, sensorId);
        database.insert(DbHelper.TABLE_NAME, null, values);
    }

    public List<String> getAllSensorIds() {
        List<String> ids = new ArrayList<>();

        Cursor cursor = database.query(DbHelper.TABLE_NAME, null, null, null, null, null, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            ids.add(cursor.getString(1));
            cursor.moveToNext();
        }

        return ids;
    }

    public void deleteSensorId(String sensorId) {
        database.delete(DbHelper.TABLE_NAME, DbHelper.COLUMN_SENSOR_ID + " = " + "\"" + sensorId + "\"", null);
    }

    public void deleteAll() {
        for (String s:getAllSensorIds()) {
            System.out.println(s);
            deleteSensorId(s);
        }
    }
}
