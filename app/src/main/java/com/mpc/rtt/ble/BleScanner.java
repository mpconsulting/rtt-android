package com.mpc.rtt.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

@TargetApi(21)
public class BleScanner {

    private BluetoothAdapter mBluetoothAdapter;
    private BleService mBleService;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 10000;

    public BleScanner(BleService bleService) {
        mBleService = bleService;
        this.mBluetoothAdapter = mBleService.getBluetoothAdapter();
        mHandler = new Handler();
    }

    public void start() {

        if (Build.VERSION.SDK_INT >= 21) {
            mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
            if (mLEScanner != null)
                Log.d("BleScanner","initialized mLEScanner");
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            filters = new ArrayList<>();
            ScanFilter.Builder builder = new ScanFilter.Builder();
            builder.setDeviceName("RTT-123456789");
            ScanFilter filter = builder.build();
            filters.add(filter);
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT < 21) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                } else {
                    if (mLEScanner != null)
                        mLEScanner.stopScan(mScanCallback);
                    else {
                        Log.e("BleScanner", "mLEScanner is null. Lets redo it.");
                        mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                        if (mLEScanner != null)
                            Log.d("BleScanner","initialized mLEScanner");
                        settings = new ScanSettings.Builder()
                                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                                .build();
                        filters = new ArrayList<>();
                        ScanFilter.Builder builder = new ScanFilter.Builder();
                        builder.setDeviceName("thescale");
                        ScanFilter filter = builder.build();
                        filters.add(filter);
                    }


                }
            }
        }, SCAN_PERIOD);

        if (Build.VERSION.SDK_INT < 21) {
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            if (mLEScanner != null)
                mLEScanner.startScan(filters, settings, mScanCallback);
        }
    }

    public void stop() {
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                if (mLEScanner != null)
                    mLEScanner.stopScan(mScanCallback);
            }
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    mBleService.getBleDevice().connect(device);
                }
            };


    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice btDevice = result.getDevice();
            mBleService.getBleDevice().connect(btDevice);
            stop();
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.d("BleScanner", "onBatchScanResults:"+sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("BleScanner", "onScanFailed" + errorCode);
        }
    };



}
