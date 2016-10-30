package com.example.iwasz.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.iwasz.myapplication.MESSAGE";
    private static final long SCAN_PERIOD = 20000;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mLeScanner;
    private Handler mHandler = new Handler();
    private boolean mScanning = false;
    private BluetoothDevice device;
    private BluetoothGatt mBluetoothGatt;
    private int lightsMask = 0;

    BluetoothGattCharacteristic leftCh;
    BluetoothGattCharacteristic rightCh;
    BluetoothGattCharacteristic lightsCh;


    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    private int mConnectionState = STATE_DISCONNECTED;
    private final static String TAG = MainActivity.class.getSimpleName();

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
//                broadcastUpdate(intentAction);
//                Log.i(TAG, "Connected to GATT server.");
//                // Attempts to discover services after successful connection.
//                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
                //UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString("");

                gatt.discoverServices();


            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
//                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
//                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

                List<BluetoothGattService> ss = gatt.getServices();

                for (BluetoothGattService s : ss) {
                    Log.i(TAG, "-->" + s.getUuid().toString());
                }


                BluetoothGattService primary = gatt.getService(UUID.fromString("02366e80-cf3a-11e1-9ab4-0002a5d5c51b"));

                if (primary == null) {
                    Log.i(TAG, "No service!");

                }

                List<BluetoothGattCharacteristic> chs = primary.getCharacteristics();

                for (BluetoothGattCharacteristic ch : chs) {
                    Log.i(TAG, ch.getUuid().toString());
                }


                leftCh = primary.getCharacteristic(UUID.fromString("02366e80-cf3a-11e1-9ab4-0002a5d5c51c"));
                rightCh = primary.getCharacteristic(UUID.fromString("02366e80-cf3a-11e1-9ab4-0002a5d5c51d"));
                lightsCh = primary.getCharacteristic(UUID.fromString("02366e80-cf3a-11e1-9ab4-0002a5d5c51e"));

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
//                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
//            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };


    private ScanCallback mLeScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {

                    //Toast.makeText(getApplicationContext(), result.getDevice().getAddress(), Toast.LENGTH_LONG).show();
                    device = result.getDevice();
                    mBluetoothGatt = device.connectGatt(getApplicationContext(), false, mGattCallback);

//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//
//su
//
//                        }
//                    });


                }

                public void onBatchScanResults(List<ScanResult> results) {
                    Toast.makeText(getApplicationContext(), "Batch", Toast.LENGTH_LONG).show();
                }

                public void onScanFailed(int errorCode) {
                    Toast.makeText(getApplicationContext(), "Scan failed " + String.valueOf(errorCode), Toast.LENGTH_LONG).show();
                }
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SpeedSlider speedSlider = (SpeedSlider)findViewById(R.id.speedSlider);
        speedSlider.setSpeedSliderChangeListener(new SpeedSlider.OnSpeedSliderChangeListener() {
            @Override
            public void onChanged(SpeedSlider speedSlider, double lSpeed, double rSpeed) {

                Log.d ("onCreate", "lSpeed = " + lSpeed + ", rSpeed = " + rSpeed);

//                leftCh.setValue((int) lSpeed, BluetoothGattCharacteristic.FORMAT_SINT8, 0);
//                mBluetoothGatt.writeCharacteristic(leftCh);

                rightCh.setValue((int)rSpeed, BluetoothGattCharacteristic.FORMAT_SINT8, 0);
                mBluetoothGatt.writeCharacteristic(rightCh);

            }
        });

        final CheckBox light1 = (CheckBox) findViewById(R.id.checkBox);
        light1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    lightsMask |= 0x01;
                }
                else {
                    lightsMask &= ~0x01;
                }

                if (rightCh == null || mBluetoothGatt == null) {
                    return;
                }

                lightsCh.setValue(lightsMask, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                mBluetoothGatt.writeCharacteristic(lightsCh);
            }
        });

        final CheckBox light2 = (CheckBox) findViewById(R.id.checkBox2);
        light2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    lightsMask |= 0x02;
                }
                else {
                    lightsMask &= ~0x02;
                }

                if (rightCh == null || mBluetoothGatt == null) {
                    return;
                }

                lightsCh.setValue(lightsMask, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                mBluetoothGatt.writeCharacteristic(lightsCh);
            }
        });

        final CheckBox light3 = (CheckBox) findViewById(R.id.checkBox3);
        light3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    lightsMask |= 0x04;
                }
                else {
                    lightsMask &= ~0x04;
                }

                if (rightCh == null || mBluetoothGatt == null) {
                    return;
                }

                lightsCh.setValue(lightsMask, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                mBluetoothGatt.writeCharacteristic(lightsCh);
            }
        });

        final CheckBox light4 = (CheckBox) findViewById(R.id.checkBox4);
        light4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    lightsMask |= 0x08;
                }
                else {
                    lightsMask &= ~0x08;
                }

                if (rightCh == null || mBluetoothGatt == null) {
                    return;
                }

                lightsCh.setValue(lightsMask, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                mBluetoothGatt.writeCharacteristic(lightsCh);
            }
        });

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mLeScanner = mBluetoothAdapter.getBluetoothLeScanner();


    }

    @Override
    protected void onResume() {
        super.onResume();
        scanLeDevice(true);
    }


    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mLeScanner.stopScan(mLeScanCallback);
                    Log.i(TAG, "Scan stopped!");
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mLeScanner.startScan(mLeScanCallback);
        } else {
            mScanning = false;
            mLeScanner.stopScan(mLeScanCallback);
        }
    }
}
