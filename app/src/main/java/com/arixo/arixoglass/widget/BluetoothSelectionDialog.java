package com.arixo.arixoglass.widget;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.arixo.arixoglass.R;
import com.arixo.arixoglass.adapter.BluetoothSelectionAdapter;
import com.arixo.bluetooth.library.connection.BluetoothServiceConnection;
import com.arixo.bluetooth.library.connection.IBluetoothServiceCommunication;
import com.arixo.bluetooth.library.entity.BluetoothInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by lovart on 2019/1/25
 */
public class BluetoothSelectionDialog extends Dialog {

    private static final String TAG = BluetoothSelectionDialog.class.getSimpleName();

    private static final int UPDATE_BLUETOOTH_LIST = 0;

    private RecyclerView mDeviceListView;
    private List<BluetoothInfo> bluetoothInfoList;
    private Map<String, String> bluetoothInfoMap;
    private BluetoothSelectionAdapter bluetoothSelectionAdapter;
    private boolean connecting;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_BLUETOOTH_LIST:
                    if (bluetoothSelectionAdapter != null) {
                        bluetoothSelectionAdapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    };

    private IBluetoothServiceCommunication.ArixoDeviceFoundListener arixoDeviceFoundListener = new IBluetoothServiceCommunication.ArixoDeviceFoundListener() {
        @Override
        public void onDeviceFound(String name, String address) {
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(address)) {
                return;
            }
            if (!bluetoothInfoMap.containsKey(address)) {
                BluetoothInfo bluetoothInfo = new BluetoothInfo();
                bluetoothInfo.setBluetoothAddress(address);
                bluetoothInfo.setBluetoothName(name);
                bluetoothInfoMap.put(address, name);
                BluetoothDevice currentDevice = BluetoothServiceConnection.getInstance().getCurrentDevice();
                if (currentDevice != null && address.equals(currentDevice.getAddress())) {
                    if (bluetoothSelectionAdapter != null) {
                        bluetoothSelectionAdapter.setConnectedDevice(address);
                    }
                    bluetoothInfoList.add(0, bluetoothInfo);
                } else {
                    bluetoothInfoList.add(bluetoothInfo);
                }
                mHandler.sendEmptyMessage(UPDATE_BLUETOOTH_LIST);
            }
        }
    };

    private BluetoothSelectionAdapter.OnItemClickListener itemClickListener = position -> {
        if (BluetoothServiceConnection.getInstance().getCurrentDevice() != null
                && BluetoothServiceConnection.getInstance().getCurrentDevice().getAddress()
                .equals(bluetoothInfoList.get(position).getBluetoothAddress())) {
            return;
        }
        if (!connecting) {
            connecting = true;
            BluetoothServiceConnection.getInstance().connectDevice(bluetoothInfoList.get(position).getBluetoothAddress());
        }
    };

    public BluetoothSelectionDialog(@NonNull Context context, boolean inSetting) {
        super(context, inSetting ? R.style.Setting_Dialog_Msg : R.style.Dialog_Msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_selection_dialog);
        setCanceledOnTouchOutside(false);
        initView();
        initData();
    }

    private void initView() {
        Button mRefreshButton = findViewById(R.id.refresh_button);
        Button mCancelButton = findViewById(R.id.cancel_button);
        mDeviceListView = findViewById(R.id.device_recycler_view);
        mRefreshButton.setOnClickListener((v) -> {
            bluetoothInfoMap.clear();
            bluetoothInfoList.clear();
            mHandler.sendEmptyMessage(UPDATE_BLUETOOTH_LIST);
            BluetoothServiceConnection.getInstance().startSearching();
        });
        mCancelButton.setOnClickListener((v) -> {
            BluetoothServiceConnection.getInstance().cancelSearch();
            dismiss();
        });
    }

    private void initData() {
        bluetoothInfoList = new CopyOnWriteArrayList<>();
        bluetoothInfoMap = new ConcurrentHashMap<>();
        bluetoothSelectionAdapter = new BluetoothSelectionAdapter(getContext(), bluetoothInfoList, itemClickListener);
        checkConnectedDevice();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mDeviceListView.setLayoutManager(layoutManager);
        mDeviceListView.setAdapter(bluetoothSelectionAdapter);
    }

    @Override
    public void show() {
        if (bluetoothInfoMap != null && bluetoothInfoList != null) {
            bluetoothInfoMap.clear();
            bluetoothInfoList.clear();
            checkConnectedDevice();
        }
        mHandler.sendEmptyMessage(UPDATE_BLUETOOTH_LIST);
        BluetoothServiceConnection.getInstance().registerArixoDeviceFoundListener(arixoDeviceFoundListener);
        super.show();
        BluetoothServiceConnection.getInstance().startSearching();
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            window.setAttributes(layoutParams);
        }
    }

    @Override
    public void dismiss() {
        connecting = false;
        BluetoothServiceConnection.getInstance().unregisterArixoDeviceFoundListener(arixoDeviceFoundListener);
        super.dismiss();
    }

    public void setConnecting(boolean connecting) {
        this.connecting = connecting;
        if (bluetoothSelectionAdapter != null && connecting) {
            bluetoothSelectionAdapter.setConnectedDevice("");
        }
    }

    public void updateItemView() {
        mHandler.sendEmptyMessage(UPDATE_BLUETOOTH_LIST);
    }

    private void checkConnectedDevice() {
        BluetoothDevice currentDevice = BluetoothServiceConnection.getInstance().getCurrentDevice();
        if (currentDevice != null) {
            String name = currentDevice.getName();
            String address = currentDevice.getAddress();
            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(address)) {
                BluetoothInfo bluetoothInfo = new BluetoothInfo();
                bluetoothInfo.setBluetoothName(currentDevice.getName());
                bluetoothInfo.setBluetoothAddress(currentDevice.getAddress());
                bluetoothInfoMap.put(address, name);
                bluetoothInfoList.add(bluetoothInfo);
                if (bluetoothSelectionAdapter != null) {
                    bluetoothSelectionAdapter.setConnectedDevice(address);
                }
            }
        }
    }
}
