package com.example.myfirstdemo.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.myfirstdemo.RequestUtil.AllData;

import com.google.gson.Gson;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONObject;

import okhttp3.Call;

public class AutoUpdateService extends Service {


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 更新当天会议列表
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        int anHour = 15 * 60 * 1000;    //15分钟更新一次
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this,AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this,0,i,0);
        alarmManager.cancel(pi);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 获取数据并将数据放在缓存里
     */
    private void updateData(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String responseText = prefs.getString("meeting_data",null);

        if (responseText!=null){
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
                                        .getDefaultSharedPreferences(AutoUpdateService.this).edit();
                                editor.clear();
                                editor.putString("meeting_data", response);
                                editor.apply();
                            }
                        }
                    });
        }
    }


    private AllData parseJSONWithGSON(String jsonData){
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            Gson gson = new Gson();
            AllData allData = gson.fromJson(jsonObject.toString(), AllData.class);
            return allData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
