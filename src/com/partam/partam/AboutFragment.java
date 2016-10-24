package com.partam.partam;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class AboutFragment extends Fragment
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View v = inflater.inflate(R.layout.fragment_about, null);
		v.findViewById(R.id.btnBack).setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v)
			{
				((MainActivity) getActivity()).removeFragment(AboutFragment.this);
			}
		});
		return v;
	}
}
