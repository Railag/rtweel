package com.rtweel.utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

public class Photo extends Activity {

	public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

	public static final int MEDIA_TYPE_IMAGE = 1;

	public static String mCurrentPhotoPath;

	private Uri fileUri;

	public Uri getFileUri() {
		return fileUri;
	}

	public void setFileUri(Uri uri) {
		fileUri = uri;
	}

	public static void setPicture(ImageView photo) {

		// photo = (ImageView) findViewById(R.id.photo_view);
		// int targetW = photo.getWidth();
		// int targetH = photo.getHeight();
		int targetW = 100;
		int targetH = 100;

		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		bmOptions.inSampleSize = calculateInSampleSize(bmOptions, targetW,
				targetH);
		BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

		int scaleFactor = 1;
		if ((targetW > 0) || (targetH > 0)) {
			scaleFactor = Math.min(photoW / targetW, photoH / targetH);
		}

		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		photo.setImageBitmap(bitmap);
//		int orientation = getOrientationFromExif(mCurrentPhotoPath);
		// imageView.ROTATION.set(imageView, (float) orientation);
//		View.ROTATION.set(photo, (float) orientation);
		// imageView.setImageBitmap(rotatedBitmap);
		// imageView.setVisibility(View.VISIBLE);
		// videoView.setVisibility(View.INVISIBLE);
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	public static int getOrientationFromExif(String imagePath) {
		int orientation = -1;
		try {
			ExifInterface exif = new ExifInterface(imagePath);
			int exifOrientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);

			switch (exifOrientation) {
			case ExifInterface.ORIENTATION_ROTATE_270:
				orientation = 270;

				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				orientation = 180;

				break;
			case ExifInterface.ORIENTATION_ROTATE_90:
				orientation = 90;

				break;

			case ExifInterface.ORIENTATION_NORMAL:
				orientation = 0;

				break;
			default:
				break;
			}
		} catch (IOException e) {
			Log.e("DEBUG", "Unable to get image exif orientation", e);
		}

		return orientation;
	}

	/** Create a File for saving an image or video */
	public static File getOutputMediaFile() {

		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"MyCameraApp");
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("MyCameraApp", "failed to create directory");
				return null;
			}
		}

		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
				Locale.getDefault()).format(new Date());
		File mediaFile;
		mediaFile = new File(mediaStorageDir.getPath() + File.separator
				+ "IMG_" + timeStamp + ".jpg");
		mCurrentPhotoPath = mediaFile.getAbsolutePath();

		return mediaFile;
	}

}
