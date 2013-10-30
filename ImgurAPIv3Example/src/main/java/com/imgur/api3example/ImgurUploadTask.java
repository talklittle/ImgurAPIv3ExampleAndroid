package com.imgur.api3example;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.imgur.api3example.login.ImgurAuthorization;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public abstract class ImgurUploadTask extends AsyncTask<Void, Void, String> {
	
	private static final String TAG = ImgurUploadTask.class.getSimpleName();
	
	private static final String UPLOAD_URL = "https://api.imgur.com/3/image";

    private Activity mActivity;
	private Uri mImageUri;  // local Uri to upload
	
	public ImgurUploadTask(Uri imageUri, Activity activity) {
		this.mImageUri = imageUri;
        this.mActivity = activity;
	}
	
	@Override
	protected String doInBackground(Void... params) {
		InputStream imageIn;
		try {
			imageIn = mActivity.getContentResolver().openInputStream(mImageUri);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "could not open InputStream", e);
			return null;
		}

        HttpURLConnection conn = null;
        InputStream responseIn = null;

		try {
            conn = (HttpURLConnection) new URL(UPLOAD_URL).openConnection();
            conn.setDoOutput(true);

            ImgurAuthorization.getInstance().addToHttpURLConnection(conn);

            OutputStream out = conn.getOutputStream();
            copy(imageIn, out);
            out.flush();
            out.close();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                responseIn = conn.getInputStream();
                return onInput(responseIn);
            }
            else {
                Log.i(TAG, "responseCode=" + conn.getResponseCode());
                responseIn = conn.getErrorStream();
                StringBuilder sb = new StringBuilder();
                Scanner scanner = new Scanner(responseIn);
                while (scanner.hasNext()) {
                    sb.append(scanner.next());
                }
                Log.i(TAG, "error response: " + sb.toString());
                return null;
            }
		} catch (Exception ex) {
			Log.e(TAG, "Error during POST", ex);
			return null;
		} finally {
            try {
                responseIn.close();
            } catch (Exception ignore) {}
            try {
                conn.disconnect();
            } catch (Exception ignore) {}
			try {
				imageIn.close();
			} catch (Exception ignore) {}
		}
	}

    private static int copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[8192];
        int count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
	
	protected String onInput(InputStream in) throws Exception {
        StringBuilder sb = new StringBuilder();
        Scanner scanner = new Scanner(in);
        while (scanner.hasNext()) {
            sb.append(scanner.next());
        }

        JSONObject root = new JSONObject(sb.toString());
        String id = root.getJSONObject("data").getString("id");
        String deletehash = root.getJSONObject("data").getString("deletehash");

		Log.i(TAG, "new imgur url: http://imgur.com/" + id + " (delete hash: " + deletehash + ")");
		return id;
	}
	
}
