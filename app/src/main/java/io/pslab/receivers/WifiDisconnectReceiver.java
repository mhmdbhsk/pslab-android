package io.pslab.receivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;


import io.pslab.R;
import io.pslab.activity.MainActivity;
import io.pslab.activity.PowerSourceActivity;
import io.pslab.communication.PacketHandler;
import io.pslab.fragment.HomeFragment;
import io.pslab.others.CustomSnackBar;
import io.pslab.others.ScienceLabCommon;

public class WifiDisconnectReceiver extends BroadcastReceiver {

    private final String TAG = this.getClass().getSimpleName();
    private Context activityContext;

    public WifiDisconnectReceiver() {
    }

    public WifiDisconnectReceiver(Context context) {
        this.activityContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                if (ScienceLabCommon.isWifiConnected) {
                    ScienceLabCommon.scienceLab.close();
                    // Clear saved values in Power Source Instrument
                    context.getSharedPreferences(PowerSourceActivity.POWER_PREFERENCES, Context.MODE_PRIVATE).edit().clear().apply();
                    CustomSnackBar.showSnackBar(((Activity) context).findViewById(android.R.id.content),
                            "Wifi Device Disconnected", null, null, Snackbar.LENGTH_SHORT);

                    PacketHandler.version = "";

                    if (activityContext != null) {
                        MainActivity mainActivity = (MainActivity) activityContext;
                        Fragment currentFragment = mainActivity.getSupportFragmentManager().findFragmentById(R.id.frame);
                        if (currentFragment instanceof HomeFragment) {
                            mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.frame, HomeFragment.newInstance(false, false)).commitAllowingStateLoss();
                        }
                        ScienceLabCommon.isWifiConnected = false;
                        mainActivity.invalidateOptionsMenu();
                    }
                } else {
                    Log.v(TAG, "Board isn't connected.");
                }
            }
        } catch (IllegalStateException ignored) {
            /**/
        }
    }
}
