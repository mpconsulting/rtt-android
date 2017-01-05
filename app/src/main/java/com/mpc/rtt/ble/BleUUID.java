package com.mpc.rtt.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class BleUUID {
    private static HashMap<String, String> mServices = new HashMap<>();
    private static HashMap<String, String> mCharacteristics = new HashMap<>();

    public BleUUID() {
        mServices.put("00001800-0000-1000-8000-00805f9b34fb", "Generic Access");
        mServices.put("00001801-0000-1000-8000-00805f9b34fb", "Generic Attribute");
        mServices.put("0000180a-0000-1000-8000-00805f9b34fb", "Device info service");
        mServices.put("0000180f-0000-1000-8000-00805f9b34fb", "Battery Service");
        mServices.put("4a000000-0000-1000-8000-00805f4a494f", "Scale Service");

        mCharacteristics.put("00002a00-0000-1000-8000-00805f9b34fb", "Device Name");
        mCharacteristics.put("00002a01-0000-1000-8000-00805f9b34fb", "Appearance");
        mCharacteristics.put("00002a05-0000-1000-8000-00805f9b34fb", "Service Changed");
        mCharacteristics.put("00002a29-0000-1000-8000-00805f9b34fb", "Device Manu Name");
        mCharacteristics.put("00002a24-0000-1000-8000-00805f9b34fb", "Device Model Number");
        mCharacteristics.put("00002a23-0000-1000-8000-00805f9b34fb", "Device System ID");
        mCharacteristics.put("00002a19-0000-1000-8000-00805f9b34fb", "Battery Level");
        mCharacteristics.put("4a004900-0000-1000-8000-00805f4a494f", "Scale WIFI SSID");
        mCharacteristics.put("4a004901-0000-1000-8000-00805f4a494f", "Scale WIFI Passphrase");
        mCharacteristics.put("4a004902-0000-1000-8000-00805f4a494f", "Scale Weight");
        mCharacteristics.put("4a004903-0000-1000-8000-00805f4a494f", "Scale Person Cnt");

    }

    static public String getServiceName(final String uuid) {
        String result = mServices.get(uuid);
        if (result == null) result = "Unknown Service";
        return result;
    }

    static public String getCharacteristicName(final String uuid) {
        String result = mCharacteristics.get(uuid);
        if (result == null) result = "Unknown Characteristic";
        return result;
    }

    static public UUID getServiceUUID(final String name) {
        String key = getKeyByValue(mServices, name);
        if (key != null)
            return UUID.fromString(key);
        else {
            Log.e("getServiceUUID","no key found for "+name);
            return null;
        }
    }

    static public UUID getCharacteristicUUID(final String name) {
        String key = getKeyByValue(mCharacteristics, name);
        if (key != null)
            return UUID.fromString(key);
        else {
            Log.e("getServiceUUID","no key found for "+name);
            Log.e("getServiceUUID",mCharacteristics.toString());
            return null;
        }
    }

    static public boolean isCharacteristic(BluetoothGattCharacteristic characteristic, String name) {
        return (characteristic.getUuid().equals(BleUUID.getCharacteristicUUID(name)));
    }

    static private <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
}
