package com.example.riku_maehara.androidbeamsample;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

public class MainActivity extends AppCompatActivity implements NfcAdapter.CreateNdefMessageCallback {

    NfcAdapter mNfcAdapter;
    BluetoothSPP bt;
    TextView textStatus;

    BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textStatus = (TextView) findViewById(R.id.text);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        bt = new BluetoothSPP(getApplicationContext());
        bt.setupService();
        bt.startService(BluetoothState.DEVICE_ANDROID);
        mNfcAdapter.setNdefPushMessageCallback(this, this);
        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            @Override
            public void onDataReceived(byte[] data, String message) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceDisconnected() {
                textStatus.setText("Status : Not connect");
//                menu.clear();
//                getMenuInflater().inflate(R.menu.menu_connection, menu);
            }

            public void onDeviceConnectionFailed() {
                textStatus.setText("Status : Connection failed");
            }

            public void onDeviceConnected(String name, String address) {
                textStatus.setText("Status : Connected to " + name);
//                menu.clear();
//                getMenuInflater().inflate(R.menu.menu_disconnection, menu);
            }
        });
    }



    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        if (!bt.isServiceAvailable()) {
            bt.setupService();
            bt.startService(true);
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String address;
        if (Build.VERSION.SDK_INT == 23) {
            address = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(), "bluetooth_address");

        } else {
             address = mBluetoothAdapter.getAddress();
        }

        Log.d("ID", address);
        NdefMessage msg = new NdefMessage(new NdefRecord[]{
                createMimeRecord("application/com.example.beamtest", address.getBytes()),
        });
        return msg;
    }

    public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
        NdefRecord mimeRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
        return mimeRecord;
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
//        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
//            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
//            NdefMessage msg = (NdefMessage) rawMsgs[0];
//            String string1 = new String(msg.getRecords()[0].getPayload());
//            String string2 = new String(msg.getRecords()[1].getPayload());
//            Toast.makeText(getApplicationContext(), string1, Toast.LENGTH_SHORT).show();
//        }
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage msg = (NdefMessage) rawMsgs[0];
            String address = new String(msg.getRecords()[0].getPayload());
//            chatManager.connect(mBluetoothAdapter.getRemoteDevice(address));
            bt.connect(address);
        }
    }

    public void button1(View v) {
        Intent intent = new Intent(getApplicationContext(), IntentActivity.class);

        startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {

            if (resultCode == Activity.RESULT_OK) {
                bt.connect(data);

            }
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
//                setup();
            } else {
                // Do something if user doesn't choose any device (Pressed back)
            }
        }
    }

    public void setup(View v) {
        bt.send("Test", true);

    }
}
