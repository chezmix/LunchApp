package com.android.demo.lunchapp;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
//import android.view.Menu;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
//import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
//import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class LocationsActivity extends ListActivity {

	private static final int DELETE_ID = Menu.FIRST;
	private LunchDbAdapter mDbHelper;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new LunchDbAdapter(this);
        mDbHelper.open();
        fillData();
        setContentView(R.layout.locations);
        registerForContextMenu(getListView());
    }
    
    private void fillData() {
        // Get all of the rows from the database and create the item list
        Cursor mNotesCursor = mDbHelper.fetchAllLocations();
        startManagingCursor(mNotesCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{LunchDbAdapter.KEY_LOCATIONS_NAME};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.locationtext};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter locations = 
            new SimpleCursorAdapter(this, R.layout.locations_row, mNotesCursor, from, to);

        setListAdapter(locations);
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.menu_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteLocation(info.id);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus){
    	super.onWindowFocusChanged(hasFocus);
    	
    	if (hasFocus) {
    		Log.d("DEBUG", "Focus gained");
    	}
    }

    
    
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
//        menu.add(0, ADD_ID, 0, R.string.menu_add);
//        return true;
//    }
//
//    @Override
//    public boolean onMenuItemSelected(int featureId, MenuItem item) {
//        switch(item.getItemId()) {
//            case ADD_ID:
//                addLocation();
//                return true;
//        }
//
//        return super.onMenuItemSelected(featureId, item);
//    }    
}
