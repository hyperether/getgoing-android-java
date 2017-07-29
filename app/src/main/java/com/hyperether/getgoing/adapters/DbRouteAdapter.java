package com.hyperether.getgoing.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyperether.getgoing.R;
import com.hyperether.getgoing.db.DbRoute;
import com.hyperether.getgoing.db.GetGoingDataSource;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;


public class DbRouteAdapter extends BaseAdapter {

    private List<DbRoute> myRoutes = Collections.emptyList();
    private final Context context;
    private GetGoingDataSource datasource;

    DecimalFormat df = new DecimalFormat("#.##");
    // limiting output values to 8 decimal places

    View lastClickedRow = null;

    public DbRouteAdapter(Context context) {
        this.context = context;
    }

    public void updateRoutes(List<DbRoute> myRoutes, GetGoingDataSource datasource) {
        this.myRoutes = myRoutes;
        this.datasource = datasource;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return myRoutes.size();
    }

    /**
     * getItem(int) in Adapter returns Object but we can override it to DbRoute thanks to Java
     * return type covariance
     */
    @Override
    public Object getItem(int position) {
        return myRoutes.get(position);
    }

    /**
     * getItemId() is often useless, I think this should be the default implementation in
     * BaseAdapter
     */
    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class ViewHolder {
        public TextView textDate;
        public TextView textTime;
        public TextView textEnergy;
        public ImageView imageViewAction;
        public ImageView imageViewDelete;
        public long route_id;
    }

    /**
     * Override of getView() method to implement specific layout
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView textDate;
        TextView textTime;
        TextView textEnergy;
        ImageView imageViewAction;
        ImageView imageViewDelete;

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.list_element, parent, false);

            ViewHolder holder = new ViewHolder();

            textDate = (TextView) convertView.findViewById(R.id.textDate);
            textTime = (TextView) convertView.findViewById(R.id.textTime);
            textEnergy = (TextView) convertView.findViewById(R.id.textEnergy);
            imageViewAction = (ImageView) convertView.findViewById(R.id.walk_image);
            imageViewDelete = (ImageView) convertView.findViewById(R.id.delete_layout);

            holder.textDate = textDate;
            holder.textTime = textTime;
            holder.textEnergy = textEnergy;
            holder.imageViewAction = imageViewAction;
            holder.imageViewDelete = imageViewDelete;

            //holder.position = position;

            holder.imageViewDelete.setOnClickListener(delButtonListener);

            convertView.setTag(holder);
        } else {
            ViewHolder holder = (ViewHolder) convertView.getTag();

            textDate = holder.textDate;
            textEnergy = holder.textEnergy;
            imageViewAction = holder.imageViewAction;
        }

        DbRoute route = myRoutes.get(position);
        ViewHolder tmpHolder = (ViewHolder) convertView.getTag();
        tmpHolder.route_id = route.getId();
        convertView.setTag(tmpHolder);

        String d = "" + route.getDate();
        String e = " Energy: " + Double.valueOf(df.format(route.getEnergy())) + " kcal";

        textDate.setText(d);
        textEnergy.setText(e);

        if (imageViewAction != null) {
            switch (route.getActivity_id()) {
                case 1:
                    imageViewAction.setImageResource(R.drawable.statistic_walk);
                    break;
                case 2:
                    imageViewAction.setImageResource(R.drawable.statistic_run);
                    break;
                case 3:
                    imageViewAction.setImageResource(R.drawable.statistic_ride);
                default:
                    break;
            }
        }

        return convertView;
    }

    /**
     * Monitor for the click on the delete button.
     */
    private final View.OnClickListener delButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            lastClickedRow = v;

            new AlertDialog.Builder(context)
                    .setTitle(R.string.delete_message)
                    .setPositiveButton(R.string.confirm,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    doPositiveClick(lastClickedRow);
                                }
                            }).setNegativeButton(R.string.dialog_cancel,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }
                    }).create().show();
        }
    };

    /**
     * This method handle, positive click on alert dialog.
     */
    void doPositiveClick(View v) {
        // getting the list element
        View listElement = (View) v.getParent().getParent();
        // get the tag for this element
        ViewHolder tmpHolder = (ViewHolder) listElement.getTag();
        // where the position of this element is stored
        if (tmpHolder != null) {
            long id = tmpHolder.route_id;

            // delete the route with all nodes attached to it
            datasource.deleteRouteById(id);

            // refresh the route list
            myRoutes = datasource.getAllRoutes();
            notifyDataSetChanged();
        }
    }
}
