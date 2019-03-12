package com.example.myfirstdemo;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.bumptech.glide.Glide;
import com.example.myfirstdemo.Helper.Meeting;
import com.example.myfirstdemo.RequestUtil.AllData;
import com.example.myfirstdemo.Util.HorizontalListView;
import com.example.myfirstdemo.Util.TimeThreadShow;
import com.example.myfirstdemo.Util.adapter.HorizontalListViewAdapter;
import com.example.myfirstdemo.common.Constants;
import com.example.myfirstdemo.fragment.AdminFragment;
import com.example.myfirstdemo.fragment.HelpFragment;
import com.example.myfirstdemo.fragment.RQCodeFragment;
import com.example.myfirstdemo.service.AutoUpdateService;
import com.google.gson.Gson;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;

public class MainActivity extends AppCompatActivity {


    TextView currentMeetingStaus;
    Boolean isMeeting = false;

    Runnable mapRefreshRun;
    private Handler handler = new Handler();
    private List<Meeting> meetingList = new ArrayList<>();

    private Toast toast = null;
    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public DrawerLayout dl;
    RelativeLayout rlRight;


    TextView showDate;
    TextView showTime;
    ImageButton plannedImageButton;
    ImageButton helpImageButton;
    ImageButton adminImageButton;

    RQCodeFragment rqCodeFragment;
    AdminFragment adminFragment;
    HelpFragment helpFragment;

    ImageView image11;

    private HorizontalListView mHorizontalListView;
    private HorizontalListViewAdapter mHorizontalListViewAdapter;

    TextView time_text;
    TextView number_text;
    TextView theme_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar1 = (Toolbar) findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar1);
        //去除默认的title显示
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //        testRequestServer();
        /////////
        /////
        OkHttpUtils
                .post()
                .url("http://test.icms.work/api/v1/boardroom/todayMeetting?")
                .addParams("room_id", "1")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(final String response, int id) {
//                        final String responseText = response;
                        Log.d("MainActivity", response);
                        final AllData allData = parseJSONWithGSON(response);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (allData != null && "成功".equals(allData.msg)) {
                                    SharedPreferences.Editor editor = PreferenceManager
                                            .getDefaultSharedPreferences(MainActivity.this).edit();
                                    editor.clear();
                                    editor.apply();
                                    editor.putString("meeting_data", response);
                                    editor.apply();
                                }
                            }
                        });

                    }
                });

//        initList();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String responseText = prefs.getString("meeting_data",null);

        initView();

        if (responseText!=null) fun(responseText);
        for (Meeting meeting : meetingList){
            Log.d("MainActivity",meeting.getTheme()+"|"+meeting.getPeopleNumber());
        }


        //定时自动读取缓存中的数据
//        useHandler();


        mHorizontalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Meeting meeting = meetingList.get(position);
                Toast.makeText(MainActivity.this,
                        meeting.getPeopleNumber(),
                        Toast.LENGTH_SHORT).show();

                time_text.setText(meeting.getStartTime().substring(11, 16) + "~" + meeting.getEndTime().substring(11, 16));
                number_text.setText(meeting.getPeopleNumber());
                theme_text.setText(meeting.getTheme());
            }
        });

    }


    class Thread1 implements Runnable{

        @Override
        public void run() {
            //实时读取显示TextView里面的字符串
            do {
                try{
                    Thread.sleep(1000);
                    final String currentTime_text = showTime.getText().toString().substring(0,5);
                    for (Meeting meeting : meetingList){
                        if (currentTime_text.compareTo(meeting.getStartTime())>=0 && currentTime_text.compareTo(meeting.getEndTime())<=0) isMeeting = true;
                        else isMeeting = false;
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isMeeting) currentMeetingStaus.setText("正在开会");
                            else currentMeetingStaus.setText("空闲中");
                        }
                    });
                }catch(Exception e){
                    e.printStackTrace();
                }
            }while(true);


        }
    }


    void useHandler(){

        mapRefreshRun = new Runnable() {
            @Override
            public void run() {


                //更新地图上的数据
                update1();
                Toast.makeText(MainActivity.this, "已完成数据更新，当前为最新会议室安排", Toast.LENGTH_SHORT).show();
                //将本runnable继续插入主线程中，再次执行。
                handler.postDelayed(this, 1000*60*10);
            }
        };
        handler.postDelayed(mapRefreshRun, 1000*60*10);
    }

    private void update1(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String responseText = prefs.getString("meeting_data",null);

        AllData allData = parseJSONWithGSON(responseText);

        meetingList.clear();

        for (AllData.Data1 data1 : allData.Data1List) {
            Log.d("MainActivity", data1.theme + "|" + data1.startTime + "~" + data1.endTime + "| 人数为" + data1.peopleNumber);
            meetingList.add(new Meeting(data1.theme, data1.peopleNumber, data1.startTime, data1.endTime));
            for (Meeting mm : meetingList) {
                Log.d("MainActivity", mm.getTheme() + "|" + mm.getStartTime() + "~" + mm.getEndTime() + "| 人数为" + mm.getPeopleNumber());
            }
        }

        mHorizontalListViewAdapter = new HorizontalListViewAdapter(MainActivity.this, meetingList);
        mHorizontalListView.setAdapter(mHorizontalListViewAdapter);
        if (meetingList!=null&&meetingList.size()>0){
            Meeting meeting = meetingList.get(0);
            time_text.setText(meeting.getStartTime().substring(11, 16) + "~" + meeting.getEndTime().substring(11, 16)+"人");
            number_text.setText(meeting.getPeopleNumber());
            theme_text.setText(meeting.getTheme());
        }else{
            time_text.setText("该会议室当前时段没有会议");
        }
    }

    /**
     * 解析缓存中的字符串然后将其刷新到listview上面，形成默认页面
     * @param response
     */
    private void fun(String response){
        AllData allData = parseJSONWithGSON(response);

        meetingList.clear();

        for (AllData.Data1 data1 : allData.Data1List) {
            Log.d("MainActivity", data1.theme + "|" + data1.startTime + "~" + data1.endTime + "| 人数为" + data1.peopleNumber);
            meetingList.add(new Meeting(data1.theme, data1.peopleNumber, data1.startTime, data1.endTime));
            for (Meeting mm : meetingList) {
                Log.d("MainActivity", mm.getTheme() + "|" + mm.getStartTime() + "~" + mm.getEndTime() + "| 人数为" + mm.getPeopleNumber());
            }
        }
        mHorizontalListView = (HorizontalListView) findViewById(R.id.horizontal_lv);
        mHorizontalListViewAdapter = new HorizontalListViewAdapter(MainActivity.this, meetingList);
        mHorizontalListView.setAdapter(mHorizontalListViewAdapter);
        if (meetingList!=null&&meetingList.size()>0){
            Meeting meeting = meetingList.get(0);
            time_text.setText(meeting.getStartTime().substring(11, 16) + "~" + meeting.getEndTime().substring(11, 16)+"人");
            number_text.setText(meeting.getPeopleNumber());
            theme_text.setText(meeting.getTheme());
        }else{
            time_text.setText("该会议室当前时段没有会议");
        }


        Intent intent = new Intent(this,AutoUpdateService.class);
        startService(intent);
    }
    //得到具体的各个组件的实例
    private void initView() {

        dl = (DrawerLayout) findViewById(R.id.drawerlayout);
        rlRight = (RelativeLayout) findViewById(R.id.right);

        showDate = (TextView) findViewById(R.id.date);
        showTime = (TextView) findViewById(R.id.time);

        adminImageButton = (ImageButton) findViewById(R.id.admin);
        helpImageButton = (ImageButton) findViewById(R.id.help);
        plannedImageButton = (ImageButton) findViewById(R.id.planned);

        image11 = (ImageView) findViewById(R.id.image11);
        Glide.with(this).load(R.drawable.room1).override(600, 400).into(image11);

        rqCodeFragment = new RQCodeFragment();
        adminFragment = new AdminFragment();
        helpFragment = new HelpFragment();
        //开启一个线程来进行实时的时间显示
        TimeThreadShow timeThreadUtil = new TimeThreadShow(showDate, showTime);
        timeThreadUtil.start();

        Thread1 stausThread = new Thread1();
        new Thread(stausThread).start();


        //显示任意时段会议的具体信息
        time_text = (TextView) findViewById(R.id.time_text);
        number_text = (TextView) findViewById(R.id.number_text);
        theme_text = (TextView) findViewById(R.id.theme_text);

        currentMeetingStaus = (TextView)findViewById(R.id.using_text);
//        dealResponse(meetingList);


//        recyclerView = (RecyclerView)findViewById(R.id.horizontal_lv);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//        recyclerView .setLayoutManager(layoutManager);
//        HRecyclerViewAdapter adapter = new HRecyclerViewAdapter(meetingList);
//        recyclerView.setAdapter(adapter);

    }

    private void testRequestServer(){
        OkHttpUtils
                .post()
                .url("http://test.icms.work/api/v1/boardroom/todayMeetting?")
                .addParams("room_id", "1")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.d("MainActivity", response);
                        AllData allData = parseJSONWithGSON(response);

                        if (allData != null && "成功".equals(allData.msg)) {
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(MainActivity.this).edit();
//                            editor.clear();
                            editor.putString("meeting_data", response);
                            editor.apply();
                        }
//                        meetingList.clear();
//                        int i = 0;
//                        for (AllData.Data1 data1 : allData.Data1List) {
//                            Log.d("MainActivity", data1.theme + "|" + data1.startTime + "~" + data1.endTime + "| 人数为" + data1.peopleNumber);
//                            meetingList.add(new Meeting(data1.theme, data1.peopleNumber, data1.startTime, data1.endTime));
//                            for (Meeting mm : meetingList) {
//                                Log.d("MainActivity", mm.getTheme() + "|" + mm.getStartTime() + "~" + mm.getEndTime() + "| 人数为" + mm.getPeopleNumber());
//                            }
//                        }
                    }
                });
    }

    public void initList() {


//        OkHttpUtils
//                .post()
//                .url("http://test.icms.work/api/v1/boardroom/todayMeetting?")
//                .addParams("room_id", "1")
//                .build()
//                .execute(new StringCallback() {
//                    @Override
//                    public void onError(Call call, Exception e, int id) {
//                        e.printStackTrace();
//                    }
//
//                    @Override
//                    public void onResponse(String response, int id) {
//                        Log.d("MainActivity", response);
//                        AllData allData = parseJSONWithGSON(response);
//
//                        if (allData != null && "成功".equals(allData.msg)) {
//                            SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();
//                            editor.clear();
//                            editor.putString("meeting_data", response);
//                            editor.apply();
//                        }
//                    }
//                });
//
//
//        SharedPreferences p = getSharedPreferences("data",MODE_PRIVATE);
//        String meetingString = p.getString("meeting_data","");
//        AllData allData = parseJSONWithGSON(meetingString);
//        meetingList.clear();
//        int i = 0;
//        for (AllData.Data1 data1 : allData.Data1List) {
//            Log.d("MainActivity", data1.theme + "|" + data1.startTime + "~" + data1.endTime + "| 人数为" + data1.peopleNumber);
//            meetingList.add(new Meeting(data1.theme, data1.peopleNumber, data1.startTime, data1.endTime));
//            for (Meeting mm : meetingList) {
//                Log.d("MainActivity", mm.getTheme() + "|" + mm.getStartTime() + "~" + mm.getEndTime() + "| 人数为" + mm.getPeopleNumber());
//            }
//
//        }


//        meetingList.add(new Meeting("第十三届全国人民代表大会","7",
//                "2019-03-11T06:00:00.000+0000",
//                "2019-03-11T10:00:00.000+0000"));
//
//        meetingList.add(new Meeting("第十四届全国人民代表大会","8",
//                "2019-03-11T12:00:00.000+0000",
//                "2019-03-11T18:00:00.000+0000"));


    }

    private void dealResponse(final List<Meeting> meetingList) {
        /**
         * http://test.icms.work/api/v1/boardroom/todayMeetting?room_id=1
         */

        OkHttpUtils
                .post()
                .url("http://test.icms.work/api/v1/boardroom/todayMeetting?")
                .addParams("room_id", "1")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.d("MainActivity", response);
                        AllData allData = parseJSONWithGSON(response);
                        meetingList.clear();

                        int i = 0;
                        for (AllData.Data1 data1 : allData.Data1List) {
                            String s1 = data1.theme;
                            String s2 = data1.peopleNumber;
                            Log.d("MainActivity", data1.theme + "|" + data1.startTime + "~" + data1.endTime + "| 人数为" + data1.peopleNumber);
                            meetingList.add(new Meeting(data1.theme, data1.peopleNumber, data1.startTime, data1.endTime));
                            for (Meeting mm : meetingList) {
                                Log.d("MainActivity", mm.getTheme() + "|" + mm.getStartTime() + "~" + mm.getEndTime() + "| 人数为" + mm.getPeopleNumber());
                            }

                        }

                    }
                });
    }

    /**
     * "code":200,
     * "msg":"成功",
     * "data":[{
     * "default_id":0,
     * "room_id":1,
     * "originator":7,
     * "startdatetime":"2019-03-10T06:00:00.000+0000",
     * "enddatetime":"2019-03-10T10:00:00.000+0000",
     * "participant":"2",
     * "theme":"第十三届全国人民代表大会",
     * "remarks":"测试",
     * "state":0,
     * "createtime":null
     * }
     *
     * @param jsonData
     */
    private AllData parseJSONWithGSON(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
//            JSONArray jsonArray = jsonObject.getJSONArray("");
//            String DataContent = jsonArray.getJSONObject(0).toString();
//            return new Gson().fromJson(DataContent,AllData.class);
            Gson gson = new Gson();
            AllData allData = gson.fromJson(jsonObject.toString(), AllData.class);
            return allData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }




    public void openDooraAndSignin(View view) {

        activeEngine(view);
        startActivity(new Intent(this, RecognizeActivity.class));
    }

    public void activeEngine(final View view) {
        //检查权限 Manifest.permission.READ_PHONE_STATE
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
            return;
        }
        if (view != null) {
            //该view不响应点击
            view.setClickable(false);
        }

        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                FaceEngine faceEngine = new FaceEngine();
                int activeCode = faceEngine.active(MainActivity.this, Constants.APP_ID, Constants.SDK_KEY);
                emitter.onNext(activeCode);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer activeCode) {
                        if (activeCode == ErrorInfo.MOK) {
                            showToast(getString(R.string.active_success));
                        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                            showToast(getString(R.string.already_activated));
                        } else {
                            showToast(getString(R.string.active_failed, activeCode));
//                            activeEngine(view);
                        }

                        if (view != null) {
                            //当view不为空时,点击有效
                            view.setClickable(true);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    private boolean checkPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this, neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    //请求运行时权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            boolean isAllGranted = true;
            for (int grantResult : grantResults) {
                isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
            }
            if (isAllGranted) {
                activeEngine(null);
            } else {
                showToast(getString(R.string.permission_denied));
            }
        }
    }

    //自定义的toast提示,可以打断直接显示最新的
    private void showToast(String s) {
        //private Toast toast = null
        if (toast == null) {
            toast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
            toast.show();
        } else {
            toast.setText(s);
            toast.show();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.planned:
                //打开具有二维码的侧滑栏
                dl.openDrawer(GravityCompat.END);
                replaceFragment(rqCodeFragment);
                break;
            case R.id.admin:
                //打开管理员界面
                dl.openDrawer(GravityCompat.END);
                replaceFragment(adminFragment);
                break;
            case R.id.help:
                //打开帮助选项
                dl.openDrawer(GravityCompat.END);
                replaceFragment(helpFragment);
                break;
            default:
                break;
        }
        return true;
    }

    //替换碎片
    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmnet00, fragment);
        transaction.commit();
    }
}
