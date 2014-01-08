package com.example.camera102;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import com.example.camera102.GoogleFaceDetection;
import com.example.camera102.GoogleFaceDetection.DetectedFace;
import com.example.camera102.MainActivity;
import com.example.camera102.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;

public class ImageEditor extends Activity implements OnTouchListener,
		OnClickListener {

	public final static String MIME_TYPE_JPEG = "image/jpeg";

	// Colors for region squares

	public final static int DRAW_COLOR = 0x00000000;
	public final static int DETECTED_COLOR = 0x00000000;
	public final static int OBSCURED_COLOR = 0x00000000;

	// Constants for the menu items, currently these are in an XML file
	// (menu/image_editor_menu.xml, strings.xml)
	public final static int ABOUT_MENU_ITEM = 0;
	public final static int DELETE_ORIGINAL_MENU_ITEM = 1;
	public final static int SAVE_MENU_ITEM = 2;
	public final static int SHARE_MENU_ITEM = 3;
	public final static int NEW_REGION_MENU_ITEM = 4;

	// Constants for Informa
	public final static int FROM_INFORMA = 100;
	public final static String LOG = "[Image Editor ********************]";

	// Image Matrix
	Matrix matrix = new Matrix();

	// Saved Matrix for not allowing a current operation (over max zoom)
	Matrix savedMatrix = new Matrix();

	// We can be in one of these 3 states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	static final int TAP = 3;
	int mode = NONE;

	// Maximum zoom scale
	static final float MAX_SCALE = 10f;

	// Constant for autodetection dialog
	static final int DIALOG_DO_AUTODETECTION = 0;

	// For Zooming
	float startFingerSpacing = 0f;
	float endFingerSpacing = 0f;
	PointF startFingerSpacingMidPoint = new PointF();

	// For Dragging
	PointF startPoint = new PointF();

	float minMoveDistanceDP = 5f;
	float minMoveDistance; // =
							// ViewConfiguration.get(this).getScaledTouchSlop();

	// zoom in and zoom out buttons
	Button zoomIn, zoomOut, btnSave, btnShare, btnPreview, btnNew;

	// ImageView for the original (scaled) image
	ImageView imageView;

	// Bitmap for the original image (scaled)
	Bitmap imageBitmap;

	// Bitmap for holding the realtime obscured image
	Bitmap obscuredBmp;

	// Canvas for drawing the realtime obscuring
	Canvas obscuredCanvas;

	// Paint obscured
	Paint obscuredPaint;

	// The original image dimensions (not scaled)
	int originalImageWidth;
	int originalImageHeight;

	// So we can give some haptic feedback to the user
	Vibrator vibe;

	// Original Image Uri
	Uri originalImageUri;

	// sample sized used to downsize from native photo
	int inSampleSize;

	// Saved Image Uri
	Uri savedImageUri;

	// Constant for temp filename
	public final static String TMP_FILE_NAME = "tmp.jpg";

	public final static String TMP_FILE_DIRECTORY = "/Android/data/org.witness.sscphase1/files/";

	/*
	 * handles threaded events for the UI thread
	 * 
	 * @author 11A Bach Ngoc Tuan
	 */
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {

			case 3: // completed
				mProgressDialog.dismiss();

				// Toast autodetectedToast = Toast.makeText(ImageEditor.this,
				// result + " face(s) detected", Toast.LENGTH_SHORT);
				// autodetectedToast.show();

				break;
			default:
				super.handleMessage(msg);
			}
		}

	};

	// UI for background threads
	ProgressDialog mProgressDialog;

	// Handles when we should do realtime preview and when we shouldn't
	boolean doRealtimePreview = true;

	// Keep track of the orientation
	private int originalImageOrientation = ExifInterface.ORIENTATION_NORMAL;

	// for saving images
	private final static String EXPORT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	/*
	 * pullPathFromUri
	 * 
	 * @param Uri originalUri
	 * 
	 * @author 11A Bach Ngoc Tuan
	 */
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

	@SuppressWarnings("unused")
	/*
	 * onCreate()
	 * 
	 * @author 11A Bach Ngoc Tuan
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String versNum = "";

		try {
			String pkg = getPackageName();
			versNum = getPackageManager().getPackageInfo(pkg, 0).versionName;
		} catch (Exception e) {
			versNum = "";
		}

		setTitle(getString(R.string.app_name) + " (" + versNum + ")");

		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.imageviewer);

		// Calculate the minimum distance
		minMoveDistance = minMoveDistanceDP
				* this.getResources().getDisplayMetrics().density + 0.5f;

		// The ImageView that contains the image we are working with
		imageView = (ImageView) findViewById(R.id.ImageEditorImageView);

		// Buttons for zooming
		zoomIn = (Button) this.findViewById(R.id.ZoomIn);
		zoomOut = (Button) this.findViewById(R.id.ZoomOut);
		zoomIn.setOnClickListener(this);
		zoomOut.setOnClickListener(this);

		// Instantiate the vibrator
		vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		// Passed in from CameraObscuraMainMenu
		originalImageUri = getIntent().getData();

		// If originalImageUri is null, we are likely coming from another app
		// via "share"
		if (originalImageUri == null) {
			if (getIntent().hasExtra(Intent.EXTRA_STREAM)) {
				originalImageUri = (Uri) getIntent().getExtras().get(
						Intent.EXTRA_STREAM);
			} else if (getIntent().hasExtra("bitmap")) {
				Bitmap b = (Bitmap) getIntent().getExtras().get("bitmap");
				setBitmap(b);

				boolean autodetect = true;

				if (autodetect) {

					mProgressDialog = ProgressDialog.show(this, "",
							"Detecting faces...", true, true);

					doAutoDetectionThread();

				}

				originalImageWidth = b.getWidth();
				originalImageHeight = b.getHeight();
				return;

			}
		}

		// Load the image if it isn't null
		if (originalImageUri != null) {

			// Get the orientation
			File originalFilename = pullPathFromUri(originalImageUri);
			try {
				ExifInterface ei = new ExifInterface(
						originalFilename.getAbsolutePath());
				originalImageOrientation = ei.getAttributeInt(
						ExifInterface.TAG_ORIENTATION,
						ExifInterface.ORIENTATION_NORMAL);
				debug(MainActivity.TAG, "Orientation: "
						+ originalImageOrientation);
			} catch (IOException e1) {
				debug(MainActivity.TAG, "Couldn't get Orientation");
				e1.printStackTrace();
			}

			// debug(MainActivity.TAG,"loading uri: " +
			// pullPathFromUri(originalImageUri));

			// Load up smaller image
			try {
				// Load up the image's dimensions not the image itself
				BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
				bmpFactoryOptions.inJustDecodeBounds = true;
				// Needs to be this config for Google Face Detection
				bmpFactoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
				// Parse the image
				Bitmap loadedBitmap = BitmapFactory.decodeStream(
						getContentResolver().openInputStream(originalImageUri),
						null, bmpFactoryOptions);

				// Hold onto the unscaled dimensions
				originalImageWidth = bmpFactoryOptions.outWidth;
				originalImageHeight = bmpFactoryOptions.outHeight;
				// If it is rotated, transpose the width and height
				// Should probably look to see if there are different rotation
				// constants being used
				if (originalImageOrientation == ExifInterface.ORIENTATION_ROTATE_90
						|| originalImageOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
					int tmpWidth = originalImageWidth;
					originalImageWidth = originalImageHeight;
					originalImageHeight = tmpWidth;
				}

				// Get the current display to calculate ratios
				Display currentDisplay = getWindowManager().getDefaultDisplay();

				// Ratios between the display and the image
				double widthRatio = Math.floor(bmpFactoryOptions.outWidth
						/ currentDisplay.getWidth());
				double heightRatio = Math.floor(bmpFactoryOptions.outHeight
						/ currentDisplay.getHeight());

				// If both of the ratios are greater than 1,
				// one of the sides of the image is greater than the screen
				if (heightRatio > widthRatio) {
					// Height ratio is larger, scale according to it
					inSampleSize = (int) heightRatio;
				} else {
					// Width ratio is larger, scale according to it
					inSampleSize = (int) widthRatio;
				}

				bmpFactoryOptions.inSampleSize = inSampleSize;

				// Decode it for real
				bmpFactoryOptions.inJustDecodeBounds = false;
				loadedBitmap = BitmapFactory.decodeStream(getContentResolver()
						.openInputStream(originalImageUri), null,
						bmpFactoryOptions);
				debug(MainActivity.TAG, "Was: " + loadedBitmap.getConfig());

				if (loadedBitmap == null) {
					debug(MainActivity.TAG, "bmp is null");

				} else {
					// Only dealing with 90 and 270 degree rotations, might need
					// to check for others
					if (originalImageOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
						debug(MainActivity.TAG, "Rotating Bitmap 90");
						Matrix rotateMatrix = new Matrix();
						rotateMatrix.postRotate(90);
						loadedBitmap = Bitmap.createBitmap(loadedBitmap, 0, 0,
								loadedBitmap.getWidth(),
								loadedBitmap.getHeight(), rotateMatrix, false);
					} else if (originalImageOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
						debug(MainActivity.TAG, "Rotating Bitmap 270");
						Matrix rotateMatrix = new Matrix();
						rotateMatrix.postRotate(270);
						loadedBitmap = Bitmap.createBitmap(loadedBitmap, 0, 0,
								loadedBitmap.getWidth(),
								loadedBitmap.getHeight(), rotateMatrix, false);
					}

					setBitmap(loadedBitmap);

					boolean autodetect = true;

					if (autodetect) {
						// Do auto detect popup

						mProgressDialog = ProgressDialog.show(this, "",
								"Detecting faces...", true, true);

						doAutoDetectionThread();
					}
				}
			} catch (IOException e) {
				Log.e(MainActivity.TAG,
						"error loading bitmap from Uri: " + e.getMessage(), e);
			}

		}

	}

	/*
	 * setBitmap
	 * 
	 * @param Bitmap
	 * 
	 * @author 11A Bach Ngoc Tuan
	 */
	private void setBitmap(Bitmap nBitmap) {
		imageBitmap = nBitmap;

		// Get the current display to calculate ratios
		Display currentDisplay = getWindowManager().getDefaultDisplay();

		float matrixWidthRatio = (float) currentDisplay.getWidth()
				/ (float) imageBitmap.getWidth();
		float matrixHeightRatio = (float) currentDisplay.getHeight()
				/ (float) imageBitmap.getHeight();

		// Setup the imageView and matrix for scaling
		float matrixScale = matrixHeightRatio;

		if (matrixWidthRatio < matrixHeightRatio) {
			matrixScale = matrixWidthRatio;
		}

		imageView.setImageBitmap(imageBitmap);

		// Set the OnTouch and OnLongClick listeners to this (ImageEditor)
		imageView.setOnTouchListener(this);
		imageView.setOnClickListener(this);

		// PointF midpoint = new PointF((float)imageBitmap.getWidth()/2f,
		// (float)imageBitmap.getHeight()/2f);
		matrix.postScale(matrixScale, matrixScale);

		// This doesn't completely center the image but it get's closer
		// int fudge = 42;
		matrix.postTranslate(
				(float) ((float) currentDisplay.getWidth() - (float) imageBitmap
						.getWidth() * (float) matrixScale) / 2f,
				(float) ((float) currentDisplay.getHeight() - (float) imageBitmap
						.getHeight() * matrixScale) / 2f);

		imageView.setImageMatrix(matrix);
	}

	/*
	 * Auto detecion thread
	 * 
	 * @author 11A Bach Ngoc Tuan
	 */
	private void doAutoDetectionThread() {
		Thread thread = new Thread() {
			public void run() {
				doAutoDetection();
				Message msg = mHandler.obtainMessage(3);
				mHandler.sendMessage(msg);
			}
		};
		thread.start();
	}

	/*
	 * Auto detecion
	 * 
	 * @author 11A Bach Ngoc Tuan
	 */
	private int doAutoDetection() {
		// This should be called via a pop-up/alert mechanism

		ArrayList<DetectedFace> dFaces = runFaceDetection();

		if (dFaces == null)
			return 0;

		// for (int adr = 0; adr < autodetectedRects.length; adr++) {

		Iterator<DetectedFace> itDFace = dFaces.iterator();

		while (itDFace.hasNext()) {
			DetectedFace dFace = itDFace.next();

			// debug(ActivityMain.TAG,"AUTODETECTED imageView Width, Height: " +
			// imageView.getWidth() + " " + imageView.getHeight());
			// debug(ActivityMain.TAG,"UNSCALED RECT:" +
			// autodetectedRects[adr].left + " " + autodetectedRects[adr].top +
			// " " + autodetectedRects[adr].right + " " +
			// autodetectedRects[adr].bottom);

			RectF autodetectedRectScaled = new RectF(dFace.bounds.left,
					dFace.bounds.top, dFace.bounds.right, dFace.bounds.bottom);

			// debug(ActivityMain.TAG,"SCALED RECT:" +
			// autodetectedRectScaled.left + " " + autodetectedRectScaled.top +
			// " " + autodetectedRectScaled.right + " " +
			// autodetectedRectScaled.bottom);

			// Probably need to map autodetectedRects to scaled rects
			// debug(ActivityMain.TAG,"MAPPED RECT:" +
			// autodetectedRects[adr].left + " " + autodetectedRects[adr].top +
			// " " + autodetectedRects[adr].right + " " +
			// autodetectedRects[adr].bottom);

			float faceBuffer = (autodetectedRectScaled.right - autodetectedRectScaled.left) / 5;

			boolean isLast = !itDFace.hasNext();

			createImageRegion((autodetectedRectScaled.left - faceBuffer),
					(autodetectedRectScaled.top - faceBuffer),
					(autodetectedRectScaled.right + faceBuffer),
					(autodetectedRectScaled.bottom + faceBuffer), isLast,
					isLast);
		}

		return dFaces.size();
	}

	/*
	 * The actual face detection calling method
	 * 
	 * @author 11A Bach Ngoc Tuan
	 */
	private ArrayList<DetectedFace> runFaceDetection() {
		ArrayList<DetectedFace> dFaces;

		try {
			Bitmap bProc = toGrayscale(imageBitmap);
			GoogleFaceDetection gfd = new GoogleFaceDetection(bProc.getWidth(),
					bProc.getHeight());
			int numFaces = gfd.findFaces(bProc);
			dFaces = gfd.getFaces(numFaces);
		} catch (NullPointerException e) {
			dFaces = null;
		}
		return dFaces;
	}

	private Bitmap toGrayscale(Bitmap bmpOriginal) {
	    int width, height;
	    height = bmpOriginal.getHeight();
	    width = bmpOriginal.getWidth();    

	    Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
	    Canvas c = new Canvas(bmpGrayscale);
	    Paint paint = new Paint();
	    ColorMatrix cm = new ColorMatrix();
	    cm.setSaturation(0);
	    
	    ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
	    
	    paint.setColorFilter(f);
	 
	    c.drawBitmap(bmpOriginal, 0, 0, paint);
	    
	    
	    
	    return bmpGrayscale;
	}

	/*
	 * For debug
	 * 
	 * @param tag & message
	 * 
	 * @author 11A Bach Ngoc Tuan
	 */
	private void debug(String tag, String message) {
		Log.d(tag, message);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

}