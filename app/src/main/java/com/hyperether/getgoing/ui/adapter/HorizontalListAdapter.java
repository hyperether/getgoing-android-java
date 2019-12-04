package com.hyperether.getgoing.ui.adapter;

import android.content.Context;
import android.os.Build;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hyperether.getgoing.R;
import com.hyperether.getgoing.util.Conversion;

public class HorizontalListAdapter extends RecyclerView.Adapter<HorizontalListAdapter.ViewHolder> {
    private SparseIntArray imgList;
    private LayoutInflater mInflater;
    private Context mContext;

    public ViewHolder mHolder;

    public HorizontalListAdapter(SparseIntArray data, Context pContext)
    {
        this.imgList = data;
        this.mInflater = LayoutInflater.from(pContext);
        this.mContext = pContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        float screenwidth = Conversion.convertPixelToDp(mContext, mContext.getResources().getDisplayMetrics().widthPixels);

        if (screenwidth == 360)
            view = mInflater.inflate(R.layout.hlist_row_item_360, parent, false);
        else if (screenwidth >= 410 && screenwidth <= 412)
            view = mInflater.inflate(R.layout.hlist_row_item_410, parent, false);
        else if (screenwidth >= 455 && screenwidth <= 462)
            view = mInflater.inflate(R.layout.hlist_row_item_460, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int positionInList = position % imgList.size();
        mHolder = holder;
        Integer img = imgList.keyAt(positionInList);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //sdk min?
            holder.img.setImageDrawable(mContext.getDrawable(img));
            holder.img.setTag(img);
        }

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

    public ViewHolder getHolder()
    {
        return mHolder;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.iv_ri_pic);
        }
    }
}
