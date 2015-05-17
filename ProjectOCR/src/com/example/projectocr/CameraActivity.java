package com.example.projectocr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

@SuppressWarnings("deprecation")
public class CameraActivity extends Activity implements SurfaceHolder.Callback,
		View.OnClickListener {

	private String TAG = CameraActivity.class.getSimpleName();
	private Button cameraShutterButton = null;
	private WireFrameView wireFrameBox = null;;
	private SurfaceView cameraSurfaceView = null;;
	private Camera camera = null;
	private boolean isCameraOn = false;

	@Override protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
	}

	@Override protected void onStart() {

		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override protected void onResume() {

		super.onResume();

		this.cameraShutterButton = (Button) findViewById(R.id.shutter_button);
		this.cameraShutterButton.setOnClickListener(this);

		this.wireFrameBox = (WireFrameView) findViewById(R.id.wireframe_box);
		this.wireFrameBox.setOnClickListener(this);

		this.cameraSurfaceView = (SurfaceView) findViewById(R.id.camera_frame);
		SurfaceHolder surfaceHolder = cameraSurfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		cameraSurfaceView.setOnClickListener(this);
	}

	@Override protected void onPause() {

		super.onPause();

		if (this.isCameraOn == true) {
			stopCamera();
		}

		SurfaceHolder surfaceHolder = this.cameraSurfaceView.getHolder();
		surfaceHolder.removeCallback(this);
	}

	@Override protected void onStop() {

		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override public void finish() {

		// TODO Auto-generated method stub
		super.finish();
	}

	@Override public void surfaceCreated(SurfaceHolder holder) {

		if (this.camera == null) {
			startCamera(holder);
		}
		else if (this.camera != null && this.isCameraOn) {
			Log.d(TAG, "Camera already opened");
			return;
		}
	}

	private void startCamera(SurfaceHolder holder) {

		try {
			this.camera = Camera.open();
		}
		catch (Exception e) {
			Log.e(TAG, "Cannot getCamera()");
		}

		try {
			this.camera.setPreviewDisplay(holder);
			this.camera.setDisplayOrientation(90);
			this.camera.startPreview();
			this.isCameraOn = true;
			Log.d(TAG, "Camera preview started");
		}
		catch (IOException e) {
			Log.e(TAG, "Error in setPreviewDisplay");
		}
	}

	private void stopCamera() {

		if (this.camera != null) {
			this.camera.release();
			this.camera = null;
		}

		this.isCameraOn = false;

		Log.d(TAG, "Camera is  stopped");
	}

	@Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

		// TODO Auto-generated method stub

	}

	@Override public void surfaceDestroyed(SurfaceHolder holder) {

		// TODO Auto-generated method stub

	}

	@Override public void onClick(View v) {

		if (v.getId() == R.id.shutter_button) {
			this.camera.takePicture(shutterCallback, rawCallback, jpegCallback);
		}
		else if (v.getId() == R.id.camera_frame) {
		}
	}

	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {

		}
	};

	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {

		}
	};

	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {

			Bitmap pictureTaken = BitmapFactory.decodeByteArray(data, 0, data.length);
			String imageFilePath = Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
					+ File.separator
					+ "projectOCR"
					+ File.separator
					+ UUID.randomUUID().toString()
					+ ".jpg";

			try {
				OutputStream outputStream = new FileOutputStream(imageFilePath);
				boolean compressed = pictureTaken.compress(Bitmap.CompressFormat.JPEG, 20,
						outputStream);
				Log.e(TAG, "picture successfully compressed at:" + imageFilePath + compressed);
				outputStream.close();
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			
			finish();

			/*
			 * ContentValues contentValues = new ContentValues();
			 * contentValues.put(Images.Media.TITLE, "image");
			 * 
			 * Uri uri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI,
			 * contentValues);
			 * 
			 * OutputStream outputStream = null; try { outputStream =
			 * getContentResolver().openOutputStream(uri); boolean compressed =
			 * pictureTaken.compress(Bitmap.CompressFormat.JPEG, 20,
			 * outputStream); Log.e(TAG, "picture successfully compressed at:" +
			 * uri + compressed); outputStream.close(); } catch
			 * (FileNotFoundException e) { e.printStackTrace(); } catch
			 * (IOException e) { e.printStackTrace(); }
			 * 
			 * Log.e(TAG, "jpeg data length " + data.length); Intent intent =
			 * new Intent(); intent.putExtra(CameraCapture.JPEG_STRING, uri);
			 * setResult(NG911Activity.IMAGE_RECEIVED_RESULT, intent); finish();
			 */
		}
	};
}
