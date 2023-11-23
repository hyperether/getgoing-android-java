package com.hyperether.getgoing.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hyperether.getgoing.databinding.ItemAllViewBinding;
import com.hyperether.getgoing.repository.room.entity.DbRoute;

import java.util.List;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.RouteViewHolder> {

    private List<DbRoute> routeList;

    public RouteAdapter(List<DbRoute> routeList) {
        this.routeList = routeList;
    }
    public void updateData(List<DbRoute> newRoutes) {
        this.routeList = newRoutes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemAllViewBinding binding = ItemAllViewBinding.inflate(inflater, parent, false);
        return new RouteViewHolder(binding);
    }


    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        DbRoute route = routeList.get(position);
        holder.bind(route);
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }

    static class RouteViewHolder extends RecyclerView.ViewHolder {

        private ItemAllViewBinding binding;

        public RouteViewHolder(ItemAllViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(DbRoute route) {
//            binding.setRoute(route);
            binding.executePendingBindings();
        }
    }
}
