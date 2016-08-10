package ru.pes.observer.service;

import android.app.ActivityManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MainService extends Service {

    private final String START = "START_SERVICE";
    private final String STOP = "STOP_SERVICE";
    Alarm alarm = new Alarm();

    public MainService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Старт будильников
        Log.i(START, "Start service...");
        alarm.setAlarm(this);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;//Сервис без превязки
    }

    @Override
    public void onDestroy() {
        Log.i(STOP, "Stop service...");
        alarm.CancelAlarn(this);
        super.onDestroy();
    }

}
