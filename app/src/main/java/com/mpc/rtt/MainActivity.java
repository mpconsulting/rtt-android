package com.mpc.rtt;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import com.mpc.rtt.ble.BleService;
import com.mpc.rtt.ble.BleWifiSetup;

public class MainActivity extends AppCompatActivity {

    private BleService mBleService;
    private BleWifiSetup bleWifiSetup;

    private Handler mRescanHandler;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int BLE_RESCAN_INTERVAL = 1000;

    private static final int SENSOR_INTERVAL = 1000;

    private TextView mConnectionView;
    private boolean mBleConnected = false;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.v("rtt","OnCreate MainActivity");

        mConnectionView = (TextView)findViewById(R.id.connection_status);
        mProgress = new ProgressDialog(this);

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        mRescanHandler = new Handler();
        //mRescanHandler.postDelayed(runnableRescanBle, BLE_RESCAN_INTERVAL);

        IntentFilter bleFilter = new IntentFilter();
        bleFilter.addAction(BleService.ACTION_GATT_CONNECTED);
        bleFilter.addAction(BleService.ACTION_GATT_DISCONNECTED);
        bleFilter.addAction(BleService.ACTION_GATT_SERVICES_DISCOVERED);
        bleFilter.addAction(BleService.ACTION_DATA_AVAILABLE);
        bleFilter.addAction(BleService.ACTION_WIFI_SSID_WRITTEN);
        bleFilter.addAction(BleService.ACTION_WIFI_NOTIFY_CONNECTED);
        bleFilter.addAction(BleService.ACTION_WIFI_NOTIFY_DISCONNECTED);

        this.registerReceiver(mBleServiceReceiver, bleFilter);

        Intent gattServiceIntent = new Intent(this, BleService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBleServiceReceiver);
        unbindService(mServiceConnection);
        mRescanHandler.removeCallbacks(runnableRescanBle);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {

            case R.id.action_ble_connect:
                Log.v("rtt", "handleBleConnectRequest");
                if (mBleService != null) {
                    mProgress.setTitle("BLE Scan");
                    mProgress.setMessage("Scanning ...");
                    mProgress.show();

                    mBleService.scan();
                }
                else {
                    Log.v("rtt","mBLEService is null");
                }
                return true;

            case R.id.action_wifi_setup:
                Log.v("rtt", "handleWifiSetup");
                handleWifiSetup();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v("rtt","onActivityResult");
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                return;
            }
            Intent gattServiceIntent = new Intent(this, BleService.class);
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBleService = ((BleService.LocalBinder) service).getService();
            if (!mBleService.init()) {
                Log.e("BLE ServiceConnection", "Unable to initialize Bluetooth");
                if (mProgress != null) mProgress.hide();
                finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e("ServiceConnection"," onServiceDisconnected");
            mBleService = null;
        }
    };

    private final BroadcastReceiver mBleServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case BleService.ACTION_GATT_CONNECTED:
                    Log.v("rtt", "BleService.ACTION_GATT_CONNECTED");
                    mRescanHandler.removeCallbacks(runnableRescanBle);
                    break;

                case BleService.ACTION_GATT_DISCONNECTED:
                    Log.v("rtt", "BleService.ACTION_GATT_DISCONNECTED");
                    mRescanHandler.postDelayed(runnableRescanBle, BLE_RESCAN_INTERVAL);
                    mBleConnected = false;
                    break;

                case BleService.ACTION_GATT_SERVICES_DISCOVERED:
                    Log.v("rtt", "BleService.ACTION_GATT_SERVICES_DISCOVERED");
                    mBleConnected = true;
                    if (mProgress != null) mProgress.hide();
                    //mBleService.getBleDevice().readScaleWeight();
                    break;

                case BleService.ACTION_DATA_AVAILABLE:
                    if (mBleService != null) {
                        Log.v("rtt", "BleService.ACTION_GATT_CONNECTED");
                    }
                    break;

                case BleService.ACTION_WIFI_SSID_WRITTEN :
                    bleWifiSetup.onSSIDWritten();
                    break;

                case BleService.ACTION_WIFI_NOTIFY_CONNECTED:
                    Log.v("rtt", "ACTION_WIFI_NOTIFY_CONNECTED");
                    break;

                case BleService.ACTION_WIFI_NOTIFY_DISCONNECTED:
                    Log.v("rtt", "ACTION_WIFI_NOTIFY_DICONNECTED");
                    mBleConnected = false;
                    break;

                default:
                    break;
            }
        }
    };

    public void handleWifiSetup() {
        if (mBleService != null) {
            bleWifiSetup = new BleWifiSetup(mBleService, this);
            bleWifiSetup.showDialog();
        } else {
            showToastMessage("Need BLE Connection");
        }
    }

    private void showToastMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private Runnable runnableRescanBle = new Runnable() {
        @Override
        public void run() {
            if (mBleService != null) {
                mBleService.scan();
            }
            mRescanHandler.postDelayed(runnableRescanBle, BLE_RESCAN_INTERVAL);
        }
    };
}
