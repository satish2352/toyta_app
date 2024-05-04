package com.autocop.legroomlamps;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.autocop.legroomlamps.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int COLOR_AMBER = 3;
    private static final int COLOR_BLUE = 1;
    private static final int COLOR_GREEN = 2;
    private static final int COLOR_RED = 0;
    private static final int COLOR_WHITE = 4;
    private static final String COMMAND_ATTACH = "$";
    private static final String INTENSITY = "BarIntensity";
    private static final String LAST_COLOR = "lastSelectedColor";
    private static final String LAST_DEVICE = "LastDevice";
    private static final String LAST_INTENSITY = "lastSelectedIntensity";
    private static final int POWER_OFF = 42;
    private static final int POWER_ON = 41;
    private static final String POWER_STATUS = "CurrentPowerStatus";
    private static final int REQUEST_COARSE_LOCATION_PERMISSIONS = 21;
    private static final int REQUEST_ENABLE_BT = 22;
    private static final String TAG = "Legroo Lamp";
    private static final String TIMING_PACKET = "300,005,008,002";
    /* access modifiers changed from: private */
    public static int clickCounter = 0;
    private static CountDownTimer timer;
    /* access modifiers changed from: private */
    public List<BluetoothDevice> allBTDevices;
    /* access modifiers changed from: private */
    public ArrayAdapter<String> arrayAdapter;
    View colorAmber;
    View colorBlue;
    View colorGreen;
    View colorRed;
    private String[] colorValues = {"FF0000", "2080FF", "00FF00", "FFC200", "FFFFFF"};
    View colorWhite;
    /* access modifiers changed from: private */
    public boolean connectionStatus = false;
    /* access modifiers changed from: private */
    public Dialog dialog;
    SharedPreferences.Editor editor;
    ImageView imageCar;
    ImageView imgMenu;
    /* access modifiers changed from: private */
    public boolean isTimerRunning = false;
    /* access modifiers changed from: private */
    public String lastSelect;
    RelativeLayout layoutHome;
    RelativeLayout layoutImage;
    /* access modifiers changed from: private */
    public AlertDialog listDevices;
    private List<String> listNames;
    /* access modifiers changed from: private */
    public BluetoothAdapter mBluetoothAdapter;
    /* access modifiers changed from: private */
    public LegRoomService mChatService;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    switch (msg.arg1) {
                        case 3:
                            if (MainActivity.this.progressDialog != null && MainActivity.this.progressDialog.isShowing()) {
                                MainActivity.this.progressDialog.dismiss();
                            }
                            MainActivity.this.textStatus.setText(MainActivity.this.getResources().getString(R.string.connected));
                            boolean unused = MainActivity.this.connectionStatus = true;
                            boolean unused2 = MainActivity.this.powerStatus = true;
                            MainActivity.this.setLastColor();
                            return;
                        default:
                            return;
                    }
                case 5:
                    if (MainActivity.this.progressDialog != null && MainActivity.this.progressDialog.isShowing()) {
                        MainActivity.this.progressDialog.dismiss();
                    }
                    MainActivity.this.textStatus.setText(MainActivity.this.getResources().getString(R.string.disconnect));
                    boolean unused3 = MainActivity.this.connectionStatus = false;
                    boolean unused4 = MainActivity.this.powerStatus = false;
                    String messsage = msg.peekData().getString(LegRoomService.TOAST);
                    if (messsage != null) {
                        Toast.makeText(MainActivity.this, messsage, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private BroadcastReceiver mReceiver;
    private MenuBuilder menuBuilder;
    /* access modifiers changed from: private */
    public MenuPopupHelper optionsMenu;
    /* access modifiers changed from: private */
    public boolean powerStatus = true;
    /* access modifiers changed from: private */
    public ProgressDialog progressDialog;
    /* access modifiers changed from: private */
    public boolean reconnect = false;
    SeekBar seekBarInt;
    SharedPreferences sharedPreferences;
    /* access modifiers changed from: private */
    public boolean stopUserInteractions = false;

    TextView textAmber;
    TextView textBlue;
    TextView textGreen;
    TextView textRed;
    TextView textStatus;
    TextView textWhite;

    public void onClick(View v) {
        if (!this.connectionStatus) {
            AppUtility.Alert(this);
        } else if (!this.powerStatus) {
            AppUtility.PowerAlert(this);
        } else {
            String lastIntensity = this.sharedPreferences.getString(LAST_INTENSITY, "#55").toUpperCase();
            switch (v.getId()) {
                case R.id.amber:
                    this.lastSelect = this.colorValues[3];
                    this.editor.putInt(LAST_COLOR, 3);
                    this.editor.commit();
                    writeCommand("@" + lastIntensity + this.lastSelect + "," + TIMING_PACKET + COMMAND_ATTACH);
                    binding.content.imageCar.setImageResource(R.drawable.car_orange);
                    binding.content.whiteText.setTextColor(getResources().getColor(R.color.colorPrimary));
                    binding.content.greenText.setTextColor(getResources().getColor(R.color.colorPrimary));
                    binding.content.amberText.setTextColor(getResources().getColor(R.color.colorPrimary));
                    binding.content.redText.setTextColor(getResources().getColor(R.color.colorPrimary));
                    binding.content.blueText.setTextColor(getResources().getColor(R.color.colorPrimary));
                    return;
                case R.id.blocker:
                    if (!this.connectionStatus) {
                        AppUtility.Alert(this);
                        return;
                    } else if (!this.powerStatus) {
                        AppUtility.PowerAlert(this);
                        return;
                    } else {
                        AppUtility.AutomodeAlert(this);
                        return;
                    }
                case R.id.blue:
                    this.lastSelect = this.colorValues[1];
                    this.editor.putInt(LAST_COLOR, 1);
                    this.editor.commit();
                    writeCommand("@" + lastIntensity + this.lastSelect + "," + TIMING_PACKET + COMMAND_ATTACH);
                    this.imageCar.setImageResource(R.drawable.car_blue);
                    binding.content.whiteText.setTextColor(getResources().getColor(R.color.colorPrimary));
                    binding.content.greenText.setTextColor(getResources().getColor(R.color.colorPrimary));
                    binding.content.amberText.setTextColor(getResources().getColor(R.color.colorPrimary));
                    binding.content.redText.setTextColor(getResources().getColor(R.color.colorPrimary));
                    binding.content.blueText.setTextColor(getResources().getColor(R.color.white));
                    return;
                case R.id.green:
                    this.lastSelect = this.colorValues[2];
                    this.editor.putInt(LAST_COLOR, 2);
                    this.editor.commit();
                    writeCommand("@" + lastIntensity + this.lastSelect + "," + TIMING_PACKET + COMMAND_ATTACH);
                    this.imageCar.setImageResource(R.drawable.car_green);
                    binding.content.whiteText.setTextColor(getResources().getColor(R.color.colorPrimary));
                    binding.content.greenText.setTextColor(getResources().getColor(R.color.colorPrimary));
                    binding.content.amberText.setTextColor(getResources().getColor(R.color.colorPrimary));
                    binding.content.redText.setTextColor(getResources().getColor(R.color.colorPrimary));
                    binding.content.blueText.setTextColor(getResources().getColor(R.color.colorPrimary));
                    return;
                case R.id.red:
                    this.lastSelect = this.colorValues[0];
                    this.editor.putInt(LAST_COLOR, 0);
                    this.editor.commit();
                    writeCommand("@" + lastIntensity + this.lastSelect + "," + TIMING_PACKET + COMMAND_ATTACH);
                    this.imageCar.setImageResource(R.drawable.car_red);
                    binding.content.whiteText.setTextColor(getResources().getColor(R.color.colorPrimary));
                    binding.content.greenText.setTextColor(getResources().getColor(R.color.colorPrimary));
                    binding.content.amberText.setTextColor(getResources().getColor(R.color.colorPrimary));
                    binding.content.redText.setTextColor(getResources().getColor(R.color.colorPrimary));
                    binding.content.blueText.setTextColor(getResources().getColor(R.color.colorPrimary));
                    return;
                case R.id.white:
                    this.lastSelect = this.colorValues[4];
                    this.editor.putInt(LAST_COLOR, 4);
                    this.editor.commit();
                    writeCommand("@" + lastIntensity + this.colorValues[4] + "," + TIMING_PACKET + COMMAND_ATTACH);
                    this.imageCar.setImageResource(R.drawable.car_white);
                    binding.content.whiteText.setTextColor(getResources().getColor(R.color.colorPrimary));
                    binding.content.greenText.setTextColor(getResources().getColor(R.color.colorPrimary));
                    binding.content.amberText.setTextColor(getResources().getColor(R.color.colorPrimary));
                    binding.content.redText.setTextColor(getResources().getColor(R.color.colorPrimary));
                    binding.content.blueText.setTextColor(getResources().getColor(R.color.colorPrimary));
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    public void writeCommand(String command) {
        Log.e("Command", command);
        timer = new CountDownTimer(2000, 100) {
            public void onTick(long l) {
            }

            public void onFinish() {
                int unused = MainActivity.clickCounter = 0;
                boolean unused2 = MainActivity.this.isTimerRunning = false;
            }
        };
        if (clickCounter < 5) {
            clickCounter++;
            if (!this.isTimerRunning) {
                timer.start();
                this.isTimerRunning = true;
            }
            if (this.mChatService != null) {
                try {
                    this.mChatService.write(command.getBytes());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Log.e("counter : ", "" + clickCounter);
            if (this.mChatService != null) {
                try {
                    this.mChatService.write(command.getBytes());
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
            }
            this.stopUserInteractions = true;
            new CountDownTimer(1100, 100) {
                public void onTick(long l) {
                }

                public void onFinish() {
                    boolean unused = MainActivity.this.stopUserInteractions = false;
                }
            }.start();
            this.isTimerRunning = false;
            timer.cancel();
            clickCounter = 0;
        }
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != -1) {
            Toast.makeText(this, "Bluetooth access denied..", 0).show();
            finish();
        }
    }

    public void onBackPressed() {
        new AlertDialog.Builder(this).setTitle((CharSequence) "Exit").setMessage((CharSequence) "Exit App?").setPositiveButton((CharSequence) "Exit", (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                MainActivity.this.finish();
            }
        }).setNegativeButton((CharSequence) "Cancel", (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setIcon((int) R.drawable.ic_launcher).show();
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.e("Event", String.valueOf(ev.getSource()));
        if (this.stopUserInteractions) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        if (isInMultiWindowMode) {
            this.imageCar.setVisibility(View.VISIBLE);
        } else {
            this.imageCar.setVisibility(View.GONE);
        }
    }

    ActivityMainBinding binding;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.sharedPreferences = getSharedPreferences(SplashActivity.PREF_NAME, 0);
        this.editor = this.sharedPreferences.edit();
        if (this.mBluetoothAdapter == null) {
            Toast.makeText(this, getResources().getString(R.string.no_bluetooth), 1).show();
            finish();
        }
        this.menuBuilder = new MenuBuilder(this);
        this.optionsMenu = new MenuPopupHelper(this, this.menuBuilder, binding.content.imgSideMenu);
        binding.content.imgSideMenu.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (MainActivity.this.optionsMenu.isShowing()) {
                    MainActivity.this.optionsMenu.dismiss();
                } else {
                    MainActivity.this.createMenu();
                }
            }
        });
        colorAmber=findViewById(R.id.amber);
        colorWhite=findViewById(R.id.white);
        colorRed=findViewById(R.id.red);
        colorBlue=findViewById(R.id.blue);
        colorGreen=findViewById(R.id.green);
        textStatus=findViewById(R.id.bt_status);
        imageCar=findViewById(R.id.imageCar);
        this.colorGreen.setOnClickListener(this);
        this.colorAmber.setOnClickListener(this);
        this.colorBlue.setOnClickListener(this);
        this.colorRed.setOnClickListener(this);
        this.colorWhite.setOnClickListener(this);
        this.allBTDevices = new ArrayList();
        this.listNames = new ArrayList();
        this.arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, this.listNames) {
            @NonNull
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                text1.setTextColor(Color.WHITE);
                return view;
            }
        };
        binding.content.seekBarHorizontal.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                String hexValue;
                if (!MainActivity.this.connectionStatus) {
                    AppUtility.Alert(MainActivity.this);
                } else if (!MainActivity.this.powerStatus) {
                    AppUtility.PowerAlert(MainActivity.this);
                } else {
                    int progress = seekBar.getProgress();
                    if (progress > 32) {
                        hexValue = Integer.toHexString(progress);
                    } else if (progress == 0) {
                        hexValue = "00";
                    } else {
                        hexValue = "20";
                    }
                    Log.e("progress: ", progress + " " + hexValue);
                    MainActivity.this.editor.putInt(MainActivity.INTENSITY, progress);
                    MainActivity.this.editor.putString(MainActivity.LAST_INTENSITY, "#" + hexValue);
                    MainActivity.this.editor.commit();
                    MainActivity.this.writeCommand("@" + ("#" + hexValue).toUpperCase() + MainActivity.this.lastSelect + "," + MainActivity.TIMING_PACKET + MainActivity.COMMAND_ATTACH);
                }
            }
        });
        if (savedInstanceState != null) {
            this.powerStatus = savedInstanceState.getBoolean(POWER_STATUS);
        }
//        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        registerReceiver(receiver, filter);
//        this.mBluetoothAdapter.startDiscovery();
//        searchDevices();

    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        this.mChatService = new LegRoomService(this, this.mHandler);
    }

    /* access modifiers changed from: private */
    public void createMenu() {
        this.menuBuilder.clear();
        new MenuInflater(this).inflate(R.menu.option_menu, this.menuBuilder);
        MenuItem item = this.menuBuilder.findItem(R.id.power);
        MenuItem btooth = this.menuBuilder.findItem(R.id.scan);
        if (!this.powerStatus || !this.connectionStatus) {
            item.setTitle(R.string.power_off);
            item.setIcon(R.drawable.power_off);
        } else {
            item.setTitle(R.string.power_on);
            item.setIcon(R.drawable.power_on);
        }
        if (this.connectionStatus) {
            btooth.setIcon(R.drawable.ic_bluetooth_on);
        } else {
            btooth.setIcon(R.drawable.ic_bluetooth_off);
        }
        this.optionsMenu.setForceShowIcon(true);
        this.menuBuilder.setCallback(new MenuBuilder.Callback() {
            public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.helpme:
                        MainActivity.this.showMessageDialog();
                        break;
                    case R.id.power:
                        if (MainActivity.this.connectionStatus) {
                            if (!MainActivity.this.powerStatus) {
                                boolean unused = MainActivity.this.powerStatus = true;
                                item.setTitle(R.string.power_on);
                                item.setIcon(R.drawable.power_on);
                                MainActivity.this.setPower(41);
                                break;
                            } else {
                                boolean unused2 = MainActivity.this.powerStatus = false;
                                item.setTitle(R.string.power_off);
                                item.setIcon(R.drawable.power_off);
                                MainActivity.this.setPower(42);
                                break;
                            }
                        } else {
                            AppUtility.Alert(MainActivity.this);
                            break;
                        }
                    case R.id.scan:
                        if (MainActivity.this.mBluetoothAdapter.isEnabled()) {
                            boolean unused3 = MainActivity.this.powerStatus = true;
                            if (MainActivity.this.mBluetoothAdapter.isDiscovering()) {
                                MainActivity.this.mBluetoothAdapter.cancelDiscovery();
                            }
                            boolean unused4 = MainActivity.this.reconnect = false;
                            MainActivity.this.searchDevices();
                            break;
                        } else {
                            MainActivity.this.startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 22);
                            break;
                        }
                }
                return true;
            }

            public void onMenuModeChange(MenuBuilder menu) {
            }
        });
        this.optionsMenu.show();
    }

    /* access modifiers changed from: protected */
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.powerStatus = savedInstanceState.getBoolean(POWER_STATUS);
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
    }

    /* access modifiers changed from: private */
    public void setPower(int power) {
        if (power == 41) {
            int progress = Integer.parseInt("F0", 16);
            String lastIntensity = "#" + "F0";
            this.editor.putString(LAST_INTENSITY, lastIntensity);
            this.editor.putInt(INTENSITY, progress);
            this.editor.commit();
            binding.content.seekBarHorizontal.setProgress(progress);
            writeCommand("@" + lastIntensity + this.lastSelect + "," + TIMING_PACKET + COMMAND_ATTACH);
            return;
        }
        this.editor.putString(LAST_INTENSITY, "#00");
        this.editor.commit();
        writeCommand("@" + "#00" + this.lastSelect + "," + TIMING_PACKET + COMMAND_ATTACH);
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        if (this.mBluetoothAdapter != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                //return;
            }
            this.mBluetoothAdapter.disable();
        }
        if (this.mReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mReceiver);
        }
    }

    /* access modifiers changed from: protected */
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(POWER_STATUS, this.powerStatus);
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        if (this.mReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mReceiver);
        }
        if (this.progressDialog != null && this.progressDialog.isShowing()) {
            this.progressDialog.dismiss();
        }
        try {
            if (this.listDevices.isShowing()) {
                this.listDevices.dismiss();
            }
        } catch (Exception e) {
        }
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        this.reconnect = true;
        if (this.mChatService != null) {
            setupReceiver();
        }
    }

    /* access modifiers changed from: private */
    public void setLastColor() {
        binding.content.seekBarHorizontal.setProgress(this.sharedPreferences.getInt(INTENSITY, 85));
        switch (this.sharedPreferences.getInt(LAST_COLOR, 3)) {
            case 0:
                this.colorRed.performClick();
                return;
            case 1:
                this.colorBlue.performClick();
                return;
            case 2:
                this.colorGreen.performClick();
                return;
            case 3:
                this.colorAmber.performClick();
                return;
            case 4:
                this.colorWhite.performClick();
                return;
            default:
                return;
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /* access modifiers changed from: private */
    public void showMessageDialog() {
        this.dialog = new Dialog(this, R.style.AlertDialogCustom);
        this.dialog.requestWindowFeature(1);
        this.dialog.setContentView(R.layout.dialogfragment);
        Window window = this.dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        //wlp.flags &= WindowManager.LayoutParams.MEMORY_TYPE_PUSH_BUFFERS;
        window.setAttributes(wlp);
        this.dialog.getWindow().setLayout(-1, -1);
        WebView webView = (WebView) this.dialog.findViewById(R.id.webView1);
        ((ImageView) this.dialog.findViewById(R.id.close)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.this.dialog.dismiss();
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadDataWithBaseURL("", "<ul><li>Bluetooth module inside Leg Room Lamp ECU is ON only when the Ignition is ON. </li>\n<li>Leg Room Lamp remain at full intensity if any of the door is open and Ignition is ON. </li>\n<li>Leg Room Lamp turns ON at opening the doors. If doors left open for more than 5 min. Lamps turn OFF.</li>\n<li>If Ignition is OFF and all the doors closed (within 5 min), Leg Room Lamp will turn OFF with dimming.If the interior lamp in the vehicle has delayed turn OFF function,then this time gets added.</li>\n<li>Leg Room Lamp remains ON at user defined intensity when all doors are closed and Ignition is ON.</li>\n<li>This function can be turned OFF using a dedicated Power ON/OFF button on the mobile phone. This button does not work if any of the door is open.</li>\n<li>Lamp color can be changed using mobile phone app if ignition is ON.</li>\n<li>Light intensity can be changed using mobile phone app if Ignition is ON & doors are closed. (Wait until interior lamps turned OFF)</li>\n</ul>", "text/html", "UTF-8", "");
        this.dialog.show();
    }

    /* access modifiers changed from: private */
    public boolean isDeviceExist(BluetoothDevice device) {
        if (this.allBTDevices.size() == 0) {
            return false;
        }
        for (BluetoothDevice d : this.allBTDevices) {
            if (d.getAddress().equals(device.getAddress())) {
                return true;
            }
        }
        return false;
    }

    private void setupReceiver() {
        Log.d("mytag","setupReceiver ");
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Log.d("mytag","onReceive ");
                String action = intent.getAction();
                if ("android.bluetooth.device.action.FOUND".equals(action)) {
                    BluetoothDevice device;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice.class);
                    }else{
                        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    }
                   // BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                    if (!MainActivity.this.isDeviceExist(device)) {
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            //return;
                        }
                        if (device.getName() != null) {
                            MainActivity.this.arrayAdapter.add(device.getName() + "\n" + device.getAddress());
                        } else {
                            MainActivity.this.arrayAdapter.add("No Name \n" + device.getAddress());
                        }
                        MainActivity.this.allBTDevices.add(device);
                    }
                    MainActivity.this.arrayAdapter.notifyDataSetChanged();
                    Log.d("mytag","onReceive FOUND");

                } else if ("android.bluetooth.adapter.action.DISCOVERY_FINISHED".equals(action)) {
                    Log.d("mytag","DISCOVERY_FINISHED ");
                    if (MainActivity.this.allBTDevices.size() <= 0 && MainActivity.this.listDevices != null && MainActivity.this.listDevices.isShowing()) {
                        MainActivity.this.listDevices.setMessage(MainActivity.this.getResources().getString(R.string.no_device));
                    }
                } else if ("android.bluetooth.device.action.BOND_STATE_CHANGED".equals(action)) {

                    Log.d("mytag","BOND_STATE_CHANGED ");

                    BluetoothDevice device2 = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        //return;
                    }
                    if (device2.getBondState() == 12) {
                        MainActivity.this.mChatService.connect(device2);
                    }
                } else {
                    if ("android.bluetooth.device.action.ACL_DISCONNECTED".equals(action) || "android.bluetooth.device.action.ACL_CONNECTED".equals(action)) {
                        Log.d("mytag","ACL_DISCONNECTED ");

                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter("android.bluetooth.device.action.FOUND");
        filter.addAction("android.bluetooth.adapter.action.DISCOVERY_FINISHED");
        filter.addAction("android.bluetooth.device.action.BOND_STATE_CHANGED");
        filter.addAction("android.bluetooth.device.action.ACL_DISCONNECTED");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(this.mReceiver, filter,RECEIVER_EXPORTED);
        }else{
            registerReceiver(this.mReceiver, filter);
        }

        searchDevices();
    }

    /* access modifiers changed from: private */
    public void searchDevices() {
        if (ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") != 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_COARSE_LOCATION"}, 21);
        } else if (!this.mBluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 22);
        } else if (this.sharedPreferences.getString(LAST_DEVICE, "").equalsIgnoreCase("") || !this.reconnect) {
            showDevicesList();
        } else {
            BluetoothDevice device = this.mBluetoothAdapter.getRemoteDevice(this.sharedPreferences.getString(LAST_DEVICE, ""));
            this.progressDialog = new ProgressDialog(this);
            if (device.getName() != null) {
                this.progressDialog.setMessage("Connecting to " + device.getName());
            } else {
                this.progressDialog.setMessage("Connecting to " + device.getAddress());
            }
            this.progressDialog.setCancelable(false);
            this.progressDialog.show();
            this.mChatService.connect(device);
        }
    }

    private void showDevicesList() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this,R.style.AlertDialogCustom);
        builderSingle.setIcon((int) R.drawable.ic_launcher);
        builderSingle.setTitle((CharSequence) getResources().getString(R.string.select_bt));
        builderSingle.setCancelable(false);
        builderSingle.setPositiveButton((CharSequence) getResources().getString(R.string.refresh), (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    //return;
                }
                if (MainActivity.this.mBluetoothAdapter.isDiscovering()) {
                    MainActivity.this.mBluetoothAdapter.cancelDiscovery();
                }
                boolean unused = MainActivity.this.reconnect = false;
                MainActivity.this.searchDevices();
            }
        });
        builderSingle.setNegativeButton((CharSequence) getResources().getString(R.string.cancel), (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builderSingle.setAdapter(this.arrayAdapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int selected) {
                BluetoothDevice device = (BluetoothDevice) MainActivity.this.allBTDevices.get(selected);
                ProgressDialog unused = MainActivity.this.progressDialog = new ProgressDialog(MainActivity.this);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    //return;
                }
                if (device.getName() != null) {
                    MainActivity.this.progressDialog.setMessage(MainActivity.this.getResources().getString(R.string.get_connect) + " " + device.getName());
                } else {
                    MainActivity.this.progressDialog.setMessage(MainActivity.this.getResources().getString(R.string.get_connect) + " " + device.getAddress());
                }
                MainActivity.this.progressDialog.setCancelable(false);
                MainActivity.this.progressDialog.show();
                MainActivity.this.mChatService.connect(device);
                dialog.dismiss();
            }
        });
        this.listDevices = builderSingle.create();
        this.listDevices.show();
        this.arrayAdapter.clear();
        this.allBTDevices.clear();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //
        }
        for (BluetoothDevice d : this.mBluetoothAdapter.getBondedDevices()) {
            this.arrayAdapter.add(d.getName() + "\n" + d.getAddress());
            this.allBTDevices.add(d);
        }
        this.mBluetoothAdapter.startDiscovery();
    }

    /* access modifiers changed from: protected */
    public void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
}
