package com.example.camera102;

/**Imagesave
 * save the image and other
 * @author: Dang Xuan Binh 11-C
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import com.example.camera102.MainActivity;

import com.example.camera102.MainActivity;
import android.net.Uri;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Toast;

public class Imagesave extends Activity implements OnTouchListener,
		OnClickListener {
	private Bitmap imageBitmap;
	private ProgressDialog mProgressDialog;
	private Bitmap obscuredBmp;
	private final static String EXPORT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private Uri originalImageUri;
	private Uri savedImageUri;
	public final static String MIME_TYPE_JPEG = "image/jpeg";

	private void debug(String tag, String message) {
		Log.d(tag, message);
	}

	final static String CAMERA_TMP_FILE = "cmr102.jpg";

	private Uri saveTmpImage() {

		String storageState = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(storageState)) {
			Toast t = Toast.makeText(this, "External storage not available",
					Toast.LENGTH_SHORT);
			t.show();
			return null;
		}

		// Create the Uri - This can't be "private"
		File tmpFileDirectory = new File(Environment
				.getExternalStorageDirectory().getPath() + CAMERA_TMP_FILE);
		File tmpFile = new File(tmpFileDirectory, CAMERA_TMP_FILE);
		debug(MainActivity.TAG, tmpFile.getPath());

		try {
			if (!tmpFileDirectory.exists()) {
				tmpFileDirectory.mkdirs();
			}
			Uri tmpImageUri = Uri.fromFile(tmpFile);

			OutputStream imageFileOS;

			int quality = 75;
			imageFileOS = getContentResolver().openOutputStream(tmpImageUri);

			obscuredBmp.compress(CompressFormat.JPEG, quality, imageFileOS);

			return tmpImageUri;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * The method that actually saves the altered image. This in combination
	 * could/should be done in another, more memory efficient manner.
	 */

	private boolean saveImage() {

		ContentValues cv = new ContentValues();

		// Add a date so it shows up in a reasonable place in the gallery -
		// Should we do this??
		SimpleDateFormat dateFormat = new SimpleDateFormat(EXPORT_DATE_FORMAT);
		Date date = new Date();
		String dateString = dateFormat.format(date);

		// Which one?
		cv.put(Images.Media.DATE_ADDED, dateString);
		cv.put(Images.Media.DATE_TAKEN, dateString);
		cv.put(Images.Media.DATE_MODIFIED, dateString);
		cv.put(Images.Media.TITLE, dateString);

		// Uri is savedImageUri which is global
		// Create the Uri, this should put it in the gallery
		// New Each time
		savedImageUri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI,
				cv);

		if (savedImageUri == null)
			return false;

		boolean nativeSuccess = false;

		if (!nativeSuccess) {
			try {

				OutputStream imageFileOS;

				int quality = 100; // lossless? good question - still a smaller

				imageFileOS = getContentResolver().openOutputStream(
						savedImageUri);
				obscuredBmp.compress(CompressFormat.JPEG, quality, imageFileOS);

			} catch (Exception e) {
				Log.e(MainActivity.TAG, "error doing redact", e);
				return false;
			}

		}

		// force media scanner to update file
		MediaScannerConnection
				.scanFile(this, new String[] { pullPathFromUri(savedImageUri)
						.getAbsolutePath() }, new String[] { MIME_TYPE_JPEG },
						null);

		Toast t = Toast.makeText(this, "Image saved to Gallery",
				Toast.LENGTH_SHORT);
		t.show();

		mProgressDialog.cancel();

		showDeleteOriginalDialog();

		return true;
	}

	// show Delete OrginalDialog
	private void showDeleteOriginalDialog() {
		final AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setIcon(android.R.drawable.ic_dialog_alert);
		b.setTitle(getString(R.string.app_name));
		b.setMessage(getString(R.string.confirm_delete));
		b.setPositiveButton(R.string.yes,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						try {
							// User clicked OK so go ahead and delete
							deleteOriginal();
							viewImage(savedImageUri);
						} catch (IOException e) {
							Log.e(MainActivity.TAG, "error saving", e);
						} finally {
							finish();
						}
					}
				});
		b.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

				viewImage(savedImageUri);
			}
		});
		b.show();
	}

	// Actual deletion of original
	private void deleteOriginal() throws IOException {

		if (originalImageUri != null) {
			if (originalImageUri.getScheme().equals("file")) {
				String origFilePath = originalImageUri.getPath();
				File fileOrig = new File(origFilePath);

				String[] columnsToSelect = { MediaStore.Images.Media._ID,
						MediaStore.Images.Media.DATA };

				Uri[] uriBases = {
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
						MediaStore.Images.Media.INTERNAL_CONTENT_URI };

				for (Uri uriBase : uriBases) {

					Cursor imageCursor = getContentResolver().query(uriBase,
							columnsToSelect,
							MediaStore.Images.Media.DATA + " = ?",
							new String[] { origFilePath }, null);

					while (imageCursor.moveToNext()) {

						long _id = imageCursor.getLong(imageCursor
								.getColumnIndex(MediaStore.Images.Media._ID));

						getContentResolver().delete(
								ContentUris.withAppendedId(uriBase, _id), null,
								null);

					}
				}

				if (fileOrig.exists())
					fileOrig.delete();

			} else {
				getContentResolver().delete(originalImageUri, null, null);
			}
		}

		originalImageUri = null;
	}

	// view image
	private void viewImage(Uri imgView) {

		Intent iView = new Intent(Intent.ACTION_VIEW);
		iView.setType(MIME_TYPE_JPEG);
		iView.putExtra(Intent.EXTRA_STREAM, imgView);
		iView.setDataAndType(imgView, MIME_TYPE_JPEG);
		startActivity(Intent.createChooser(iView, "View Image"));

	}

	// use the original path to refresh media scanner
	public File pullPathFromUri(Uri originalUri) {

		String originalImageFilePath = null;

		if (originalUri.getScheme() != null
				&& originalUri.getScheme().equals("file")) {
			originalImageFilePath = originalUri.toString();
		} else {
			String[] columnsToSelect = { MediaStore.Images.Media.DATA };
			Cursor imageCursor = getContentResolver().query(originalUri,
					columnsToSelect, null, null, null);
			if (imageCursor != null && imageCursor.getCount() == 1) {
				imageCursor.moveToFirst();
				originalImageFilePath = imageCursor.getString(imageCursor
						.getColumnIndex(MediaStore.Images.Media.DATA));
			}
		}

		return new File(originalImageFilePath);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
}