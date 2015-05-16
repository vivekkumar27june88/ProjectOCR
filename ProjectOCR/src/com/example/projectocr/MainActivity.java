/*
 * File: MainActivity.java
 * Created: Sat May 16 11:10:51 IST 2015
 * Email: vivek.kumar.27@outlook.com
 * 
 * Description: Main activity class for ProjectOCR, this is the first activity seen upon lauching this app.
 */

package com.example.projectocr;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;

public class MainActivity extends Activity implements View.OnClickListener {

	private ArrayList<File> imageFileList;
	private String imageDirPath;
	private ImageView imageView = null;
	private Button prevButton = null;
	private Button startCameraButton = null;
	private Button nextButton = null;
	private int curSelectedIndex = -1;

	private int getCurSelectedIndex() {

		return curSelectedIndex;
	}

	private void setCurSelectedIndex(int curSelectedIndex) {

		this.curSelectedIndex = curSelectedIndex;
		setButtonState();
	}

	private enum Naviagation {
		NEXT, PREV, NO_CHANGE
	};

	private void init() {

		// imageDirPath = getFilesDir().getAbsolutePath() + File.separator +
		// "images";
		imageDirPath = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
				+ File.separator + "projectOCR";
		this.imageFileList = new ArrayList<File>();
		populateImageFileList();

		this.prevButton = (Button) findViewById(R.id.prev_button);
		this.prevButton.setOnClickListener(this);

		this.startCameraButton = (Button) findViewById(R.id.start_camera_button);
		this.startCameraButton.setOnClickListener(this);

		this.nextButton = (Button) findViewById(R.id.next_button);
		this.nextButton.setOnClickListener(this);

		this.imageView = (ImageView) findViewById(R.id.imageView1);
		if (this.imageFileList.isEmpty() == false) {
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

		imageFileList.clear();
		File[] files = imageDir.listFiles();

		for (File file : files) {
			if (file.isFile() == true) {
				if (file.getName().endsWith(".png") == true
						|| file.getName().endsWith(".jpg") == true
						|| file.getName().endsWith(".jpeg") == true) {
					imageFileList.add(file);
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

		case R.id.next_button:
			changeImage(MainActivity.Naviagation.NEXT);
			break;
		}
	}

	private void changeImage(Naviagation nav) {

		if ((nav == MainActivity.Naviagation.NEXT)
				&& (this.getCurSelectedIndex() < this.imageFileList.size() - 1)) {
			this.setCurSelectedIndex(this.curSelectedIndex + 1);
		}
		else if (nav == MainActivity.Naviagation.PREV && this.getCurSelectedIndex() > 0) {
			this.setCurSelectedIndex(this.curSelectedIndex - 1);
		}

		Bitmap bitmap = BitmapFactory.decodeFile(this.imageFileList.get(this.getCurSelectedIndex())
				.toString());
		this.imageView.setImageBitmap(bitmap);
	}

	private void setButtonState() {

		this.prevButton.setEnabled(this.getCurSelectedIndex() != 0);
		this.nextButton.setEnabled(this.getCurSelectedIndex() != (this.imageFileList.size() - 1));
	}
}