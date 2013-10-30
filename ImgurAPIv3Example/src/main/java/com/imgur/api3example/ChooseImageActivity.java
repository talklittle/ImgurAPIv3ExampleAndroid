package com.imgur.api3example;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.imgur.api3example.login.ImgurAuthorization;
import com.imgur.api3example.login.LoginActivity;
import com.imgur.api3example.login.RefreshAccessTokenTask;

public class ChooseImageActivity extends ActionBarActivity {
	
	private static final int REQ_CODE_PICK_IMAGE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_upload, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean loggedIn = ImgurAuthorization.getInstance().isLoggedIn();
        menu.findItem(R.id.menu_login).setVisible(!loggedIn);
        menu.findItem(R.id.menu_logout).setVisible(loggedIn);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_login) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            return true;
        }
        else if (item.getItemId() == R.id.menu_logout) {
            ImgurAuthorization.getInstance().logout();
            Toast.makeText(this, R.string.logged_out, Toast.LENGTH_SHORT).show();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void pickImage(View view) {
		Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, REQ_CODE_PICK_IMAGE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
	    super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 

	    switch(requestCode) { 
	    case REQ_CODE_PICK_IMAGE:
	        if(resultCode == RESULT_OK){  
	            Uri selectedImage = imageReturnedIntent.getData();
	            getChooseImageFragment().setImage(selectedImage);
	        }
	    }
	}
	
	private ChooseImageFragment getChooseImageFragment() {
		return (ChooseImageFragment) getSupportFragmentManager().findFragmentById(R.id.choose_image_fragment);
	}
	
	public void copyLink(View view) {
		getChooseImageFragment().copyLink(view);
	}

    @Override
    protected void onResume() {
        super.onResume();
        new RefreshAccessTokenTask().execute();
    }

}
