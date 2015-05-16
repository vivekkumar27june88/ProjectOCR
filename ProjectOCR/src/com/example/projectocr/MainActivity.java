/*
 * File: MainActivity.java
 * Created: Sat May 16 11:10:51 IST 2015
 * Email: vivek.kumar.27@outlook.com
 * 
 * Description: Main activity class for ProjectOCR, this is the first activity seen upon lauching this app.
 */

package com.example.projectocr;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class MainActivity extends Activity implements View.OnClickListener {

	private ArrayList<File> locImageFileList = null;
	private String imageDirPath = null;
	private ImageView imageView = null;
	private Button prevButton = null;
	private Button nextButton = null;
	private Button startCameraButton = null;
	private Button selectImagebutton = null;
	private int curSelectedIndex = -1;
	private static final int REQUEST_CODE = 1;

	private int getCurSelectedIndex() {

		return curSelectedIndex;
	}

	private void setCurSelectedIndex(int curSelectedIndex) {

		this.curSelectedIndex = curSelectedIndex;
		setButtonState();
	}

	private enum Naviagation {
		NEXT, PREV, LAST, NO_CHANGE
	};

	private void init() {

		// imageDirPath = getFilesDir().getAbsolutePath() + File.separator +
		// "images";
		imageDirPath = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
				+ File.separator + "projectOCR";
		this.locImageFileList = new ArrayList<File>();
		populateImageFileList();

		this.prevButton = (Button) findViewById(R.id.prev_button);
		this.prevButton.setOnClickListener(this);

		this.nextButton = (Button) findViewById(R.id.next_button);
		this.nextButton.setOnClickListener(this);

		this.startCameraButton = (Button) findViewById(R.id.start_camera_button);
		this.startCameraButton.setOnClickListener(this);

		this.selectImagebutton = (Button) findViewById(R.id.selectImagebutton);
		this.selectImagebutton.setOnClickListener(this);

		this.imageView = (ImageView) findViewById(R.id.imageView1);
		if (this.locImageFileList.isEmpty() == false) {
			this.setCurSelectedIndex(0);
			changeImage(MainActivity.Naviagation.NO_CHANGE);
		}
	}

	private void populateImageFileList() {

		File imageDir = new File(imageDirPath);
		if (imageDir.exists() == false) {
			boolean r = imageDir.mkdirs();
			Log.d("VIVEK", "Is directory created : " + r);
		}
		else {
			Log.d("VIVEK", "directory already present");
		}

		locImageFileList.clear();
		File[] files = imageDir.listFiles();

		for (File file : files) {
			if (file.isFile() == true) {
				if (file.getName().endsWith(".png") == true
						|| file.getName().endsWith(".jpg") == true
						|| file.getName().endsWith(".jpeg") == true) {
					locImageFileList.add(file);
				}
			}
		}
	}

	@Override protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		init();
	}

	@Override public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {

		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override public void onClick(View v) {

		switch (v.getId()) {
		case R.id.prev_button:
			changeImage(MainActivity.Naviagation.PREV);
			break;

		case R.id.start_camera_button:
			break;

		case R.id.selectImagebutton:
			pickImageFromDevice();
			break;

		case R.id.next_button:
			changeImage(MainActivity.Naviagation.NEXT);
			break;
		}
	}

	private void pickImageFromDevice() {

		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		startActivityForResult(intent, REQUEST_CODE);
	}

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

		if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			Uri uri = intent.getData();
			if (uri.getScheme().toString().compareTo("content") == 0) {
				Cursor cursor = getContentResolver().query(uri, null, null, null, null);
				if (cursor.moveToFirst()) {
					int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					Uri filePathUri = Uri.parse(cursor.getString(column_index));
					String filePath = filePathUri.getPath();
					Log.d("VIVEK", "File Name & PATH are:" + filePath);
					File file = new File(filePath);
					if (file.isFile() == true) {
						if (file.getName().endsWith(".png") == true
								|| file.getName().endsWith(".jpg") == true
								|| file.getName().endsWith(".jpeg") == true) {
							this.locImageFileList.add(file);
							this.changeImage(MainActivity.Naviagation.LAST);
						}
					}
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}

	private void changeImage(Naviagation nav) {

		if ((nav == MainActivity.Naviagation.NEXT)
				&& (this.getCurSelectedIndex() < this.locImageFileList.size() - 1)) {
			this.setCurSelectedIndex(this.curSelectedIndex + 1);
		}
		else if (nav == MainActivity.Naviagation.PREV && this.getCurSelectedIndex() > 0) {
			this.setCurSelectedIndex(this.curSelectedIndex - 1);
		}
		else if (nav == MainActivity.Naviagation.LAST) {
			this.setCurSelectedIndex(this.locImageFileList.size() - 1);
		}
		

		Bitmap bitmap = BitmapFactory.decodeFile(this.locImageFileList.get(
				this.getCurSelectedIndex()).toString());
		this.imageView.setImageBitmap(bitmap);
	}

	private void setButtonState() {

		this.prevButton.setEnabled(this.getCurSelectedIndex() != 0);
		this.nextButton
				.setEnabled(this.getCurSelectedIndex() != (this.locImageFileList.size() - 1));
	}
}