package com.autocop.legroomlamps;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public interface ThreadCallBack {
    void setMessage(BluetoothDevice bluetoothDevice, String str, boolean z);

    void setResult(BluetoothSocket bluetoothSocket, BluetoothDevice bluetoothDevice, boolean z);
}
