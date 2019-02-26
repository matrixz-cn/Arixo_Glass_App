package com.arixo.arixoglass.view.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.arixo.arixoglass.BaseActivity;
import com.arixo.arixoglass.R;
import com.arixo.arixoglass.adapter.GalleryItemAdapter;
import com.arixo.arixoglass.model.IGalleryModel;
import com.arixo.arixoglass.model.impl.GalleryModelImpl;
import com.arixo.arixoglass.presenter.IGalleryPresenter;
import com.arixo.arixoglass.presenter.impl.GalleryPresenterImpl;
import com.arixo.arixoglass.view.IGalleryView;

public class GalleryActivity extends BaseActivity<IGalleryModel, IGalleryView, IGalleryPresenter> implements IGalleryView, TabLayout.BaseOnTabSelectedListener {

    private static final String TAG = GalleryActivity.class.getSimpleName();
    private static final int SHOW_TOAST = 0;
    private static final int UPDATE_ITEM_VIEW = 1;

    private TextView optionButton;
    private TextView mSelectAllButton;
    private LinearLayout mFunctionBox;
    private GalleryItemAdapter mGalleryItemAdapter;
    private int tab = 0;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_TOAST:
                    Toast.makeText(getApplicationContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case UPDATE_ITEM_VIEW:
                    if (mGalleryItemAdapter != null) {
                        mGalleryItemAdapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    };

    private GalleryItemAdapter.OnItemClickListener itemClickListener = new GalleryItemAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            if (presenter != null) {
                presenter.handleItemClick(position, tab);
            }
        }

        @Override
        public void onItemLongClick(int position) {
            if (presenter != null) {
                presenter.handleItemLongClick(position, tab);
            }
        }
    };

    @Override
    protected void initView() {
        setContentView(R.layout.activity_gallery);

        // Set Title
        TextView titleText = findViewById(R.id.tv_title_text);
        titleText.setText(getResources().getString(R.string.gallery_text));

        // Get Views
        TextView backButton = findViewById(R.id.tv_backward_button);
        TextView deleteButton = findViewById(R.id.tv_delete);
        mSelectAllButton = findViewById(R.id.tv_select_all);
        optionButton = findViewById(R.id.tv_option_button);
        RecyclerView mGalleryItemsView = findViewById(R.id.rv_gallery_item_view);
        TabLayout mTabs = findViewById(R.id.tl_tabs);
        mFunctionBox = findViewById(R.id.ll_option_box);

        // Set Action Listener
        mTabs.addOnTabSelectedListener(this);
        optionButton.setText(getResources().getString(R.string.select_text));
        optionButton.setOnClickListener((v) -> {
            if (presenter != null) {
                presenter.handleOption(tab);
            }
        });
        backButton.setOnClickListener((v) -> onBackPressed());
        deleteButton.setOnClickListener((v) -> {
            if (presenter != null) {
                presenter.handleDelete(tab);
            }
        });
        mSelectAllButton.setOnClickListener((v) -> {
            if (presenter != null) {
                presenter.handleSelectAll(tab);
            }
        });

        // Set Gallery Item Adapter
        mGalleryItemAdapter = new GalleryItemAdapter(this, itemClickListener);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mGalleryItemsView.setLayoutManager(layoutManager);
        mGalleryItemsView.setAdapter(mGalleryItemAdapter);
    }

    @Override
    protected void initData() {
        if (presenter != null) {
            mGalleryItemAdapter.setGalleryItemList(presenter.getPhotos());
            updateItemView();
        }
    }

    @Override
    public IGalleryModel createModel() {
        return new GalleryModelImpl();
    }

    @Override
    public IGalleryView createView() {
        return this;
    }

    @Override
    public IGalleryPresenter createPresenter() {
        return new GalleryPresenterImpl();
    }

    @Override
    public void showToast(String info) {
        Message msg = new Message();
        msg.what = SHOW_TOAST;
        msg.obj = info;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (presenter != null) {
            presenter.handleTabSwitch(this.tab);
            switch (tab.getPosition()) {
                case 0:
                    this.tab = 0;
                    mGalleryItemAdapter.setGalleryItemList(presenter.getPhotos());
                    updateItemView();
                    break;
                case 1:
                    this.tab = 1;
                    mGalleryItemAdapter.setGalleryItemList(presenter.getVideos());
                    updateItemView();
                    break;
            }
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

    @Override
    public void updateItemView() {
        mHandler.sendEmptyMessage(UPDATE_ITEM_VIEW);
    }

    @Override
    public void displayOptionBox(boolean show) {
        optionButton.setText(getResources().getString(show ? R.string.cancel : R.string.select_text));
        mFunctionBox.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void updateSelectAllButton(boolean selectAll) {
        mSelectAllButton.setText(getResources().getString(selectAll ? R.string.select_all_text : R.string.deselect_all_text));
    }

    @Override
    public Context getContext() {
        return this;
    }
}
