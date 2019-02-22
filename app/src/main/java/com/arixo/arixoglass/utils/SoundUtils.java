package com.arixo.arixoglass.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import com.arixo.arixoglass.R;

/**
 * Created by lovart on 2019/1/30
 */
public class SoundUtils {

    // SoundPool对象
    private static SoundPool mSoundPlayer;
    private static SoundUtils mInstance;

    public static synchronized SoundUtils getInstance() {
        if (mInstance == null) {
            synchronized (SoundUtils.class) {
                if (mInstance == null) {
                    mInstance = new SoundUtils();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化
     *
     * @param context 上下文
     */
    public void init(Context context) {
        if (mSoundPlayer == null) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build();
            mSoundPlayer = new SoundPool.Builder()
                    .setMaxStreams(10)
                    .setAudioAttributes(audioAttributes)
                    .build();
        }

        // 初始化声音
        mSoundPlayer.load(context, R.raw.camera_click, 1);// 1
    }

    /**
     * 播放声音
     *
     * @param soundID soundId
     */
    public void play(int soundID) {
        mSoundPlayer.play(soundID, 1, 1, 0, 0, 1);
    }

    public void stop(int soundID) {
        mSoundPlayer.stop(soundID);
    }

    public void unInit() {
        mSoundPlayer.release();
        mInstance = null;
    }

}
