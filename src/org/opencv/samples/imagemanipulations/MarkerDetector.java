package org.opencv.samples.imagemanipulations;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.util.Log;

public class MarkerDetector {
	MatOfPoint2f tmpContour = new MatOfPoint2f();
	MatOfPoint2f outerCurve = new MatOfPoint2f();
	MatOfPoint2f innerCurve = new MatOfPoint2f();
	Mat hierarchy = new Mat();
	

	public List<Marker> detect(Mat rgba) {
		Mat gray = new Mat();
		Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGB2GRAY);
		Imgproc.blur(gray, gray, new Size(3, 3));

		Mat thrs = new Mat();
		Imgproc.threshold(gray, thrs, 0, 255, Imgproc.THRESH_OTSU);
		gray.release();
		// Imgproc.Canny(gray, thrs, 150, 200, 3, true);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(thrs, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
		
		thrs.release();
		List<Marker> detected = new ArrayList<Marker>();

		for (int i = 0; i < contours.size(); i++) {
			int child = (int) hierarchy.get(0, i)[2];
			if (child < 0)
				continue;
			if (hierarchy.get(0, child)[2] < 0)
				continue;
			contours.get(i).convertTo(tmpContour, CvType.CV_32FC2);
			Imgproc.approxPolyDP(tmpContour, outerCurve, 0.1 * tmpContour.height(), true);
			//Log.i("OUTER", "" + outerCurve.height());

			if (outerCurve.height() != 4)
				continue;

			contours.get(child).convertTo(tmpContour, CvType.CV_32FC2);
			Imgproc.approxPolyDP(tmpContour, innerCurve, 0.1 * tmpContour.height(), true);
			//Log.i("INNER", "" + innerCurve.height());
			if (innerCurve.height() != 6)
				continue;
			detected.add(new Marker(innerCurve.toList(), outerCurve.toList()));
			// int innerIndex = (int) hierarchy.get(0, (int) child)[2];
			// contours.get(innerIndex).convertTo(c, CvType.CV_32FC2);
			// Imgproc.approxPolyDP(c, curve, 0.1 * c.height(), true);
			//
			// int innerOuterVertxNum = curve.height();
			//
			// innerIndex = (int) hierarchy.get(0, innerIndex)[2];
			// contours.get(innerIndex).convertTo(c, CvType.CV_32FC2);
			//
			// Imgproc.approxPolyDP(c, curve, 0.1 * c.height(), true);
			// if (curve.height() == innerOuterVertxNum)
			// Log.i("KOD ", "" + innerOuterVertxNum);
			// // Core.circle(rgba, contours.get((int)
			// // child).get(row, col), 3, new Scalar(0,255,255));
			//
			// Imgproc.drawContours(rgba, contours, innerIndex, new Scalar(255,
			// 0, 0));
			// }
			//
			// }
			
			// }
		}
		
		for(MatOfPoint p : contours)
			p.release();
		return detected;
	}
}
