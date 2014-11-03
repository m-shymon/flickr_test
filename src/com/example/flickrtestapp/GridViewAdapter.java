package com.example.flickrtestapp;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.example.flickrtestapp.disclrucache.DiskLruImageCache;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class GridViewAdapter extends ArrayAdapter<FlickrPhoto> {
	
	private Context 	mContext;
	private int 		mLayoutResourceId;
	
	private ArrayList<FlickrPhoto> 		mPhotos;
	private LruCache<String, Bitmap>	mMemLruCache;
	private DiskLruImageCache			mDiskLruCache;

	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 50; // 50MB
	private static final String DISK_CACHE_SUBDIR = "FlickrTestData";
	
	public GridViewAdapter(Context context, int layoutResourceId,
			ArrayList<FlickrPhoto> data) {
		super(context, layoutResourceId, data);
		
		mLayoutResourceId = layoutResourceId;
		mContext = context;
		mPhotos = data;

		// Get max available VM memory, exceeding this amount will throw an
	    // OutOfMemory exception. Stored in kilobytes as LruCache takes an
	    // int in its constructor.
	    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

	    // Use 1/4th of the available memory for this memory cache.
	    final int cacheSize = maxMemory / 4;
	    mMemLruCache = new LruCache<String, Bitmap>(cacheSize);
	    
	    // Initialize disk cache
	    mDiskLruCache = new DiskLruImageCache(context, DISK_CACHE_SUBDIR, DISK_CACHE_SIZE, Bitmap.CompressFormat.JPEG, 100);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View cell = convertView;
		ViewHolder holder = null;

		if (cell == null) {
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			cell = inflater.inflate(mLayoutResourceId, parent, false);
			holder = new ViewHolder();
			holder.image = (ImageView) cell.findViewById(R.id.grid_cell_image);
			holder.imageTitle = (TextView) cell.findViewById(R.id.grid_cell_title);
			holder.progressBar = (ProgressBar) cell.findViewById(R.id.grid_cell_progress_bar);
			cell.setTag(holder);
		} else {
			holder = (ViewHolder) cell.getTag();
		}

		FlickrPhoto item = (FlickrPhoto) mPhotos.get(position);
		holder.imageTitle.setText(item.title);
		
		DownloadImageTask task = (DownloadImageTask)holder.image.getTag();
		if (task != null)
		{
			task.cancel(true);
			task = null;
			holder.image.setTag(null);
		}
		
		String key = item.toString();
		Bitmap bitmap = (Bitmap)mMemLruCache.get(key);
		if (bitmap != null) {
			holder.image.setVisibility(View.VISIBLE);
			holder.progressBar.setVisibility(View.GONE);
			holder.image.setImageBitmap(bitmap);
		} else {
			holder.image.setVisibility(View.GONE);
			holder.progressBar.setVisibility(View.VISIBLE);
			task = new DownloadImageTask(holder.image, holder.progressBar);
			task.execute(item);
			holder.image.setTag(task);
		}
		
		return cell;
	}

	static class ViewHolder {
		TextView	imageTitle;
		ImageView	image;
		ProgressBar progressBar;
	}
	
	private class DownloadImageTask extends AsyncTask<FlickrPhoto, Void, Bitmap> {

        private final WeakReference<ImageView> mImageViewReference;
        private final WeakReference<ProgressBar> mProgressBarReference;

        public DownloadImageTask(ImageView bmImage, ProgressBar progressBar) {

        	mImageViewReference = new WeakReference<ImageView>(bmImage);
        	mProgressBarReference = new WeakReference<ProgressBar>(progressBar);
        }

        protected Bitmap doInBackground(FlickrPhoto... photos) {
        	FlickrPhoto photo = photos[0];
            Bitmap bitmap = null;
            
            String key = photo.toString();
            if (mDiskLruCache.containsKey(key)) {
            	bitmap = mDiskLruCache.getBitmap(key);
            	Log.i("Flickr", "Got bitmap from disk cache");
            } else {
	            try {
	            	String url = photo.buildPhotoURL();
	                InputStream in = new java.net.URL(url).openStream();
	                bitmap = BitmapFactory.decodeStream(in);
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	            
	            mDiskLruCache.put(key, bitmap);
	            Log.i("Flickr", "Put bitmap to disk cache");
            }
            
            mMemLruCache.put(key, bitmap);
            return bitmap;
        }

        protected void onPostExecute(Bitmap result) {
        	
        	if (isCancelled()) {
        		result = null;
	        }
        	
        	ImageView imageView = this.mImageViewReference.get();
            if( imageView != null ) {
              imageView.setImageBitmap(result);
              imageView.setVisibility(View.VISIBLE);
            }
            
            ProgressBar progressBar = this.mProgressBarReference.get();
            if( progressBar != null ) {
            	progressBar.setVisibility(View.GONE);
            }
        }
    }
}
