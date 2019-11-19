package com.hyperether.getgoing.ui.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hyperether.getgoing.R;

import java.util.ArrayList;

public class HorizontalListAdapter extends RecyclerView.Adapter<HorizontalListAdapter.ViewHolder> {
    private ArrayList<Integer> imgList;
    private LayoutInflater mInflater;
    private Context mContext;

    public HorizontalListAdapter(ArrayList<Integer> data, Context pContext)
    {
        this.imgList = data;
        this.mInflater = LayoutInflater.from(pContext);
        this.mContext = pContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.hlist_row_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int positionInList = position % imgList.size();
        Integer img = imgList.get(positionInList);

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
        return Integer.MAX_VALUE;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.iv_ri_pic);
        }
    }
}
