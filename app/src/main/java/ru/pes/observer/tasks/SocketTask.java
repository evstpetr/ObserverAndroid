package ru.pes.observer.tasks;

import android.os.AsyncTask;
import android.util.Log;


import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import ru.pes.observer.objects.Message;
import ru.pes.observer.objects.Sensor;


public class SocketTask extends AsyncTask<Sensor, Void, Void> {
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private ArrayList<Object> aList;
    private static final String SERVER_ADDRESS = "109.123.160.7";
    private static final int PORT = 8081;

    @Override
    protected Void doInBackground(Sensor[] params) {
        Sensor sensor = params[0];
        System.out.println("Sendind info to server...");
        try {
            client = new Socket(SERVER_ADDRESS, PORT); // соединяемся с сервером
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);
            Gson gson = new Gson();
            Message message = new Message();
            message.setType("SEN");
            message.setData(gson.toJson(sensor));
            String json = gson.toJson(message);
            out.println(json);
            String answer;
            int answerCount = 0;
            while ((answer = in.readLine()) != null) {
                System.out.println(answer);
                if (answer.equalsIgnoreCase("ERR") && answerCount < 7) {
                    answerCount++;
                    out.println(json);
                } else if (answer.equalsIgnoreCase("OK")) {
                    break;
                } else {
                    Log.e("CON_ERROR", "Bad connection...");
                    break;
                }
            }
        } catch (IOException e) {
            Log.e("IO_ERROR", e.getMessage());
        } finally {
            if (out != null) {
                out.close();
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                Log.e("I_O", "Can't close input... " + e.getMessage());
            }
            try {
                if (client != null) {
                    client.close();
                }
            } catch (IOException e) {
                Log.e("CLIENT_ERROR", "Can't close client... " + e.getMessage());
            }
        }

        return null;
    }
}
