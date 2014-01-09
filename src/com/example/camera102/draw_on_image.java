package com.example.camera102;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
/* package/detection face
***
@author 11B Vu Tien Dung
*/

public class draw_on_image extends Activity {
	private ImageView img;
	private Bitmap bitmap;
	private int numberface, maxface = 50;
	private FaceDetector dect;
	 FaceDetector.Face[] face;
	private PointF mypoint = new PointF();
	private Paint mypaint = new Paint();
	private Uri originalImageUri;
		@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imagepreview);
		img = (ImageView) findViewById(R.id.PreviewImageView);
		init();


	}

	private void init() {
		
		// setContentView(new draw(this));
		BitmapFactory.Options BitmapFactoryOptionsbfo = new BitmapFactory.Options();
		BitmapFactoryOptionsbfo.inPreferredConfig = Bitmap.Config.RGB_565;
		originalImageUri = getIntent().getData();
		try {
			bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), originalImageUri);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		img.setImageBitmap(bitmap);
		if(bitmap!=null){
		FaceDetector decto = new FaceDetector(bitmap.getWidth(), bitmap.getHeight(), maxface);
		face = new FaceDetector.Face[maxface];
		numberface = decto.findFaces(bitmap, face);
		Toast.makeText(getApplicationContext(), "Face Count: " + String.valueOf(numberface), Toast.LENGTH_SHORT).show();
		setContentView(new draw(this));
		}
	}
	class draw extends View {
		public draw(Context context){
			super(context);
		}
		public void onDraw(Canvas canvas){
			canvas.drawBitmap(bitmap, 0, 0, null);
			mypaint.setColor(Color.RED);
			mypaint.setStyle(Paint.Style.STROKE);
			mypaint.setStrokeWidth(3);
			for (int i = 0; i < numberface; i++) {
				FaceDetector.Face fa = face[i];
			 	fa.getMidPoint(mypoint);
				canvas.drawLine(mypoint.x-30, mypoint.y+30, mypoint.x-100, mypoint.y+30, mypaint);
		}}
	}
}
