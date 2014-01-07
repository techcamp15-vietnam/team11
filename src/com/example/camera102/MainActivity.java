package com.example.camera102;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	public final static String TAG = "CMR";

	final static int CAMERA_RESULT = 0;
	final static int GALLERY_RESULT = 1;
	final static int IMAGE_EDITOR = 2;
	final static int VIDEO_EDITOR = 3;
	final static int ABOUT = 0;

	final static String CAMERA_TMP_FILE = "cmr102.jpg";
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 200;

	private Button choosePictureButton, chooseVideoButton, takePictureButton,
			exit;
	private ImageView image;
	private Uri uriCameraImage = null;

	@Override
	protected void onDestroy() {
		super.onDestroy();
		deleteTmpFile();

	}

	private void deleteTmpFile() {
		File fileDir = getExternalFilesDir(null);

		if (fileDir == null || !fileDir.exists())
			fileDir = getFilesDir();

		File tmpFile = new File(fileDir, CAMERA_TMP_FILE);
		if (tmpFile.exists())
			tmpFile.delete();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setLayout();
		deleteTmpFile();

	}

	private void setLayout() {

		setContentView(R.layout.mainmenu);

		choosePictureButton = (Button) this
				.findViewById(R.id.ChoosePictureButton);
		choosePictureButton.setOnClickListener(this);

		chooseVideoButton = (Button) this.findViewById(R.id.ChooseVideoButton);
		chooseVideoButton.setOnClickListener(this);

		takePictureButton = (Button) this.findViewById(R.id.TakePictureButton);
		takePictureButton.setOnClickListener(this);
		exit = (Button) this.findViewById(R.id.exit);
		exit.setOnClickListener(this);

	}

	public void onClick(View v) {
		if (v == exit) {
			finish();
			System.exit(0);
		} else if (v == choosePictureButton) {

			try {
				// setContentView(R.layout.mainloading);
				Intent intent = new Intent(Intent.ACTION_PICK);
				intent.setType("image/*"); // limit to image types for now
				startActivityForResult(intent, GALLERY_RESULT);

			} catch (Exception e) {
				Toast.makeText(this, "Unable to open Gallery app", Toast.LENGTH_LONG).show();
				Log.e(TAG,"error loading gallery app to choose photo: "	+ e.getMessage(), e);
			}

		} else if (v == chooseVideoButton) {

			try {
				setContentView(R.layout.mainloading);
				Intent intent = new Intent(Intent.ACTION_PICK);
				intent.setType("video/*"); // limit to video types for now
				startActivityForResult(intent, GALLERY_RESULT);

			} catch (Exception e) {
				Toast.makeText(this, "Unable to open Gallery app",
						Toast.LENGTH_LONG).show();
				Log.e(TAG,
						"error loading gallery app to choose photo: "
								+ e.getMessage(), e);
			}

		} else if (v == takePictureButton) {

			setContentView(R.layout.mainloading);

			String storageState = Environment.getExternalStorageState();
			if (storageState.equals(Environment.MEDIA_MOUNTED)) {

				ContentValues values = new ContentValues();

				values.put(MediaStore.Images.Media.TITLE, CAMERA_TMP_FILE);

				values.put(MediaStore.Images.Media.DESCRIPTION, "cam102");

				File tmpFileDirectory = new File(Environment
						.getExternalStorageDirectory().getPath()
						+ "/DCIM/Camera");
				if (!tmpFileDirectory.exists())
					tmpFileDirectory.mkdirs();

				File tmpFile = new File(tmpFileDirectory, CAMERA_TMP_FILE);

				uriCameraImage = Uri.fromFile(tmpFile);
				// uriCameraImage =
				// getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				// values);

				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, uriCameraImage);

				startActivityForResult(intent, CAMERA_RESULT);
			} else {
				new AlertDialog.Builder(MainActivity.this)
						.setMessage(
								"External Storeage (SD Card) is required.\n\nCurrent state: "
										+ storageState).setCancelable(true)
						.create().show();
			}

			takePictureButton.setVisibility(View.VISIBLE);
			choosePictureButton.setVisibility(View.VISIBLE);
			chooseVideoButton.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		String aboutString = "About Camera102";

		MenuItem aboutMenuItem = menu.add(Menu.NONE, ABOUT, Menu.NONE,
				aboutString);

		return true;
	}

}