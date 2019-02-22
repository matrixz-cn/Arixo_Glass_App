package com.arixo.arixoglass.view.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.Handler;
import android.os.Process;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.arixo.arixoglass.BaseActivity;
import com.arixo.arixoglass.R;
import com.arixo.arixoglass.model.ISplashModel;
import com.arixo.arixoglass.model.impl.SplashModelImpl;
import com.arixo.arixoglass.presenter.impl.SplashPresenter;
import com.arixo.arixoglass.utils.SystemParams;
import com.arixo.arixoglass.view.ISplashView;
import com.arixo.glasssdk.core.ArixoGlassSDKManager;
import com.arixo.glasssdk.interfaces.DeviceConnectListener;
import com.arixo.glasssdk.interfaces.ServiceInitListener;
import com.arixo.glasssdk.serviceclient.DeviceClient;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends BaseActivity<ISplashModel, ISplashView, SplashPresenter> implements ISplashView {

    private static final String TAG = SplashActivity.class.getSimpleName();

    private TextView mStatusText;

    private boolean mInitStatus;

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
                } else if (!mInitStatus) {
                    ArixoGlassSDKManager.getInstance().destroy();
                    SystemClock.sleep(100);
                    ArixoGlassSDKManager.getInstance()
                            .init(SplashActivity.this, mServiceInitListener);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mInitStatus = status;
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
            mStatusText.setText(getResources().getString(R.string.connected_text));
            startMain();
        }

        @Override
        public void onDisconnect(UsbDevice usbDevice) {
            Log.d(TAG, "onDisconnect: ");
        }

        @Override
        public void onCancel(UsbDevice usbDevice) {

        }
    };

    private static final int PERMISSION_REQ = 0x123456;

    private final String[] mPermission = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
    };

    private final List<String> mRequestPermission = new ArrayList<>();


    private final Handler handler = new Handler();

    private boolean mCancelled = false;
    private DeviceClient mDeviceClient;

    private void requestPermission() {
        for (String one : mPermission) {
            if (PackageManager.PERMISSION_GRANTED != this.checkPermission(one, Process.myPid(), Process.myUid())) {
                mRequestPermission.add(one);
            }
        }
        if (!mRequestPermission.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.requestPermissions(mRequestPermission.toArray(new String[mRequestPermission.size()]), PERMISSION_REQ);
            }
        } else {
            init();
//            startMain();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQ) {
            for (int i = 0; i < grantResults.length; i++) {
                for (String one : mPermission) {
                    if (permissions[i].equals(one) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        mRequestPermission.remove(one);
                    }
                }
            }
            init();
//            startMain();

        }
    }

    private void startMain() {
        handler.postDelayed(() -> {
            if (!mCancelled) {
                mDeviceClient.unregisterDeviceListener(mDeviceConnectListener);
                ArixoGlassSDKManager.getInstance().destroy();
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 500);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mCancelled = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public ISplashModel createModel() {
        return new SplashModelImpl();
    }

    @Override
    public ISplashView createView() {
        return this;
    }

    @Override
    public SplashPresenter createPresenter() {
        return new SplashPresenter();
    }

    @Override
    public void showToast(String info) {

    }

    @Override
    protected void initView() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        mStatusText = findViewById(R.id.usb_status_text);
    }

    @Override
    protected void initData() {
        requestPermission();
    }

    private void init() {
        SystemParams.init(getApplicationContext(), getPackageName());
        ArixoGlassSDKManager.getInstance().init(this, mServiceInitListener);
    }
}
