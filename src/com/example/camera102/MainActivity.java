package com.example.camera102;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
import android.view.View.OnClickListener;
import android.view.Window;
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
	private static final int CAPTURE_IMAGE = 200;
	public static final int MEDIA_TYPE_IMAGE = 100;
	private static final String IMAGE_DIRECTORY_NAME = "Camera102";
	private Button choosePictureButton, chooseVideoButton, takePictureButton,
			exit;
	private ImageView image;
	private Uri uriCameraImage;
	private String selectedImagePath;

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setLayout();

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

	/*
	 * Onclick Event button exit, choose picture and video onclick
	 * 
	 * @author 11A Bach Ngoc Tuan
	 */
	public void onClick(View v) {
		if (v == exit) {
			finish();
			System.exit(0);
		} else if (v == choosePictureButton) {
			setContentView(R.layout.mainloading);
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(
					Intent.createChooser(intent, "Select Picture"),
					GALLERY_RESULT);

		} else if (v == chooseVideoButton) {

			setContentView(R.layout.mainloading);
			Intent intent = new Intent();
			intent.setType("video/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(
					Intent.createChooser(intent, "Select Video"),
					GALLERY_RESULT);

		} else if (v == takePictureButton) {
			/*
			 * Onclick Event button takePicture
			 * 
			 * @author 11C Dang Xuan Binh
			 */
			setContentView(R.layout.mainloading);
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			uriCameraImage = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uriCameraImage);
			startActivityForResult(intent, CAPTURE_IMAGE);

		}
	}

	/*
	 * Creating file uri to store image/video
	 * 
	 * @author 11C Dang Xuan Binh
	 */
	public Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/*
	 * getOutputMediaFile
	 * 
	 * @author 11C Dang Xuan Binh
	 */
	private static File getOutputMediaFile(int type) {

		// External sdcard location
		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				IMAGE_DIRECTORY_NAME);

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
						+ IMAGE_DIRECTORY_NAME + " directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
				Locale.getDefault()).format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "CMR102_" + timeStamp + ".jpg");
		} else {
			return null;
		}

		return mediaFile;
	}


	/*
	 * onActivityResult
	 * 
	 * @author 11A Bach Ngoc Tuan
	 */
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {

		if (resultCode == RESULT_OK) {		
			if (requestCode == GALLERY_RESULT) {
				Uri selectedImageUri = intent.getData();
				 selectedImagePath = getPath(selectedImageUri);	   
				 Intent ImagePath = new Intent(this,DrawOnImage.class);
				 ImagePath.putExtra("name", selectedImagePath);
				 startActivity(ImagePath);
				
			} else if (requestCode == CAPTURE_IMAGE) {
				setContentView(R.layout.imageviewer);
			    image = (ImageView) findViewById(R.id.ImageEditorImageView);
				image.setImageURI(uriCameraImage);
				
			}

		} else
			setLayout();
	}
	/*
	 * get string Path
	 * @param Uri
	 * @author 11A Bach Ngoc Tuan
	 */
	 public String getPath(Uri uri) {
	        String[] projection = { MediaStore.Images.Media.DATA };
	        Cursor cursor = managedQuery(uri, projection, null, null, null);
	        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	        cursor.moveToFirst();
	        return cursor.getString(column_index);
	    }
	/*
	 * onCreateOptionsMenu CreateOptionsMenu
	 * 
	 * @author 11A Bach Ngoc Tuan
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		String aboutString = "About Camera102";

		MenuItem aboutMenuItem = menu.add(Menu.NONE, ABOUT, Menu.NONE,
				aboutString);

		return true;
	}
}