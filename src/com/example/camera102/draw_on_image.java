package com.example.camera102;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
/* package/detection face
***
@author 11B Vu Tien Dung
*/
public class draw_on_image extends Activity{
	private Bitmap bitmap;
	private FaceDetector detect;
	private FaceDetector.Face[] face;
	Paint mypaint = new Paint();
	PointF mypoint = new PointF();
	private ImageView img;
	private int NUMBER_OF_FACES;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		img = (ImageView)findViewById(R.id.imageview1);
		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		detect = new FaceDetector(bitmap.getWidth(),bitmap.getHeight(),NUMBER_OF_FACES);
		face = new FaceDetector.Face[NUMBER_OF_FACES];
		NUMBER_OF_FACES=detect.findFaces(bitmap, face);
		}
	class Face_Detection_View extends View{
	    public Face_Detection_View(Context context) {
				super(context);
				// TODO Auto-generated constructor stub
				
			}
		public void onDraw(Canvas canvas)
	    {
	    	canvas.drawBitmap(bitmap, 0, 0, null);
	    	for(int i=0;i<NUMBER_OF_FACES;i++)
	    	{
	    		FaceDetector.Face fa = face[i];
	    		mypaint.setColor(Color.RED);
	    		mypaint.setAlpha(100);
	    		fa.getMidPoint(mypoint);
	    		canvas.drawCircle(mypoint.x, mypoint.y, fa.eyesDistance(), mypaint);
	    		
	    	}
	    	
	    }
	    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
