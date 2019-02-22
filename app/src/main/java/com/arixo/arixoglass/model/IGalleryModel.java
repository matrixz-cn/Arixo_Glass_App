package com.arixo.arixoglass.model;

import com.arixo.arixoglass.base.Model;
import com.arixo.arixoglass.entity.GalleryItem;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by lovart on 2019/1/29
 */
public interface IGalleryModel extends Model {

    CopyOnWriteArrayList<GalleryItem> getPhotos();

    CopyOnWriteArrayList<GalleryItem> getVideos();

}
