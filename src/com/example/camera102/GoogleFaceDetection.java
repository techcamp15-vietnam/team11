package com.example.camera102;
/* package/detection face
***
@author 11B Vu Tien Dung
*/
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;

public class GoogleFaceDetection {

	public static final String LOGTAG = "GoogleFaceDetection";

	public static int MAX_FACES = 10;

	Face[] faces = new Face[MAX_FACES];
	FaceDetector faceDetector;

	int numFaces = 0;

	public final static float CONFIDENCE_FILTER = .15f;

	public GoogleFaceDetection(int width, int height) {

		faceDetector = new FaceDetector(width, height, MAX_FACES);

	}

	public class DetectedFace {

		public RectF bounds;
		public PointF midpoint;
		public float eyeDistance;
	}

	public interface FaceDetection {
		int findFaces(Bitmap bmp); // returns number of faces

		ArrayList<DetectedFace> getFaces(int numberFound); // returns array of rectangles of
		                                                     // found faces
															 
	}

	public int findFaces(Bitmap bmp) {

		numFaces = faceDetector.findFaces(bmp, faces);
		return numFaces;
	}

	public ArrayList<DetectedFace> getFaces(int foundFaces) {

		ArrayList<DetectedFace> dFaces = new ArrayList<DetectedFace>();

		for (int i = 0; i < foundFaces; i++) {

			if (faces[i].confidence() > CONFIDENCE_FILTER) {
				PointF midPoint = new PointF();

				float eyeDistance = faces[i].eyesDistance();
				faces[i].getMidPoint(midPoint);

				float widthBuffer = eyeDistance * 1.5f;
				float heightBuffer = eyeDistance * 2f;
				RectF faceRect = new RectF((midPoint.x - widthBuffer),
						(midPoint.y - heightBuffer),
						(midPoint.x + widthBuffer), (midPoint.y + heightBuffer));

				DetectedFace dFace = new DetectedFace();
				dFace.bounds = faceRect;
				dFace.midpoint = midPoint;
				dFace.eyeDistance = eyeDistance;

				dFaces.add(dFace);
			}
		}

		return dFaces;
	}

}