package com.autocop.legroomlamps;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class LegRoomService {
    private static final boolean D = true;
    public static final String DEVICE_NAME = "device_name";
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_WRITE = 3;
    /* access modifiers changed from: private */
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String NAME = "BluetoothChat";
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_NONE = 0;
    private static final String TAG = "BluetoothChatService";
    public static final String TOAST = "toast";
    private final Context context;
    private AcceptThread mAcceptThread;
    /* access modifiers changed from: private */
    public final BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
    /* access modifiers changed from: private */
    public ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    /* access modifiers changed from: private */
    public int mState = 0;

    public LegRoomService(Context context2, Handler handler) {
        this.mHandler = handler;
        this.context = context2;
    }

    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + this.mState + " -> " + state);
        this.mState = state;
        this.mHandler.obtainMessage(1, state, -1).sendToTarget();
    }

    public synchronized int getState() {
        return this.mState;
    }

    public synchronized void start() {
        Log.d(TAG, "start");
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        if (this.mAcceptThread == null) {
            this.mAcceptThread = new AcceptThread();
            this.mAcceptThread.start();
        }
        setState(1);
    }

    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);
        if (this.mState == 2 && this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        this.mConnectThread = new ConnectThread(device);
        this.mConnectThread.start();
        setState(2);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "connected");
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        if (this.mAcceptThread != null) {
            this.mAcceptThread.cancel();
            this.mAcceptThread = null;
        }
        this.mConnectedThread = new ConnectedThread(socket);
        this.mConnectedThread.start();
        Message msg = this.mHandler.obtainMessage(4);
        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_NAME, device.getName());
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
        setState(3);
    }

    public synchronized void stop() {
        Log.d(TAG, "stop");
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        if (this.mAcceptThread != null) {
            this.mAcceptThread.cancel();
            this.mAcceptThread = null;
        }
        setState(0);
    }

    public void write(byte[] out) throws InterruptedException {
        synchronized (this) {
            if (this.mState == 3) {
                this.mConnectedThread.write(out);
            }
        }
    }

    /* access modifiers changed from: private */
    public void connectionFailed() {
        setState(1);
        Message msg = this.mHandler.obtainMessage(5);
        Bundle bundle = new Bundle();
        bundle.putString(TOAST, this.context.getResources().getString(R.string.unable_to_connect));
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
    }

    /* access modifiers changed from: private */
    public void connectionLost() {
        setState(1);
        Message msg = this.mHandler.obtainMessage(5);
        Bundle bundle = new Bundle();
        bundle.putString(TOAST, this.context.getResources().getString(R.string.connect_lost));
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = LegRoomService.this.mAdapter.listenUsingRfcommWithServiceRecord(LegRoomService.NAME, LegRoomService.MY_UUID);
            } catch (IOException e) {
                Log.e(LegRoomService.TAG, "listen() failed", e);
            }
            this.mmServerSocket = tmp;
        }

        public void run() {
            Log.d(LegRoomService.TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");
            BluetoothSocket socket = null;
            while (LegRoomService.this.mState != 3) {
                try {
                    if (this.mmServerSocket != null) {
                        socket = this.mmServerSocket.accept();
                    }
                    if (socket != null) {
                        synchronized (LegRoomService.this) {
                            switch (LegRoomService.this.mState) {
                                case 0:
                                case 3:
                                    try {
                                        socket.close();
                                        break;
                                    } catch (IOException e) {
                                        Log.e(LegRoomService.TAG, "Could not close unwanted socket", e);
                                        break;
                                    }
                                case 1:
                                case 2:
                                    LegRoomService.this.connected(socket, socket.getRemoteDevice());
                                    break;
                            }
                        }
                    }
                } catch (IOException e2) {
                    Log.e(LegRoomService.TAG, "accept() failed", e2);
                }
            }
            Log.i(LegRoomService.TAG, "END mAcceptThread");
            return;
        }

        public void cancel() {
            Log.d(LegRoomService.TAG, "cancel " + this);
            try {
                this.mmServerSocket.close();
            } catch (IOException e) {
                Log.e(LegRoomService.TAG, "close() of server failed", e);
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothDevice mmDevice;
        private final BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {
            this.mmDevice = device;
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(LegRoomService.MY_UUID);
            } catch (IOException e) {
                Log.e(LegRoomService.TAG, "create() failed", e);
            }
            this.mmSocket = tmp;
        }

        public void run() {
            Log.i(LegRoomService.TAG, "BEGIN mConnectThread");
            setName("ConnectThread");
            LegRoomService.this.mAdapter.cancelDiscovery();
            try {
                this.mmSocket.connect();
                Log.i(LegRoomService.TAG, "BEGIN mConnectThread");
                synchronized (LegRoomService.this) {
                    ConnectThread unused = LegRoomService.this.mConnectThread = null;
                }
                LegRoomService.this.connected(this.mmSocket, this.mmDevice);
            } catch (IOException e) {
                LegRoomService.this.connectionFailed();
                try {
                    this.mmSocket.close();
                } catch (IOException e2) {
                    Log.e(LegRoomService.TAG, "unable to close() socket during connection failure", e2);
                }
                LegRoomService.this.start();
            }
        }

        public void cancel() {
            try {
                this.mmSocket.close();
            } catch (IOException e) {
                Log.e(LegRoomService.TAG, "close() of connect socket failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final BluetoothSocket mmSocket;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(LegRoomService.TAG, "create ConnectedThread");
            this.mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(LegRoomService.TAG, "temp sockets not created", e);
            }
            this.mmInStream = tmpIn;
            this.mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(LegRoomService.TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            while (true) {
                try {
                    LegRoomService.this.mHandler.obtainMessage(2, this.mmInStream.read(buffer), -1, buffer).sendToTarget();
                } catch (IOException e) {
                    Log.e(LegRoomService.TAG, "disconnected", e);
                    LegRoomService.this.connectionLost();
                    return;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                this.mmOutStream.write(buffer);
                LegRoomService.this.mHandler.obtainMessage(3, -1, -1, buffer).sendToTarget();
            } catch (IOException e) {
                Log.e(LegRoomService.TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                this.mmSocket.close();
            } catch (IOException e) {
                Log.e(LegRoomService.TAG, "close() of connect socket failed", e);
            }
        }
    }
}
