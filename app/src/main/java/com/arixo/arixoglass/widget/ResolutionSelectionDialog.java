package com.arixo.arixoglass.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.arixo.arixoglass.R;
import com.arixo.arixoglass.adapter.ResolutionItemAdapter;
import com.arixo.arixoglass.utils.Constant;
import com.arixo.arixoglass.utils.SystemParams;

/**
 * Created by lovart on 2019/2/13
 */
public class ResolutionSelectionDialog extends Dialog {

    private ResolutionItemAdapter.OnItemClickListener onItemClickListener = resolution -> {
        SystemParams.getInstance().setString(Constant.PREVIEW_RESOLUTION, resolution);
        dismiss();
    };
    private ResolutionItemAdapter resolutionItemAdapter;

    public ResolutionSelectionDialog(@NonNull Context context) {
        super(context, R.style.Setting_Dialog_Msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resolution_selection_dialog);
        setCanceledOnTouchOutside(true);
        RecyclerView resolutionListView = findViewById(R.id.rv_resolution_list);
        resolutionItemAdapter = new ResolutionItemAdapter(getContext(), onItemClickListener);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        resolutionListView.setLayoutManager(layoutManager);
        resolutionListView.setAdapter(resolutionItemAdapter);
    }

    @Override
    public void show() {
        super.show();
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            window.setAttributes(layoutParams);
        }
        if (resolutionItemAdapter != null) {
            resolutionItemAdapter.notifyDataSetChanged();
        }
    }
}
