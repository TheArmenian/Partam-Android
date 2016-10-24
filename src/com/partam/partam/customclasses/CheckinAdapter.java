package com.partam.partam.customclasses;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.partam.partam.AppManager;
import com.partam.partam.MainActivity;
import com.partam.partam.image_loader.ImageLoader;

import com.partam.partam.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CheckinAdapter extends BaseAdapter {

	long pointNumber;
	ImageLoader imageLoader;
	JSONArray jsArray;
	ArrayList<JSONObject> jsonInfoArray = new ArrayList<JSONObject>();
	private LayoutInflater inflater = null;
	Context context;
	MainActivity mainActivity;
	public String categoryName;
	public JSONObject webInfo;
	public View viewLoader;
	View viewLoaqderForImage;
	public TextView checkinCounts;
	JSONObject jsonCheckinObject;

	public CheckinAdapter(Activity activity, ArrayList<JSONObject> jsonInfoArray)
	{
		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		this.jsArray = jsArray;
		imageLoader = new ImageLoader();
		mainActivity = (MainActivity) activity;
//		for(int i = 0; i < jsArray.length(); i++)
//			jsonInfoArray.add(AppManager.getJsonObject(jsArray, i));
		this.jsonInfoArray = jsonInfoArray;
//		Collections.reverse(this.jsonInfoArray);
	}

	public ImageLoader getImageLoader()
	{
		return imageLoader;
	}

	//	public void reloadData(JSONArray arrInfo)
	//	{
	//		this.jsArray = arrInfo;
	//		notifyDataSetChanged();
	//	}
	public void reloadData(ArrayList<JSONObject> arrInfo)
	{
		jsonInfoArray = arrInfo;
		notifyDataSetChanged();
	}


	@Override
	public int getCount() {

		return jsonInfoArray != null ? jsonInfoArray.size() : 0;
	}

	public ArrayList<JSONObject> getArrInfo() 
	{
		return jsonInfoArray != null ? jsonInfoArray : new ArrayList<JSONObject>();
	}

	@Override
	public JSONObject getItem(int position) {
		return  jsonInfoArray.get(position); //AppManager.getJsonObject(jsArray, position);
	}

	@Override
	public long getItemId(int position) {
		return AppManager.getJsonLong(getItem(position), "id");  //AppManager.getJsonLong(AppManager.getJsonObject(jsArray, position), "id");
	}


	public static class ViewHolder
	{
		ImageView img_user_frame;
		public ImageView img_selfie;
		TextView txt_user_name;
		TextView txt_user_hometown;
		TextView txt_date;
		ImageButton delete_checkin;
		ProgressBar progressBar;
	}

	@SuppressLint({ "SimpleDateFormat", "NewApi" })
	@Override
	public  View getView(int position, View convertView, ViewGroup parent) {
		Log.e("Info", "getView!!!!!!!!!!!!!!!!!!!!>>>>>>>>>>>>>>>"); 
		View v = convertView;     
		ViewHolder holder;

		int loggedInUserId = AppManager.getJsonInt(AppManager.getJsonObject(AppManager.getInstanse().userInfo, "user"), "id");

		if(convertView == null)
		{
			v = inflater.inflate(R.layout.checkin_item, null);
			holder = new ViewHolder();

			holder.txt_date = (TextView) v.findViewById(R.id.txt_date); 
			holder.img_selfie = (ImageView) v.findViewById(R.id.img_selfie); 
			holder.img_user_frame = (ImageView) v.findViewById(R.id.img_user_photo);
			holder.txt_user_name = (TextView) v.findViewById(R.id.txt_user_name);
			holder.txt_user_hometown = (TextView) v.findViewById(R.id.txt_user_hometown);
			holder.delete_checkin = (ImageButton) v.findViewById(R.id.btn_delete_checkin);
			holder.progressBar = (ProgressBar) v.findViewById(R.id.my_bar);

			v.setTag(holder);
		}else 
			holder = (ViewHolder) v.getTag();

		jsonCheckinObject = getItem(position); //AppManager.getJsonObject(jsArray, position); 
		JSONObject jsonUserObject = AppManager.getJsonObject(jsonCheckinObject, "user");
		int selfieOwnerId = AppManager.getJsonInt(jsonUserObject, "id");
		final int checkinId = AppManager.getJsonInt(jsonCheckinObject, "id");
		final int pointId = AppManager.getJsonInt(webInfo, "id");


		holder.txt_user_name.setText(AppManager.getJsonString(jsonUserObject, "displayname"));
		holder.txt_user_hometown.setText(AppManager.getJsonString(jsonUserObject, "location"));
		String userImageUrl = AppManager.getInstanse().getUrlForSmallProfPhoto(AppManager.getJsonString(jsonUserObject, "picture"));
		imageLoader.DisplayImage(userImageUrl, holder.img_user_frame);

		String selfieUrl = AppManager.getInstanse().getUrlForSelfiePhoto(AppManager.getJsonString(jsonCheckinObject, "image"));
		imageLoader.showDefaultImage(false);
		imageLoader.DisplayImageSelfie(selfieUrl, holder.img_selfie);


		//		holder.img_selfie.invalidate();

		if(loggedInUserId == selfieOwnerId)
			holder.delete_checkin.setVisibility(View.VISIBLE);
		else
			holder.delete_checkin.setVisibility(View.INVISIBLE);

		holder.delete_checkin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				viewLoader.setVisibility(View.VISIBLE);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
				{
					new DeleteCheckin().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, checkinId, pointId);
				} 
				else
				{
					new DeleteCheckin().execute(checkinId, pointId);
				}

			}
		});

		//		android.net.Uri auri = Uri.parse(selfieUrl);
		//		setRoundedImage(mainActivity.getApplicationContext(), auri, holder.img_selfie);


		String targetDate = AppManager.getJsonString(jsonCheckinObject, "created");
		String edtitStr = targetDate.substring(0, 10);

		DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat outputFormat = new SimpleDateFormat("MMM d");

		try
		{
			Date parsed = inputFormat.parse(edtitStr);
			String outputText = outputFormat.format(parsed);
			Log.e("Info", "outputText = " + outputText);
			holder.txt_date.setText(outputText);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}

//				reloadData(jsonInfoArray);
		return v;
	}

	//	public static void setRoundedImage(Context context, Uri url, ImageView imageView) {
	//        Transformation transformation =  new RoundedTransformationBuilder()
	//                //.borderColor(Color.WHITE)
	//                //.borderWidthDp(2)
	//                .cornerRadiusDp(10)
	//                .oval(false)
	//                .build();
	//
	//        Picasso.with(context)
	//                .load(url)
	////                .placeholder(placeholder)
	////                .error(placeholder)
	//                .fit()
	//                .transform(transformation)
	//                .into(imageView);
	//    }

	private class DeleteCheckin extends AsyncTask<Integer, Void, Void>
	{
		@Override
		protected Void doInBackground(Integer... params) {
			PartamHttpClient mHttpClient = PartamHttpClient.getInstance();
			mHttpClient.deleteCheckin(params[0], params[1]);
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			Log.e("Info", "AppManager.getInstanse().checkinsArray.length = " + AppManager.getInstanse().checkinsArray.length());
			jsonInfoArray.clear();
			//for(int i = 0; i < jsArray.length(); i++)
			for(int i = 0; i < AppManager.getInstanse().checkinsArray.length(); i++)
				jsonInfoArray.add(AppManager.getJsonObject(AppManager.getInstanse().checkinsArray, i));
			Collections.reverse(jsonInfoArray);
			reloadData(jsonInfoArray);
			checkinCounts.setText(String.valueOf(AppManager.getInstanse().checkinsArray.length()));
			viewLoader.setVisibility(View.GONE);
		}
	}
	
	

}
