package com.example.camera102;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

public class DrawOnImage extends Activity {

	private ImageView img;
	private Bitmap bitmap;
	private Bitmap FaceBlur;
	private int numberface, maxface = 50;
	private FaceDetector dect;
	FaceDetector.Face[] face;

	private Paint mypaint = new Paint();
	private Uri originalImageUri;
	private PointF bitmap_point = new PointF();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imageviewer);
		img = (ImageView) findViewById(R.id.ImageEditorImageView);
		init();

	}

	/*
	 * package/detection face
	 * 
	 * @author 11B Vu Tien Dung
	 */
	private void init() {

		BitmapFactory.Options BitmapFactoryOptionsbfo = new BitmapFactory.Options();
		BitmapFactoryOptionsbfo.inPreferredConfig = Bitmap.Config.RGB_565;
		Bundle bundle = getIntent().getExtras();
		String path = bundle.getString("path");
		String path2 = bundle.getString("name");
		bitmap = BitmapFactory.decodeFile(path, BitmapFactoryOptionsbfo);
		if (bitmap == null)
			bitmap = BitmapFactory.decodeFile(path2, BitmapFactoryOptionsbfo);
		if (bitmap != null) {
			FaceDetector decto = new FaceDetector(bitmap.getWidth(),
					bitmap.getHeight(), maxface);
			face = new FaceDetector.Face[maxface];
			numberface = decto.findFaces(bitmap, face);
			Toast.makeText(getApplicationContext(),
					"Face Count: " + String.valueOf(numberface),
					Toast.LENGTH_SHORT).show();
			Bitmap NewBitmap;
			NewBitmap = FaceBlur(bitmap, face);
			img.setImageBitmap(NewBitmap);

		}

	}

	/*
	 * package/blur face
	 * 
	 * @param Bitmap
	 * 
	 * @author 11A Bach Ngoc Tuan
	 */
	private Bitmap FaceBlur(Bitmap bitmap, FaceDetector.Face[] faces) {
		int a = 0;
		FaceBlur = BitmapFactory.decodeResource(getResources(),
				R.drawable.faceblur);
		Bitmap NewBitmap = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), bitmap.getConfig());
		Canvas canvas = new Canvas(NewBitmap);
		canvas.drawBitmap(bitmap, 0, 0, null);
		for (int i = 0; i < numberface; i++) {
			FaceDetector.Face face = faces[i];
			face.getMidPoint(bitmap_point);
			a = (int) face.eyesDistance();

			Bitmap resize = Bitmap.createScaledBitmap(FaceBlur, (int) (4 * a),
					(int) (4.5 * a), false);
			canvas.drawBitmap(resize, (int) (bitmap_point.x - 2 * a),
					(int) (bitmap_point.y - 2.6 * a), null);
		}
		return NewBitmap;
	}

}
