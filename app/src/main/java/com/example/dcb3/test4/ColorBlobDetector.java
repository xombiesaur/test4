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
    private int tX = 0;
    private int tY = 0;


    // Cache
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


        //grab the hue value for the touch point pixel
        double[] pixel = mHueMat.get(tY, tX);
        double testHue = pixel[0];
        Log.i(TAG, "Hue is "+pixel[0]);//+","+pixel[1]+","+pixel[2]);
        //use getbound to get the x and y bounds for the shape
        int[][] boundList = new int[4][2];
        boundList[0] = getBound(testHue,1,0);
        boundList[1] = getBound(testHue,0,1);
        boundList[2] = getBound(testHue,-1,0);
        boundList[3] = getBound(testHue,0,-1);

        //Log.i(TAG, "bounds "+Arrays.deepToString(boundList));
        //find the average of the object
        int bll = boundList.length;
        int newX = tX;
        int newY = tY;
        for(int i=0; i<bll; i++){
            newX += boundList[i][0];
            newY += boundList[i][1];
        }
        //set it as the neww "touch" input
        tX = (newX/bll);
        tY = (newY/bll);
        Rect boundbox = new Rect(boundList[2][0],boundList[3][1],(boundList[0][0]-boundList[2][0]),(boundList[1][1]-boundList[3][1]));




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

    private int[] getBound(double hue, int xAdd, int yAdd){
        double testHue = hue;
        int xPos = tX;
        int yPos = tY;

        boolean BFlag = true;
        //check hue value for pixel then increment to next pixel as directed by input
        do {
            double [] testHueA = mHueMat.get(yPos,xPos);
            Log.i(TAG, "xy , "+xPos+","+yPos);
            //Log.i(TAG, "color,  "+testHueA[0]);
            double testHue2 = testHueA[0];
            if(testHue2<=(testHue+5)&& testHue2 >= (testHue-5)){
                testHue = testHue2;

            }
            else{BFlag = false;}
            xPos += xAdd;
            yPos += yAdd;

        } while(BFlag && (xPos > 0 && yPos > 0));
        int[] corPair = {xPos,yPos};
        //Log.i(TAG, "x y out, "+xPos+","+yPos);
        return corPair;

    }
}
