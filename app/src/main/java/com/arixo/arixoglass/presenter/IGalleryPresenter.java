package com.arixo.arixoglass.presenter;

import com.arixo.arixoglass.base.Presenter;
import com.arixo.arixoglass.entity.GalleryItem;
import com.arixo.arixoglass.model.IGalleryModel;
import com.arixo.arixoglass.view.IGalleryView;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by lovart on 2019/1/29
 */
public interface IGalleryPresenter extends Presenter<IGalleryModel, IGalleryView> {

    CopyOnWriteArrayList<GalleryItem> getPhotos();

    CopyOnWriteArrayList<GalleryItem> getVideos();

    void handleTabSwitch(int tab);

    void handleItemClick(Integer position, int tab);

    void handleItemLongClick(Integer position, int tab);

    void handleOption(int tab);

    void handleSelectAll(int tab);

    void handleDelete(int tab);

}
