package com.example.ticker;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    InputStream inStream;
    OutputStream outputStream;
    private static final int REQUEST_ENABLE_BT = 1;
    private int eth = 0, btc = 0;

    RequestQueue queue = null;

    protected RequestQueue getQueue() {
        if (queue == null) {
            queue = Volley.newRequestQueue(this.getApplicationContext());
        }
        return queue;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connect();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                getQueue().add(new StringRequest(Request.Method.GET, "https://min-api.cryptocompare.com/data/pricemulti?fsyms=ETH,BTC&tsyms=USD",
                        response -> {
                            // Display the first 500 characters of the response string.
                            try {
                                JSONObject jsonObject = new JSONObject(response.toString());
                                double eth = jsonObject.getJSONObject("ETH").getDouble("USD");
                                double btc = jsonObject.getJSONObject("BTC").getDouble("USD");
                                outputStream.write(((eth + "").substring(0, 6) + (btc + "").substring(0, 7)).getBytes());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }, error -> {
                }));
                handler.postDelayed(this, 3000);
            }
        }, 10000);
    }

    protected void connect() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            Object[] devices = pairedDevices.toArray();
            BluetoothDevice device = (BluetoothDevice) Arrays.stream(devices).filter(el -> ((BluetoothDevice) el).getName().equals("HC-06")).collect(Collectors.toList()).get(0);
            ParcelUuid[] uuid = device.getUuids();
            try {
                BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(uuid[0].getUuid());
                socket.connect();
                Toast.makeText(this, "Socket connected", Toast.LENGTH_LONG).show();
                outputStream = socket.getOutputStream();
                inStream = socket.getInputStream();
            } catch (IOException e) {
                Toast.makeText(this, "Exception found", Toast.LENGTH_LONG).show();
            }

        }
    }

    public void SendMessage(View v) {
        try {
            if (outputStream != null)
                outputStream.write("outMessage".toString().getBytes());
        } catch (IOException e) {/*Do nothing*/}
        Toast.makeText(this, "No output stream", Toast.LENGTH_LONG).show();
    }
}