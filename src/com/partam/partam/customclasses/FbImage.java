package com.partam.partam.customclasses;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.partam.partam.AppManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class FbImage extends AsyncTask<Void, Void, Bitmap>
{
	String url;
	
	public interface FbImageListener
	{
		public void donwloadComlete();
	}
	
	private FbImageListener listener = null;
	public void setOnFbImageEventListener(final FbImageListener listener)
	{
		this.listener = listener;
	}
	
	public FbImage(String url)
	{
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
		InputStream is = null;
		String json = "";

		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httppost = new HttpGet(url);

		try {
			httppost.addHeader("Content-Type", "application/x-www-form-urlencoded");
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity httpEntity = response.getEntity();
			is = httpEntity.getContent();
		}catch (Exception e) {
			e.printStackTrace();
		}

		json  = getString(is);
		try {
			JSONObject obj = new JSONObject(json);
			url = obj.getJSONObject("data").getString("url");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Bitmap bmp =  getBitmap(url);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] imageData = bos.toByteArray();
        PartamHttpClient client = PartamHttpClient.getInstance();
        client.changeUserAvatar(imageData);
        client.loadUserInfo();
        AppManager.getInstanse().save();
		return bmp;
	}

	@Override
	protected void onPostExecute(Bitmap result) 
	{
		super.onPostExecute(result);
		listener.donwloadComlete();
	}
	
	private Bitmap getBitmap(String src)
	{
		try {
			URL url = new URL(src);
			Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
			return bmp;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String getString(InputStream is) {
		String str = "";
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			str = sb.toString();
			return str;
		} catch (Exception e) {}

		return str;
	}
}
