package com.example.myfirstdemo.Helper;

import android.util.Log;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static android.content.ContentValues.TAG;

/**
 * Created by bobi on 2019/3/8.
 */


public class MyFaceHelper {
    private FaceEngine faceEngine;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean frThreadRunning = false;
    /**
     * fr 线程数，建议和ft初始化时的maxFaceNum相同
     */
    private int frThreadNum = 5;
    private MyFaceListener faceListener;
    private LinkedBlockingQueue<FaceRecognizeRunnable> faceRecognizeRunnables;


    private MyFaceHelper(Builder builder) {
        faceEngine = builder.faceEngine;
        faceListener = builder.faceListener;

        if (builder.frThreadNum > 0) {
            frThreadNum = builder.frThreadNum;
            faceRecognizeRunnables = new LinkedBlockingQueue<>(frThreadNum);
        } else {
            Log.e(TAG, "frThread num must > 0,now using default value:" + frThreadNum);
        }
    }


    /**
     * 请求获取人脸特征数据，需要传入FR的参数，以下参数同 AFR_FSDKEngine.AFR_FSDK_ExtractFRFeature
     *
     * @param nv21     NV21格式的图像数据
     * @param faceInfo 人脸信息
     * @param width    图像宽度
     * @param height   图像高度
     * @param format   图像格式
     */
    public void requestFaceFeature(byte[] nv21, FaceInfo faceInfo, int width, int height, int format) {
        if (faceListener != null) {
            if (faceEngine != null && faceRecognizeRunnables != null && faceRecognizeRunnables.size() < frThreadNum && !frThreadRunning) {
                faceRecognizeRunnables.add(new FaceRecognizeRunnable(nv21, faceInfo, width, height, format));
                executor.execute(faceRecognizeRunnables.poll());
            } else {
                faceListener.onFaceFeatureInfoGet(null);
            }
        }
    }

    public void release() {
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
        if (faceRecognizeRunnables != null) {
            faceRecognizeRunnables.clear();
        }
        faceRecognizeRunnables = null;
        faceListener = null;
    }

    /**
     * 人脸解析的线程
     */
    public class FaceRecognizeRunnable implements Runnable {
        private FaceInfo faceInfo;
        private int width;
        private int height;
        private int format;
        private byte[] nv21Data;

        private FaceRecognizeRunnable(byte[] nv21Data, FaceInfo faceInfo, int width, int height, int format) {
            if (nv21Data == null) {
                return;
            }
            this.nv21Data = nv21Data;
            this.faceInfo = new FaceInfo(faceInfo);
            this.width = width;
            this.height = height;
            this.format = format;
        }

        @Override
        public void run() {
            frThreadRunning = true;
            if (faceListener != null && nv21Data != null) {
                if (faceEngine != null) {
                    FaceFeature faceFeature = new FaceFeature();
                    long frStartTime = System.currentTimeMillis();
                    int frCode;
                    synchronized (MyFaceHelper.this) {
                        frCode = faceEngine.extractFaceFeature(nv21Data, width, height, format, faceInfo, faceFeature);
                    }
                    if (frCode == ErrorInfo.MOK) {
//                        Log.i(TAG, "run: fr costTime = " + (System.currentTimeMillis() - frStartTime) + "ms");
                        faceListener.onFaceFeatureInfoGet(faceFeature);
                    } else {
                        faceListener.onFaceFeatureInfoGet(null);
                        faceListener.onFail(new Exception("fr failed errorCode is " + frCode));
                    }
                } else {
                    faceListener.onFaceFeatureInfoGet(null);
                    faceListener.onFail(new Exception("fr failed ,frEngine is null"));
                }
                if (faceRecognizeRunnables != null && faceRecognizeRunnables.size() > 0) {
                    executor.execute(faceRecognizeRunnables.poll());
                }
            }
            nv21Data = null;
            frThreadRunning = false;
        }
    }

    public static final class Builder {
        private FaceEngine faceEngine;
        private MyFaceListener faceListener;
        private int frThreadNum;


        public Builder() {
        }


        public Builder faceEngine(FaceEngine val) {
            faceEngine = val;
            return this;
        }




        public Builder faceListener(MyFaceListener val) {
            faceListener = val;
            return this;
        }

        public Builder frThreadNum(int val) {
            frThreadNum = val;
            return this;
        }



        public MyFaceHelper build() {
            return new MyFaceHelper(this);
        }
    }
}
