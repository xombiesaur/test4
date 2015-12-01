package com.example.dcb3.test4;

/**
 * Created by dcb3 on 10/21/15.
 * Edited by npr216 on 10/27/15.
 */

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

class Pixel<X,Y> {
    private X x;
    private Y y;
    public Pixel(X x, Y y){
        this.x = x;
        this.y = y;
    }
    public X getX(){
        return x;
    }
    public Y gety(){
        return y;
    }
    public void setL(X x){
        this.x = x;
    }
    public void setR(Y y){
        this.y = y;
    }
}
public class ColorBlobDetector {

    private static final String  TAG              = "colorblobdetec::";
    // Lower and Upper bounds for range checking in HSV color space
    private Scalar mLowerBound = new Scalar(0);
    private Scalar mUpperBound = new Scalar(0);
    // Minimum contour area in percent for contours filtering
    private static double mMinContourArea = 0.05;
    // Color radius for range checking in HSV color space
    private Scalar mColorRadius = new Scalar(25,50,50,0);
    private Mat mSpectrum = new Mat();
    private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();
    private List<Point> pointList = new ArrayList<Point>();
    private int tX = 0;
    private int tY = 0;


    // Cache
    Mat zeros = new MatOfInt();
    Mat mPyrDownMat = new Mat();
    Mat mHsvMat = new Mat();
    Mat mMask = new Mat();
    Mat mDilatedMask = new Mat();
    Mat mHierarchy = new Mat();
    Mat mHueMat = new Mat();
    Mat HsvS = new Mat();
    Mat HsvV = new Mat();
    List<Mat> mlHsv = new ArrayList(3);
    List<Mat> mlHsv2 = new ArrayList(3);

    public void setColorRadius(Scalar radius) {
        mColorRadius = radius;
    }

    public void setHsvColor(Scalar hsvColor) {
        double minH = (hsvColor.val[0] >= mColorRadius.val[0]) ? hsvColor.val[0]-mColorRadius.val[0] : 0;
        double maxH = (hsvColor.val[0]+mColorRadius.val[0] <= 255) ? hsvColor.val[0]+mColorRadius.val[0] : 255;

        mLowerBound.val[0] = minH;
        mUpperBound.val[0] = maxH;

        mLowerBound.val[1] = hsvColor.val[1] - mColorRadius.val[1];
        mUpperBound.val[1] = hsvColor.val[1] + mColorRadius.val[1];

        mLowerBound.val[2] = hsvColor.val[2] - mColorRadius.val[2];
        mUpperBound.val[2] = hsvColor.val[2] + mColorRadius.val[2];

        mLowerBound.val[3] = 0;
        mUpperBound.val[3] = 255;

        Mat spectrumHsv = new Mat(1, (int)(maxH-minH), CvType.CV_8UC3);

        for (int j = 0; j < maxH-minH; j++) {
            byte[] tmp = {(byte)(minH+j), (byte)255, (byte)255};
            spectrumHsv.put(0, j, tmp);
        }

        Imgproc.cvtColor(spectrumHsv, mSpectrum, Imgproc.COLOR_HSV2RGB_FULL, 4);
    }

    public Mat getSpectrum() {
        return mSpectrum;
    }

    public void setMinContourArea(double area) {
        mMinContourArea = area;
    }

    public void process(Mat rgbaImage) {
        mPyrDownMat=rgbaImage;
        //Imgproc.pyrDown(rgbaImage, mPyrDownMat);
        //Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);

        Imgproc.cvtColor(mPyrDownMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);


        //dcb3: my algorithm
        //splits out hue mat from hsv mat
        Core.split(mHsvMat, mlHsv);
        mHueMat = mlHsv.get(0);

        int tempr = mHueMat.rows();
        int tempc = mHueMat.cols();
        //set sero mat to a bunch of zeros
     //   zeros = new MatOfInt(tempr,tempc);
     //   zeros=zeroset(zeros);
        //grab the hue value for the touch point pixel
        double[] pixel = mHueMat.get(tY, tX);
        double testHue = pixel[0];
        Log.i(TAG, "Hue is " + pixel[0]);//+","+pixel[1]+","+pixel[2]);
        //X is col Y is row
        getBound(testHue, tX, tY);

        MatOfPoint pointmat = new MatOfPoint();
        pointmat.fromList(pointList);
        mContours.add(pointmat);


    }

    public Mat getMask() {
        return mHsvMat;
    }
    public List<MatOfPoint> getContours() {
        return mContours;
    }

    public void setXY(int x, int y){
        tX =x;
        tY = y;
    }
    public int gettX() {
        return tX;
    }
    public int gettY() {
        return tY;
    }

    private void getBound(double hue, int c, int r){
        double testHue = hue;
        Point inpoint = new Point(c,r);
        //Log.i(TAG, "you made it!");
        if((c < mHueMat.cols()) && r < mHueMat.rows()){
            //  c max = 960         r max = 750
            double [] testHueA = mHueMat.get(r,c);
            if (!pointList.contains(inpoint)){
                //Log.i(TAG,"dos");
                //Log.i(TAG, r +" "+ c );
                if((testHueA[0] <= hue+2)&&(testHueA[0] >= hue-2)) {
                    //Log.i(TAG,"tres");
                    pointList.add(inpoint);
                    inpoint = new Point(c+1,r);
                    if (!pointList.contains(inpoint)) {
                        getBound(testHueA[0], c + 1, r);
                    }
                    inpoint = new Point(c+1,r+1);
                    if (!pointList.contains(inpoint)) {
                        getBound(testHueA[0], c + 1, r +1);
                    }
                    inpoint = new Point(c,r+1);
                    if (!pointList.contains(inpoint)) {
                        getBound(testHueA[0], c , r + 1);
                    }
                    inpoint = new Point(c-1,r+1);
                    if (!pointList.contains(inpoint)) {
                        getBound(testHueA[0], c - 1, r + 1);
                    }
                    inpoint = new Point(c-1,r);
                    if (!pointList.contains(inpoint)) {
                        getBound(testHueA[0], c - 1, r);
                    }
                    inpoint = new Point(c-1,r-1);
                    if (!pointList.contains(inpoint)) {
                        getBound(testHueA[0], c - 1, r - 1);
                    }
                    inpoint = new Point(c,r-1);
                    if (!pointList.contains(inpoint)) {
                        getBound(testHueA[0], c , r -1);
                    }
                    inpoint = new Point(c+1,r-1);
                    if (!pointList.contains(inpoint)) {
                        getBound(testHueA[0], c + 1, r -1);
                    }
                    //Log.i(TAG, "all surronding pixels tested");
                    //Log.i(TAG, r +" "+ c );
                }
            }
        }
    }
    public MatOfInt zeroset(MatOfInt matin){
        for(int a=0;a<matin.rows();a++){
            //a is row b is column
            for(int b =0;b<matin.cols();b++){
                matin.put(a,b,0);
            }
        }
        return matin;
    }
}
