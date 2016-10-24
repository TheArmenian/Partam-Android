package com.partam.partam.customclasses;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.partam.partam.MediaFragment;

public class DetailPagerAdapter extends FragmentStatePagerAdapter
{
	private ArrayList<MediaInfo> arrInfo;
	
	public DetailPagerAdapter(FragmentManager fm, ArrayList<MediaInfo> arrInfo)
	{
		super(fm);
		this.arrInfo = arrInfo;
	}

	@Override
	public Fragment getItem(int position) 
	{
		MediaFragment frag = new MediaFragment();
		frag.mediaInfo = arrInfo.get(position);
        return frag;
	}

	public MediaInfo getMediaInfo(int position)
	{
		return arrInfo.get(position);
	}
	
	@Override
	public int getCount() 
	{
		return arrInfo != null ? arrInfo.size() : 0;
	}
	
	@Override
	public int getItemPosition(Object object) {
		return  POSITION_NONE;
	}
}
