package com.arixo.arixoglass.presenter.impl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;

import com.arixo.arixoglass.R;
import com.arixo.arixoglass.base.BasePresenter;
import com.arixo.arixoglass.model.IMainModel;
import com.arixo.arixoglass.presenter.IMainPresenter;
import com.arixo.arixoglass.utils.Constant;
import com.arixo.arixoglass.utils.FileUtil;
import com.arixo.arixoglass.utils.SoundUtils;
import com.arixo.arixoglass.utils.SystemParams;
import com.arixo.arixoglass.utils.ToolUtil;
import com.arixo.arixoglass.view.IMainView;
import com.arixo.bluetooth.library.connection.BluetoothServiceConnection;
import com.arixo.bluetooth.library.connection.IBluetoothServiceCommunication;
import com.arixo.glasssdk.core.ArixoGlassSDKManager;
import com.arixo.glasssdk.interfaces.CameraClientCallback;
import com.arixo.glasssdk.interfaces.DeviceConnectListener;
import com.arixo.glasssdk.interfaces.ServiceInitListener;
import com.arixo.glasssdk.serviceclient.CameraClient;
import com.arixo.glasssdk.serviceclient.DeviceClient;
import com.arixo.glasssdk.serviceclient.LCDClient;

/**
 * Created by lovart on 2019/1/24
 */
public class MainPresenterImpl extends BasePresenter<IMainModel, IMainView> implements IMainPresenter {

    private static final String TAG = MainPresenterImpl.class.getSimpleName();
    private static final int BURST_SHOT = 0;

    private CameraClient mCameraClient;
    private long startTime;
    private boolean burstShot;
    private long keyDownTime;
    private boolean offActivity;
    private AudioManager mAudioManager;
    private MediaSession mMediaSession;
    private LCDClient mLcdClient;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BURST_SHOT:
                    handleCapture();
                    break;
            }
        }
    };

    private ServiceInitListener mServiceInitListener = new ServiceInitListener() {
        @Override
        public void onInitStatus(boolean status) {
            Log.d(TAG, "onInitStatus: " + status);
            try {
                if (status) {
                    mDeviceClient = ArixoGlassSDKManager.getInstance().getDeviceClient();
                    if (mDeviceClient != null) {
                        mDeviceClient.registerDeviceListener(mDeviceConnectListener);
                    }
                    mCameraClient = ArixoGlassSDKManager.getInstance().getCameraClient();
                    mLcdClient = ArixoGlassSDKManager.getInstance().getLCDClient();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private DeviceConnectListener mDeviceConnectListener = new DeviceConnectListener() {
        @Override
        public void onAttach(UsbDevice usbDevice) {
            Log.d(TAG, "onAttach: ");
        }

        @Override
        public void onDeAttach(UsbDevice usbDevice) {
            Log.d(TAG, "onDeAttach: ");
        }

        @Override
        public void onConnect(UsbDevice usbDevice) {
            Log.d(TAG, "onConnect: ");
            if (mCameraClient == null) {
                mCameraClient = ArixoGlassSDKManager.getInstance().getCameraClient();
            }
            if (mLcdClient != null) {
                mLcdClient.setLCDLuminance(SystemParams.getInstance().getInt(Constant.DEFAULT_LCD_BRIGHTNESS_LEVEL, 10));
            }
            openCamera();
        }

        @Override
        public void onDisconnect(UsbDevice usbDevice) {
            Log.d(TAG, "onDisconnect: ");
            if (mCameraClient != null) {
                mCameraClient.disconnect();
                mCameraClient = null;
            }
            if (mLcdClient != null) {
                mLcdClient.stopCaptureRecord();
            }
        }

        @Override
        public void onCancel(UsbDevice usbDevice) {

        }
    };

    private CameraClientCallback mClientCallback = new CameraClientCallback() {
        @Override
        public void onCameraOpened() {
            Log.d(TAG, "onCameraOpened: ");
            if (mCameraClient != null && getView().getCameraVew().getHolder().getSurface().isValid()) {
                mCameraClient.addSurface(getView().getCameraVew().getHolder().getSurface(), false);
            }
        }

        @Override
        public void onCameraClosed() {
            Log.d(TAG, "onCameraClosed: ");
            if (mCameraClient != null) {
                mCameraClient.removeSurface(getView().getCameraVew().getHolder().getSurface());
            }
        }
    };

    private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            if (mCameraClient != null) {
                mCameraClient.addSurface(surfaceHolder.getSurface(), false);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            if (mCameraClient != null) {
                mCameraClient.removeSurface(surfaceHolder.getSurface());
            }
        }
    };
    private DeviceClient mDeviceClient;

    @Override
    public void openCamera() {
        if (mCameraClient != null && !mCameraClient.isOpened()) {
            Log.i(TAG, "openCamera: ");
            int[] resolution = getPreviewResolution();
            mCameraClient.open(resolution[0], resolution[1], mClientCallback);
            mCameraClient.setPreviewFrameCallback((buffer, width, height) ->
                    Log.d(TAG, "PreviewFrameCallback: " + System.currentTimeMillis())
            );
        }
    }

    @Override
    public int[] getPreviewResolution() {
        int[] resolution = new int[2];
        String savedResolution = SystemParams.getInstance().getString(Constant.PREVIEW_RESOLUTION, Constant.DEFAULT_RESOLUTION);
        String[] resolutions = savedResolution.split("x");
        resolution[0] = Integer.parseInt(resolutions[0]);
        resolution[1] = Integer.parseInt(resolutions[1]);
        return resolution;
    }

    @Override
    public void setOffActivity(boolean offActivity) {
        this.offActivity = offActivity;
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getView().getContext().getSystemService(Context.AUDIO_SERVICE);
        }
        if (!offActivity) {
            if (mAudioManager != null && !mAudioManager.isBluetoothScoOn()) {
                mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                mAudioManager.startBluetoothSco();
                mAudioManager.setBluetoothScoOn(true);
            }
            final MediaPlayer mediaPlayer = MediaPlayer.create(getView().getContext(), R.raw.camera_click);
            mediaPlayer.setVolume(0, 0);
            mediaPlayer.setOnCompletionListener(mp -> mediaPlayer.release());
            mediaPlayer.start();
            mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            createMediaSession();
        }
    }

    @Override
    public void closeCamera() {
        Log.d(TAG, "closeCamera: ");
        if (mCameraClient != null && mCameraClient.isOpened()) {
            mCameraClient.disconnect();
            mCameraClient.release();
        }
    }

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

    private IBluetoothServiceCommunication.BluetoothEventListener eventListener = (key, event) -> {
        if (!offActivity) {
            switch (key) {
                case IBluetoothServiceCommunication.PHYSICAL_KEY:
                    switch (event) {
                        case IBluetoothServiceCommunication.EVENT_SHORT:
                            if ((System.currentTimeMillis() - keyDownTime) < 1000) {
                                burstShot = false;
                                handleCapture();
                            }
                            break;
                        case IBluetoothServiceCommunication.EVENT_LONG:
                            break;
                        case IBluetoothServiceCommunication.EVENT_DOUBLE:
                            break;
                    }
                    break;
                case IBluetoothServiceCommunication.TOUCH_PAD:
                    switch (event) {
                        case IBluetoothServiceCommunication.EVENT_SHORT:
                            handleRecording();
                            break;
                    }
                    break;
            }
        }
    };

    private IBluetoothServiceCommunication.BluetoothActionListener actionListener = action -> {
        if (!offActivity) {
            switch (action) {
                case IBluetoothServiceCommunication.ACTION_UP:
                    if (burstShot) {
                        mHandler.removeMessages(BURST_SHOT);
                        burstShot = false;
                    }
                    break;
                case IBluetoothServiceCommunication.ACTION_DOWN:
                    if (!burstShot) {
                        keyDownTime = System.currentTimeMillis();
                        mHandler.sendEmptyMessageDelayed(BURST_SHOT, 1000);
                        burstShot = true;
                    }
                    break;
            }
        }
    };

    @Override
    public void handleRecording() {
        if (mCameraClient != null && !mCameraClient.isRecording()) {
            startTime = SystemClock.elapsedRealtime();
            mCameraClient.startRecording(FileUtil.getVideoBasePath().getAbsolutePath());
            getView().setToRecord();
        } else if (mCameraClient != null && mCameraClient.isRecording()) {
            mCameraClient.stopRecording();
            getView().setToCapture();
        }
    }

    @Override
    public void handleCapture() {
        if (mCameraClient != null) {
            int pictureToken = 0;
            int burstShotCount = SystemParams.getInstance().getInt(Constant.DEFAULT_BURST_SHOT_COUNT, 1);
            do {
                StringBuilder path = new StringBuilder();
                path.append(ToolUtil.getTime());
                if (burstShot) {
                    path.append("-").append(burstShotCount);
                }
                if (!TextUtils.isEmpty(path)) {
                    mCameraClient.captureStill(FileUtil.getPicturePath(path.toString()));
                    SoundUtils.getInstance().play(1);
                } else {
                    getView().showToast(getView().getContext().getResources().getString(R.string.take_pic_failed));
                    return;
                }
                pictureToken += 1;
                burstShotCount -= 1;
                if (burstShotCount <= 0) {
                    burstShot = false;
                }
            } while (burstShot);
            getView().showToast(getView().getContext().getResources().getString(R.string.picture_token, pictureToken));
        }
    }

    @Override
    public void handleLongClick() {
        burstShot = SystemParams.getInstance().getBoolean(Constant.USE_BURST_SHOT, false);
        handleCapture();
    }

    @Override
    public String getTime() {
        if (model != null) {
            return model.getTimeFrom(startTime);
        }
        return null;
    }

    @Override
    protected void onViewDestroy() {
        if (mAudioManager != null) {
            mAudioManager.setMode(AudioManager.MODE_NORMAL);
            mAudioManager.stopBluetoothSco();
            mAudioManager.setBluetoothScoOn(false);
            mAudioManager.abandonAudioFocus(null);
        }
        releaseMediaSession();
    }


    @Override
    public void initCameraService() {
        getView().getCameraVew().getHolder().addCallback(surfaceCallback);
        ArixoGlassSDKManager.getInstance().init(getView().getContext(), mServiceInitListener);
    }

    @Override
    public void initBluetoothService() {
        BluetoothServiceConnection.getInstance()
                .registerBluetoothDeviceConnectionListener(bluetoothDeviceConnectionListener)
                .registerBluetoothActionListener(actionListener)
                .registerBluetoothEventListener(eventListener)
                .init(getView().getContext());
    }

    @Override
    public void unInitCameraService() {
        if (mLcdClient != null && mLcdClient.isScreenSyncing()) {
            mLcdClient.stopCaptureRecord();
        }
        ArixoGlassSDKManager.getInstance().destroy();
    }

    @Override
    public void unInitBluetoothService() {
        BluetoothServiceConnection.getInstance()
                .unregisterBluetoothActionListener(actionListener)
                .unregisterBluetoothEventListener(eventListener)
                .unregisterBluetoothDeviceConnectionListener(bluetoothDeviceConnectionListener)
                .destroy();
    }

    private void createMediaSession() {
        if (mMediaSession == null) {
            mMediaSession = new MediaSession(getView().getContext(), getView().getClass().getSimpleName());
        }
        mMediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public boolean onMediaButtonEvent(@NonNull Intent mediaButtonIntent) {
                return true;
            }
        });
        mMediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS);
        mMediaSession.setActive(true);

    }

    private void releaseMediaSession() {
        if (mMediaSession != null) {
            mMediaSession.setCallback(null);
            mMediaSession.setActive(false);
            mMediaSession.release();
            mMediaSession = null;
        }
    }
}
