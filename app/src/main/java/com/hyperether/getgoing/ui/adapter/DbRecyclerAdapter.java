package com.hyperether.getgoing.ui.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyperether.getgoing.R;
import com.hyperether.getgoing.repository.room.DbHelper;
import com.hyperether.getgoing.repository.room.entity.DbRoute;
import com.hyperether.getgoing.ui.activity.ShowRouteActivity;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by nikola on 29.1.18..
 */

public class DbRecyclerAdapter extends RecyclerView.Adapter<DbRecyclerAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private List<DbRoute> myRoutes = Collections.emptyList();
    private DecimalFormat df = new DecimalFormat("#.##");
    private String date;
    private String energy;
    private String energyp1;
    private String energyp2;
    private Context context;

    public DbRecyclerAdapter(Context context, List<DbRoute> myRoutes) {
        inflater = LayoutInflater.from(context);
        this.myRoutes = myRoutes;
        energyp1 = context.getString(R.string.energyLabel1);
        energyp2 = context.getString(R.string.energyLabel2);
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.list_element, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final DbRoute route = myRoutes.get(position);
        date = route.getDate();
        energy = energyp1 + " " + Double.valueOf(df.format(route.getEnergy()).replace(",", ".")) +
                " " + energyp2;

        if (holder.imageViewAction != null) {
            switch (route.getActivity_id()) {
                case 1:
                    holder.imageViewAction.setImageResource(R.drawable.statistic_walk);
                    break;
                case 2:
                    holder.imageViewAction.setImageResource(R.drawable.statistic_run);
                    break;
                case 3:
                    holder.imageViewAction.setImageResource(R.drawable.statistic_ride);
                default:
                    break;
            }
        }

        holder.textDate.setText(date);
        holder.textEnergy.setText(energy);

        //click on element to start new activity
        holder.elementLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onListItemClick(position);
            }
        });

        //click for delete
        holder.imageViewDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.delete_message)
                        .setPositiveButton(R.string.confirm,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        deleteItem(position);
                                        notifyDataSetChanged();
                                    }
                                }).setNegativeButton(R.string.dialog_cancel,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        }).create().show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return myRoutes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textDate;
        public TextView textTime;
        public TextView textEnergy;
        public ImageView imageViewAction;
        public ImageView imageViewDelete;
        public ConstraintLayout elementLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            textDate = itemView.findViewById(R.id.textDate);
            textTime = itemView.findViewById(R.id.textTime);
            textEnergy = itemView.findViewById(R.id.textEnergy);
            imageViewAction = itemView.findViewById(R.id.walk_image);
            imageViewDelete = itemView.findViewById(R.id.delete_layout);
            elementLayout = itemView.findViewById(R.id.element_layout);
        }
    }

    public void deleteItem(int position) {
        DbRoute route = myRoutes.get(position);
        DbHelper.getInstance(context).deleteRouteById(route.getId());
        myRoutes.remove(position);
        notifyDataSetChanged();
    }

    public void onListItemClick(int position) {
        DbRoute route = myRoutes.get(position);
        Intent intent = new Intent(context, ShowRouteActivity.class);
        intent.putExtra("ROUTE_ID", route.getId());
        context.startActivity(intent);
    }
}
