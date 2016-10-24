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

public class CategoriesAdapter extends BaseAdapter
{
	private ArrayList<ItemCategory> arrInfo;

	private LayoutInflater inflater = null;

	public CategoriesAdapter(Activity a, ArrayList<JSONObject> arr) 
	{
		inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		reloadData(arr);
	}

	public void reloadData(ArrayList<JSONObject> arr)
	{		
		if (arr == null) {
			arr = new ArrayList<>();
		}

		arrInfo = new ArrayList<>(arr.size());
		for (int i = 0; i < arr.size(); i++) 
		{
			ItemCategory item = new ItemCategory();
			item.info = arr.get(i);
			item.isSelected = false;
			arrInfo.add(item);
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() 
	{
		return arrInfo != null ? arrInfo.size() : 0;
	}

	@Override
	public ItemCategory getItem(int position) {
		return arrInfo.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public static class ViewHolder
	{
		TextView txtName;
		ImageView imgSelected;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{    	
		View v = convertView;     
		ViewHolder holder;

		if(convertView == null)
		{
			v = inflater.inflate(R.layout.item_menu, null);
			holder = new ViewHolder();
			holder.txtName = (TextView) v.findViewById(R.id.txtName);
			holder.imgSelected = (ImageView) v.findViewById(R.id.imgSelected);
			v.setTag(holder);
		}
		else
			holder = (ViewHolder) v.getTag();

		ItemCategory item = getItem(position);
		holder.txtName.setText(AppManager.getJsonString(item.info, "title"));
        if (!item.isSelected) {
        	holder.imgSelected.setVisibility(View.INVISIBLE);
		}
        else
        {
        	holder.imgSelected.setVisibility(View.VISIBLE);
        }

		return v;
	}
	
	public boolean itemSelected(int position)
	{
		getItem(position).isSelected = !getItem(position).isSelected;
		notifyDataSetChanged();
		return getItem(position).isSelected;
	}

}
