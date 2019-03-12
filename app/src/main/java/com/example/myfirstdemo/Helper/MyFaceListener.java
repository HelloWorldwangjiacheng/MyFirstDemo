package com.example.myfirstdemo.Helper;

import android.support.annotation.Nullable;

import com.arcsoft.face.FaceFeature;

public interface MyFaceListener {
    /**
     * 当出现异常时执行
     *
     * @param e 异常信息
     */
    void onFail(Exception e);


    /**
     * 请求人脸特征后的回调
     *
     * @param faceFeature    人脸特征数据
     */
    void  onFaceFeatureInfoGet(@Nullable FaceFeature faceFeature);
}
