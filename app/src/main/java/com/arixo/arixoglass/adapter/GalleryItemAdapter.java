package com.arixo.arixoglass.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arixo.arixoglass.R;
import com.arixo.arixoglass.entity.GalleryItem;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lovart on 2019/1/31
 */
public class GalleryItemAdapter extends RecyclerView.Adapter<GalleryItemAdapter.GalleryItemViewHolder> {

    private static final String TAG = GalleryItemAdapter.class.getSimpleName();

    private List<GalleryItem> galleryItemList = new ArrayList<>();
    private Context mContext;
    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onItemLongClick(int position);
    }

    public GalleryItemAdapter(Context context, OnItemClickListener listener) {
        this.mContext = context;
        this.itemClickListener = listener;
    }

    public void setGalleryItemList(List<GalleryItem> itemList) {
        this.galleryItemList = itemList;
    }

    @NonNull
    @Override
    public GalleryItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gallery_item, parent, false);
        return new GalleryItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryItemViewHolder holder, int position) {
        final int clickedPosition = position;
        holder.itemView.setOnClickListener(view -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(clickedPosition);
            }
        });
        holder.itemView.setOnLongClickListener(view -> {
            if (itemClickListener != null) {
                itemClickListener.onItemLongClick(clickedPosition);
            }
            return true;
        });
        Glide.with(mContext).load(galleryItemList.get(position).getPath()).into(holder.itemImage);
        holder.itemName.setText(galleryItemList.get(position).getName());
        holder.itemSize.setText(galleryItemList.get(position).getSize());
        if (galleryItemList.get(position).isSelected()) {
            holder.itemView.setBackgroundColor(Color.GREEN);
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return galleryItemList.size();
    }

    class GalleryItemViewHolder extends RecyclerView.ViewHolder {

        private ImageView itemImage;
        private TextView itemName;
        private TextView itemSize;

        GalleryItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.iv_item_image);
            itemName = itemView.findViewById(R.id.tv_item_name);
            itemSize = itemView.findViewById(R.id.tv_item_size);
        }
    }
}
