package com.example.myfirstdemo.RequestUtil;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AllData {

    @SerializedName("code")
    public String code;
    @SerializedName("msg")
    public String msg;

    @SerializedName("data")
    public List<Data1> Data1List;

    public static class Data1{
        @SerializedName("startdatetime")
        public String startTime;
        @SerializedName("enddatetime")
        public String endTime;
        @SerializedName("originator")
        public String peopleNumber;
        @SerializedName("theme")
        public String theme;
    }
}
