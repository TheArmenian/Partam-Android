package com.partam.partam.customclasses;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.partam.partam.AppManager;
import com.partam.partam.R;
import com.partam.partam.image_loader.ImageLoader;

public class FavoritesAdapter extends BaseAdapter
{
	private ArrayList<JSONObject> arrInfo;
	
	private LayoutInflater inflater = null;
    private ImageLoader imageLoader;
    
    private boolean isEditMode;
	
	public FavoritesAdapter(Activity a, ArrayList<JSONObject> arrInfo, ImageLoader imageLoader) 
	{
		inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.arrInfo = arrInfo;
		this.imageLoader = imageLoader;
	}
	
	public void reloadData(ArrayList<JSONObject> arrInfo, boolean isEditMode)
	{
		this.arrInfo = arrInfo;
		this.isEditMode = isEditMode;
		notifyDataSetChanged();
	}
	
	public boolean isEditMode()
	{
		return isEditMode;
	}
	
	@Override
	public int getCount() 
	{
		return arrInfo != null ? arrInfo.size() : 0;
	}

	@Override
	public JSONObject getItem(int position) 
	{
		return arrInfo.get(position);
	}

	@Override
	public long getItemId(int position) 
	{
		return 0;
	}

    public static class ViewHolder
    {
    	ImageView img;
    	TextView txtName;
    	TextView txtCategory;
    	TextView txtAddress;
    	ImageView drag_handle;
    }
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
    	View v = convertView;     
        ViewHolder holder;
        
        if(convertView == null)
        {
			v = inflater.inflate(R.layout.item_favorite, null);
            holder = new ViewHolder();
			holder.img = (ImageView) v.findViewById(R.id.img);
			holder.txtName = (TextView) v.findViewById(R.id.txtName);
			holder.txtCategory = (TextView) v.findViewById(R.id.txtCategory);
			holder.txtAddress = (TextView) v.findViewById(R.id.txtAddress);
			holder.drag_handle = (ImageView) v.findViewById(R.id.drag_handle);
			v.setTag(holder);
        }
        else
            holder = (ViewHolder) v.getTag();
        
        if (isEditMode) {
			holder.drag_handle.setVisibility(View.VISIBLE);
		}
        else
        {
			holder.drag_handle.setVisibility(View.GONE);
        }

        holder.txtName.setText(AppManager.getJsonString(getItem(position), "name"));
        holder.txtCategory.setText(AppManager.getJsonString(AppManager.getJsonObject(getItem(position), "category"), "title"));
        holder.txtAddress.setText(AppManager.getJsonString(getItem(position), "address"));
        
        String url = AppManager.getJsonString(getItem(position), "picture");
        imageLoader.DisplayImage(url, holder.img);
        
        return v;
	}
}
