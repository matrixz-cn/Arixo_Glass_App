package com.arixo.arixoglass.view.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arixo.arixoglass.BaseActivity;
import com.arixo.arixoglass.R;
import com.arixo.arixoglass.model.IMainModel;
import com.arixo.arixoglass.model.impl.MainModelImpl;
import com.arixo.arixoglass.presenter.IMainPresenter;
import com.arixo.arixoglass.presenter.impl.MainPresenterImpl;
import com.arixo.arixoglass.utils.SoundUtils;
import com.arixo.arixoglass.view.IMainView;
import com.arixo.arixoglass.widget.BluetoothConnectingDialog;
import com.arixo.arixoglass.widget.BluetoothSelectionDialog;
import com.arixo.bluetooth.library.connection.BluetoothServiceConnection;
import com.arixo.glasssdk.widget.AspectRatioSurfaceView;

public class MainActivity extends BaseActivity<IMainModel, IMainView, IMainPresenter> implements IMainView {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int SHOW_TOAST = 0;
    private static final int UPDATE_TIME = 1;

    private AspectRatioSurfaceView mCameraView;

    private BluetoothSelectionDialog bluetoothSelectionDialog;
    private BluetoothConnectingDialog bluetoothConnectingDialog;

    private ImageView shutterButton;
    private ImageView recordButton;
    private TextView recordingTime;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_TOAST:
                    Toast.makeText(getApplicationContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case UPDATE_TIME:
                    if (presenter != null) {
                        recordingTime.setText(presenter.getTime());
                    }
                    sendEmptyMessageDelayed(UPDATE_TIME, 1000);
                    break;
            }
        }
    };


    @Override
    public IMainModel createModel() {
        return new MainModelImpl();
    }

    @Override
    public IMainView createView() {
        return this;
    }

    @Override
    public IMainPresenter createPresenter() {
        return new MainPresenterImpl();
    }

    @Override
    public void showToast(String info) {
        Message msg = new Message();
        msg.what = SHOW_TOAST;
        msg.obj = info;
        mHandler.sendMessage(msg);
    }

    @Override
    protected void initView() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mCameraView = findViewById(R.id.camera_view);
        recordingTime = findViewById(R.id.tv_recording_time);
        ImageView settingButton = findViewById(R.id.iv_settings_button);
        shutterButton = findViewById(R.id.iv_shutter_button);
        recordButton = findViewById(R.id.iv_record_button);
        ImageView galleryButton = findViewById(R.id.iv_gallery_button);
        galleryButton.setOnClickListener((v) -> startActivity(new Intent(MainActivity.this, GalleryActivity.class)));
        settingButton.setOnClickListener((v) -> startActivity(new Intent(MainActivity.this, SettingActivity.class)));
        setToCapture();
        bluetoothSelectionDialog = new BluetoothSelectionDialog(this, false);
        bluetoothConnectingDialog = new BluetoothConnectingDialog(this, false);
    }

    @Override
    protected void initData() {
        if (presenter != null) {
            presenter.initCameraService();
            presenter.initBluetoothService();
        }
        SoundUtils.getInstance().init(this);
        mHandler.postDelayed(() -> {
            if (BluetoothServiceConnection.getInstance().getCurrentDevice() == null) {
                showSelectionDialog();
            }
        }, 1000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        resizeCameraView();
        Log.d(TAG, "onResume: ");
        if (presenter != null) {
            presenter.openCamera();
            presenter.setOffActivity(false);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        if (presenter != null) {
            presenter.closeCamera();
            presenter.setOffActivity(true);
        }
    }

    @Override
    protected void onDestroy() {

        if (presenter != null) {
            presenter.unInitCameraService();
            presenter.unInitBluetoothService();
        }
        SoundUtils.getInstance().unInit();
        dismissConnectionDialog();
        dismissSelectionDialog();
        super.onDestroy();
    }

    @Override
    public AspectRatioSurfaceView getCameraVew() {
        return mCameraView;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void showSelectionDialog() {
        if (bluetoothSelectionDialog != null && !bluetoothSelectionDialog.isShowing()) {
            bluetoothSelectionDialog.show();
        }
    }

    @Override
    public void dismissSelectionDialog() {
        if (bluetoothSelectionDialog != null && bluetoothSelectionDialog.isShowing()) {
            bluetoothSelectionDialog.dismiss();
        }
    }

    @Override
    public void showConnectionDialog() {
        if (bluetoothConnectingDialog != null && !bluetoothConnectingDialog.isShowing()) {
            bluetoothConnectingDialog.show();
            if (bluetoothSelectionDialog != null) {
                bluetoothSelectionDialog.setConnecting(true);
            }
        }
    }

    @Override
    public void dismissConnectionDialog() {
        if (bluetoothConnectingDialog != null && bluetoothConnectingDialog.isShowing()) {
            bluetoothConnectingDialog.dismiss();
            if (bluetoothSelectionDialog != null) {
                bluetoothSelectionDialog.setConnecting(false);
            }
        }
    }

    @Override
    public void setToRecord() {
        shutterButton.setImageResource(R.mipmap.recording);
        shutterButton.setOnClickListener((v) -> {
            if (presenter != null) {
                presenter.handleRecording();
            }
        });
        shutterButton.setOnLongClickListener(null);
        recordButton.setImageResource(R.mipmap.shutter_small);
        recordButton.setOnClickListener((v) -> {
            if (presenter != null) {
                presenter.handleCapture();
            }
        });
        recordButton.setOnLongClickListener((v) -> {
            if (presenter != null) {
                presenter.handleLongClick();
            }
            return true;
        });
        recordingTime.setVisibility(View.VISIBLE);
        mHandler.sendEmptyMessageDelayed(UPDATE_TIME, 1000);
    }

    @Override
    public void setToCapture() {
        mHandler.removeMessages(UPDATE_TIME);
        recordingTime.setText(getResources().getString(R.string.default_time));
        recordingTime.setVisibility(View.GONE);
        shutterButton.setImageResource(R.mipmap.shutter_large);
        shutterButton.setOnClickListener((v) -> {
            if (presenter != null) {
                presenter.handleCapture();
            }
        });
        shutterButton.setOnLongClickListener((v) -> {
            if (presenter != null) {
                presenter.handleLongClick();
            }
            return true;
        });
        recordButton.setImageResource(R.mipmap.record);
        recordButton.setOnClickListener((v) -> {
            if (presenter != null) {
                presenter.handleRecording();
            }
        });
        recordButton.setOnLongClickListener(null);
    }

    @Override
    public void updateBluetoothItemView() {
        if (bluetoothSelectionDialog != null) {
            bluetoothSelectionDialog.updateItemView();
        }
    }

    private void resizeCameraView() {
        int[] resolution = presenter.getPreviewResolution();
        mCameraView.setAspectRatio(resolution[0], resolution[1]);
    }
}
