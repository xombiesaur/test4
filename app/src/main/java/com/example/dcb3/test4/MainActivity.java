package com.example.dcb3.test4;

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;


import android.app.Application;
import android.os.Bundle;
//import android.os.Handler;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.app.Activity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.TextView;


public class MainActivity extends Activity implements OnTouchListener, CvCameraViewListener2 {
    private static final String  TAG              = "OCVSample::Activity";

    private boolean              mIsColorSelected = false;
    private Mat                  mRgba;
    private Scalar               mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private ColorBlobDetector    mDetector;
    private Mat                  mSpectrum;
    private Size                 SPECTRUM_SIZE;
    private Scalar               CONTOUR_COLOR;
    private int                  tX;
    private int                  tY;
    private int                  flagcount;
    public TextView              t;
    public CharSequence          bigtext;
    android.os.Handler texthandle = new android.os.Handler();
    private Runnable             runnable;
/*    public class hopethisworks extends Thread {
        private static final String TAG = "hopethisworks";

        @Override
        public void run() {

                int xx = mDetector.gettX();
                int yy = mDetector.gettY();
                bigtext = "" + xx + "" + yy;

        }
        private void updatetext(){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
        }
    }*/

    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(MainActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.color_blob_detection_surface_view);


        runnable = new Runnable() {
            @Override
            public void run() {
                    t = (TextView) findViewById(R.id.textview);
                    //bigtext = "center";
                    t.setText(bigtext);
                    texthandle.postDelayed(this,100);
            }
        };
        texthandle.post(runnable);
        runnable.run();




        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);

//        hopethisworks textthread = new hopethisworks();
//        textthread.start();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
    @Override
    public void onBackPressed(){
        mIsColorSelected = false;
        bigtext = "";
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {

        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mDetector = new ColorBlobDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255,0,0,255);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public boolean onTouch(View v, MotionEvent event) {
        int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;

        mDetector.setXY(x,y);

        //Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        //Core.circle(mRgba,(x,y),100,(0,0,255));
        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        Rect touchedRect = new Rect();

        //touchedRect.x = (x>4) ? x-4 : 0;
        //touchedRect.y = (y>4) ? y-4 : 0;
        touchedRect.x = 2;
        touchedRect.y = 2;

        //dcb:changed from 4 to 1
        //this checks if the box is inside the frame
        touchedRect.width = (x+1 < cols) ? x + 1 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+1 < rows) ? y + 1 - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);

        //Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
        //        ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");

        mDetector.setHsvColor(mBlobColorHsv);

        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        mIsColorSelected = true;
        //flagcount = 100;

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return false; // don't need subsequent touch events
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        //setContentView(R.layout.color_blob_detection_surface_view);
        //t = (TextView) findViewById(R.id.textview);

        mRgba = inputFrame.rgba();
       /* if (flagcount == 0){
            mIsColorSelected = false;
        }
        */
        if (mIsColorSelected) {
            int cols = mRgba.cols();
            int rows = mRgba.rows();
           mDetector.process(mRgba);
            Imgproc.drawContours(mRgba, mDetector.getContours(), -1, CONTOUR_COLOR, 2, 4, mDetector.getContours().get(0), 0, new Point(0, 0));

            int xx = mDetector.gettX();
            int yy = mDetector.gettY();
            if(yy>(rows/2)) {
                if (xx > (cols / 3)) {
                    if (xx > ((cols / 3) * 2)) {
                        bigtext = "Bottom Right";
                    } else {
                        bigtext = "Bottom Center";
                    }
                } else {
                    bigtext = "Bottom Left";
                }
            }
            else{
                if (xx > (cols / 3)) {
                    if (xx > ((cols / 3) * 2)) {
                        bigtext = "Top Right";
                    } else {
                        bigtext = "Top Center";
                    }
                } else {
                    bigtext = "Top Left";
                }
            }
            texthandle.post(runnable);

            Mat colorLabel = mRgba.submat(104, 124, 104, 124);
            colorLabel.setTo(mBlobColorRgba);

            Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
            mSpectrum.copyTo(spectrumLabel);
            //flagcount -= 1;
        }

        return mRgba;
    }

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }


}
