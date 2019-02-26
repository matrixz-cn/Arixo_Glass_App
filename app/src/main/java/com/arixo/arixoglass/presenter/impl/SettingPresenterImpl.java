package com.arixo.arixoglass.presenter.impl;

import android.hardware.usb.UsbDevice;

import com.arixo.arixoglass.R;
import com.arixo.arixoglass.base.BasePresenter;
import com.arixo.arixoglass.model.ISettingModel;
import com.arixo.arixoglass.presenter.ISettingPresenter;
import com.arixo.arixoglass.utils.Constant;
import com.arixo.arixoglass.utils.SystemParams;
import com.arixo.arixoglass.view.ISettingView;
import com.arixo.bluetooth.library.connection.BluetoothServiceConnection;
import com.arixo.bluetooth.library.connection.IBluetoothServiceCommunication;
import com.arixo.glassframework.utils.CameraUtils;
import com.arixo.glasssdk.core.ArixoGlassSDKManager;
import com.arixo.glasssdk.interfaces.DeviceConnectListener;
import com.arixo.glasssdk.serviceclient.DeviceClient;
import com.arixo.glasssdk.serviceclient.ILCDClient;
import com.arixo.glasssdk.serviceclient.LCDClient;

/**
 * Created by lovart on 2019/1/29
 */
public class SettingPresenterImpl extends BasePresenter<ISettingModel, ISettingView> implements ISettingPresenter {

    private IBluetoothServiceCommunication.BluetoothDeviceConnectionListener bluetoothDeviceConnectionListener
            = new IBluetoothServiceCommunication.BluetoothDeviceConnectionListener() {
        @Override
        public void onConnectionStart() {
            getView().showConnectionDialog();
            getView().updateBluetoothItemView();
        }

        @Override
        public void onConnected() {
            getView().showToast(getView().getContext().getResources().getString(R.string.success_connect_text));
            getView().dismissConnectionDialog();
            getView().dismissSelectionDialog();
        }

        @Override
        public void onDisconnected() {
            getView().showToast(getView().getContext().getResources().getString(R.string.device_disconnected_text));
            getView().dismissConnectionDialog();
        }

        @Override
        public void onConnectionFailed() {
            getView().showToast(getView().getContext().getResources().getString(R.string.failed_connect_text));
            getView().dismissConnectionDialog();
        }

        @Override
        public void noDeviceConnected() {
            getView().showToast(getView().getContext().getResources().getString(R.string.no_bluetooth_device_connect_text));
        }
    };

    private DeviceConnectListener deviceConnectListener = new DeviceConnectListener() {
        @Override
        public void onAttach(UsbDevice device) {

        }

        @Override
        public void onDeAttach(UsbDevice device) {

        }

        @Override
        public void onConnect(UsbDevice device) {
            lcdClient = ArixoGlassSDKManager.getInstance().getLCDClient();
            if (lcdClient != null) {
                getView().setLCDSwitchStatus(true);
                getView().showLCDBrightnessSettingBox();
            }
        }

        @Override
        public void onDisconnect(UsbDevice device) {
            getView().setOSDSwitchStatus(false);
            getView().setLCDSwitchStatus(false);
            getView().hideLCDBrightnessSettingBox();
        }

        @Override
        public void onCancel(UsbDevice device) {

        }
    };

    private DeviceClient deviceClient;
    private LCDClient lcdClient;

    @Override
    protected void onViewDestroy() {

    }

    @Override
    public void initDevice() {
        deviceClient = ArixoGlassSDKManager.getInstance().getDeviceClient();
        lcdClient = ArixoGlassSDKManager.getInstance().getLCDClient();
        if (deviceClient != null) {
            deviceClient.registerDeviceListener(deviceConnectListener);
        }
        if (lcdClient != null) {
            getView().setOSDSwitchStatus(lcdClient.isScreenSyncing());
            getView().setLCDSwitchStatus(lcdClient.getLCDLuminance() > 0);
            if (lcdClient.getLCDLuminance() > 0) {
                getView().showLCDBrightnessSettingBox();
                getView().setLCDLevelChecked(SystemParams.getInstance().getInt(Constant.DEFAULT_LCD_BRIGHTNESS_LEVEL, 10) / 5 - 1);
            } else {
                getView().hideLCDBrightnessSettingBox();
            }
        } else {
            getView().setLCDSwitchStatus(false);
            getView().hideLCDBrightnessSettingBox();
        }
    }

    @Override
    public void unInitDevice() {
        if (deviceClient != null) {
            deviceClient.unregisterDeviceListener(deviceConnectListener);
        }
    }

    @Override
    public void initBluetoothService() {
        BluetoothServiceConnection.getInstance()
                .registerBluetoothDeviceConnectionListener(bluetoothDeviceConnectionListener);
    }

    @Override
    public void unInitBluetoothService() {
        BluetoothServiceConnection.getInstance()
                .unregisterBluetoothDeviceConnectionListener(bluetoothDeviceConnectionListener);
    }

    @Override
    public void handleBluetoothSetting() {
        getView().showSelectionDialog();
    }

    @Override
    public void handleBurstShotSetting(boolean checked) {
        SystemParams.getInstance().setBoolean(Constant.USE_BURST_SHOT, checked);
        if (checked) {
            getView().showBurstShotSettingBox();
        } else {
            getView().hideBurstShotSettingBox();
        }
    }

    @Override
    public void handleOSDSetting(boolean isOSD) {
        if (lcdClient != null && supportOSD()) {
            if (isOSD && !isOSD()) {
                lcdClient.startCaptureRecord(getView().getContext());
            } else if (!isOSD && isOSD()) {
                lcdClient.stopCaptureRecord();
            }
        } else {
            getView().setOSDSwitchStatus(false);
            getView().showToast(getView().getContext().getResources().getString(R.string.device_not_support_function));
        }
    }

    @Override
    public void handleLCDSetting(boolean checked) {
        SystemParams.getInstance().setBoolean(Constant.LCD_OPEN, checked);
        if (checked) {
            getView().showLCDBrightnessSettingBox();
            setLCDBrightnessLevel(SystemParams.getInstance().getInt(Constant.DEFAULT_LCD_BRIGHTNESS_LEVEL, 10));
        } else {
            getView().hideLCDBrightnessSettingBox();
            setLCDBrightnessLevel(0);
        }
    }

    @Override
    public boolean isOSD() {
        if (lcdClient != null) {
            switch (lcdClient.getLCDDisplayMode()) {
                case ILCDClient.LCD_MODE_ASYNC:
                    return false;
                case ILCDClient.LCD_MODE_SYNC:
                    return true;
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean supportOSD() {
        return ArixoGlassSDKManager.getInstance().getCameraType() == CameraUtils.TYPE_USB_MOVIDIUS;
    }

    @Override
    public void setLCDBrightnessLevel(int level) {
        if (lcdClient != null) {
            if (level > 0) {
                SystemParams.getInstance().setInt(Constant.DEFAULT_LCD_BRIGHTNESS_LEVEL, level);
            }
            lcdClient.setLCDLuminance(level);
        } else {
            getView().setLCDSwitchStatus(false);
            getView().hideLCDBrightnessSettingBox();
            getView().showToast(getView().getContext().getResources().getString(R.string.device_not_support_function));
        }
    }

}
