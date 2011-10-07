package com.android.demo.lunchapp;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class LunchApp extends TabActivity {
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	try {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.main);
	
	        Resources res = getResources(); // Resource object to get Drawables
	        TabHost tabHost = getTabHost();  // The activity TabHost
	        TabHost.TabSpec spec;  // Reusable TabSpec for each tab
	        Intent intent;  // Reusable Intent for each tab
	        
	        
	        // Create an Intent to launch an Activity for the tab (to be reused)
	        intent = new Intent().setClass(this, MainActivity.class);
	
	        // Initialize a TabSpec for each tab and add it to the TabHost
	        spec = tabHost.newTabSpec("Main").setIndicator("Today",
	                          res.getDrawable(R.drawable.ic_tab_main))
	                      .setContent(intent);
	        tabHost.addTab(spec);
	
	        // Do the same for the other tabs
	        intent = new Intent().setClass(this, HistoryActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        spec = tabHost.newTabSpec("History").setIndicator("History",
	                          res.getDrawable(R.drawable.ic_tab_main))
	                      .setContent(intent);
	        tabHost.addTab(spec);
	
	        intent = new Intent().setClass(this, LocationsActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        spec = tabHost.newTabSpec("Locations").setIndicator("Locations",
	        				res.getDrawable(R.drawable.ic_tab_main))
	        				.setContent(intent);
	        tabHost.addTab(spec);
	
	        tabHost.setCurrentTab(2);
	        
	        
	        /*
	        Spinner spinner = (Spinner) findViewById(R.id.spinner1);
	        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	        		this, R.array.food_array1, android.R.layout.simple_spinner_item);
	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        spinner.setAdapter(adapter);
	        spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
	        */
        
		} catch (Exception e)
		{
			// this is the line of code that sends a real error message to the log
			Log.e("ERROR", "ERROR IN CODE: " + e.toString());
	     		// this is the line that prints out the location in
			// the code where the error occurred.
			e.printStackTrace();
		}        
    }
    
    public class MyOnItemSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent,
            View view, int pos, long id) {
          Toast.makeText(parent.getContext(), "The choice is " +
              parent.getItemAtPosition(pos).toString(), Toast.LENGTH_LONG).show();
        }

        public void onNothingSelected(AdapterView parent) {
          // Do nothing.
        }
    }    
}