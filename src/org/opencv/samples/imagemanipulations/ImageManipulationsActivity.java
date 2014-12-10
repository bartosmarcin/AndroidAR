package org.opencv.samples.imagemanipulations;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

public class ImageManipulationsActivity extends Activity implements CvCameraViewListener2 {
	private CameraBridgeViewBase mOpenCvCameraView;
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				System.loadLibrary("gray");
				mOpenCvCameraView.enableView();
				detector = new MarkerDetector();
				initCameraMat();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.image_manipulations_surface_view);

		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.image_manipulations_activity_surface_view);
		mOpenCvCameraView.setCvCameraViewListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.e("PAUSED", "Mfcker paused");
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
	}

	public void onCameraViewStarted(int width, int height) {
	}

	public void onCameraViewStopped() {
	}

	// public native int gray(long matAddrRgba);

	private MarkerDetector detector;
	private Marker marker;
	private Mat rgba;

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		rgba = inputFrame.rgba();
		List<Marker> detectedMarkers = detector.detect(rgba);
		for (Marker marker : detectedMarkers) {
			Point center = marker.getCenter();
			Core.circle(rgba, center, 3, new Scalar(255, 0, 0));
			List<Point> markerPoints = marker.getSortedPoints();
			Core.circle(rgba, markerPoints.get(0), 3, new Scalar(255, 255, 0));
			Core.circle(rgba, markerPoints.get(1), 3, new Scalar(255, 0, 255));
		}
		if(oldPoints3f != null && detectedMarkers.size() > 0)
		{
			Marker detectedMarker = detectedMarkers.get(0);			
			List<Point> newPoints = detectedMarker.getSortedPoints();
			String n = "";
			for(int i=0; i<newPoints.size(); i++)
				n += "("+newPoints.get(i).x+","+newPoints.get(i).y+"), ";
			Log.i("NEW POINTS", n);
				
			
			
			
			MatOfPoint2f newPoint2f = new MatOfPoint2f();
			newPoint2f.fromList(newPoints);
			Mat rvec = new Mat();
			Mat tvec = new Mat();
			MatOfDouble K = new MatOfDouble();
			Calib3d.solvePnP(oldPoints3f, newPoint2f, cameraMat, K, rvec, tvec);
					
			Log.i("TVEC", tvec.dump());
			Log.i("RVEC", rvec.dump());



			Point3[] ar_pts = {new Point3(200,200,0), new Point3(200,300,0), new Point3(300,200,0), new Point3(200,200,100)};
			
			MatOfPoint3f ar_verts = new MatOfPoint3f();
			MatOfPoint2f projPts = new MatOfPoint2f();
			ar_verts.fromArray(ar_pts);
			Calib3d.projectPoints(ar_verts, rvec, tvec, cameraMat, K, projPts);
			aps = projPts.toArray();
			
			
		}
		
		if(aps != null){
			Core.line(rgba, aps[0], aps[1], new Scalar(0,255,0));
			Core.line(rgba, aps[0], aps[2], new Scalar(255,0,0));
			Core.line(rgba, aps[0], aps[3], new Scalar(0,0,255));
		}
		// Mat thrs = new Mat();
		//
		// Imgproc.threshold(gray, thrs, 0, 255, Imgproc.THRESH_OTSU);
		//
		// List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		// Mat hierarchy = new Mat();
		// Imgproc.findContours(thrs, contours, hierarchy, Imgproc.RETR_TREE,
		// Imgproc.CHAIN_APPROX_SIMPLE);
		//
		// for (int i = 0; i < contours.size(); i++)
		// Imgproc.drawContours(rgba, contours, i, new Scalar(255, 0, 0));
		// gray(rgba.getNativeObjAddr());
		return rgba;
	}

	MatOfPoint3f oldPoints3f;

	public void onClick(View view){
		Log.i("LOL", "CLICKED!");
		List<Marker> detectedMarkers = detector.detect(rgba);

		if(detectedMarkers.size() < 0)
			return;
		
		if(marker == null)
		{	
			marker = detectedMarkers.get(0);
			List<Point> oldPoints = marker.getSortedPoints();
			List<Point3> listOldPoints3 = new ArrayList<Point3>();			
			
			String o ="";
			for(int i=0; i<oldPoints.size(); i++){
				listOldPoints3.add(new Point3(oldPoints.get(i)));
				o += "("+oldPoints.get(i).x+","+oldPoints.get(i).y+"), ";
					
			}
			Log.i("OLD POINTS", o);
			oldPoints3f = new MatOfPoint3f();
			oldPoints3f.fromList(listOldPoints3);
			
		}

	}
	Point[] aps;
	Mat cameraMat;
	private void initCameraMat(){
		double f = 1f;
		cameraMat = Mat.zeros(3,3,CvType.CV_32FC1);
		cameraMat.put(0, 0, f);
		cameraMat.put(1, 1, f);
		
		//Jesli f != 1 zmienic !
		cameraMat.put(2, 2, 1f);
		
		cameraMat.put(0, 2, 0.5 * 800.0);
		cameraMat.put(1, 2, 0.5 * 400.0);
		
		
	}
}
