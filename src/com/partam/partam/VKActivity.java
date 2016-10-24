package com.partam.partam;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.partam.partam.image_loader.FileCache;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.photo.VKImageParameters;
import com.vk.sdk.api.photo.VKUploadImage;
import com.vk.sdk.dialogs.VKShareDialog;
import com.vk.sdk.util.VKUtil;

public class VKActivity extends FragmentActivity
{
	public static String imgUrl;
	public static String link;
	public static String text;

	/**
	 * Scope is set of required permissions for your application
	 * @see <a href="https://vk.com/dev/permissions">vk.com api permissions documentation</a>
	 */
	private static final String[] sMyScope = new String[] {
        VKScope.WALL,
        VKScope.PHOTOS,
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vk);
		VKUIHelper.onCreate(this);
		VKSdk.initialize(sdkListener, "4553914");
		VKSdk.wakeUpSession();

		String[] fingerprint = VKUtil.getCertificateFingerprint(this, this.getPackageName());
		Log.d("Info", fingerprint[0]);

		if (!VKSdk.isLoggedIn()) 
		{
			VKSdk.authorize(sMyScope, true, false);
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		VKUIHelper.onResume(this);

		if (VKSdk.wakeUpSession()) 
		{
			showShareDialog();
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		VKUIHelper.onDestroy(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		VKUIHelper.onActivityResult(this, requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}

	private final VKSdkListener sdkListener = new VKSdkListener()
	{
		@Override
		public void onCaptchaError(VKError captchaError)
		{
			finish();
		}

		@Override
		public void onTokenExpired(VKAccessToken expiredToken)
		{
			VKSdk.authorize(sMyScope);
		}

		@Override
		public void onAccessDenied(final VKError authorizationError) 
		{
			finish();
		}

		@Override
		public void onReceiveNewToken(VKAccessToken newToken)
		{
		}

		@Override
		public void onAcceptUserToken(VKAccessToken token)
		{
		}
	};

	private void showShareDialog()
	{
		final Bitmap b = getPhoto();
//		VKPhotoArray photos = new VKPhotoArray();
//		photos.add(new VKApiPhoto("photo-47200925_314622346"));
		
		VKShareDialog dialog = new VKShareDialog();
		dialog.setText(text);
//		dialog.setUploadedPhotos(photos);
		dialog.setAttachmentImages(new VKUploadImage[]
		{
				new VKUploadImage(b, VKImageParameters.pngImage())
		});
		dialog.setAttachmentLink("", link);
		dialog.setShareDialogListener(new VKShareDialog.VKShareDialogListener()
		{
			@Override
			public void onVkShareComplete(int postId)
			{
				b.recycle();
				finish();
			}

			@Override
			public void onVkShareCancel() 
			{
				b.recycle();
				finish();
			}
		});
		dialog.show(getSupportFragmentManager(), "VK_SHARE_DIALOG");
	}

	private Bitmap getPhoto() 
	{
		Bitmap b = null;
		b = BitmapFactory.decodeFile(new FileCache().getFile(imgUrl).getPath());
		return b;
	}
}
