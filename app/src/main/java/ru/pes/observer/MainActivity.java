package ru.pes.observer;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import ru.pes.observer.fragments.MainFragment;
import ru.pes.observer.fragments.SearchFragment;
import ru.pes.observer.objects.Sensor;
import ru.pes.observer.service.MainService;
import ru.pes.observer.tasks.BleTask;
import ru.pes.observer.tasks.DbTask;
import ru.pes.observer.tasks.SocketTask;
import ru.pes.observer.utils.Decoder;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String RUNNING = "SERVICE_ALREADY_RUNNING";
    private ProgressBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new CustomAdapter(getSupportFragmentManager()));
        pager.setCurrentItem(1);
    }

    private MainActivity getActivity() {
        return this;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStart:
                if (!checkRunningServices(MainService.class)) {
                    startService(new Intent(this, MainService.class));
                    Toast.makeText(this, "Запуск сервиса", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Сервис запущен", Toast.LENGTH_SHORT).show();
                    Log.i(RUNNING, "Service running...");
                }
                break;
            case R.id.btnStop:
                stopService(new Intent(this, MainService.class));
                Toast.makeText(this, "Остановка сервиса", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnSearch:
                BleSingleTask task = new BleSingleTask();
                task.execute(this.getBaseContext());
                bar = (ProgressBar) findViewById(R.id.progressBar);
                bar.setVisibility(View.VISIBLE);
                break;
            case R.id.btnSave:
                ListView list = (ListView) findViewById(R.id.lvSensors);
                SparseBooleanArray chosen = list.getCheckedItemPositions();
                List ids = new ArrayList();
                for (int i = 0; i < chosen.size(); i++) {
                    if (chosen.valueAt(i)) {
                        ids.add(list.getAdapter().getItem(chosen.keyAt(i)));
                    }

                }
                DbTask dbTask = new DbTask();
                dbTask.execute(getApplicationContext(), ids);
                break;
        }
    }

    private boolean checkRunningServices(Class<?> serviceClass) {
        ActivityManager am = (ActivityManager) this
                .getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(50); //50 - The maximum number of entries to return in the list.
        // The actual number returned may be smaller, depending on how many services are running.
        for (int i = 0; i < rs.size(); i++) {
            ActivityManager.RunningServiceInfo rsi = rs.get(i);
            if (rsi.service.getClassName().equals(serviceClass.getName())) {
                return true;
            }
        }
        return false;
    }

    private class CustomAdapter extends FragmentPagerAdapter {

        public CustomAdapter(FragmentManager mgr) {
            super(mgr);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return (SearchFragment.newInstance());
                case 1:
                    return (MainFragment.newInstance(getActivity()));
                default:
                    return (MainFragment.newInstance(getActivity()));
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    private class BleSingleTask extends AsyncTask<Object, Void, Void> {

        private BluetoothAdapter mBluetoothAdapter;
        private GregorianCalendar calendar;
        private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        private Sensor sensor;
        private String address;
        private List<String> data;
        private final char[] hexArray = "0123456789ABCDEF".toCharArray();
        private static final String DEVICE_ADDRESS = "10:10:10:10:10:10";

        @Override
        protected Void doInBackground(Object... params) {
            StartBlueTooth((Context) params[0]);
            return null;
        }

        /**
         * Функция сканирования
         *
         * @param context
         */
        private void StartBlueTooth(Context context) {
            System.out.println("Start scanning...");
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            sensor = new Sensor(); // Объект для хранения данных
            data = new ArrayList<>(); // Данные
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
                try {
                    Thread.sleep(3000); // Даем время на включения БТ
                } catch (InterruptedException e) {
                    Log.e("THREAD_SLEEP", "Can't wait while BT turning on... " + e.getMessage());
                }
            }
            if (Build.VERSION.SDK_INT >= 19) {
                scanLeDevice(context);
            }
        }

        private void scanLeDevice(final Context context) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        Log.e("THREAD_SLEEP", "Can't sleep... " + e.getMessage());
                    }
                    sendResults(context);
                }
            }).start();
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }

        private BluetoothAdapter.LeScanCallback mLeScanCallback =
                new BluetoothAdapter.LeScanCallback() {
                    @Override
                    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                        if (device.getAddress().equals(DEVICE_ADDRESS)) { // Если адресс устройства пригоден читаем данные
                            byte[] msg = new byte[26];
                            System.arraycopy(scanRecord, 5, msg, 0, 26); // массив байт в котором находятся данные
                            data.add(bytesToHex(msg));
                        }
                    }
                };

        /**
         * Отправляем результаты сканирование на экран пользователя
         *
         * @param context
         */
        private void sendResults(final Context context) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mBluetoothAdapter.disable();
            final Set sensId = new HashSet();
            for (String str : data) {
                try {
                    byte[] all, part = new byte[16];
                    all = Hex.decodeHex(str.toCharArray());
                    // Расшифровка
                    System.arraycopy(all, 0, part, 0, 16);
                    System.arraycopy(Decoder.decrypt(part), 0, all, 0, 16);
                    /*System.arraycopy(all, 10, part, 0, 16);
                    System.arraycopy(Decoder.decrypt2(part), 0, all, 10, 16);
                    System.arraycopy(all, 6, part, 0, 16);
                    System.arraycopy(Decoder.decrypt2(part), 0, all, 6, 16);*/
                    // Чтение
                    char[] string;
                    String id = "", count ="";
                    string = bytesToHex(all).toCharArray();
                    // Идентификатор
                    for (int i = 4; i < 12; i++) {
                        id = id + string[i];
                    }
                    for (int i = 12; i < 14; i++) {
                        count = count + string[i];
                    }
                    System.out.println("id: " + id + " count: " + Integer.parseInt(count, 16));
                    sensId.add(id);
                } catch (DecoderException e) {
                    Log.e("DECODE ERROR", "Can't decode...");
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ListView listView = (ListView) findViewById(R.id.lvSensors);
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_checked, new ArrayList<String>(sensId));
                    listView.setAdapter(adapter);
                    bar.setVisibility(View.INVISIBLE);
                }
            });
        }

        /**
         * Функция преобразования массива байт в хекс строку
         *
         * @param bytes
         * @return HexString
         */
        public String bytesToHex(byte[] bytes) {
            char[] hexChars = new char[bytes.length * 2];
            for (int j = 0; j < bytes.length; j++) {
                int v = bytes[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars);
        }
    }
}
