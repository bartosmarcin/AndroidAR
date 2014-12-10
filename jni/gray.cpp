#include <jni.h>

#include <opencv2/opencv.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <vector>

using namespace cv;
using namespace std;

vector< vector<Point> > qr_markers;
vector< vector<Point> > find_qr_markers(vector< vector<Point> > contours, vector<Vec4i> hierarchy){
    qr_markers.clear();
    int child = -1;
    vector<Point> approx;
    vector<Point> orientationPoint;
    for(int i = 0; i < contours.size(); i++) {
        child = hierarchy[i][2];
        if(child != -1)
            if(hierarchy[child][2] != -1){
                approxPolyDP(contours[i], approx, double(contours[i].size()) * 0.1, true);
                if(approx.size() == 4)
                    qr_markers.push_back(approx);
                else {
                    approxPolyDP(contours[child], approx, double(contours[child].size()) * 0.1, true);
                    if(approx.size() == 4)
                    {    for(int j=0; j<4; j++)
                            orientationPoint.push_back(approx[j]);
                    }
                }
            }
    }
    qr_markers.push_back(orientationPoint);
    return qr_markers;
}



RotatedRect enclosingRectangle(vector< vector<Point> > qrMarkers){
    vector<Point> allPoints = qrMarkers[0];
    for(int i = 1; i < qrMarkers.size(); i++)
        allPoints.insert(allPoints.end(), qrMarkers[i].begin(), qrMarkers[i].end());
    return minAreaRect(allPoints);
}




Mat gray,thrs;
vector<Vec4i> hierarchy;
vector< vector<Point> > contours;

void detect(Mat frame){
        cvtColor(frame, gray, COLOR_BGR2GRAY );
        blur(gray, gray, Size(3,3));
        //Canny(gray, thrs, 150, 200, 3 );
        threshold(gray, thrs, 50, 250, THRESH_OTSU);

        findContours(thrs, contours, hierarchy, RETR_TREE, CHAIN_APPROX_NONE);
        vector< vector<Point> > qr_markers = find_qr_markers(contours, hierarchy);
        vector<Point> centers;
        for(int i = 0; i < qr_markers.size(); i++) {
            if(qr_markers[i].size() != 4)
                continue;
            float x=0;
            float y=0;
            for(int j =0; j< qr_markers[i].size(); j++){
                Point p = qr_markers[i][j];
                x+=p.x;
                y+=p.y;
            }
            x /= qr_markers[i].size();
            y /= qr_markers[i].size();
            centers.push_back(Point(x,y));
            circle(frame, Point(x,y), 3, Scalar(255,255,0));

            drawContours(frame, qr_markers, i, Scalar(255,0,0));
            Point2f vertices[4];
            RotatedRect rect = enclosingRectangle(qr_markers);
            rect.points(vertices);
            for(int j=0; j<4; j++)
                line(frame, vertices[j], vertices[(j+1)%4], Scalar(0,255,255));
        }
        hierarchy.clear();

}


extern "C" {

JNIEXPORT jint JNICALL Java_org_opencv_samples_imagemanipulations_ImageManipulationsActivity_gray(JNIEnv*, jobject, jlong addrRgba, jlong addrGray);

JNIEXPORT jint JNICALL Java_org_opencv_samples_imagemanipulations_ImageManipulationsActivity_gray(JNIEnv*, jobject, jlong addrRgba, jlong addrGray) {

    Mat& mRgb = *(Mat*)addrRgba;

    detect(mRgb);

    int conv=1;
    jint retVal;

    retVal = (jint)conv;

    return retVal;

}

}

