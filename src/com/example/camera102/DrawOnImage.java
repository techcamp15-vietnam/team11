package com.example.camera102;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

public class DrawOnImage extends Activity {
	
	private ImageView img;
	private Bitmap bitmap;
	private Bitmap FaceBlur;
	private int numberface, maxface = 50;
	private FaceDetector dect;
	 FaceDetector.Face[] face;
	private PointF mypoint = new PointF();
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

	private void init() {
		
		// setContentView(new draw(this));
		
		
		BitmapFactory.Options BitmapFactoryOptionsbfo = new BitmapFactory.Options();
		BitmapFactoryOptionsbfo.inPreferredConfig = Bitmap.Config.RGB_565;
		String path =  getIntent().getStringExtra("name");
		bitmap = BitmapFactory.decodeFile(path, BitmapFactoryOptionsbfo);
		Toast.makeText(this, path, Toast.LENGTH_LONG).show();
		//img.setImageBitmap(bitmap);
		if(bitmap!=null){
		FaceDetector decto = new FaceDetector(bitmap.getWidth(), bitmap.getHeight(), maxface);
		face = new FaceDetector.Face[maxface];
		numberface = decto.findFaces(bitmap, face);
		Toast.makeText(getApplicationContext(), "Face Count: " + String.valueOf(numberface), Toast.LENGTH_SHORT).show();
	    Bitmap NewBitmap;
	    NewBitmap=FaceBlur(bitmap, face);
	    img.setImageBitmap(NewBitmap);
		//	setContentView(new ImageDraw(this));
		}
	}
	/* package/blur face
	***
	@author 11A Bach Ngoc Tuan
	*/
	private Bitmap FaceBlur(Bitmap bitmap,FaceDetector.Face[] faces) {
    	int a=0;
    	FaceBlur=BitmapFactory.decodeResource(getResources(), R.drawable.faceblur);
    	Bitmap NewBitmap = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(), bitmap.getConfig());
        Canvas canvas = new Canvas(NewBitmap);
        canvas.drawBitmap(bitmap, 0, 0, null);
        for(int i=0;i<numberface;i++)
    	{
    		FaceDetector.Face face = faces[i];
    		face.getMidPoint(bitmap_point);
    		a = (int) face.eyesDistance();
    		
    		Bitmap resize = Bitmap.createScaledBitmap(FaceBlur,(int)(4*a), (int)(4.5*a), false);
    		canvas.drawBitmap(resize ,(int)(bitmap_point.x -2*a) ,(int)(bitmap_point.y -2.6 *a), null);
    	}
        return NewBitmap;
    }
	 
	class ImageDraw extends View {
		public ImageDraw(Context context){
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
			 	for(int j=0;j<(2*fa.eyesDistance());j++){
					canvas.drawCircle(mypoint.x, mypoint.y, j, mypaint);}
				
		}}
	}
}
