package ru.pes.observer.tasks;


import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.List;

import ru.pes.observer.db.SensorIdsDataSource;

public class DbTask extends AsyncTask<Object, Void, Object> {

    SensorIdsDataSource source;

    @Override
    protected Object doInBackground(Object... params) {
        source = new SensorIdsDataSource((Context) params[0]);
        source.open();

        List<String> ids = (List<String>) params[1];
        source.deleteAll();
        for (String s : ids) {
            source.addSensorId(s);
        }
        source.close();

        return params[0];
    }

    @Override
    protected void onPostExecute(Object result) {
        Toast.makeText((Context) result, "Список датчиков сохранен", Toast.LENGTH_LONG).show();

    }
}
