package com.mpc.rtt.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import java.util.List;


public class BleDevice {

    private BluetoothAdapter mBluetoothAdapter;
    private BleService mBleService;

    private BluetoothGatt mGatt;

    public BleDevice(BleService bleService) {
        mBleService = bleService;
        this.mBluetoothAdapter = mBleService.getBluetoothAdapter();
    }

    public void connect (BluetoothDevice device) {
        if (mGatt == null) {
            mGatt = device.connectGatt(mBleService, false, gattCallback);
        }
    }

    public void disconnect() {
        if (mGatt == null) {
            return;
        }
        mGatt.close();
        mGatt = null;
    }


    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("BleDevice", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("BleDevice", "STATE_CONNECTED");
                    mBleService.broadcastUpdate(BleService.ACTION_GATT_CONNECTED);
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("BleDevice", "STATE_DISCONNECTED");
                    mBleService.broadcastUpdate(BleService.ACTION_GATT_DISCONNECTED);
                    disconnect();
                    break;
                default:
                    Log.e("BleDevice", "STATE_OTHER");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            List<BluetoothGattService> services = gatt.getServices();
            for (BluetoothGattService service : services) {
                String uuid = service.getUuid().toString();
                Log.i("BleDevice", uuid + " " + BleUUID.getServiceName(uuid));
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    if (characteristic.getUuid() != null) {
                        uuid = characteristic.getUuid().toString();
                        Log.i("BleDevice", uuid
                                + " " + BleUUID.getCharacteristicName(uuid));
                    } else Log.i("BleDevice", "char uuid is null");
                }
            }

            Log.i("BleDevice","broadcast ACTION_GATT_SERVICES_DISCOVERED");
            mBleService.broadcastUpdate(BleService.ACTION_GATT_SERVICES_DISCOVERED);

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (BleUUID.isCharacteristic(characteristic,"Scale WIFI SSID")) {
                    mBleService.broadcastUpdate(BleService.ACTION_WIFI_SSID_WRITTEN);
                }
                else if (BleUUID.isCharacteristic(characteristic, "Scale WIFI Passphrase")) {
                    //   appContext.getWifiDevice().onPassphraseWritten();
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {

            //Log.v("the scale","onCharacteristicRead "+characteristic.getUuid());

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            Log.v("Bledevice", "onCharacteristicChanged char=" + characteristic.toString() + ",status=" + characteristic.getValue().toString());

            if (BleUUID.isCharacteristic(characteristic, "Scale WIFI Notify")) {
                byte[] status = characteristic.getValue();
                if (status[0] == 0)
                    mBleService.broadcastUpdate(BleService.ACTION_WIFI_NOTIFY_CONNECTED);
                else
                    mBleService.broadcastUpdate(BleService.ACTION_WIFI_NOTIFY_DISCONNECTED);

                //  appContext.getWifiDevice().onWifiNotification(data[0]);
            }
        }
    };

    public void setNotification(String service, String characteristic, boolean enable) {
        if (mBluetoothAdapter == null || mGatt == null) {
            Log.w("BleDevice", "BluetoothAdapter not initialized");
            return;
        }
        /*check if the service is available on the device*/
        BluetoothGattService mCustomService =
                mGatt.getService(BleUUID.getServiceUUID(service));

        if (mCustomService == null) {
            Log.w("BleDevice", service + " not found");
            return;
        }

        BluetoothGattCharacteristic notifyCharacteristic =
                mCustomService.getCharacteristic(BleUUID.getCharacteristicUUID(characteristic));

        if (! mGatt.setCharacteristicNotification(notifyCharacteristic, enable)) {
            Log.w("BleDevice", "Failed to set "+characteristic);
        }
    }

    public void readCharacteristic(String service, String characteristic) {
        if (mBluetoothAdapter == null || mGatt == null) {
            Log.w("BleDevice", "BluetoothAdapter not initialized");
            return;
        }
        /*check if the service is available on the device*/
        BluetoothGattService mCustomService =
                mGatt.getService(BleUUID.getServiceUUID(service));

        if (mCustomService == null) {
            Log.w("BleDevice", service + " not found");
            return;
        }

        BluetoothGattCharacteristic mReadCharacteristic =
                mCustomService.getCharacteristic(BleUUID.getCharacteristicUUID(characteristic));

        mGatt.readCharacteristic(mReadCharacteristic);
    }

    public void writeCharacteristic(String service, String characteristic, byte[] value) {
        if (mBluetoothAdapter == null || mGatt == null) {
            Log.w("BleDevice", "BluetoothAdapter not initialized");
            return;
        }
        /*check if the service is available on the device*/
        BluetoothGattService mCustomService =
                mGatt.getService(BleUUID.getServiceUUID(service));

        if (mCustomService == null) {
            Log.w("BleDevice", service + " not found");
            return;
        }

        Log.v("thescale","service="+service+",char="+characteristic+",val="+value.toString());

        BluetoothGattCharacteristic mWriteCharacteristic =
                mCustomService.getCharacteristic(BleUUID.getCharacteristicUUID(characteristic));

        Log.v("thescale","uuid="+ BleUUID.getCharacteristicUUID(characteristic));


        mWriteCharacteristic.setValue(value);
        if (! mGatt.writeCharacteristic(mWriteCharacteristic)) {
            Log.w("BleDevice", "Failed to write characteristic");
        } else {
            Log.w("BleDevice", "Success write characteristic");
        }
    }

    // Wifi Stuff
    public void enableWifiNotifyCharacteristic() {
        // setNotification("Scale Service WIFI", "Scale WIFI Notify", true);
    }

    public void writeSSIDCharacteristic(byte[] value) {
        writeCharacteristic("Scale Service", "Scale WIFI SSID", value);
    }

    public void writePassphraseCharacteristic(byte[] value) {
        writeCharacteristic("Scale Service", "Scale WIFI Passphrase", value);
    }

}
