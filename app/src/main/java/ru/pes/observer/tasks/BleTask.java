package ru.pes.observer.tasks;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.TextView;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import ru.pes.observer.db.SensorIdsDataSource;
import ru.pes.observer.objects.Sensor;
import ru.pes.observer.utils.Decoder;

/**
 * Created by Admin on 07.06.2016.
 */
public class BleTask extends AsyncTask<Object, Void, String> {
    private BluetoothAdapter mBluetoothAdapter;
    private GregorianCalendar calendar;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Sensor sensor;
    private String address;
    private List<String> data;
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static final String DEVICE_ADDRESS = "10:10:10:10:10:10";

    @Override
    protected String doInBackground(Object... params) {

        StartBlueTooth((Context) params[0]);

        return null;
    }

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
                Thread.sleep(2000); // Даем время на включения БТ
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

    // запаковываем данные добавляя адресс устрояства, время отправки и хеш сумму
    private void sendResults(Context context) {
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        mBluetoothAdapter.disable();
        data = getFilteredData(data, context);
        address = mBluetoothAdapter.getAddress();
        sensor.setAddress(address);
        sensor.setData(data);
        calendar = new GregorianCalendar(TimeZone.getDefault());
        sensor.setDate(sdf.format(calendar.getTime()));
        sensor.setHash(sensor.getAddress().hashCode() + sensor.getData().hashCode() + sensor.getDate().hashCode());
        if (data.size() > 0) {
            SocketTask task = new SocketTask();
            task.execute(sensor);
        }
    }

    public List getFilteredData(List<String> list, Context context) {
        Map<String, String> mapId = new HashMap<>();
        List filteredData = new ArrayList();
        for (String str : list) {
            try {
                byte[] all, part = new byte[16];
                all = Hex.decodeHex(str.toCharArray());
                // Расшифровка
                System.arraycopy(all, 0, part, 0, 16);
                System.arraycopy(Decoder.decrypt(part), 0, all, 0, 16);
                // Чтение
                char[] string;
                String id = "";
                string = bytesToHex(all).toCharArray();
                // Идентификатор
                for (int i = 4; i < 12; i++) {
                    id = id + string[i];
                }
                mapId.put(id, str);
            } catch (DecoderException e) {
                Log.e("DECODE ERROR", "Can't decode...");
            }
        }
        SensorIdsDataSource source = new SensorIdsDataSource(context);
        source.open();
        List<String> ids = source.getAllSensorIds();
        source.close();
        for (String s : ids) {
            filteredData.add(mapId.get(s));
        }
        return filteredData;
    }

    /**
     * Функция преобразования массива байт в хекс строку
     *
     * @param bytes
     * @return HexString
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
