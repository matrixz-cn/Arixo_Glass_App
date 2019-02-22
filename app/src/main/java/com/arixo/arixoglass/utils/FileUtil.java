package com.arixo.arixoglass.utils;

import android.os.Environment;
import android.util.Log;

import com.arixo.arixoglass.entity.GalleryItem;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by lovart on 2019/1/25
 */
public class FileUtil {

    private static final String TAG = FileUtil.class.getSimpleName();
    private static final String PIC_PATH = "image";
    private static final String VIDEO_PATH = "video";
    private static final String BASE_PATH = "ArixoGlass";

    private static final boolean DEBUG = true;

    public static File getVideoBasePath() {
        File fileDir = new File(Environment.getExternalStorageDirectory(), File.separator + BASE_PATH + File.separator + VIDEO_PATH);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        return fileDir;
    }

    public static File getPicBasePath() {
        File fileDir = new File(Environment.getExternalStorageDirectory(), File.separator + BASE_PATH + File.separator + PIC_PATH);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        return fileDir;
    }

    public static String getPicturePath(String fileName) {

        File file = new File(getPicBasePath(), fileName + ".jpg");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return file.getAbsolutePath();
    }

    /**
     * 获取指定路径中的文件
     *
     * @param list 装扫描出来的视频文件实体类
     * @param file 指定的文件夹
     */
    public static void getFiles(final List<GalleryItem> list, File file) {// 获得视频文件
        file.listFiles(file1 -> {
            // sdCard找到视频名称
            String name = file1.getName();
            int i = name.indexOf('.');
            if (i != -1) {
                name = name.substring(i);//获取文件后缀名
                if (name.equalsIgnoreCase(".mp4") || name.equalsIgnoreCase(".jpg")) {
                    GalleryItem galleryItem = new GalleryItem();
                    galleryItem.setName(file1.getName());//文件名
                    galleryItem.setPath(file1.getAbsolutePath());//文件路径
                    galleryItem.setSize(toFileSize(file1.length()));
                    list.add(0, galleryItem);
                    return true;
                }
            } else if (file1.isDirectory()) {
                getFiles(list, file1);
            }
            return false;
        });
    }

    private static String toFileSize(long size) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString;
        String wrongSize = "0B";
        if (size == 0) {
            return wrongSize;
        }
        if (size < 1024) {
            fileSizeString = df.format((double) size) + "B";
        } else if (size < 1048576) {
            fileSizeString = df.format((double) size / 1024) + "KB";
        } else if (size < 1073741824) {
            fileSizeString = df.format((double) size / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) size / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 删除单个文件
     *
     * @param fileName 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                if (DEBUG) Log.d(TAG, "deleteFile: 删除单个文件" + fileName + "成功！");
                return true;
            } else {
                if (DEBUG) Log.d(TAG, "删除单个文件" + fileName + "失败！");
                return false;
            }
        } else {
            if (DEBUG) Log.d(TAG, "删除单个文件失败：" + fileName + "不存在！");
            return false;
        }

    }
}
