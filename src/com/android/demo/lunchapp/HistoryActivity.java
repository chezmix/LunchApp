package com.android.demo.lunchapp;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class HistoryActivity extends ListActivity {
    private LunchDbAdapter mDbHelper;
    private static final int DELETE_ID = Menu.FIRST;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new LunchDbAdapter(this);
        mDbHelper.open();        
        fillData();
        setContentView(R.layout.history);
        registerForContextMenu(getListView());
   	}
    
    private void fillData() {
    	try {
	        // Get all of the rows from the database and create the item list
	        Cursor mNotesCursor = mDbHelper.fetchAllHistory();
	        startManagingCursor(mNotesCursor);
	
	        // Create an array to specify the fields we want to display in the list (only TITLE)
	        String[] from = new String[]{"locationName", "historyDate"};
	
	        // and an array of the fields we want to bind those fields to (in this case just text1)
	        int[] to = new int[]{R.id.historylocationtext, R.id.historydatetext};
	
	        // Now create a simple cursor adapter and set it to display
	        SimpleCursorAdapter locations = 
	            new SimpleCursorAdapter(this, R.layout.history_row, mNotesCursor, from, to);
	        setListAdapter(locations);
    	} catch (Exception e) 
    	{
			// this is the line of code that sends a real error message to the log
			Log.e("ERROR", "ERROR IN CODE: " + e.toString());
	
			// this is the line that prints out the location in
			// the code where the error occurred.
			e.printStackTrace();        	
    	}
    } //filldata()
    
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
                mDbHelper.deleteHistory(info.id);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }    
        


}