package com.partam.partam.image_loader;

import java.io.File;

import com.partam.partam.AppManager;

public class FileCache {

	private File cacheDir;

	public FileCache()
	{
		// Find the dir to save cached images
//		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
//			cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "JsonParseTutorialCache");
//		else
//			cacheDir = context.getCacheDir();
//		if (!cacheDir.exists())
//			cacheDir.mkdirs();
		cacheDir = new File(AppManager.getInstanse().getFilesPath());
	}

	public File getFile(String url) 
	{
		String filename = AppManager.createFileNameFromUrl(url);
		File f = new File(cacheDir, filename);
		return f;
	}

	public void clear()
	{
		File[] files = cacheDir.listFiles();
		if (files == null)
			return;
		for (File f : files)
			f.delete();
	}
}