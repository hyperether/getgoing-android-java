package com.hyperether.getgoing.ui.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hyperether.getgoing.R;
import com.hyperether.getgoing.listeners.GgOnClickListener;
import com.hyperether.getgoing.repository.room.entity.Route;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static com.hyperether.getgoing.util.Constants.BUNDLE_PARCELABLE;


public class RouteListAdapter extends RecyclerView.Adapter<RouteListAdapter.ViewHolder> {
    private List<Route> routes;
    private GgOnClickListener listener;

    public RouteListAdapter(GgOnClickListener ggOnClickListener, List<Route> routes) {
        this.routes = routes;
        listener = ggOnClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.show_data_row_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Route route = routes.get(position);
        if (!"null".equals(route.getDate()) || route.getDate() != null) {
            holder.chartProgress.setMax((int) route.getGoal());
            holder.chartProgress.setProgress((int) route.getLength());
            //TODO Fix line below
            holder.chartDate.setText(route.getDate().substring(0, 6));
            holder.chartDate.setOnClickListener((new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(BUNDLE_PARCELABLE, routes.get(position));
                    listener.onClick(bundle);
                }
            }));
        }
    }

    @Override
    public int getItemCount() {
        if (routes != null) {
            return routes.size();
        }
        return 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ProgressBar chartProgress;
        TextView chartDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            chartProgress = itemView.findViewById(R.id.chart_progress);
            chartDate = itemView.findViewById(R.id.chart_date);
        }
    }
}
