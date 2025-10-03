package com.teamspeak.ts3sdkclient;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

public class MainActivity extends AppCompatActivity {

    private TS3Application application;

    public static final String TAG = MainActivity.class.getSimpleName();

    final private int REQUEST_CODE_RECORD_AUDIO_PERMISSIONS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        application = (TS3Application) getApplication();

        if(savedInstanceState == null) {
            //Check if the Android version is below 23 or if the user already granted the RECORD_AUDIO permission
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                loadStartView();

            } else {
                requestPermission();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // Show settings
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_version:
                // Show a dialog with version info
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                String message = getString(R.string.version_is, BuildConfig.VERSION_NAME);
                builder.setTitle(R.string.app_name);
                builder.setMessage(message);
                builder.create().show();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadStartView(){
        Fragment startFragment;
        //Check if the device CPU and nativeAudio is supported
        if(!application.isCpuSupported()){
            startFragment = HardwareNotSupportedFragment.newInstance(application.isCpuSupported(), true);
        }else {
            /* Create a new client identity */
            /* In your real application you should do this only once, store the assigned identity locally and then reuse it. */
            String identity = application.getNativeInstance().ts3client_createIdentity();
            startFragment = ExampleFragment.newInstance(identity, checkEmulator());
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, startFragment)
                .commitAllowingStateLoss();
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_RECORD_AUDIO_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_RECORD_AUDIO_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                boolean allPermissionsGranted = true;
                for (int permission : grantResults) {
                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        allPermissionsGranted = false;
                    }
                }
                if (!allPermissionsGranted) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                        new AlertDialog.Builder(this)
                                .setTitle(getString(R.string.permission_ask_again_title))
                                .setMessage(getString(R.string.permission_ask_again_info))
                                .setPositiveButton(getString(R.string.button_request_permissions), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        requestPermission();
                                        dialog.dismiss();
                                    }
                                })
                                .setCancelable(false)
                                .create().show();
                    }
                    else {
                        String infoMessage = getString(R.string.permission_ask_again_info) + " "
                                + getString(R.string.permission_open_app_settings);

                        new AlertDialog.Builder(this)
                                .setTitle(getString(R.string.permission_ask_again_title))
                                .setMessage(infoMessage)
                                .setPositiveButton(getString(R.string.button_settings), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        intent.setData(Uri.parse("package:" + getPackageName()));
                                        startActivity(intent);
                                    }
                                })
                                .setCancelable(false)
                                .create().show();
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadStartView();
                        }
                    });
                }
            }
        }
    }

    public boolean checkEmulator()
    {
        try
        {
            String buildDetails = (Build.FINGERPRINT + Build.DEVICE + Build.MODEL + Build.BRAND + Build.PRODUCT + Build.MANUFACTURER + Build.HARDWARE).toLowerCase();
            Log.d(TAG,"Build Details: " + buildDetails);
            if (buildDetails.contains("generic")
                    ||  buildDetails.contains("unknown")
                    ||  buildDetails.contains("emulator")
                    ||  buildDetails.contains("sdk")
                    ||  buildDetails.contains("genymotion")
                    ||  buildDetails.contains("x86") // this includes vbox86
                    ||  buildDetails.contains("goldfish")
                    ||  buildDetails.contains("test-keys")) {
                Log.d(TAG, "Emulator detected in BUILD_DETAILS");
                return true;
            }
        }
        catch (Throwable t) { Log.e(TAG, t.getMessage()); }

        try
        {
            TelephonyManager tm = (TelephonyManager)getApplicationContext()
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                String non = tm.getNetworkOperatorName().toLowerCase();
                if (non.equals("android")) {
                    Log.d(TAG, "Emulator detected in TELEPHONEMANAGER!");
                    return true;
                } else return false;
            }
        }
        catch (Throwable t) { Log.e(TAG, t.getMessage()); }

        /*try
        {
            // this is triggered on Samsung J3
            if (new File("/init.goldfish.rc").exists()) {
                Log.d(TAG, "Emulator detected in init.goldfish.rc");
                return true;
            }
        }
        catch (Throwable t) { Log.e(TAG, t.getMessage().toString()); }*/

        return false;
    }
}
