package com.partam.partam.image_loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.AvoidXfermode.Mode;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.widget.ImageView;

import com.partam.partam.R;

public class ImageLoader 
{
	public interface ImageLoaderListener
	{
		public void bitmapDownloadDone();
	}
	
	private ImageLoaderListener listener = null;
	
	public void setOnImageLoaderListener(final ImageLoaderListener listener)
	{
		this.listener = listener;
	}
	
	MemoryCache memoryCache = new MemoryCache();
	FileCache fileCache;
	private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
	ExecutorService executorService;
	// Handler to display images in UI thread
	Handler handler = new Handler();

	boolean scale = false;
	boolean showDefaultImage = true;

	public ImageLoader() 
	{
		fileCache = new FileCache();
		executorService = Executors.newFixedThreadPool(5);
	}

	public void setScale(boolean scale)
	{
		this.scale = scale;
	}
	
	public void showDefaultImage(boolean show)
	{
		showDefaultImage = show;
	}
	
	final int stub_id = R.drawable.default_profile_image;
	

	public void DisplayImage(String url, ImageView imageView) 
	{
		imageViews.put(imageView, url);
		Bitmap bitmap = memoryCache.get(url);
		if (bitmap != null)
			imageView.setImageBitmap(bitmap);
		else {
			queuePhoto(url, imageView);
			if (showDefaultImage)
			{
				imageView.setImageResource(stub_id);
			}
		}
	}
	
	public void DisplayImageSelfie(String url, ImageView imageView) 
	{
		imageViews.put(imageView, url);
		Bitmap bitmap = memoryCache.get(url);
		
		if (bitmap != null)
			{
			imageView.setImageBitmap(getRoundedCornerBitmap(bitmap));
			imageView.invalidate();
			}
		else {
			queuePhoto(url, imageView);
			if (showDefaultImage)
			{
				imageView.setImageResource(stub_id);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
	    Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
	        bitmap.getHeight(), Config.ARGB_8888);
	    int w = bitmap.getWidth();
	    int h = bitmap.getHeight();
	    Canvas canvas = new Canvas(output);
	 
	    final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	    final RectF rectF = new RectF(0, 0, w, h); 

	    canvas.drawRoundRect(rectF, 30, 30, paint);
	    
	    paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
	    canvas.drawBitmap(bitmap, null, rectF, paint);

	    /**
	     * here to define your corners, this is for left bottom and right bottom corners
	     */
	    final Rect clipRect = new Rect(0, 30, w, h);
	    paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC));
	    canvas.drawRect(clipRect, paint);

	    paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
	    canvas.drawBitmap(bitmap, null, rectF, paint);
	    
	    return output;
	  }
	

	private void queuePhoto(String url, ImageView imageView) {
		PhotoToLoad p = new PhotoToLoad(url, imageView);
		executorService.submit(new PhotosLoader(p));
	}

	private Bitmap getBitmap(String url) 
	{
		File f = fileCache.getFile(url);

		Bitmap b = decodeFile(f);
		if (b != null)
			return b;

		// Download Images from the Internet
		try {
			Bitmap bitmap = null;
			URL imageUrl = new URL(url.replace(" ", "%20"));
			HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(true);
			InputStream is = conn.getInputStream();
			OutputStream os = new FileOutputStream(f);
			Utils.CopyStream(is, os);
			os.close();
			conn.disconnect();
			bitmap = decodeFile(f);
			return bitmap;
		} catch (Throwable ex) {
			ex.printStackTrace();
			if (ex instanceof OutOfMemoryError)
				memoryCache.clear();
			return null;
		}
	}

	// Decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(File f) 
	{
		if (!scale) 
		{
			return BitmapFactory.decodeFile(f.getPath());
		}
		try {
			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			FileInputStream stream1 = new FileInputStream(f);
			BitmapFactory.decodeStream(stream1, null, o);
			stream1.close();

			// Find the correct scale value. It should be the power of 2.
			// Recommended Size 512
			final int REQUIRED_SIZE = 128;
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE
						|| height_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			// Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			FileInputStream stream2 = new FileInputStream(f);
			Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
			stream2.close();
			return bitmap;
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Task for the queue
	private class PhotoToLoad {
		public String url;
		public ImageView imageView;

		public PhotoToLoad(String u, ImageView i) {
			url = u;
			imageView = i;
		}
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run() {
			try {
				if (imageViewReused(photoToLoad))
					return;
				Bitmap bmp = getBitmap(photoToLoad.url);
				memoryCache.put(photoToLoad.url, bmp);
				if (imageViewReused(photoToLoad))
					return;
				BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
				handler.post(bd);
			} catch (Throwable th) {
				th.printStackTrace();
			}
		}
	}

	boolean imageViewReused(PhotoToLoad photoToLoad) {
		String tag = imageViews.get(photoToLoad.imageView);
		if (tag == null || !tag.equals(photoToLoad.url))
			return true;
		return false;
	}

	// Used to display bitmap in the UI thread
	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		PhotoToLoad photoToLoad;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
			bitmap = b;
			photoToLoad = p;
		}

		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			if (bitmap != null)
			{
				photoToLoad.imageView.setImageBitmap(bitmap);
				if (listener != null)
				{
					listener.bitmapDownloadDone();
				}
			}
			else
			{
				if (showDefaultImage)
				{
					photoToLoad.imageView.setImageResource(stub_id);
				}
			}
		}
	}

	public void clearCache()
	{
		memoryCache.clear();
		fileCache.clear();
	}
	
	public void clearMemoryCache()
	{
		memoryCache.clear();
	}
}