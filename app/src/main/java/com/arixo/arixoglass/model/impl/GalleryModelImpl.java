package com.arixo.arixoglass.model.impl;

import com.arixo.arixoglass.entity.GalleryItem;
import com.arixo.arixoglass.model.IGalleryModel;
import com.arixo.arixoglass.utils.FileUtil;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by lovart on 2019/1/29
 */
public class GalleryModelImpl implements IGalleryModel {

    @Override
    public CopyOnWriteArrayList<GalleryItem> getPhotos() {
        CopyOnWriteArrayList<GalleryItem> galleryItemList = new CopyOnWriteArrayList<>();
        FileUtil.getFiles(galleryItemList, FileUtil.getPicBasePath());
        return galleryItemList;
    }

    @Override
    public CopyOnWriteArrayList<GalleryItem> getVideos() {
        CopyOnWriteArrayList<GalleryItem> galleryItemList = new CopyOnWriteArrayList<>();
        FileUtil.getFiles(galleryItemList, FileUtil.getVideoBasePath());
        return galleryItemList;
    }

}
