/*
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.demo.lunchapp;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class LunchDbAdapter {

	public static final String TABLE_LOCATIONS = "locations";
	public static final String TABLE_HISTORY = "history";
    public static final String KEY_LOCATIONS_ROWID = "_id";
    public static final String KEY_LOCATIONS_NAME = "name";
    public static final String KEY_LOCATIONS_GOOGLE_ID = "google_id";    
    public static final String KEY_HISTORY_ROWID = "_id";
    public static final String KEY_HISTORY_LOCATION_ID = "location_id";
    public static final String KEY_HISTORY_DATE = "date";

    //private static final String TAG = "LunchDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
        "create table locations ( _id integer primary key autoincrement, " +
        	"name varchar(100) not null, google_id varchar(50) not null);" +
    	"create table history ( _id integer primary key autoincrement, " +
    		"location_id integer not null, date date not null);";

    private static final String DATABASE_NAME = "data";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	/*
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
            */
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public LunchDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public LunchDbAdapter open() throws SQLException {
    	try {
    		mDbHelper = new DatabaseHelper(mCtx);
    		mDb = mDbHelper.getWritableDatabase();
    	} catch (Exception e) {
    		// this is the line of code that sends a real error message to the log
    		Log.e("ERROR", "ERROR IN CODE: " + e.toString());
 
    		// this is the line that prints out the location in
    		// the code where the error occurred.
    		e.printStackTrace();    	
    	}
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    /**
     * Delete the location with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    
    
    public boolean deleteLocation(long rowId) {

    	mDb.delete(TABLE_LOCATIONS, KEY_LOCATIONS_ROWID + "=" + rowId, null);
    	mDb.delete(TABLE_HISTORY, KEY_HISTORY_ROWID + "=" +rowId, null);
        return true;
    }
    
    public boolean deleteHistory(long rowId) {

    	mDb.delete(TABLE_HISTORY, KEY_HISTORY_ROWID + "=" +rowId, null);
        return true;
    }    
    
    /**
     * Return a Cursor over the list of all notes in the database
     * 
     * @return Cursor over all notes
     */
    public Cursor fetchAllLocations() {
        return mDb.query(TABLE_LOCATIONS, new String[] {KEY_LOCATIONS_ROWID, KEY_LOCATIONS_NAME}
        , KEY_LOCATIONS_NAME + "<> ''", null, null, null, null);
    }
    
    public Cursor fetchAllHistory() {
    	try {
	    	String joinSQL = TABLE_HISTORY + " INNER JOIN " + TABLE_LOCATIONS + " ON ";
	    	joinSQL += KEY_HISTORY_ROWID + " = " + KEY_LOCATIONS_ROWID;
	    	String rawSQL = "SELECT history._id _id, locations.name locationName, history.date historyDate FROM history INNER JOIN locations ON locations._id = history.location_id"; 
	    	return mDb.rawQuery(rawSQL, null);
    	} catch (Exception e) 
	    {
			// this is the line of code that sends a real error message to the log
			Log.e("ERROR", "ERROR IN CODE: " + e.toString());
	
			// this is the line that prints out the location in
			// the code where the error occurred.
			e.printStackTrace();
			return null;
	    }
    }    

    /**
     */
    public Boolean addHistory(String name, String googleId) {
        ContentValues initialValues = new ContentValues();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
        Date date = new Date();
        String today = dateFormat.format(date);
        long locationId;
		try {
			//Update locations table (if needed)
			locationId = this.addLocation(name, googleId);
			
	        //Update history table
			Cursor c = mDb.rawQuery("SELECT COUNT (*) AS count FROM history WHERE date = '" + today + "'" , null);
			c.moveToFirst();
			if (c.getInt(c.getColumnIndex("count")) == 0) { //History doesn't exist for today, let's add it
				initialValues.clear();
		        initialValues.put(KEY_HISTORY_LOCATION_ID, locationId);
		        initialValues.put(KEY_HISTORY_DATE, today);
		        mDb.insert(TABLE_HISTORY, null, initialValues);
			} else { //just do an update
				ContentValues cv = new ContentValues();
				cv.put(KEY_HISTORY_LOCATION_ID, locationId);
				mDb.update(TABLE_HISTORY, cv, "date = '" + today + "'", null);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
        
        return true;
    }    
    

    /**
     * Create a new note using the title and body provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title the title of the note
     * @param body the body of the note
     * @return rowId or -1 if failed
     */
    public long addLocation(String name, String googleId) {
    	long id;
    	ContentValues initialValues = new ContentValues();
        
		//Update locations table
		Cursor c = mDb.rawQuery("SELECT _id FROM locations WHERE google_id = '" + googleId +"'", null);
		
        c.moveToFirst();
        if (c.getCount() == 0) { //Location doesn't exist yet, let's add it
        	initialValues.clear();
        	initialValues.put(KEY_LOCATIONS_NAME, name);
        	initialValues.put(KEY_LOCATIONS_GOOGLE_ID, googleId);
        	id = mDb.insert(TABLE_LOCATIONS, null, initialValues);
        } else { 
        	id = c.getInt(c.getColumnIndex("_id"));
        }
        
        return id;
    }    
   
}
