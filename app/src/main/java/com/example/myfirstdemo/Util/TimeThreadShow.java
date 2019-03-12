package com.example.myfirstdemo.Util;

import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeThreadShow extends Thread{
    public TextView tvDate1;
    public TextView tvTime;
    private int msgKey1 = 222;
    public TimeThreadShow(TextView tvDate,TextView tvTime){
        this.tvDate1 = tvDate;
        this.tvTime = tvTime;
    }

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 222:
                    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy年MM月dd日");
                    SimpleDateFormat sdf2 = new SimpleDateFormat(" HH:mm:ss");
                    SimpleDateFormat sdf3 = new SimpleDateFormat("HH:mm");
                    String date1 = sdf1.format(new Date(System.currentTimeMillis()));
                    String date2 = sdf2.format(new Date(System.currentTimeMillis()));
                    tvDate1.setText(date1+getWeek());
                    tvTime.setText(date2);


                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void run() {
        super.run();
        do {
            try{
                Thread.sleep(1000);
                Message msg = new Message();
                msg.what = msgKey1;
                mHandler.sendMessage(msg);
            }catch (InterruptedException e){
                e.printStackTrace();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }while(true);
    }

    public static String getWeek(){
        Calendar cal = Calendar.getInstance();
        int i = cal.get(Calendar.DAY_OF_WEEK);
        String week = "";
        switch(i){
            case 1:
                week="星期一";
                break;
            case 2:
                week="星期二";
                break;
            case 3:
                week="星期三";
                break;
            case 4:
                week="星期四";
                break;
            case 5:
                week="星期五";
                break;
            case 6:
                week="星期六";
                break;
            case 7:
                week="星期日";
                break;
            default:
                break;
        }
        return week;
    }
}
