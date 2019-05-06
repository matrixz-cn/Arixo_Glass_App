package com.arixo.arixoglass.presenter.impl;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import static android.content.Context.AUDIO_SERVICE;

/**
 * Created by lovart on 2019/1/24
 */
public class MainPresenterImpl extends BasePresenter<IMainModel, IMainView> implements IMainPresenter {

    private static final String TAG = MainPresenterImpl.class.getSimpleName();
    private static final int BURST_SHOT = 0;
    private static final int RETRY_BLUETOOTH_SCO = 1;

    private CameraClient mCameraClient;
    private long startTime;
    private boolean burstShot;
    private long keyDownTime;
    private boolean offActivity;
    private AudioManager mAudioManager;
    private MediaSession mMediaSession;
    private LCDClient mLcdClient;
    private boolean scoReceiverRegistered = true;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BURST_SHOT:
                    handleCapture();
                    break;
                case RETRY_BLUETOOTH_SCO:
                    mAudioManager.startBluetoothSco();
                    break;
            }
        }
    };

    private BroadcastReceiver bluetoothSCOReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
            Log.d(TAG, "Audio SCO state: " + state);
            if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) {
                mAudioManager.setBluetoothScoOn(true);  //打开SCO
                scoReceiverRegistered = false;
                getView().getContext().unregisterReceiver(this);  //别遗漏
                Log.d(TAG, "Audio SCO connected");
                mHandler.removeMessages(RETRY_BLUETOOTH_SCO);
            } else {//等待一秒后再尝试启动SCO
                mHandler.sendEmptyMessageDelayed(RETRY_BLUETOOTH_SCO, 1000);
            }
        }
    };

    private CameraClientCallback mClientCallback = new CameraClientCallback() {
        @Override
        public void onCameraOpened() {
            Log.d(TAG, "Camera Device onCameraOpened: ");
            if (mCameraClient != null && getView().getCameraVew().getHolder().getSurface().isValid()) {
                mCameraClient.addSurface(getView().getCameraVew().getHolder().getSurface(), false);
            }
        }

        @Override
        public void onCameraClosed() {
            Log.d(TAG, "Camera Device onCameraClosed: ");
            if (mCameraClient != null) {
                mCameraClient.removeSurface(getView().getCameraVew().getHolder().getSurface());
            }
        }
    };
    private DeviceConnectListener mDeviceConnectListener = new DeviceConnectListener() {
        @Override
        public void onAttach(UsbDevice usbDevice) {
            Log.d(TAG, "Camera Device onAttach: ");
        }

        @Override
        public void onDeAttach(UsbDevice usbDevice) {
            Log.d(TAG, "Camera Device onDeAttach: ");
        }

        @Override
        public void onConnect(UsbDevice usbDevice) {
            Log.d(TAG, "Camera Device onConnect: ");
            mCameraClient = ArixoGlassSDKManager.getInstance().getCameraClient();
            mLcdClient = ArixoGlassSDKManager.getInstance().getLCDClient();
            if (mLcdClient != null) {
                mLcdClient.setLCDLuminance(SystemParams.getInstance().getInt(Constant.DEFAULT_LCD_BRIGHTNESS_LEVEL, 10));
            }
            openCamera();
        }

        @Override
        public void onDisconnect(UsbDevice usbDevice) {
            Log.d(TAG, "Camera Device onDisconnect: ");
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
    private ServiceInitListener mServiceInitListener = new ServiceInitListener() {
        @Override
        public void onInitStatus(boolean status) {
            Log.d(TAG, "onInitStatus: " + status);
            try {
                if (status) {
                    DeviceClient mDeviceClient = ArixoGlassSDKManager.getInstance().getDeviceClient();
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

    /**
     * 开启Camera
     */
    @Override
    public void openCamera() {
        if (mCameraClient != null && !mCameraClient.isOpened()) {
            Log.i(TAG, "openCamera: ");
            int[] resolution = getPreviewResolution();
            mCameraClient.open(resolution[0], resolution[1], mClientCallback);

            // 摄像头回调帧，如需要取消注释
//            mCameraClient.setPreviewFrameCallback((buffer, width, height) -> {
//                Log.d(TAG, "PreviewFrameCallback: " + System.currentTimeMillis());
//            });
        }
    }

    /**
     * 获取Camera预览分辨率
     *
     * @return 分辨率，如：[1280,720]
     */
    @Override
    public int[] getPreviewResolution() {
        int[] resolution = new int[2];
        String savedResolution = SystemParams.getInstance().getString(Constant.PREVIEW_RESOLUTION, Constant.DEFAULT_RESOLUTION);
        String[] resolutions = savedResolution.split("x");
        resolution[0] = Integer.parseInt(resolutions[0]);
        resolution[1] = Integer.parseInt(resolutions[1]);
        return resolution;
    }

    /**
     * 设置View是否暂停
     *
     * @param offActivity 是否暂停
     */
    @Override
    public void setOffActivity(boolean offActivity) {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getView().getContext().getSystemService(AUDIO_SERVICE);
        }
        this.offActivity = offActivity;
        if (!offActivity) {
            final MediaPlayer mediaPlayer = MediaPlayer.create(getView().getContext(), R.raw.camera_click);
            mediaPlayer.setVolume(0, 0);
            mediaPlayer.setOnCompletionListener(mp -> mediaPlayer.release());
            mediaPlayer.start();
            mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            createMediaSession();
            if (!mAudioManager.isBluetoothScoOn()) {
                openSco();
            }
        } else {
            if (mAudioManager != null) {
                mAudioManager.abandonAudioFocus(null);
                if (mAudioManager.isBluetoothScoOn()) {
                    closeSco();
                }
            }
            releaseMediaSession();
        }
    }

    /**
     * 关闭Camera
     */
    @Override
    public void closeCamera() {
        Log.d(TAG, "closeCamera: ");
        if (mCameraClient != null && mCameraClient.isOpened()) {
            mCameraClient.disconnect();
            mCameraClient.release();
        }
    }

    /**
     * 处理录像
     */
    @Override
    public void handleRecording() {
        mCameraClient = ArixoGlassSDKManager.getInstance().getCameraClient();
        if (mCameraClient != null && !mCameraClient.isRecording()) {
            startTime = SystemClock.elapsedRealtime();
            mCameraClient.startRecording(FileUtil.getVideoBasePath().getAbsolutePath());
            getView().setToRecord();
        } else if (mCameraClient != null && mCameraClient.isRecording()) {
            mCameraClient.stopRecording();
            getView().setToCapture();
        }
    }

    /**
     * 处理拍照
     */
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
            } while (burstShot); // 如果为连拍模式，继续拍照
            getView().showToast(getView().getContext().getResources().getString(R.string.picture_token, pictureToken));
        }
    }

    /**
     * 处理长按
     */
    @Override
    public void handleLongClick() {
        burstShot = SystemParams.getInstance().getBoolean(Constant.USE_BURST_SHOT, false);
        handleCapture();
    }

    /**
     * 获取录像时间
     *
     * @return ”00：00：00“格式时间
     */
    @Override
    public String getTime() {
        if (model != null) {
            return model.getTimeFrom(startTime);
        }
        return null;
    }

    @Override
    protected void onViewDestroy() {
        if (scoReceiverRegistered) {
            getView().getContext().unregisterReceiver(bluetoothSCOReceiver);
        }
    }

    /**
     * 初始化设备服务
     */
    @Override
    public void initCameraService() {
        getView().getCameraVew().getHolder().addCallback(surfaceCallback);
        ArixoGlassSDKManager.getInstance().init(getView().getContext(), mServiceInitListener);
    }

    /**
     * 初始化蓝牙连接服务
     */
    @Override
    public void initBluetoothService() {
        BluetoothServiceConnection.getInstance()
                .registerBluetoothDeviceConnectionListener(bluetoothDeviceConnectionListener)
                .registerBluetoothActionListener(actionListener)
                .registerBluetoothEventListener(eventListener)
                .init(getView().getContext());
    }

    /**
     * 销毁设备服务
     */
    @Override
    public void unInitCameraService() {
        if (mLcdClient != null && mLcdClient.isScreenSyncing()) {
            mLcdClient.stopCaptureRecord();
        }
        ArixoGlassSDKManager.getInstance().destroy();
    }

    /**
     * 销毁蓝牙连接服务
     */
    @Override
    public void unInitBluetoothService() {
        BluetoothServiceConnection.getInstance()
                .unregisterBluetoothActionListener(actionListener)
                .unregisterBluetoothEventListener(eventListener)
                .unregisterBluetoothDeviceConnectionListener(bluetoothDeviceConnectionListener)
                .destroy();
    }

    /**
     * 开启蓝牙SCO
     */
    private void openSco() {
        if (!mAudioManager.isBluetoothScoAvailableOffCall()) {
            Log.d(TAG, "系统不支持蓝牙录音");
            return;
        }
        //蓝牙录音的关键，启动SCO连接，耳机话筒才起作用
        //蓝牙SCO连接建立需要时间，连接建立后会发出ACTION_SCO_AUDIO_STATE_CHANGED消息，通过接收该消息而进入后续逻辑。
        //也有可能此时SCO已经建立，则不会收到上述消息，可以startBluetoothSco()前先 stopBluetoothSco()
        if (mAudioManager.isBluetoothScoOn()) {
            mAudioManager.stopBluetoothSco();
            mAudioManager.setBluetoothScoOn(false);
        }
        getView().getContext().registerReceiver(bluetoothSCOReceiver, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
        mAudioManager.startBluetoothSco();
    }

    /**
     * 关闭蓝牙SCO
     */
    private void closeSco() {
        if (mAudioManager.isBluetoothScoOn()) {
            mAudioManager.setBluetoothScoOn(false);
            mAudioManager.stopBluetoothSco();
        }
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
