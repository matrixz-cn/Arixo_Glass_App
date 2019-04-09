package com.arixo.arixoglass.presenter;

import com.arixo.arixoglass.base.Presenter;
import com.arixo.arixoglass.model.IMainModel;
import com.arixo.arixoglass.view.IMainView;

/**
 * Created by lovart on 2019/1/29
 */
public interface IMainPresenter extends Presenter<IMainModel, IMainView> {

    /**
     * 初始化设备服务
     */
    void initCameraService();

    /**
     * 初始化蓝牙连接服务
     */
    void initBluetoothService();

    /**
     * 销毁设备服务
     */
    void unInitCameraService();

    /**
     * 销毁蓝牙连接服务
     */
    void unInitBluetoothService();

    /**
     * 处理录像
     */
    void handleRecording();

    /**
     * 处理拍照
     */
    void handleCapture();

    /**
     * 处理长按
     */
    void handleLongClick();

    /**
     * 获取录像时间
     * @return ”00：00：00“格式时间
     */
    String getTime();

    /**
     * 关闭Camera
     */
    void closeCamera();

    /**
     * 开启Camera
     */
    void openCamera();

    /**
     * 获取Camera预览分辨率
     * @return 分辨率，如：[1280,720]
     */
    int[] getPreviewResolution();

    /**
     * 设置View是否暂停
     * @param offActivity 是否暂停
     */
    void setOffActivity(boolean offActivity);
}
