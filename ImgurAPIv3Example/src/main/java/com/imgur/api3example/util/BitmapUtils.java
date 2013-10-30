package com.imgur.api3example.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.imgur.api3example.ImgurSampleApplication;

/**
 * http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
 */
public class BitmapUtils {
	
	private static final String TAG = BitmapUtils.class.getSimpleName();
	
    public static Bitmap decodeSampledBitmapFromUri(Uri imageUri, int reqWidth, int reqHeight) {
    	Log.d(TAG, "imageUri=" + imageUri);
    	
    	Context context = ImgurSampleApplication.getAppContext();
    	
    	InputStream in;
		try {
			in = context.getContentResolver().openInputStream(imageUri);
		} catch (FileNotFoundException e) {
			Log.w(TAG, "file not found", e);
			return null;
		}
    	
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(in, null, options);
        
        try {
			in.close();
		} catch (IOException e) {
			Log.e(TAG, "error closing InputStream", e);
		}
        try {
			in = context.getContentResolver().openInputStream(imageUri);
		} catch (FileNotFoundException e) {
			Log.w(TAG, "file not found", e);
			return null;
		}
        
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;        // http://stackoverflow.com/questions/7068132/why-would-i-ever-not-use-bitmapfactorys-inpurgeable-option
        options.inInputShareable = false;  // InputStream is not reusable
        try {
        	return BitmapFactory.decodeStream(in, null, options);
        } finally {
        	try {
				in.close();
			} catch (IOException ignore) {}
        }
    }
    
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
    	// Raw height and width of image
    	final int height = options.outHeight;
    	final int width = options.outWidth;
    	int inSampleSize = 1;
    	
    	Log.d(TAG, "height=" + height + " width=" + width);

    	if (height > reqHeight || width > reqWidth) {
    		if (width > height) {
    			inSampleSize = Math.round((float)height / (float)reqHeight);
    		} else {
    			inSampleSize = Math.round((float)width / (float)reqWidth);
    		}
    	}
    	return inSampleSize;
    }
}
