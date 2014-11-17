package com.hyperether.getgoing;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class GetGoing extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        Zadatak:
        1. pronadji image buttone po id-u iz xml-a
        2. dodeli svakom od njih onClickListener
        3. u metodi onClick pozovi novu stranicu ShowLocation.class preko Intent-a i startActivity(...) metode
        4. Ta nova stranica ShowLocation.class je napravljena da ucitava show_location.xml
        5. Kad kliknes na dugme prebacice te na novi layout

         */

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.get_going, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
