package com.partam.partam.customclasses;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.partam.partam.AppManager;
import com.partam.partam.image_loader.Utils;

public class WebImage extends AsyncTask<Void, Void, Bitmap>
{
	ImageView img;
	String url;

	public WebImage(ImageView img, String url)
	{
		this.img = img;
		if (url != null)
		{
			this.url = url;
		}
		else
		{
			this.url = "";
		}
	}
	
	@Override
	protected Bitmap doInBackground(Void... params)
	{
        return getBitmap();
	}

	@Override
	protected void onPostExecute(Bitmap result) 
	{
		if (result != null)
		{
			img.setBackgroundColor(Color.TRANSPARENT);
			img.setImageBitmap(result);
		}
		super.onPostExecute(result);
	}
	
	private Bitmap getBitmap()
	{
		String image = AppManager.createFileNameFromUrl(url);
        String filesPath = AppManager.getInstanse().getFilesPath();
		File f = new File(new File(filesPath), image);

		//from SD cache
		Bitmap b = decodeFile(f);
		if(b != null)
		{
			return b;
		}

		//from web
		try {
			InputStream is = new URL(url.replace(" ", "%20")).openStream();
			OutputStream os = new FileOutputStream(f);
			Utils.CopyStream(is, os);
			os.close();
			b = decodeFile(f);
			return b;
		} catch (Exception ex){
			ex.printStackTrace();
			return null;
		}
	}
	
	//decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(File f)
	{
		try {
			//decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f),null,o);

			//Find the correct scale value. It should be the power of 2.
			final int REQUIRED_SIZE = 200;
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while(true){
				if(width_tmp/2 < REQUIRED_SIZE || height_tmp/2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale++;
			}

			//decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} 
		catch (Exception e)
		{
			Log.e("myLogs", "Exception in WebImage = " + e.getMessage());
		}
		return null;
	}
	
}
