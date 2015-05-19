/*
 * File: MainActivity.java
 * Created: Sat May 16 11:10:51 IST 2015
 * Email: vivek.kumar.27@outlook.com
 * 
 * Description: Main activity class for ProjectOCR, this is the first activity seen upon lauching this app.
 */

package com.example.projectocr;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class MainActivity extends Activity implements View.OnClickListener, Runnable {

	private final String TAG = MainActivity.class.getSimpleName();
	private ArrayList<File> locImageFileList = null;
	private String imageDirPath = null;
	private ImageView imageView = null;
	private TextView infoTextView = null;
	private Button prevButton = null;
	private Button nextButton = null;
	private Button startCameraButton = null;
	private Button selectImagebutton = null;
	private int curSelectedIndex = -1;
	private static final int REQUEST_CODE = 1;
	private static final int REQUEST_CODE_TAKE_IMAGE = 2;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private String imageFilePathToBeCapture = null;
	private OCREngine ocrEngine = null;
	private ProgressDialog progressDlg = null;

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

	private void setupSampleData() {

		String sampleDataDirPath = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
				+ File.separator + "projectOCR" + File.separator;

		File sampleDataDirFile = new File(sampleDataDirPath);
		if (sampleDataDirFile.exists() == false) {
			if (sampleDataDirFile.mkdirs() == false)
				Log.v(TAG, "ERROR: Creation of directory " + sampleDataDirFile
						+ " on sdcard failed");
			else
				Log.v(TAG, "Created directory " + sampleDataDirFile + " on sdcard");
		}

		String[] sampleDataFileNames = { "sample_1.jpg", "sample_2.jpg", "sample_3.jpg",
				"sample_4.jpg", "sample_5.png", "sample_6.jpg" };

		for (String sampleDataFileName : sampleDataFileNames) {
			String sampleDataPath = sampleDataDirPath + sampleDataFileName;
			File tessEngDataFile = new File(sampleDataPath);
			if (tessEngDataFile.exists() == false) {
				try {
					AssetManager assetManager = getAssets();
					InputStream inputStream = assetManager.open("sample" + File.separator
							+ sampleDataFileName);
					OutputStream outputStream = new FileOutputStream(sampleDataPath);
					byte[] buf = new byte[1024];
					int len = 0;

					while ((len = inputStream.read(buf)) > 0)
						outputStream.write(buf, 0, len);

					inputStream.close();
					outputStream.close();

					Log.v(TAG, "Copied " + sampleDataPath);
				}
				catch (IOException e) {
					Log.e(TAG, "Was unable to copy " + sampleDataPath + e.toString());
				}
			}
		}
	}

	private void init() {

		setupSampleData();
		this.ocrEngine = new OCREngine(getApplicationContext());
		// imageDirPath = getFilesDir().getAbsolutePath() + File.separator +
		// "images";
		imageDirPath = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
				+ File.separator + "projectOCR";
		this.locImageFileList = new ArrayList<File>();
		populateImageFileList();

		this.infoTextView = (TextView) findViewById(R.id.infoTextView);

		this.prevButton = (Button) findViewById(R.id.prev_button);
		this.prevButton.setOnClickListener(this);

		this.nextButton = (Button) findViewById(R.id.next_button);
		this.nextButton.setOnClickListener(this);

		this.startCameraButton = (Button) findViewById(R.id.start_camera_button);
		this.startCameraButton.setOnClickListener(this);

		this.selectImagebutton = (Button) findViewById(R.id.selectImagebutton);
		this.selectImagebutton.setOnClickListener(this);

		this.imageView = (ImageView) findViewById(R.id.imageView1);
		this.imageView.setOnClickListener(this);
		if (this.locImageFileList.isEmpty() == false) {
			this.setCurSelectedIndex(0);
			changeImage(MainActivity.Naviagation.NO_CHANGE);
		}
		else {
			this.setButtonState();
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

		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override public void onClick(View v) {

		int id = v.getId();
		if (id == R.id.prev_button) {
			changeImage(MainActivity.Naviagation.PREV);
		}
		else if (id == R.id.next_button) {
			changeImage(MainActivity.Naviagation.NEXT);
		}
		else if (id == R.id.start_camera_button) {
			// pickImageUsingImageCaptureIntent();
			launchCameraActivity();
		}
		else if (id == R.id.selectImagebutton) {
			pickImageFromDevice();
		}
		else if (id == R.id.imageView1) {
			if (this.locImageFileList.size() > 0) {
				this.progressDlg = ProgressDialog.show(MainActivity.this, "ProjectOCR",
						"Extrating info ...", true, false);
				new Thread(this).start();
			}
		}
	}

	private void setExtractedText() {

		File f = this.locImageFileList.get(this.getCurSelectedIndex());
		String renderedText = this.ocrEngine.renderText(f);
		String infoText = getString(R.string.info_extracted_) + "\n" + renderedText;
		//this.infoTextView.setText(infoText);
		
	    Message msg = this.handler.obtainMessage(0, infoText);
	    this.handler.sendMessage(msg);
	}

	private void pickImageFromDevice() {

		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		startActivityForResult(intent, MainActivity.REQUEST_CODE);
	}

	private void pickImageUsingImageCaptureIntent() {

		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		File storageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
				+ File.separator + "projectOCR");
		File image = null;
		try {
			image = File.createTempFile(imageFileName, /* prefix */
					".jpg", /* suffix */
					storageDir /* directory */
			);
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		this.imageFilePathToBeCapture = image.getAbsolutePath();
		Uri fileUri = Uri.fromFile(image);

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
		startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}

	private void launchCameraActivity() {

		Intent cameraLanchIntent = new Intent(this, CameraActivity.class);
		startActivityForResult(cameraLanchIntent, MainActivity.REQUEST_CODE_TAKE_IMAGE);
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
					addNewItemInImageList(filePath);
				}
			}
		}
		else if (requestCode == MainActivity.REQUEST_CODE_TAKE_IMAGE
				&& resultCode == Activity.RESULT_OK) {
			addNewItemInImageList(intent.getStringExtra("IMAGE_PATH"));
		}
		else if (requestCode == MainActivity.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				addNewItemInImageList(this.imageFilePathToBeCapture);
			}
			else {
				new File(imageFilePathToBeCapture).delete();
			}
			this.imageFilePathToBeCapture = null;
		}

		super.onActivityResult(requestCode, resultCode, intent);
	}

	private void addNewItemInImageList(String filePath) {

		File file = new File(filePath);
		if (file.isFile() == true) {
			if (file.getName().endsWith(".png") == true || file.getName().endsWith(".jpg") == true
					|| file.getName().endsWith(".jpeg") == true) {
				this.locImageFileList.add(file);
				this.changeImage(MainActivity.Naviagation.LAST);
			}
		}
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

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 2;

		Bitmap bitmap = BitmapFactory.decodeFile(
				this.locImageFileList.get(this.getCurSelectedIndex()).toString(), options);
		this.imageView.setImageBitmap(bitmap);

		this.infoTextView.setText("Click image to extract text");
	}

	private void setButtonState() {

		this.prevButton.setEnabled(this.getCurSelectedIndex() > 0);
		this.nextButton.setEnabled(this.getCurSelectedIndex() < (this.locImageFileList.size() - 1));
	}

	@Override public void run() {
		setExtractedText();
		handler.sendEmptyMessage(0);
	}

	private Handler handler = new Handler() {
		@Override public void handleMessage(Message msg) {
			MainActivity.this.infoTextView.setText((String)msg.obj);
			MainActivity.this.progressDlg.dismiss();
			handler.removeMessages(0);
		}
	};
}