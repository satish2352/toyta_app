package com.autocop.legroomlamps;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;


public class SplashActivity extends AppCompatActivity {
    public static final String ITS_LOLLIPOP = "OSlollipop";
    private static final int PERMISSION_FINE_LOC = 51;
    public static final String PREF_NAME = "LegroomPref";
    private static final int REQUEST_COARSE_LOCATION_PERMISSIONS = 52;
    private static final String SDK_VERSION = "AndroidVersion";
    private CountDownTimer countDownTimer;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_splash);
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, 0);
        SharedPreferences.Editor prefEditor = sharedPreferences.edit();
        if (sharedPreferences.getInt(SDK_VERSION, 0) != 0) {
            return;
        }
        if (Build.VERSION.SDK_INT == 21 || Build.VERSION.SDK_INT == 22) {
            prefEditor.putInt(SDK_VERSION, Build.VERSION.SDK_INT);
            prefEditor.putBoolean(ITS_LOLLIPOP, true);
            prefEditor.commit();
            return;
        }
        prefEditor.putInt(SDK_VERSION, Build.VERSION.SDK_INT);
        prefEditor.putBoolean(ITS_LOLLIPOP, false);
        prefEditor.commit();
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        this.countDownTimer = new CountDownTimer(3000, 1000) {
            public void onTick(long l) {
            }

            public void onFinish() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                SplashActivity.this.startActivity(intent);
                SplashActivity.this.finish();
            }
        };


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        {

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1000);
            }
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 1001);
            }
            if (ActivityCompat.checkSelfPermission(this, "android.permission.BLUETOOTH") != 0 ) {
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION"}, 51);
            } else if (ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") != 0) {
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_COARSE_LOCATION"}, 52);
            }else
            {
                this.countDownTimer.start();
                Log.d("mytag","Else 1");
                Log.d("mytag","IF 1");
            }
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {




            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 51);
            }else
            {
//                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) !=
//                        PackageManager.PERMISSION_GRANTED) {
//                    // Location permission not granted, request it
//                    ActivityCompat.requestPermissions(this,
//                            new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
//                            500);
//                    Log.d("mytag","IF 2");
//                } else {
//                    // Location permission granted, proceed with your functionality
//                    // Access background location as needed
//                    this.countDownTimer.start();
//
//                    Log.d("mytag","Else 2");
//                }
                this.countDownTimer.start();
            }

        }
        else{
            if (ActivityCompat.checkSelfPermission(this, "android.permission.BLUETOOTH") != 0 ) {
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION"}, 51);
            } else if (ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") != 0) {
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_COARSE_LOCATION"}, 52);
            }else
            {
                this.countDownTimer.start();

                Log.d("mytag","Else 3");
            }
        }
    }

    private void setLanguageLocale() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle((CharSequence) "Choose Language");
        builder.setItems((CharSequence[]) new String[]{"English", "Chinese"}, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        SplashActivity.this.forceLocale("en");
                        return;
                    case 1:
                        SplashActivity.this.forceLocale("zh");
                        return;
                    default:
                        return;
                }
            }
        });
        builder.setNegativeButton((CharSequence) getResources().getString(R.string.cancel), (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                SplashActivity.this.finish();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    public void forceLocale(String localeCode) {
        LocaleHelper.setLocale(this, localeCode);
        this.countDownTimer.start();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0)
        {
            if (requestCode == 51 && grantResults[0] == 0) {
                Log.d("mytag","51");
                onResume();
            } else if (requestCode == 52 && grantResults[0] == 0) {
                Log.d("mytag","52");
                onResume();
            } else if(requestCode == 1000 && grantResults[0] == 0) {
                //finish();
                Log.d("mytag","1000");
                onResume();
            }
            else if(requestCode == 1001 && grantResults[0] == 0) {
                //finish();
                Log.d("mytag","1001");
                onResume();

            }
            else if(requestCode == 500 && grantResults[0] == 0) {
                //finish();
                Log.d("mytag","1001");
                onResume();

            }
        }
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                // Check if permission is denied permanently
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                    redirectToAppSettings();

                    break;
                }
            }
        }

    }

   /* public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean allPermissionsGranted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (!allPermissionsGranted) {
            boolean allPermissionsPermanentlyDenied = true;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                        // Permission is denied, but not permanently
                        allPermissionsPermanentlyDenied = false;
                        break;
                    }
                }
            }

            // Check if all permissions are permanently denied
            if (allPermissionsPermanentlyDenied) {
                // All permissions are permanently denied, redirect to app settings
                redirectToAppSettings();
            } else {
                // Permissions are denied, handle it as needed
                onResume();
            }
        } else {
            // All permissions are granted, resume operations
            onResume();
        }
    }*/

    private void redirectToAppSettings() {
        // Redirect to app settings...

        Log.d("mytag","redirectToAppSettings");

        AlertDialog.Builder builder=new AlertDialog.Builder(SplashActivity.this)
                .setTitle("Allow All Permissions Manually")
                .setMessage("You Need to allow all permissions manually from settings")
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog=builder.create();
        alertDialog.show();



    }


    /* access modifiers changed from: protected */
    public void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }


}
