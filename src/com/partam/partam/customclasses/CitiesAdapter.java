package com.partam.partam.customclasses;

import java.util.ArrayList;
import java.util.HashMap;

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

public class CitiesAdapter extends BaseAdapter
{
	private ArrayList<JSONObject> arrInfo;
	private ArrayList<HashMap<String, String>> arrSection;
	
	private LayoutInflater inflater = null;
	private int selectedPosition = -1;
	
	public CitiesAdapter(Activity a, ArrayList<JSONObject> arrInfo, ArrayList<HashMap<String, String>> arrSection) 
	{
		inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.arrInfo = arrInfo;
		this.arrSection = arrSection;
	}
	
	public void reloadData(ArrayList<JSONObject> arrInfo, ArrayList<HashMap<String, String>> arrSection)
	{
		this.arrInfo = arrInfo;
		this.arrSection = arrSection;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() 
	{
		return arrInfo != null ? arrInfo.size() : 0;
	}

	@Override
	public JSONObject getItem(int position) {
		return arrInfo.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

    public static class ViewHolder
    {
    	TextView txtSection;
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
			v = inflater.inflate(R.layout.item_city, null);
            holder = new ViewHolder();
            holder.txtSection = (TextView) v.findViewById(R.id.txtSection);
			holder.txtName = (TextView) v.findViewById(R.id.txtName);
			holder.imgSelected = (ImageView) v.findViewById(R.id.imgSelected);
			v.setTag(holder);
        }
        else
            holder = (ViewHolder) v.getTag();
		
        holder.txtName.setText(AppManager.getJsonString(arrInfo.get(position), "city"));
        
        if (position != selectedPosition) {
        	holder.imgSelected.setVisibility(View.INVISIBLE);
		}
        else
        {
        	holder.imgSelected.setVisibility(View.VISIBLE);
        }
        
        String section = showSection(position);
        if (section != null)
		{
        	holder.txtSection.setVisibility(View.VISIBLE);
			holder.txtSection.setText(section);
		}
        else
        {
        	holder.txtSection.setVisibility(View.GONE);
        }
		
		return v;
	}
	
	public boolean itemSelected(int position)
	{
		boolean selected = true;
		
		if (position != selectedPosition) {
			selectedPosition = position;
		}
		else
		{
			selected = false;
			selectedPosition = -1;
		}
		notifyDataSetChanged();
		return selected;
	}
	
	private String showSection(int position)
	{
		for (int i = 0; i < arrSection.size(); i++)
		{
			HashMap<String, String> hm = arrSection.get(i);
			if (Integer.parseInt(hm.get("section")) == position)
			{
				return hm.get("country");
			}
		}
		
		return null;
	}
}
