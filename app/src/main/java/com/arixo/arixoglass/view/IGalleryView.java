package com.arixo.arixoglass.view;

import android.content.Context;

import com.arixo.arixoglass.base.View;

/**
 * Created by lovart on 2019/1/29
 */
public interface IGalleryView extends View {

    void updateItemView();

    void displayOptionBox(boolean show);

    void updateSelectAllButton(boolean selectAll);

    Context getContext();
}
