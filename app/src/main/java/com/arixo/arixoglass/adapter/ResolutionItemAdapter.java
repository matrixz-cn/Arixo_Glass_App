package com.arixo.arixoglass.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.arixo.arixoglass.R;
import com.arixo.arixoglass.utils.Constant;
import com.arixo.arixoglass.utils.SystemParams;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lovart on 2019/2/15
 */
public class ResolutionItemAdapter extends RecyclerView.Adapter<ResolutionItemAdapter.ItemViewHolder> {

    private String defaultResolution;
    private List<String> resolutionList;
    private Context mContext;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onResolutionChosen(String resolution);
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView resolutionText;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            resolutionText = itemView.findViewById(R.id.tv_resolution);
        }
    }

    public ResolutionItemAdapter(Context context, OnItemClickListener onItemClickListener) {
        defaultResolution = SystemParams.getInstance().getString(Constant.PREVIEW_RESOLUTION, Constant.DEFAULT_RESOLUTION);
        resolutionList = Arrays.asList(context.getResources().getStringArray(R.array.preview_resolution));
        this.onItemClickListener = onItemClickListener;
        mContext = context;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.resolution_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.resolutionText.setText(resolutionList.get(position));
        holder.itemView.setOnClickListener(v -> {
            defaultResolution = resolutionList.get(position);
            if (onItemClickListener != null) {
                onItemClickListener.onResolutionChosen(resolutionList.get(position));
            }
        });
        Log.d("CHECK", "onBindViewHolder: " + holder.resolutionText.getText().toString() + " :  " + (defaultResolution));
        if (holder.resolutionText.getText().toString().equals(defaultResolution)) {
            holder.resolutionText.setTextColor(Color.BLACK);
            holder.itemView.setBackground(mContext.getResources().getDrawable(R.drawable.background_corner_round_light_color));
        } else {
            holder.resolutionText.setTextColor(Color.WHITE);
            holder.itemView.setBackground(mContext.getResources().getDrawable(R.drawable.background_corner_round_function));
        }
    }

    @Override
    public int getItemCount() {
        return resolutionList.size();
    }
}
