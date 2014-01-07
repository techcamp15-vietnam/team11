/**
 * Image Preview class
 * @athor 11A Bach Ngoc Tuan
 */

package com.example.camera102;

import java.io.IOException;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class ImagePreview extends Activity implements OnClickListener {
	
	public final static String IMAGEURI = "passedimage";
	
	ImageView imageView;
	Uri imageUri;
	Bitmap imageBitmap;
			
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imagepreview);
        
        Bundle passedBundle = getIntent().getExtras();
        if (passedBundle.containsKey(IMAGEURI)) {
        	
        	imageUri = Uri.parse(passedBundle.getString(IMAGEURI));
        	
            imageView = (ImageView) findViewById(R.id.PreviewImageView);
            imageView.setOnClickListener(this);
            
            try {
				// Load up the image's dimensions not the image itself
				BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
				
				// Parse the image
				imageBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri), null, bmpFactoryOptions);
		
				imageView.setImageBitmap(imageBitmap);
								
			} catch (IOException e) {
				e.printStackTrace();
			}            
        }
        else {
        	// Not passed in, nothing to display
        	finish();
        }
        
    }
    
	public void onClick(View view) {
		finish();
	}

    /*
     * Handling screen configuration changes ourselves, we don't want the activity to restart on rotation
     */
    @Override
    public void onConfigurationChanged(Configuration conf) 
    {
        super.onConfigurationChanged(conf);
    }
}
