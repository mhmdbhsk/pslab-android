package io.pslab.fragment;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import io.pslab.R;
import io.pslab.others.CustomSnackBar;

public class BluetoothScanFragment extends DialogFragment {
    private Button bluetoothScanStopButton;
    private ProgressBar scanProgressBar;
    private ListView scannedDevicesListView;
    private ArrayAdapter<String> deviceListAdapter;
    private List<String> deviceList;
    private List<BluetoothDevice> bluetoothDevices;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && !bluetoothDevices.contains(device) && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                    String deviceName = device.getName();
                    deviceList.add(deviceName == null ? device.getAddress() : deviceName);
                    bluetoothDevices.add(device);
                    deviceListAdapter.notifyDataSetChanged();
                } else {
                    /**/
                }
            }
        }
    };
    private BluetoothAdapter bluetoothAdapter;
    private boolean startScanning = false;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket mSocket;
    private OutputStream mOutputStream;
    private InputStream mInputStream;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_bluetooth_scan, container, false);
        scanProgressBar = rootView.findViewById(R.id.bluetooth_scan_progressbar);
        scanProgressBar.setVisibility(View.GONE);
        scannedDevicesListView = rootView.findViewById(R.id.bluetooth_scanned_devices_list);
        deviceList = new ArrayList<>();
        bluetoothDevices = new ArrayList<>();
        deviceListAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, deviceList);
        scannedDevicesListView.setAdapter(deviceListAdapter);
        bluetoothScanStopButton = rootView.findViewById(R.id.bluetooth_scan_stop_button);

        bluetoothScanStopButton.setOnClickListener(v -> {
            if (startScanning) {
                if (bluetoothAdapter != null) {
                    if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        /**/
                    } else {
                        bluetoothAdapter.cancelDiscovery();
                    }
                }
                scanProgressBar.setVisibility(View.GONE);
                startScanning = false;
                bluetoothScanStopButton.setText(getResources().getString(R.string.bluetooth_scan_text));
                scannedDevicesListView.setClickable(true);
            } else
                scanDevices();
        });


        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        requireContext().registerReceiver(broadcastReceiver, filter);

        scannedDevicesListView.setOnItemClickListener((parent, view, position, id) -> {
            CustomSnackBar.showSnackBar(requireActivity().findViewById(android.R.id.content),
                    bluetoothDevices.get(position).getAddress(), null, null, Snackbar.LENGTH_SHORT);
            bluetoothDevice = bluetoothDevices.get(position);
            Objects.requireNonNull(getDialog()).cancel();
            connectBluetooth();
        });
        scanDevices();
        return rootView;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
    }

    private void scanDevices() {
        deviceList.clear();
        bluetoothDevices.clear();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            startScanning = false;
            CustomSnackBar.showSnackBar(requireActivity().findViewById(android.R.id.content),
                    getString(R.string.bluetooth_not_supported), null, null, Snackbar.LENGTH_SHORT);
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                int BLUETOOTH_REQUEST_CODE = 100;
                startActivityForResult(enableBtIntent, BLUETOOTH_REQUEST_CODE);
                startScanning = false;
                bluetoothScanStopButton.setText(getResources().getString(R.string.bluetooth_scan_text));
                scannedDevicesListView.setClickable(true);
            } else {
                startScanning = true;
                scannedDevicesListView.setClickable(false);
                if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    /**/
                } else {
                    bluetoothAdapter.startDiscovery();
                }
                scanProgressBar.setVisibility(View.VISIBLE);
                bluetoothScanStopButton.setText(getResources().getString(R.string.bluetooth_stop_text));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        WindowManager.LayoutParams params = Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow()).getAttributes();
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes(params);
    }

    private void connectBluetooth() {
        try {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
            if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                /**/
            } else {
                mSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);
                mSocket.connect();
                mOutputStream = mSocket.getOutputStream();
                mInputStream = mSocket.getInputStream();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
