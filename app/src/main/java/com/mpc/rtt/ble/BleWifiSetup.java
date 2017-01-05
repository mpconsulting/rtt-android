package com.mpc.rtt.ble;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.mpc.rtt.MainActivity;

public class BleWifiSetup {

    BleService mBleService;
    private String mPassphrase;
    private MainActivity mContext;

    private SharedPreferences SP;

    public BleWifiSetup(BleService bleService, MainActivity context) {
        mBleService = bleService;
        mContext = context;
        SP = context.getPreferences(Context.MODE_PRIVATE);
    }

    public void showDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle("Wifi Setup");
        alert.setMessage("Enter SSID and Passphrase");

        String ssidSaved = SP.getString("ssid","");
        String passphraseSaved = SP.getString("passphrase","");

        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20,0,20,0);

        final EditText ssid = new EditText(mContext);

        if (ssidSaved.equals(""))
            ssid.setHint("SSID");
        else
            ssid.setText(ssidSaved);

        layout.addView(ssid);

        final EditText passphrase = new EditText(mContext);

        if (passphraseSaved.equals(""))
            passphrase.setHint("Passphrase");
        else
            passphrase.setText(passphraseSaved);

        layout.addView(passphrase);

        alert.setView(layout);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                setupWifi(ssid.getText().toString(),passphrase.getText().toString());
                SharedPreferences.Editor editor = SP.edit();
                editor.putString("ssid", ssid.getText().toString());
                editor.putString("passphrase", passphrase.getText().toString());
                editor.commit();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    public void setupWifi(String ssid, String passphrase) {
        mPassphrase = passphrase;
        mBleService.getBleDevice().enableWifiNotifyCharacteristic();
        mBleService.getBleDevice().writeSSIDCharacteristic(ssid.getBytes());
    }

    public void onSSIDWritten() {

        mBleService.getBleDevice().writePassphraseCharacteristic(mPassphrase.getBytes());
    }

}