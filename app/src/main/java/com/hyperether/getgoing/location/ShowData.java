package com.hyperether.getgoing.location;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hyperether.getgoing.R;
import com.hyperether.getgoing.db.DbRoute;
import com.hyperether.getgoing.db.GetGoingDataSource;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShowData extends ListActivity {

    private GetGoingDataSource datasource;
    private List<DbRoute> routes;
    private ListView list;
    private DbRouteAdapter adapter;

    DecimalFormat df = new DecimalFormat("#.##");      // limiting output values to 8 decimal places

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_data);

        routes = new ArrayList<DbRoute>();
        // Initialize database connection
        datasource = new GetGoingDataSource(this);
        datasource.open();
        routes = datasource.getAllRoutes(); // Get the list of all routes from database

        // use the custom adapter to show the
        // elements in a ListView
        populateListView();
    }

    private void populateListView() {
        adapter = new DbRouteAdapter(ShowData.this);
        adapter.updateRoutes(routes); // populate adapter with routes
        list = (ListView) findViewById(android.R.id.list);
        list.setAdapter(adapter);
    }

    /**
     * Monitor for the click on one row of the ListView
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        DbRoute route = routes.get((int) id);
        // Passing the unique database id of the chosen route in order to draw it correctly
        Intent intent = new Intent(getBaseContext(), ShowRoute.class);
        intent.putExtra("ROUTE_ID", route.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        datasource.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        datasource.close();
        super.onPause();
    }

    @Override
    protected void onStop() {
        datasource.close();
        super.onStop();
    }

    /**
     * @author dusan
     *         <p/>
     *         Our custom ArrayAdapter class for showing the route data
     */
    private class DbRouteAdapter extends BaseAdapter {

        private List<DbRoute> myRoutes = Collections.emptyList();

        private final Context context;

        DecimalFormat df = new DecimalFormat("#.##");      // limiting output values to 8 decimal places

        View lastClickedRow = null;

        public DbRouteAdapter(Context context) {
            this.context = context;
        }

        public void updateRoutes(List<DbRoute> myRoutes) {
            this.myRoutes = myRoutes;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return myRoutes.size();
        }

        /**
         * getItem(int) in Adapter returns Object but we can override
         * it to DbRoute thanks to Java return type covariance
         */
        @Override
        public Object getItem(int position) {
            return myRoutes.get(position);
        }

        /**
         * getItemId() is often useless, I think this should be the default
         * implementation in BaseAdapter
         */
        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        public class ViewHolder {
            public TextView textDate;
            public TextView textEnergy;
            public ImageView image;
            public Button button;
            public long route_id;
        }

        /**
         * Override of getView() method to implement specific layout
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TextView textDate;
            TextView textEnergy;
            ImageView image;
            Button button;

            if (convertView == null) {
                convertView = LayoutInflater.from(context)
                        .inflate(R.layout.list_element, parent, false);

                ViewHolder holder = new ViewHolder();

                textDate = (TextView) convertView.findViewById(R.id.textDate);
                textEnergy = (TextView) convertView.findViewById(R.id.textEnergy);
                image = (ImageView) convertView.findViewById(R.id.walk_layout);
                button = (Button) convertView.findViewById(R.id.delete);

                holder.textDate = textDate;
                holder.textEnergy = textEnergy;
                holder.image = image;
                holder.button = button;
                //holder.position = position;

                holder.button.setOnClickListener(delButtonListener);

                convertView.setTag(holder);
            } else {
                ViewHolder holder = (ViewHolder) convertView.getTag();

                textDate = holder.textDate;
                textEnergy = holder.textEnergy;
                image = holder.image;
                button = holder.button;
            }

            DbRoute route = myRoutes.get(position);
            ViewHolder tmpHolder = (ViewHolder) convertView.getTag();
            tmpHolder.route_id = route.getId();
            convertView.setTag(tmpHolder);

            String d = "" + route.getDate();
            String e = " Energy: " + Double.valueOf(df.format(route.getEnergy())) + " kcal";

            textDate.setText(d);
            textEnergy.setText(e);

            switch (route.getActivity_id()) {
                case 1:
                    image.setImageResource(R.drawable.walk);
                    break;
                case 2:
                    image.setImageResource(R.drawable.run);
                    break;
                case 3:
                    image.setImageResource(R.drawable.ride);
                default:
                    break;
            }

            return convertView;
        }

        // Monitor for the click on the delete button
        private final OnClickListener delButtonListener = new OnClickListener() {
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

        void doPositiveClick(View v) {
            View listElement = (View) v.getParent();    // getting the list element
            ViewHolder tmpHolder = (ViewHolder) listElement.getTag();    // get the tag for this element
            long id = tmpHolder.route_id;        // where the position of this element is stored

            datasource.deleteRouteById(id);  // delete the route with all nodes attached to it

            routes = datasource.getAllRoutes();  // refresh the route list
            adapter.updateRoutes(routes);
            adapter.notifyDataSetChanged();        // redraw the list
        }
    }
}
