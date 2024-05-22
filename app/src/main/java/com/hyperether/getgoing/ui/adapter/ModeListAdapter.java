package com.hyperether.getgoing.ui.adapter;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hyperether.getgoing.R;
import com.hyperether.getgoing.util.Conversion;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ModeListAdapter extends RecyclerView.Adapter<ModeListAdapter.ViewHolder> {
    private SparseIntArray imgList;
    private LayoutInflater mInflater;
    private Context mContext;
    public ViewHolder mHolder;

    public ModeListAdapter(SparseIntArray data, Context pContext) {
        this.imgList = data;
        this.mInflater = LayoutInflater.from(pContext);
        this.mContext = pContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        float screenwidth = Conversion.convertPixelToDp(mContext, mContext.getResources().getDisplayMetrics().widthPixels);

        if (screenwidth >= 358 && screenwidth <= 362)
            view = mInflater.inflate(R.layout.hlist_row_item_360, parent, false);
        else if (screenwidth >= 390 && screenwidth <= 396)
            view = mInflater.inflate(R.layout.hlist_row_item_390, parent, false);
        else if (screenwidth >= 410 && screenwidth <= 412)
            view = mInflater.inflate(R.layout.hlist_row_item_410, parent, false);
        else if (screenwidth >= 455 && screenwidth <= 462)
            view = mInflater.inflate(R.layout.hlist_row_item_460, parent, false);
        else if (screenwidth >= 595 && screenwidth <= 605)
            view = mInflater.inflate(R.layout.hlist_row_item_600, parent, false);
        else
            view = mInflater.inflate(R.layout.hlist_row_item_410, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int positionInList = position % imgList.size();
        mHolder = holder;
        int img = imgList.keyAt(positionInList);
        holder.img.setImageDrawable(mContext.getDrawable(img));
        holder.img.setTag(img);
    }

    @Override
    public long getItemId(int i) {
        int positionInList = i % imgList.size();
        return imgList.get(positionInList);
    }

    @Override
    public int getItemCount() {
        return imgList.size() * 10;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.iv_ri_pic);
        }
    }
}
