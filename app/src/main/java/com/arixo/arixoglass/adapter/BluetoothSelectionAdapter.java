package com.arixo.arixoglass.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.arixo.arixoglass.R;
import com.arixo.bluetooth.library.entity.BluetoothInfo;

import java.util.List;

/**
 * Created by lovart on 2019/1/26
 */
public class BluetoothSelectionAdapter extends RecyclerView.Adapter<BluetoothSelectionAdapter.DeviceViewHolder> {

    private List<BluetoothInfo> bluetoothInfoList;
    private OnItemClickListener itemClickListener;
    private String connectedDevice;
    private Context mContext;

    public interface OnItemClickListener {
        void onItemClicked(int position);
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder {

        TextView bluetoothName;
        TextView bluetoothAddress;
        TextView connectionStatus;

        DeviceViewHolder(View itemView) {
            super(itemView);
            bluetoothName = itemView.findViewById(R.id.bluetooth_name);
            bluetoothAddress = itemView.findViewById(R.id.bluetooth_address);
            connectionStatus = itemView.findViewById(R.id.tv_connected_status);
        }
    }

    public BluetoothSelectionAdapter(Context context, List<BluetoothInfo> bluetoothInfos, OnItemClickListener onButtonClickListener) {
        this.bluetoothInfoList = bluetoothInfos;
        this.itemClickListener = onButtonClickListener;
        this.mContext = context;
    }

    public void setConnectedDevice(String address) {
        connectedDevice = address;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bluetooth_device_item, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        final int clickPosition = position;
        holder.itemView.setOnClickListener((v) -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClicked(clickPosition);
            }
        });
        String address = bluetoothInfoList.get(clickPosition).getBluetoothAddress();
        holder.bluetoothName.setText(bluetoothInfoList.get(clickPosition).getBluetoothName());
        holder.bluetoothAddress.setText(address);
        if (!TextUtils.isEmpty(connectedDevice) && connectedDevice.equals(address)) {
            holder.connectionStatus.setText(mContext.getResources().getString(R.string.connected_text));
        } else {
            holder.connectionStatus.setText("");

        }
    }

    @Override
    public int getItemCount() {
        return bluetoothInfoList.size();
    }


}
