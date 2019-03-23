package com.example.myfirstdemo;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;


import com.example.myfirstdemo.Helper.MyFaceHelper;
import com.example.myfirstdemo.Helper.MyFaceListener;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.VersionInfo;


import com.example.myfirstdemo.Util.ConfigUtil;
import com.example.myfirstdemo.Util.DrawHelper;
import com.example.myfirstdemo.Util.camera.CameraHelper;
import com.example.myfirstdemo.Util.camera.CameraListener;

import com.example.myfirstdemo.Util.FaceRectView;
import com.example.myfirstdemo.model.DrawInfo;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import Decoder.BASE64Encoder;

import okhttp3.Call;
import okhttp3.MediaType;


public class RecognizeActivity extends AppCompatActivity implements ViewTreeObserver.OnGlobalLayoutListener {

    private int my_status_code = 0;
    private boolean isP = false;

    private String name;
    private MyFaceHelper faceHelper;
    private Timer timer;
    private volatile int bad_count = 0;


    private Boolean NOFACE_FOUND = true;

    private volatile Boolean isRecognizing = false;


    private static final String TAG = "PreviewActivity";
    private CameraHelper cameraHelper;
    private DrawHelper drawHelper;
    private Camera.Size previewSize;
    private Integer cameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private FaceEngine faceEngine;
    private int afCode = -1;
    private int processMask = FaceEngine.ASF_AGE | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_GENDER | FaceEngine.ASF_LIVENESS;
    /**
     * 相机预览显示的控件，可为SurfaceView或TextureView
     */
    private View previewView;
    private FaceRectView faceRectView;
    private TextView messageView;

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    /**
     * 所需的所有权限信息
     */
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE
    };


    IntentFilter intentFilter;
    NetWorkStateReceiver netWorkStateReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize);
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        netWorkStateReceiver = new NetWorkStateReceiver();
        registerReceiver(netWorkStateReceiver,intentFilter);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams attributes = getWindow().getAttributes();
            attributes.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            getWindow().setAttributes(attributes);
        }

        // Activity启动后就锁定为启动时的方向
        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            default:
                break;
        }



        previewView = findViewById(R.id.texture_preview);
        faceRectView = findViewById(R.id.face_rect_view);
        messageView = findViewById(R.id.messageView);
//        cameraBtn = findViewById(R.id.cameraBtn);
//        cameraBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
        //在布局结束后才做初始化操作
        previewView.getViewTreeObserver().addOnGlobalLayoutListener(this);


    }

    /**
     * 检测网络状态
     */
    class NetWorkStateReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {

            System.out.println("网络状态发生变化");
            //检测API是不是小于23，因为到了API23之后getNetworkInfo(int networkType)方法被弃用
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {

                //获得ConnectivityManager对象
                ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

                //获取ConnectivityManager对象对应的NetworkInfo对象
                //获取WIFI连接的信息
                NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                //获取移动数据连接的信息
                NetworkInfo dataNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
                    Toast.makeText(context, "WIFI已连接,移动数据已连接", Toast.LENGTH_SHORT).show();
                } else if (wifiNetworkInfo.isConnected() && !dataNetworkInfo.isConnected()) {
                    Toast.makeText(context, "WIFI已连接,移动数据已断开", Toast.LENGTH_SHORT).show();
                } else if (!wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
                    Toast.makeText(context, "WIFI已断开,移动数据已连接", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "WIFI已断开,移动数据已断开", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder dialog = new AlertDialog.Builder(RecognizeActivity.this);
                    dialog.setTitle("网络检测提醒");
                    dialog.setMessage("当前网络断开，请立即通知管理员，希望您的谅解，谢谢您的合作！");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    dialog.show();
                }
//API大于23时使用下面的方式进行网络监听
            }else {

                System.out.println("API level 大于23");
                //获得ConnectivityManager对象
                ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

                //获取所有网络连接的信息
//                Network[] networks = connMgr.getAllNetworks();
//                //用于存放网络连接信息
//                StringBuilder sb = new StringBuilder();
//                //通过循环将网络信息逐个取出来
//                for (int i=0; i < networks.length; i++){
//                    //获取ConnectivityManager对象对应的NetworkInfo对象
//                    NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
//                    sb.append(networkInfo.getTypeName() + " connect is " + networkInfo.isConnected());
//                }
//                Toast.makeText(context, sb.toString(),Toast.LENGTH_SHORT).show();

                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo!=null && networkInfo.isAvailable()){
                    Toast.makeText(context,"网络正常",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(context,"网络异常",Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder dialog = new AlertDialog.Builder(RecognizeActivity.this);
                    dialog.setTitle("网络检测提醒");
                    dialog.setMessage("当前网络断开，请立即通知管理员，希望您的谅解，谢谢您的合作！");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    dialog.show();
                }
            }
        }

    }






    private void initEngine() {
        faceEngine = new FaceEngine();
        afCode = faceEngine.init(this.getApplicationContext(), FaceEngine.ASF_DETECT_MODE_VIDEO, FaceEngine.ASF_OP_0_HIGHER_EXT,
                16, 20, FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_AGE | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_GENDER | FaceEngine.ASF_LIVENESS);
        VersionInfo versionInfo = new VersionInfo();
        faceEngine.getVersion(versionInfo);
        Log.i(TAG, "initEngine:  init: " + afCode + "  version:" + versionInfo);
        if (afCode != ErrorInfo.MOK) {
            Toast.makeText(this, getString(R.string.init_failed, afCode), Toast.LENGTH_SHORT).show();
        }

    }

    private void unInitEngine() {

        if (afCode == 0) {
            afCode = faceEngine.unInit();
            Log.i(TAG, "unInitEngine: " + afCode);
        }
    }


    @Override
    protected void onDestroy() {
        if (cameraHelper != null) {
            cameraHelper.release();
            cameraHelper = null;
        }
        //faceHelper中可能会有FR耗时操作仍在执行，加锁防止crash
        if (faceHelper != null) {
            synchronized (faceHelper) {
                unInitEngine();
            }
            faceHelper.release();
        } else {
            unInitEngine();
        }
        super.onDestroy();
    }

    private void deal_back(){
        Log.d("ERROR","111111111111111");
        AlertDialog.Builder dialog = new AlertDialog.Builder(RecognizeActivity.this);
        dialog.setTitle("人脸识别界面退出提醒");
        if (my_status_code == 1){
            dialog.setMessage("人脸识别成功，您已完成会议签到，当前会议室门禁已开请进入！五秒后自动退出");
        }else{
            dialog.setMessage("人脸识别失败，请您确认会议的时间和地点，谢谢您的合作！五秒后自动退出");
        }

        dialog.setCancelable(false);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        dialog.show();

    }

    private boolean checkPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this.getApplicationContext(), neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    private void initCamera() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final MyFaceListener faceListener = new MyFaceListener() {
            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "onFail: " + e.getMessage());
            }

            //请求FR的回调
            @Override
            public void onFaceFeatureInfoGet(@Nullable final FaceFeature faceFeature) {
                //FR成功
                if (faceFeature != null) {
                    JSONObject jsonObject = new JSONObject();
                    BASE64Encoder encoder = new BASE64Encoder();
                    String bytestr = encoder.encode(faceFeature.getFeatureData());
                    try {
                        jsonObject.put("bytestr", bytestr);
                        jsonObject.put("boardroomid", 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    OkHttpUtils.postString()//
                            .url("http://test.icms.work/api/v1/boardroom/facesearch")
                            .mediaType(MediaType.parse("application/json; charset=utf-8"))
                            .content(jsonObject.toString())
                            .build()
                            .execute(new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e, int id) {

                                }

                                @Override
                                public void onResponse(String response, int id) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(response);
                                        name = (String) jsonObject.getJSONObject("data").get("name");
                                        messageView.setText("检测成功，已完成签到");

                                        my_status_code = 1;
                                        if (isP == false){
                                            deal_back();
                                            isP = true;
                                        }
                                    } catch (JSONException e) {
                                        bad_count++;
                                        messageView.setText("检测失败，未能匹配到相关信息");
                                        Log.d("Re",bad_count+"");
                                    }
                                }
                            });
                }
            }

        };

        CameraListener cameraListener = new CameraListener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                Log.i(TAG, "onCameraOpened: " + cameraId + "  " + displayOrientation + " " + isMirror);
                previewSize = camera.getParameters().getPreviewSize();
                drawHelper = new DrawHelper(previewSize.width, previewSize.height, previewView.getWidth(), previewView.getHeight(), displayOrientation
                        , cameraId, isMirror);

                faceHelper = new MyFaceHelper.Builder()
                        .faceEngine(faceEngine)
                        .frThreadNum(5)
                        .faceListener(faceListener)
                        .build();
            }


            @Override
            public void onPreview(byte[] nv21, Camera camera) {
//                deal_back();
                if (bad_count > 120 && isP == false){
                    deal_back();
                    isP = true;
                }
                if (faceRectView != null) {
                    faceRectView.clearFaceInfo();
                }
                List<FaceInfo> faceInfoList = new ArrayList<>();
                int code = faceEngine.detectFaces(nv21, previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, faceInfoList);


                if (code == ErrorInfo.MOK) {
                    if (faceInfoList.size() < 1) {
                        messageView.setText("未检测到人脸");
                        bad_count++;
                        Log.d("Re",bad_count+"");
                        name = null;
                        return;
                    }
                    if (faceInfoList.size() > 1) {
                        messageView.setText("请保持画面中只有一个人");
                        return;
                    }
                    code = faceEngine.process(nv21, previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, faceInfoList, processMask);
                    if (code != ErrorInfo.MOK) {
                        if (code == ErrorInfo.MERR_FSDK_FACEFEATURE_LOW_CONFIDENCE_LEVEL){
                            messageView.setText("人脸置信度低");
                        }
                        bad_count++;
                        Log.d("Re",bad_count+"");
                        return;
                    }
                } else {
                    return;
                }

                List<AgeInfo> ageInfoList = new ArrayList<>();
                List<GenderInfo> genderInfoList = new ArrayList<>();
                List<LivenessInfo> faceLivenessInfoList = new ArrayList<>();
                int ageCode = faceEngine.getAge(ageInfoList);
                int genderCode = faceEngine.getGender(genderInfoList);
                int livenessCode = faceEngine.getLiveness(faceLivenessInfoList);

                //有其中一个的错误码不为0，return
                if ((ageCode | genderCode | livenessCode) != ErrorInfo.MOK) {
                    return;
                }


////////////////////////////////
//                if (!isRecognizing) {
//                    synchronized (isRecognizing) {
//                        isRecognizing = true;

                if (faceLivenessInfoList.get(0).getLiveness() == 1) {
                    faceHelper.requestFaceFeature(nv21, faceInfoList.get(0), previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21);
                } else {
                    messageView.setText("活体检测失败");
                }
//                    }
//                }

/////////////////////////////


                if (faceRectView != null && drawHelper != null) {
                    List<DrawInfo> drawInfoList = new ArrayList<>();
                    for (int i = 0; i < faceInfoList.size(); i++) {
                        drawInfoList.add(new DrawInfo(faceInfoList.get(i).getRect(), genderInfoList.get(i).getGender(), ageInfoList.get(i).getAge(), faceLivenessInfoList.get(i).getLiveness(), name));
                    }
                    drawHelper.draw(faceRectView, drawInfoList);
                }
                //name = null;
            }

            @Override
            public void onCameraClosed() {
                Log.i(TAG, "onCameraClosed: ");
            }

            @Override
            public void onCameraError(Exception e) {
                Log.i(TAG, "onCameraError: " + e.getMessage());
            }

            @Override
            public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
                if (drawHelper != null) {
                    drawHelper.setCameraDisplayOrientation(displayOrientation);
                }
                Log.i(TAG, "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
            }
        };
        cameraHelper = new CameraHelper.Builder()
                .previewViewSize(new Point(previewView.getMeasuredWidth(), previewView.getMeasuredHeight()))
                .rotation(getWindowManager().getDefaultDisplay().getRotation())
                .specificCameraId(cameraID != null ? cameraID : Camera.CameraInfo.CAMERA_FACING_FRONT)
                .isMirror(false)
                .previewOn(previewView)
                .cameraListener(cameraListener)
                .build();
        cameraHelper.init();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            boolean isAllGranted = true;
            for (int grantResult : grantResults) {
                isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
            }
            if (isAllGranted) {
                initEngine();
                initCamera();
                if (cameraHelper != null) {
                    cameraHelper.start();
                }
            } else {
                Toast.makeText(this.getApplicationContext(), R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 在{@link #previewView}第一次布局完成后，去除该监听，并且进行引擎和相机的初始化
     */
    @Override
    public void onGlobalLayout() {
        previewView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        } else {
            initEngine();
            initCamera();
//            initTimer();
        }
    }

    private void initTimer() {
        final Timer finish_timer = new Timer();
//        finish_timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                if (bad_count>1000){
//                    deal_back();
//                    Log.d("ERROR","33333333333333333");
//                    // finish_timer.cancel();
//                }
////                finish();
////                Toast.makeText(RecognizeActivity.this, "检测失败，请在3秒之后再进行检测", Toast.LENGTH_SHORT).show();
//
//            }
//        }, 5000, 5000);

    }
}
