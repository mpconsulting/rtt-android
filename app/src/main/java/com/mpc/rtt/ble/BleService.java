package com.mpc.rtt.ble;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class BleService extends Service {

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String ACTION_WIFI_SSID_WRITTEN =
            "com.rtt.thescale.ble.ACTION_WIFI_SSID_WRITTEN";
    public final static String ACTION_WIFI_NOTIFY_CONNECTED =
            "com.rtt.thescale.ble.ACTION_WIFI_NOTIFY_CONNECTED";
    public final static String ACTION_WIFI_NOTIFY_DISCONNECTED =
            "com.rtt.thescale.ble.ACTION_WIFI_NOTIFY_DISCONNECTED";
    public final static String ACTION_READ_SCALE_WEIGHT =
            "com.example.bluetooth.le.ACTION_READ_SCALE_WEIGHT";
    public final static String ACTION_READ_SCALE_PERSON_CNT =
            "com.example.bluetooth.le.ACTION_READ_SCALE_PERSON_CNT";

    private BluetoothAdapter mBluetoothAdapter;
    private BleScanner mBleScanner;
    private BleDevice mBleDevice;

    public class LocalBinder extends Binder {
        public BleService getService() {
            return BleService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }
    private final IBinder mBinder = new LocalBinder();

    public void close() {
        if (mBleDevice != null)
            mBleDevice.disconnect();
        if (mBleScanner != null)
            mBleScanner.stop();
    }

    public boolean init() {
        Log.v("thescale","BleService init");
        final BluetoothManager bluetoothManager =
                (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        return true;
    }

    public void scan() {
        BleUUID bleUUID = new BleUUID();
        mBleScanner = new BleScanner(this);
        mBleDevice = new BleDevice(this);
        Log.v("thescale","start BLE scan");
        mBleScanner.start();
    }

    public BleDevice getBleDevice() {
        return mBleDevice;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    public void broadcastUpdate(final Intent intent) {
        sendBroadcast(intent);
    }

}
