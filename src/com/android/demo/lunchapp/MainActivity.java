package com.android.demo.lunchapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Random;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
 
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class MainActivity extends Activity {
	private LunchDbAdapter mDbHelper;
	private Button mSearchButton;
	private Button mAddHistoryButton;
	private Button mAddLocationsButton;
	private EditText mSearchForm;
	private TextView mLunchResult;
	private Boolean mGetNewResults;
	private JSONArray mLocations;
	private JSONObject mJSONLocation;
	private int mCuisineId;
	private LocationManager lm;
	private double mLatitude;
	private double mLongitude;
	private final String GOOGLE_PLACES_API_KEY = "AIzaSyCRdRdD5BZoFqxP0-h1hfPfuKbLeSTteBY";
    
	private final LocationListener locationListener = new LocationListener() {
	    public void onLocationChanged(Location location) {
	        mLongitude = location.getLongitude();
	        mLatitude = location.getLatitude();
	    }

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.d("LOCATION", "location status changed:" + status);
		}
	};

	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mDbHelper = new LunchDbAdapter(this);
        mDbHelper.open();           
        setContentView(R.layout.today);
        mLunchResult= (TextView) findViewById(R.id.lunch_result);
        mLunchResult.setText("no result");
        
        mSearchButton = (Button) findViewById(R.id.search_button);
        mAddHistoryButton = (Button) findViewById(R.id.add_to_history_button);
        mAddLocationsButton = (Button) findViewById(R.id.add_to_locations_button);
        mGetNewResults = true;
        mLocations = null;
        mCuisineId = 3;
        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        
        mSearchForm = (EditText) findViewById(R.id.searchText);
        mSearchForm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	mSearchForm.setText("");
            	mGetNewResults = true;
            }
        });           
        
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	try {
            		Boolean error = false;
            	    if (mGetNewResults) {
            	    	String url = getGoogleURL();
            	    	if (url != "") {
                	    	mLocations = getLocations(new URL(url));
            	    		//new GetLocationsTask().execute(new URL(url));
                	    	mGetNewResults = false;
            	    	} else {
            	    		mLunchResult.setText("Error: cannot find GPS coordinates.");
            	    		error = true;
            	    	}
            	    }
         	    
            	    if (!error) {
            	    	Random generator = new Random();
            	    	int r = generator.nextInt(mLocations.length());
            	    	mJSONLocation = mLocations.getJSONObject(r);
            	    	mJSONLocation.put("cuisine_id", Integer.toString(mCuisineId));
            	    	mLunchResult.setText(mJSONLocation.getString("name"));
        	    		mAddHistoryButton.setVisibility(View.VISIBLE);
        	    		mAddLocationsButton.setVisibility(View.VISIBLE);
            	    } else {
        	    		mAddHistoryButton.setVisibility(View.INVISIBLE);
        	    		mAddLocationsButton.setVisibility(View.INVISIBLE);
            	    }
            	    
                } catch (IOException e) {  
                    e.printStackTrace(); 
				} catch (Exception e) {
					Log.e("ERROR", "ERROR IN CODE: " + e.toString());
					e.printStackTrace();
				}
            }
        });
        
        
        mAddHistoryButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	try {
            		if (mJSONLocation != null) {
            			mDbHelper.setTodaysLunch(mJSONLocation);           		
            			mLunchResult.setText(R.string.msg_commit);
            		}
				} catch (Exception e) {
					Log.e("ERROR", "ERROR IN CODE: " + e.toString());
					e.printStackTrace();
				}
            }
        });        
        

    }
    
    public JSONArray getLocations(URL url) {
        HttpURLConnection http = null;
        JSONObject object = null;
        JSONArray locations = null;
        
        try {
	        if (url.getProtocol().toLowerCase().equals("https")) {
	            trustAllHosts();
	                HttpsURLConnection https;
					
						https = (HttpsURLConnection) url.openConnection();
	
	                https.setHostnameVerifier(DO_NOT_VERIFY);
	                http = https;
	        } else {
	                http = (HttpURLConnection) url.openConnection();
	        }
      
  
		    // Read all the text returned by the server
	        BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream(), "UTF-8"));
		    StringBuilder builder = new StringBuilder();
	
		    for (String line = null; (line = reader.readLine()) != null;) {
		        builder.append(line).append("\n");
		    }
		    object = (JSONObject) new JSONTokener(builder.toString()).nextValue();
		    locations = object.getJSONArray("results"); 
		    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  
     	return locations;
	    
    }
  
    public String getGoogleURL() {
    	String url = "";
    	Location gpsLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    	
    	if (gpsLocation != null) {
	    	String domain = "https://maps.googleapis.com/";
	    	String path = "maps/api/place/search/json";
	    	String location = Double.toString(mLatitude) + "," + Double.toString(mLongitude);
	    	String radius = "100";
	    	String types = "food";
	    	String name = mSearchForm.getText().toString();
	    	String sensor = "true";
	    	//String url = "https://maps.googleapis.com/maps/api/place/search/json?location=40.7457,-73.9823&radius=100&types=food&name=thai&sensor=true&key=" + GOOGLE_PLACES_API_KEY;
	    	url = domain + path + "?location=" + location + "&radius=" + radius + "&types=" + types + "&name=" + name + "&sensor=" + sensor + "&key=" + GOOGLE_PLACES_API_KEY; 
    	}
    	
    	return url;
    }
    
    
    // always verify the host - dont check for certificate
    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                    return true;
            }
    };

    /**
     * Trust every server - dont check for any certificate
     */
    private static void trustAllHosts() {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[] {};
                    }

                    public void checkClientTrusted(X509Certificate[] chain,
                                    String authType) throws CertificateException {
                    }

                    public void checkServerTrusted(X509Certificate[] chain,
                                    String authType) throws CertificateException {
                    }
            } };

            // Install the all-trusting trust manager
            try {
                    SSLContext sc = SSLContext.getInstance("TLS");
                    sc.init(null, trustAllCerts, new java.security.SecureRandom());
                    HttpsURLConnection
                                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (Exception e) {
                    e.printStackTrace();
            }
    }
   /*
    private class GetLocationsTask extends AsyncTask<URL, Integer, JSONArray> {
        protected JSONArray doInBackground(URL... urls) {
            return getLocations(urls[0]);
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(JSONArray result) {
        	mLocation = result;
        }
    }
    */
}
