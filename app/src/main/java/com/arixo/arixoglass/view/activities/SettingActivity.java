package com.arixo.arixoglass.view.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.arixo.arixoglass.BaseActivity;
import com.arixo.arixoglass.R;
import com.arixo.arixoglass.model.ISettingModel;
import com.arixo.arixoglass.model.impl.SettingModelImpl;
import com.arixo.arixoglass.presenter.ISettingPresenter;
import com.arixo.arixoglass.presenter.impl.SettingPresenterImpl;
import com.arixo.arixoglass.utils.Constant;
import com.arixo.arixoglass.utils.SystemParams;
import com.arixo.arixoglass.view.ISettingView;
import com.arixo.arixoglass.widget.BluetoothConnectingDialog;
import com.arixo.arixoglass.widget.BluetoothSelectionDialog;
import com.arixo.arixoglass.widget.ClearCacheDialog;
import com.arixo.arixoglass.widget.ResolutionSelectionDialog;

public class SettingActivity extends BaseActivity<ISettingModel, ISettingView, ISettingPresenter> implements ISettingView, CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = SettingActivity.class.getSimpleName();
    private static final int SHOW_TOAST = 0;

    private LinearLayout burstShotCountSettingBox;
    private LinearLayout lcdBrightnessSettingBox;
    private TextView burstShotCountText;
    private SeekBar burstShotCountBar;
    private RadioButton brightnessLowButton;
    private RadioButton brightnessMediumButton;
    private RadioButton brightnessHighButton;

    private BluetoothSelectionDialog bluetoothSelectionDialog;
    private BluetoothConnectingDialog bluetoothConnectingDialog;
    private ResolutionSelectionDialog resolutionSelectionDialog;
    private ClearCacheDialog clearCacheDialog;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_TOAST:
                    Toast.makeText(getApplicationContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private Switch osdSwitch;
    private Switch lcdBrightnessSwitch;

    @Override
    protected void initView() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_setting);

        // Set Title
        TextView titleText = findViewById(R.id.tv_title_text);
        titleText.setText(getResources().getString(R.string.setting_text));

        // Hide Option Button
        TextView optionButton = findViewById(R.id.tv_option_button);
        optionButton.setVisibility(View.GONE);

        burstShotCountSettingBox = findViewById(R.id.rl_burst_shot_count_setting_box);
        lcdBrightnessSettingBox = findViewById(R.id.rg_lcd_brightness_setting_box);
        burstShotCountText = findViewById(R.id.tv_burst_shot_count);
        burstShotCountBar = findViewById(R.id.sb_burst_shot_count_setting);
        Switch burstShotSwitch = findViewById(R.id.s_burst_shot_setting);
        osdSwitch = findViewById(R.id.s_osd_setting);
        lcdBrightnessSwitch = findViewById(R.id.s_lcd_brightness_setting);
        TextView bluetoothSettingButton = findViewById(R.id.tv_bluetooth_setting);
        TextView clearCacheButton = findViewById(R.id.tv_clear_cache);
        TextView backButton = findViewById(R.id.tv_backward_button);
        TextView resolutionSettingButton = findViewById(R.id.tv_resolution_setting);
        TextView aboutButton = findViewById(R.id.tv_about);
        brightnessLowButton = findViewById(R.id.rb_brightness_low);
        brightnessMediumButton = findViewById(R.id.rb_brightness_medium);
        brightnessHighButton = findViewById(R.id.rb_brightness_high);

        burstShotCountBar.setOnSeekBarChangeListener(this);
        burstShotSwitch.setOnCheckedChangeListener(this);
        burstShotSwitch.setChecked(SystemParams.getInstance().getBoolean(Constant.USE_BURST_SHOT, false));
        lcdBrightnessSwitch.setOnCheckedChangeListener(this);
        osdSwitch.setOnCheckedChangeListener(this);
        if (presenter != null) {
            osdSwitch.setChecked(presenter.isOSD());
        }
        bluetoothSettingButton.setOnClickListener((v) -> {
            if (presenter != null) {
                presenter.handleBluetoothSetting();
            }
        });
        clearCacheButton.setOnClickListener((v) -> showClearCacheDialog());
        backButton.setOnClickListener(view -> onBackPressed());
//        resolutionSettingButton.setOnClickListener((v) -> showResolutionSelectionDialog());
        resolutionSettingButton.setOnClickListener((v) -> showToast("目前仅支持1280x720"));
        aboutButton.setOnClickListener(view -> startActivity(new Intent(SettingActivity.this, AboutActivity.class)));
        brightnessLowButton.setOnCheckedChangeListener(this);
        brightnessMediumButton.setOnCheckedChangeListener(this);
        brightnessHighButton.setOnCheckedChangeListener(this);

        bluetoothSelectionDialog = new BluetoothSelectionDialog(this, true);
        bluetoothConnectingDialog = new BluetoothConnectingDialog(this, true);
        resolutionSelectionDialog = new ResolutionSelectionDialog(this);
        clearCacheDialog = new ClearCacheDialog(this);
    }

    @Override
    protected void initData() {
        if (presenter != null) {
            presenter.initBluetoothService();
            presenter.initDevice();
        }
    }

    @Override
    protected void onDestroy() {
        if (presenter != null) {
            presenter.unInitBluetoothService();
            presenter.unInitDevice();
        }
        dismissClearCacheDialog();
        dismissConnectionDialog();
        dismissSelectionDialog();
        dismissResolutionSelectionDialog();
        super.onDestroy();
    }

    @Override
    public ISettingModel createModel() {
        return new SettingModelImpl();
    }

    @Override
    public ISettingView createView() {
        return this;
    }

    @Override
    public ISettingPresenter createPresenter() {
        return new SettingPresenterImpl();
    }

    @Override
    public void showToast(String info) {
        Message msg = new Message();
        msg.what = SHOW_TOAST;
        msg.obj = info;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        switch (compoundButton.getId()) {
            case R.id.s_burst_shot_setting:
                if (presenter != null) {
                    presenter.handleBurstShotSetting(checked);
                }
                break;
            case R.id.s_osd_setting:
                if (presenter != null) {
                    presenter.handleOSDSetting(checked);
                } else {
                    setOSDSwitchStatus(false);
                }
                break;
            case R.id.s_lcd_brightness_setting:
                if (presenter != null) {
                    presenter.handleLCDSetting(checked);
                } else {
                    setLCDSwitchStatus(false);
                }
                break;
            case R.id.rb_brightness_low:
                if (checked && presenter != null) {
                    presenter.setLCDBrightnessLevel(5);
                }
                break;
            case R.id.rb_brightness_medium:
                if (checked && presenter != null) {
                    presenter.setLCDBrightnessLevel(10);
                }
                break;
            case R.id.rb_brightness_high:
                if (checked && presenter != null) {
                    presenter.setLCDBrightnessLevel(15);
                }
                break;

        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (i <= 0) {
            i = 1;
            burstShotCountBar.setProgress(i);
        }
        burstShotCountText.setText(String.valueOf(i * 5));
        SystemParams.getInstance().setInt(Constant.DEFAULT_BURST_SHOT_COUNT, i * 5);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

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
    public void updateBluetoothItemView() {
        if (bluetoothSelectionDialog != null) {
            bluetoothSelectionDialog.updateItemView();
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void setOSDSwitchStatus(boolean isOSD) {
        osdSwitch.setChecked(isOSD);
    }

    @Override
    public void setLCDSwitchStatus(boolean isOpen) {
        lcdBrightnessSwitch.setChecked(isOpen);
    }

    @Override
    public void showBurstShotSettingBox() {
        burstShotCountSettingBox.setVisibility(View.VISIBLE);
        burstShotCountBar.setProgress(SystemParams.getInstance().getInt(Constant.DEFAULT_BURST_SHOT_COUNT) / 5);
        burstShotCountText.setText(String.valueOf(SystemParams.getInstance().getInt(Constant.DEFAULT_BURST_SHOT_COUNT, 5)));
    }

    @Override
    public void hideBurstShotSettingBox() {
        burstShotCountSettingBox.setVisibility(View.GONE);
    }

    @Override
    public void showLCDBrightnessSettingBox() {
        lcdBrightnessSettingBox.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLCDBrightnessSettingBox() {
        lcdBrightnessSettingBox.setVisibility(View.GONE);
    }

    private void showClearCacheDialog() {
        if (clearCacheDialog != null && !clearCacheDialog.isShowing()) {
            clearCacheDialog.show();
        }
    }

    private void dismissClearCacheDialog() {
        if (clearCacheDialog != null && clearCacheDialog.isShowing()) {
            clearCacheDialog.dismiss();
        }
    }

    private void showResolutionSelectionDialog() {
        if (resolutionSelectionDialog != null && !resolutionSelectionDialog.isShowing()) {
            resolutionSelectionDialog.show();
        }
    }

    private void dismissResolutionSelectionDialog() {
        if (resolutionSelectionDialog != null && resolutionSelectionDialog.isShowing()) {
            resolutionSelectionDialog.dismiss();
        }
    }

}
