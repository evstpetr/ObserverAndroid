package ru.pes.observer.tasks;

import android.os.AsyncTask;
import android.util.Log;


import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import ru.pes.observer.objects.Sensor;

/**
 * Created by Admin on 07.06.2016.
 */
public class SocketTask extends AsyncTask<Sensor, Void, Void> {
    private Socket client;
    private DataInputStream dis;
    private DataOutputStream dos;
    private static final String SERVER_ADDRESS = "109.123.160.7";
    private static final int PORT = 8081;

    @Override
    protected Void doInBackground(Sensor[] params) {
        Sensor sensor =  params[0];
        System.out.println("Sendind info to server...");
        try {
            client = new Socket(SERVER_ADDRESS, PORT); // соединяемся с сервером
            dis = new DataInputStream(client.getInputStream());
            dos = new DataOutputStream(client.getOutputStream());
            Gson gson = new Gson();
            String json = gson.toJson(sensor);
            dos.writeBytes(json);
            dos.flush();
        } catch (IOException e) {
            Log.e("IO_ERROR", e.getMessage());
        } finally {
            try {
                if (dos!=null) {
                    dos.close();
                }
            } catch (IOException e) {
                Log.e("I_O", "Can't close output... " +e.getMessage());
            }
            try {
                if (dis!=null) {
                    dis.close();
                }
            } catch (IOException e) {
                Log.e("I_O", "Can't close input... " +e.getMessage());
            }
            try {
                if (client!=null) {
                    client.close();
                }
            } catch (IOException e) {
                Log.e("CLIENT_ERROR", "Can't close client... " + e.getMessage());
            }
        }

        return null;
    }
}
