package com.imgur.api3example;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.ClipboardManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.imgur.api3example.util.BitmapUtils;

@SuppressWarnings("deprecation")
public class ChooseImageFragment extends Fragment {
	
	private Bitmap mImagePreviewBitmap;
	private Uri mImageUri;
	private String mImgurUrl;

	private MyImgurUploadTask mImgurUploadTask;
	private int mImgurUploadStatus;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.choose_image_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
        if (mImagePreviewBitmap != null) {
        	((ImageView) getView().findViewById(R.id.choose_image_preview)).setImageBitmap(mImagePreviewBitmap);
        	if (mImageUri != null && mImgurUrl == null)
        		new MyImgurUploadTask(mImageUri).execute();
        }
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (savedInstanceState != null) {
			mImgurUrl = savedInstanceState.getString("imgurUrl");
			mImgurUploadStatus = savedInstanceState.getInt("imgurUploadStatus");
			mImageUri = (Uri) savedInstanceState.getParcelable("imageUri");
		}
		
		if (mImgurUploadStatus != 0) {
			// update the TextView
			setImgurUploadStatus(mImgurUploadStatus);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("imgurUrl", mImgurUrl);
		outState.putInt("imgurUploadStatus", mImgurUploadStatus);
		outState.putParcelable("imageUri", mImageUri);
	}

	private class MyImgurUploadTask extends ImgurUploadTask {
		public MyImgurUploadTask(Uri imageUri) {
			super(imageUri, getActivity());
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (mImgurUploadTask != null) {
				boolean cancelled = mImgurUploadTask.cancel(false);
				if (!cancelled)
					this.cancel(true);
			}
			mImgurUploadTask = this;
			mImgurUrl = null;
			getView().findViewById(R.id.choose_image_button).setEnabled(false);
			setImgurUploadStatus(R.string.choose_image_upload_status_uploading);
		}
		@Override
		protected void onPostExecute(String imageId) {
			super.onPostExecute(imageId);
			mImgurUploadTask = null;
			if (imageId != null) {
				mImgurUrl = "http://imgur.com/" + imageId;
				setImgurUploadStatus(R.string.choose_image_upload_status_success);
				if (isResumed()) {
					getView().findViewById(R.id.imgur_link_layout).setVisibility(View.VISIBLE);
					((TextView) getView().findViewById(R.id.link_url)).setText(mImgurUrl);
				}
			} else {
				mImgurUrl = null;
				setImgurUploadStatus(R.string.choose_image_upload_status_failure);
				if (isResumed()) {
					getView().findViewById(R.id.imgur_link_layout).setVisibility(View.GONE);
					if (isVisible()) {
						((ImageView) getView().findViewById(R.id.choose_image_preview)).setImageBitmap(null);
						Toast.makeText(getActivity(), R.string.imgur_upload_error, Toast.LENGTH_LONG).show();
					}
				}
			}
			if (isVisible())
				getView().findViewById(R.id.choose_image_button).setEnabled(true);
		}
	}
	
	private void setImgurUploadStatus(int stringResId) {
		mImgurUploadStatus = stringResId;
		if (getView() != null) {
			TextView status = (TextView) getView().findViewById(R.id.choose_image_upload_status);
			if (stringResId > 0) {
				status.setVisibility(View.VISIBLE);
				status.setText(stringResId);
			} else {
				status.setVisibility(View.GONE);
			}
		}
	}

	void setImage(Uri imageUri) {
		if (mImagePreviewBitmap != null) {
			mImagePreviewBitmap.recycle();
		}

		mImageUri = imageUri;
		mImagePreviewBitmap = BitmapUtils.decodeSampledBitmapFromUri(imageUri, 400, 400);
		if (getView() != null) {
			((ImageView) getView().findViewById(R.id.choose_image_preview)).setImageBitmap(mImagePreviewBitmap);
			new MyImgurUploadTask(imageUri).execute();
		}
	}
	
	private void resetFields() {
		mImageUri = null;
		// TODO
	}
	
	void copyLink(View view) {
		ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
		if (view.getId() == R.id.copy_link) {
			clipboardManager.setText(mImgurUrl);
		}
		Toast.makeText(getActivity(), R.string.copied_link, Toast.LENGTH_SHORT).show();
	}
	
}
