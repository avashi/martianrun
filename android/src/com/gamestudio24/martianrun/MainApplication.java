/*
 * Copyright (c) 2014. William Mora
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gamestudio24.martianrun;

import android.app.Application;
import com.gamestudio24.martianrun.android.R;
import com.google.android.gms.analytics.GoogleAnalytics;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.gamestudio24.martianrun.actors.bluetoothConnector;

public class MainApplication extends Application {
    private BluetoothSocket connection;
    private DataInputStream iis;
    private double sensorX = 0;
    private BluetoothServerSocket myServerSocket = null;
    //private BluetoothServerSocket myServerSocket_r = null;
    private BluetoothAdapter mBtAdapter = null;
    BluetoothSocket myBSock_l = null;
    private static final UUID MY_UUID=
            UUID.fromString("5db4284e-01ff-435c-9b80-2df4e78bd214"); //L, motorola or nexus 5.
    private static final String NAME = "BluetoothManager";
    AsyncTask<Integer, Integer, Integer> asynctask;


    //bluetooth manager whatever
    //*********************
//listen usingrfcommwithservicerecord
    //reciever socet
     //*********************
    bluetoothConnector listener;
    @Override
    public void onCreate() {
        super.onCreate();
        GoogleAnalytics.getInstance(this).newTracker(R.xml.app_tracker_config);
        //put data from manager here use bluetooth thread here PUT DATA HERE?... nah not in onCreate.. that wouldnt be good; put into new method... listener.dodge(0);
        final BluetoothManager bluetoothManager  = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        //create a bluetooth server socket on galaxy and then a socket on the zte...
        BluetoothAdapter bluetoothAdapter= bluetoothManager.getAdapter();
        try{
            Log.e("CONNECT", "ATTEMPTING CONNECTION");
            connection = bluetoothAdapter.getRemoteDevice("C0:11:73:F7:12:58").createRfcommSocketToServiceRecord(MY_UUID);
            Toast.makeText(this, "Attempting Connection...", Toast.LENGTH_LONG).show();
            connection.connect();
            iis = new DataInputStream(connection.getInputStream());
        } catch(Exception e){
            Log.e("ERROR", e.getMessage());
        }
        try {
            myServerSocket = mBtAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);//.createUsingRfcommWithServiceRecord(NAME, MY_UUID); //
            try {
                myBSock_l = myServerSocket.accept();
            } catch(Exception e){
                Log.e("ERROR", e.getMessage());
            }// If a connection was accepted
            try {
                if (myServerSocket != null)
                    myServerSocket.close();
            } catch(Exception e){
                Log.e("ERROR", e.getMessage());
            }
            // asynctask = new Bt_AsyncSocketServer(); // 1st thread
            //asynctask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); // works > API 13
        } catch (Exception e){
            Log.e("ERROR", e.getMessage());
        }


        Thread gyroThread1 = new Thread(new GyroThreadx());
        gyroThread1.start();
    }

    public class GyroThreadx implements Runnable {

        @Override
        public void run () {
            Log.e("Log", "started thread");

            while(connection.isConnected()) //when bluetooth is connected
            {

                try {
                    sensorX = iis.readDouble();
                    Log.d("LogData",""+sensorX);
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
                    listener.dodge(sensorX);
            }

        }

    }

};

