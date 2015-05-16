package com.example.projectocr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.googlecode.tesseract.android.TessBaseAPI;

import android.content.res.AssetManager;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class OCREngine {

	private final String TAG = OCREngine.class.getSimpleName();
	private String tessDataDirPath = null;
	private final String lang = "eng";
	private Context context = null;

	public OCREngine(Context context) {

		this.context = context;
		setupTessDataFiles();
	}

	private void setupTessDataFiles() {

		this.tessDataDirPath = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
				+ File.separator + "projectOCR" + File.separator + "tessdata" + File.separator;

		File tessDataDirFile = new File(tessDataDirPath);
		if (tessDataDirFile.exists() == false) {
			if (tessDataDirFile.mkdirs() == false)
				Log.v(TAG, "ERROR: Creation of directory " + tessDataDirPath + " on sdcard failed");
			else
				Log.v(TAG, "Created directory " + tessDataDirPath + " on sdcard");
		}

		String[] tessDataFileNames = {"eng.traineddata", "number.jpg"};/*{ "eng.cube.bigrams", "eng.cube.fold", "eng.cube.lm",
				"eng.cube.nn", "eng.cube.params", "eng.cube.size", "eng.cube.word-freq",
				"eng.tesseract_cube.nn", "eng.traineddata", "grc.traineddata", "number.png" };*/

		for (String tessDataFileName : tessDataFileNames) {
			String tessEngDataPath = tessDataDirPath + tessDataFileName;
			File tessEngDataFile = new File(tessEngDataPath);
			if (tessEngDataFile.exists() == false) {
				try {
					AssetManager assetManager = this.context.getAssets();
					InputStream inputStream = assetManager.open("tessdata" + File.separator
							+ tessDataFileName);
					OutputStream outputStream = new FileOutputStream(tessEngDataPath);
					byte[] buf = new byte[1024];
					int len = 0;

					while ((len = inputStream.read(buf)) > 0)
						outputStream.write(buf, 0, len);

					inputStream.close();
					outputStream.close();

					Log.v(TAG, "Copied " + tessEngDataPath);
				}
				catch (IOException e) {
					Log.e(TAG, "Was unable to copy " + tessEngDataPath + e.toString());
				}
			}
		}
	}

	private Bitmap getBitmapFromImageFile(File imageFile) {

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;
		
		Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

		try {
			ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
			int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);

			Log.v(TAG, "Orient: " + exifOrientation);
			int rotate = 0;

			switch (exifOrientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate = 90;
				break;
				
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate = 180;
				break;
				
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate = 270;
				break;
			}

			Log.v(TAG, "Rotation: " + rotate);

			if (rotate != 0) {

				// Getting width & height of the given image.
				int w = bitmap.getWidth();
				int h = bitmap.getHeight();

				// Setting pre rotate
				Matrix mtx = new Matrix();
				mtx.preRotate(rotate);

				// Rotating Bitmap
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
			}

			// Convert to ARGB_8888, required by tess
			bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
		}
		catch (IOException e) {
			Log.e(TAG, "Couldn't correct orientation: " + e.toString());
		}

		// _image.setImageBitmap( bitmap );
		Log.v(TAG, "Before baseApi");
		return bitmap;
	}

	public String renderText(File imageFile) {

		TessBaseAPI tessBaseAPI = new TessBaseAPI();
		tessBaseAPI.setDebug(true);
		
		String tessDataDirPath1 = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
				+ File.separator + "projectOCR" + File.separator;

		
		tessBaseAPI.init(tessDataDirPath1, lang);
		//For example if we want to only detect numbers
        tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "1234567890");
        tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-qwertyuiop[]}{POIU" +
                "YTREWQasdASDfghFGHjklJKLl;L:'\"\\|~`xcvXCVbnmBNM,./<>?");
		Bitmap bitmap = getBitmapFromImageFile(imageFile);
		tessBaseAPI.setImage(bitmap);

		String recognizedText = tessBaseAPI.getUTF8Text();
		tessBaseAPI.end();

		return recognizedText;
	}
}