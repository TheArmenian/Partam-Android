package com.partam.partam;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.partam.partam.customclasses.MediaInfo;

public class MediaFragment extends Fragment 
{
	public MediaInfo mediaInfo;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    	View v = inflater.inflate(R.layout.fragment_media, null);
    	if (mediaInfo.isVideo) 
    	{
			View btnPlay = v.findViewById(R.id.btnPlay);
			btnPlay.setVisibility(View.VISIBLE);
			btnPlay.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					String videoId = mediaInfo.videoID;
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + videoId));
					intent.putExtra("force_fullscreen",true); 
					startActivity(intent);
//					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId)); 
//					intent.putExtra("VIDEO_ID", videoId); 
//					intent.putExtra("force_fullscreen", true);
//					startActivity(intent);
				}
			});
		}
    	
    	ImageView img = (ImageView) v.findViewById(R.id.img);
    	mediaInfo.loader.DisplayImage(mediaInfo.imageUrl, img);

        return v;
    }
}
