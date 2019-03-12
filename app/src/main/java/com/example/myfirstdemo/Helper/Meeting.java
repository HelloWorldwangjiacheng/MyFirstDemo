package com.example.myfirstdemo.Helper;

public class Meeting {
    private String theme;
    private String peopleNumber;
    private String startTime;
    private String endTime;

    public Meeting(String theme, String peopleNumber, String startTime, String endTime) {
        this.theme = theme;
        this.peopleNumber = peopleNumber;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getPeopleNumber() {
        return peopleNumber;
    }

    public void setPeopleNumber(String peopleNumber) {
        this.peopleNumber = peopleNumber;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
