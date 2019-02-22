package com.arixo.arixoglass.presenter.impl;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.arixo.arixoglass.BuildConfig;
import com.arixo.arixoglass.R;
import com.arixo.arixoglass.base.BasePresenter;
import com.arixo.arixoglass.entity.GalleryItem;
import com.arixo.arixoglass.model.IGalleryModel;
import com.arixo.arixoglass.presenter.IGalleryPresenter;
import com.arixo.arixoglass.utils.FileUtil;
import com.arixo.arixoglass.view.IGalleryView;

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by lovart on 2019/1/29
 */
public class GalleryPresenterImpl extends BasePresenter<IGalleryModel, IGalleryView> implements IGalleryPresenter {

    private static final String TAG = GalleryPresenterImpl.class.getSimpleName();

    private boolean isSelecting;
    private CopyOnWriteArrayList<GalleryItem> selectedItems = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<GalleryItem> photoList;
    private CopyOnWriteArrayList<GalleryItem> videoList;

    @Override
    protected void onViewDestroy() {

    }

    @Override
    public CopyOnWriteArrayList<GalleryItem> getPhotos() {
        if (model != null) {
            photoList = model.getPhotos();
        }
        if (photoList == null) {
            photoList = new CopyOnWriteArrayList<>();
            photoList.clear();
        }
        return photoList;
    }

    @Override
    public CopyOnWriteArrayList<GalleryItem> getVideos() {
        if (model != null) {
            videoList = model.getVideos();
        }
        if (videoList == null) {
            videoList = new CopyOnWriteArrayList<>();
            videoList.clear();
        }
        return videoList;
    }

    @Override
    public void handleTabSwitch(int tab) {
        selectedItems.clear();
        handleCancel(tab == 0 ? photoList : videoList);
        getView().updateItemView();
        getView().updateSelectAllButton(false);
        getView().displayOptionBox(false);
    }

    @Override
    public void handleItemClick(Integer position, int tab) {
        if (isSelecting) {
            handleItemClick(tab == 0 ? photoList.get(position) : videoList.get(position));
            getView().updateItemView();
            if (isAllSelected(tab == 0 ? photoList : videoList)) {
                getView().updateSelectAllButton(false);
            } else {
                getView().updateSelectAllButton(true);
            }
        } else {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            try {
                String path = tab == 0 ? photoList.get(position).getPath() : videoList.get(position).getPath();
                String type = tab == 0 ? "image/*" : "video/*";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri uri = FileProvider.getUriForFile(getView().getContext(), BuildConfig.APPLICATION_ID + ".fileprovider", new File(path));
                    intent.setDataAndType(uri, type);
                } else {
                    intent.setDataAndType(Uri.fromFile(new File(path)), type);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getView().getContext().startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "GalleryViewItemError: ", e);
                getView().showToast(getView().getContext().getResources().getString(R.string.unable_view_item_text));
            }
        }
    }

    @Override
    public void handleItemLongClick(Integer position, int tab) {
        if (!isSelecting) {
            isSelecting = true;
        }
        getView().displayOptionBox(true);
        handleItemClick(position, tab);
    }

    @Override
    public void handleOption(int tab) {
        isSelecting = !isSelecting;
        if (!isSelecting) {
            handleCancel(tab == 0 ? photoList : videoList);
            getView().updateItemView();
        }
        getView().updateSelectAllButton(isSelecting);
        getView().displayOptionBox(isSelecting);
    }

    @Override
    public void handleSelectAll(int tab) {
        handleSelectOrDeselect(tab == 0 ? photoList : videoList);
        getView().updateItemView();
    }

    @Override
    public void handleDelete(int tab) {
        handleDelete(tab == 0 ? photoList : videoList);
        getView().updateItemView();
        getView().updateSelectAllButton(false);
        getView().displayOptionBox(false);
        getView().showToast(getView().getContext().getResources().getString(R.string.delete_success_text));
    }

    private void handleItemClick(GalleryItem item) {
        if (!item.isSelected()) {
            selectedItems.add(item);
        } else {
            selectedItems.remove(item);
        }
        item.setSelected(!item.isSelected());
    }

    private boolean isAllSelected(List<GalleryItem> galleryItemList) {
        return selectedItems.size() == galleryItemList.size();
    }

    private void handleSelectOrDeselect(List<GalleryItem> galleryItemList) {
        if (!isAllSelected(galleryItemList)) {
            handleSelectAll(galleryItemList);
            getView().updateSelectAllButton(false);
        } else {
            handleCancel(galleryItemList);
            getView().updateSelectAllButton(true);
        }
    }

    private void handleSelectAll(List<GalleryItem> galleryItemList) {
        for (GalleryItem i : galleryItemList) {
            if (!selectedItems.contains(i)) {
                selectedItems.add(i);
            }
            i.setSelected(true);
        }
    }

    private void handleCancel(List<GalleryItem> galleryItemList) {
        for (GalleryItem i : galleryItemList) {
            if (selectedItems.contains(i)) {
                i.setSelected(false);
            }
        }
        selectedItems.clear();
    }

    private void handleDelete(List<GalleryItem> galleryItemList) {
        for (GalleryItem i : galleryItemList) {
            if (selectedItems.contains(i)) {
                FileUtil.deleteFile(i.getPath());
                galleryItemList.remove(i);
                selectedItems.remove(i);
            }
        }
    }

}
