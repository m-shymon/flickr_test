package com.example.flickrtestapp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.GridView;
import android.widget.TextView;


public class MainActivity extends Activity {

	private static final String urlQueryString="https://api.flickr.com/services/rest/?method=flickr.photos.getRecent"
										+ "&api_key=6762531119f38becf03d668c030ab0dc&per_page=200&format=rest";
	
	private GridView		mGridView = null;
	private GridViewAdapter mGridViewAdapter = null;
	private ProgressDialog 	mProgressDialog = null;
	private TextView		mMessageView = null;
	
	private ArrayList<FlickrPhoto> mPhotos = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mPhotos = new ArrayList<FlickrPhoto>();
        
        mGridView = (GridView) findViewById(R.id.grid_view);
        mGridViewAdapter = new GridViewAdapter(this, R.layout.grid_cell, mPhotos);
		mGridView.setAdapter(mGridViewAdapter);
		
		mMessageView = (TextView) findViewById(R.id.msg_view);
		
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setMessage("Connect with Flickr server...");
        mProgressDialog.show();

        new RequestPhotoTask().execute();
    }
    
	private class RequestPhotoTask extends AsyncTask<Void, Void, Boolean> {

        public RequestPhotoTask() {
        }

		@Override
		protected Boolean doInBackground(Void... params) {
			InputStream in = null;
			  
	        // HTTP Get
	        try {
	  
	            URL url = new URL(urlQueryString);
	            URLConnection urlConnection = url.openConnection();
	  
	            if ( urlConnection != null) {
		            in = new BufferedInputStream(urlConnection.getInputStream());
	            }
	         } catch (Exception e ) {
	  
	            System.out.println(e.toString());
	            return false;
	         }
	  
	        // Parse XML
	        FlickrXMLParser parser = new FlickrXMLParser();
	        
	        try {
				mPhotos.addAll(parser.parseXML(in));
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	        
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mProgressDialog.dismiss();
            
            if (result) {
            	mGridViewAdapter.notifyDataSetChanged();
            } else {
            	mGridView.setVisibility(View.GONE);
            	mMessageView.setVisibility(View.VISIBLE);
            	mMessageView.setText("Couldn't connect to server. Check connection and start app again.");
            }
		}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
