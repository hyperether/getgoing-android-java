package com.hyperether.getgoing.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.hyperether.getgoing.R;
import com.hyperether.getgoing.adapters.DbRouteAdapter;
import com.hyperether.getgoing.db.DbRoute;
import com.hyperether.getgoing.db.GetGoingDataSource;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ShowDataActivity extends ListActivity {

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

    /**
     * This method is for populating list view
     */
    private void populateListView() {
        adapter = new DbRouteAdapter(ShowDataActivity.this);
        adapter.updateRoutes(routes, datasource); // populate adapter with routes
        list = (ListView) findViewById(android.R.id.list);
        list.setAdapter(adapter);
    }

    /**
     * Monitor for the click on one row of the ListView
     */
    protected void onListItemClick(ListView l, View v, int position, long id) {
        DbRoute route = routes.get((int) id);
        // Passing the unique database id of the chosen route in order to draw it correctly
        Intent intent = new Intent(getBaseContext(), ShowRouteActivity.class);
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
}
