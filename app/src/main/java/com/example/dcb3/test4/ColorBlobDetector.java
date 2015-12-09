package com.example.dcb3.test4;

import android.graphics.Color;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.w3c.dom.Text;

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
    private List<Point> edgeList = new ArrayList<Point>();
    private List<Point> opointList = new ArrayList<Point>();
    private int tX = 0;
    private int tY = 0;
    private int xmax = 0;
    private int ymax = 0;
    private int xmin = 0;
    private int ymin = 0;



    Mat mPyrDownMat = new Mat();
    Mat mHsvMat = new Mat();
    Mat mHueMat = new Mat();
    List<Mat> mlHsv = new ArrayList(3);


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


    public void process(Mat rgbaImage) {
        mPyrDownMat=rgbaImage;
        //Imgproc.pyrDown(rgbaImage, mPyrDownMat);
        //Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);

        Imgproc.cvtColor(mPyrDownMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);

        //mPyrDownMat.release();

        //dcb3: my algorithm
        //splits out hue mat from hsv mat
        Core.split(mHsvMat, mlHsv);
        mHueMat = mlHsv.get(0);

        mHsvMat.release();

        mContours = new ArrayList<MatOfPoint>();
        pointList = new ArrayList<Point>();
        edgeList = new ArrayList<Point>();
        opointList = new ArrayList<Point>();

//        xmin = mPyrDownMat.cols();
//        ymin = mPyrDownMat.rows();
        xmin = mHueMat.cols();
        ymin = mHueMat.rows();
        xmax = 0;
        ymax = 0;


        //grab the hue value for the touch point pixel
        double[] pixel = mHueMat.get(tY, tX);
//        double[] pixel = mPyrDownMat.get(tY, tX);
//        float[] pixel2 = new float[3];
//        Color.RGBToHSV((int) pixel[0],(int) pixel[1],(int)pixel[2],pixel2);

        int testHue = (int)pixel[0];
        //
        //Log.i(TAG, "Hue is " + pixel2[0] +","+pixel2[1]+","+pixel2[2]);
        //X is col Y is row
        Point starthere = new Point(tX,tY);
        opointList.add(starthere);
        while(opointList.size()>0 && pointList.size() < 1600) {
            //Log.i(TAG,"while while while");
            getBound(testHue, opointList.get(0));
        }
        MatOfPoint pointmat = new MatOfPoint();
        pointmat.fromList(pointList);
        mContours.add(pointmat);
        tX = ((xmin+xmax)/2);
        tY = ((ymin+ymax)/2);
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

    private void getBound(int hue, Point checkPoint){
//        double testHue = hue;
        int c = (int)checkPoint.x;
        int r = (int)checkPoint.y;
        int j = 4;




        //Point inpoint = new Point(c,r);
       // Log.i(TAG, ""+pointList.size());

        if((checkPoint.x < mHueMat.cols()) && (checkPoint.y < mHueMat.rows())){
//        if((checkPoint.x < mPyrDownMat.cols()) && (checkPoint.y < mPyrDownMat.rows())){
            //  c max = 960         r max = 750
            //Log.i(TAG,"uno");
            double [] testHueA = mHueMat.get((int)checkPoint.y,(int)checkPoint.x);
//            double [] testHueA = mPyrDownMat.get((int)checkPoint.y,(int)checkPoint.x);
//            float [] testHueB = new float[3];
//            Color.RGBToHSV((int) testHueA[0],(int) testHueA[1],(int)testHueA[2],testHueB);
//            int checkhue = (int) testHueB[0];
            if (!pointList.contains(checkPoint)&&!edgeList.contains(checkPoint)){
                //Log.i(TAG,"dos");
                //Log.i(TAG, r +" "+ c );
                if((testHueA[0] <= hue+1)&&(testHueA[0] >= hue-1)) {
//                if((checkhue <= hue+1)&&(checkhue >= hue-1)) {
                    //Log.i(TAG,"tres");
                    ///pointList.add(inpoint);
                    pointList.add(checkPoint);
                    if(c>xmax){
                        xmax = c;
                    }
                    else if (c<xmin){
                        xmin = c;
                    }

                    if(r>ymax){
                        ymax = r;
                    }
                    else if (r<ymin){
                        ymin = r;
                    }

                    Point inpoint = new Point( c + j, r);
                    if (!pointList.contains(inpoint)) {
                        opointList.add(inpoint);
                    }
                    inpoint = new Point( c , r + j);
                    if (!pointList.contains(inpoint)) {
                        opointList.add(inpoint);
                    }
                    inpoint = new Point( c , r - j);
                    if (!pointList.contains(inpoint)) {
                        opointList.add(inpoint);
                    }
                    inpoint = new Point( c - j ,r + j);
                    if (!pointList.contains(inpoint)) {
                        opointList.add(inpoint);
                    }
                    inpoint = new Point( c - j, r );
                    if (!pointList.contains(inpoint)) {
                        opointList.add(inpoint);
                    }
                    inpoint = new Point( c + j, r - j);
                    if (!pointList.contains(inpoint)) {
                        opointList.add(inpoint);
                    }
                    inpoint = new Point( c + j, r + j);
                    if (!pointList.contains(inpoint)) {
                        opointList.add(inpoint);
                    }
                    inpoint = new Point( c - j, r - j);
                    if (!pointList.contains(inpoint)) {
                        opointList.add(inpoint);
                    }
                    opointList.remove(0);
                    //Log.i(TAG, "all surronding pixels tested");
                    //Log.i(TAG, r +" "+ c );
                }
                else{
                    //Log.i(TAG, checkPoint.y +" "+ checkPoint.x );
                    edgeList.add(checkPoint);
                    opointList.remove(0);
                }
            }
            else{

                opointList.remove(0);
            }
        }
        else{
            edgeList.add(checkPoint);
            opointList.remove(0);
        }

    }

}
