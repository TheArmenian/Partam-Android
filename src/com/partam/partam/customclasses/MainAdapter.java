package com.partam.partam.customclasses;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.partam.partam.AppManager;
import com.partam.partam.R;
import com.partam.partam.image_loader.ImageLoader;

public class MainAdapter extends BaseAdapter
{
	private JSONArray arrInfo;
	
	private LayoutInflater inflater = null;
    private ImageLoader imageLoader; 
	
	public MainAdapter(Activity a, JSONArray arrInfo) 
	{
		inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.arrInfo = arrInfo;
        imageLoader = new ImageLoader();
	}
	
	public ImageLoader getImageLoader()
	{
		return imageLoader;
	}
	
	public void reloadData(JSONArray arrInfo)
	{
		this.arrInfo = arrInfo;
		notifyDataSetChanged();
	}
	
	public JSONArray getArrInfo() 
	{
		return arrInfo != null ? arrInfo : new JSONArray();
	}
	
	@Override
	public int getCount() 
	{
		return arrInfo != null ? arrInfo.length() : 0;
	}

	@Override
	public JSONObject getItem(int position) {
		return AppManager.getJsonObject(arrInfo, position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

    public static class ViewHolder
    {
    	ImageView imgProfile;
    	View viewRed;
    	ImageView imgPromoted;
    	ImageView imgComment;
    	TextView txtCommentCount;
    	TextView txtName;
    	TextView txtCity;
    }
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
    	View v = convertView;     
        ViewHolder holder;
        
        if(convertView == null)
        {
			v = inflater.inflate(R.layout.item_main, null);
            holder = new ViewHolder();
			holder.imgProfile = (ImageView) v.findViewById(R.id.imgProfile);
			LayoutParams params = (LayoutParams) holder.imgProfile.getLayoutParams();
			params.height = AppManager.getInstanse().screenWidth;
			params.width = AppManager.getInstanse().screenWidth;
			holder.imgProfile.setLayoutParams(params);
			holder.viewRed = v.findViewById(R.id.viewRed);
			holder.imgPromoted = (ImageView) v.findViewById(R.id.imgPromoted);
			holder.imgComment = (ImageView) v.findViewById(R.id.imgComment);
			holder.txtCommentCount = (TextView) v.findViewById(R.id.txtCommentCount);
			holder.txtName = (TextView) v.findViewById(R.id.txtName);
			holder.txtCity = (TextView) v.findViewById(R.id.txtCity);
			v.setTag(holder);
        }
        else
            holder = (ViewHolder) v.getTag();

        if (AppManager.getJsonBoolean(getItem(position), "promoted")) 
        {
			holder.viewRed.setVisibility(View.VISIBLE);
			holder.imgPromoted.setVisibility(View.VISIBLE);
		}
        else
        {
			holder.viewRed.setVisibility(View.GONE);
			holder.imgPromoted.setVisibility(View.GONE);
        }
        
        holder.txtCommentCount.setText(AppManager.getJsonString(getItem(position), "comment_count"));
        if (AppManager.getJsonInt(getItem(position), "comment_count") > 0)
        {
        	holder.imgComment.setImageResource(R.drawable.icon_comment);
		}
        else
        {
        	holder.imgComment.setImageResource(R.drawable.icon_comment_empty);
        }
        
        holder.txtName.setText(AppManager.getJsonString(getItem(position), "name"));
        holder.txtCity.setText(AppManager.getJsonString(getItem(position), "city"));
        
        
        String url = AppManager.getJsonString(getItem(position), "picture");
        imageLoader.DisplayImage(url, holder.imgProfile);
		
		return v;
	}
}
