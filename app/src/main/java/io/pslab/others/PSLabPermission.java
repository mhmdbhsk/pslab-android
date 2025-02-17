package io.pslab.others;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Created by Padmal on 11/3/18.
 */

public class PSLabPermission {

    private String[] allPermissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private String[] csvPermissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private String[] logPermissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private String[] mapPermissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private String[] bluetoothPermission = new String[]{
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
    };

    List<String> listPermissionsNeeded = new ArrayList<>();

    public static final int ALL_PERMISSION = 100;
    public static final int LOG_PERMISSION = 101;
    public static final int MAP_PERMISSION = 102;
    public static final int GPS_PERMISSION = 103;
    public static final int CSV_PERMISSION = 104;
    public static final int BLUETOOTH_PERMISSION = 105;

    public static int REQUEST_CODE = 0;

    public static int PERMISSIONS_REQUIRED = 0;

    private static final PSLabPermission pslabPermission = new PSLabPermission();

    public static PSLabPermission getInstance() {
        return pslabPermission;
    }

    private PSLabPermission() {/**/}

    public boolean checkPermissions(Activity activity, int mode) {
        if (mode == ALL_PERMISSION) {
            listPermissionsNeeded = new ArrayList<>();
            for (String permission : allPermissions) {
                if (ContextCompat.checkSelfPermission(activity, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(permission);
                }
            }
        } else if (mode == LOG_PERMISSION) {
            listPermissionsNeeded = new ArrayList<>();
            for (String permission : logPermissions) {
                if (ContextCompat.checkSelfPermission(activity, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(permission);
                }
            }
        } else if (mode == MAP_PERMISSION) {
            listPermissionsNeeded = new ArrayList<>();
            for (String permission : mapPermissions) {
                if (ContextCompat.checkSelfPermission(activity, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(permission);
                }
            }
        } else if (mode == GPS_PERMISSION) {
            listPermissionsNeeded = new ArrayList<>();
            for (String permission : mapPermissions) {
                if (ContextCompat.checkSelfPermission(activity, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(permission);
                }
            }
        } else if (mode == CSV_PERMISSION) {
            listPermissionsNeeded = new ArrayList<>();
            for (String permission : csvPermissions) {
                if (ContextCompat.checkSelfPermission(activity, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(permission);
                }
            }
        } else if (mode == BLUETOOTH_PERMISSION) {
            listPermissionsNeeded = new ArrayList<>();
            for (String permission : bluetoothPermission) {
                if (ContextCompat.checkSelfPermission(activity, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(permission);
                }
            }
        }
        PERMISSIONS_REQUIRED = listPermissionsNeeded.size();
        if (!listPermissionsNeeded.isEmpty()) {
            for (String permission : listPermissionsNeeded) {
                if (Objects.equals(permission, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(activity);
                    alert.setTitle("Location Permission Disclosure");
                    alert.setCancelable(false);
                    alert.setMessage("PSLab requires access to location data to show the location of measurements on a map.");
                    alert.setPositiveButton("ACCEPT", (dialog, which) -> {
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ++REQUEST_CODE);
                    });
                    alert.setNegativeButton("DENY", (dialog, which) -> {
                        Toast.makeText(activity, "Please grant the permission.", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ++REQUEST_CODE);
                    });
                    AlertDialog alertDialog = alert.create();
                    alertDialog.show();
                } else if (Objects.equals(permission, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(activity);
                    alert.setTitle("Storage Permission Disclosure");
                    alert.setMessage("PSLab requires access to storage to enable the storage and import of sensor data.");
                    alert.setPositiveButton("ACCEPT", (dialog, which) -> {
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, ++REQUEST_CODE);
                    });
                    alert.setNegativeButton("DENY", (dialog, which) -> {
                        Toast.makeText(activity, "Please grant the permission.", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, ++REQUEST_CODE);
                    });
                    AlertDialog alertDialog = alert.create();
                    alertDialog.show();
                } else if (Objects.equals(permission, Manifest.permission.RECORD_AUDIO)) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(activity);
                    alert.setTitle("Audio Permission Disclosure");
                    alert.setMessage("PSLab requires access to record audio for recording data using the Built-In MIC.");
                    alert.setPositiveButton("ACCEPT", (dialog, which) -> {
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, ++REQUEST_CODE);
                    });
                    alert.setNegativeButton("DENY", (dialog, which) -> {
                        Toast.makeText(activity, "Please grant the permission.", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, ++REQUEST_CODE);
                    });
                    AlertDialog alertDialog = alert.create();
                    alertDialog.show();
                } else if (Objects.equals(permission, Manifest.permission.BLUETOOTH_SCAN)) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, ++REQUEST_CODE);
                }
            }
            return false;
        }
        return true;
    }
}
