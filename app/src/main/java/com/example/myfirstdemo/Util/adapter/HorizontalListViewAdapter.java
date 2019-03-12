package com.example.myfirstdemo.Util.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.myfirstdemo.Helper.Meeting;
import com.example.myfirstdemo.R;



import java.util.List;

public class HorizontalListViewAdapter extends BaseAdapter{
    List<Meeting> meetingList;
    private Context mContext;

    public HorizontalListViewAdapter(Context con, List<Meeting> objects) {
        mInflater = LayoutInflater.from(con);
        mContext = con;
        meetingList = objects;
    }
    @Override
    public int getCount() {
        return meetingList.size();
    }

    private LayoutInflater mInflater;

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh = null;
        if (convertView == null) {
            vh = new ViewHolder();
            convertView = mInflater.inflate(R.layout.horizontallistview_item, null);
            vh.dateTv = (TextView) convertView.findViewById(R.id.tv_date);
//            vh.titleTv = (TextView) convertView.findViewById(R.id.tv_weather);
            vh.viewRl = convertView.findViewById(R.id.viewLl);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        try {
            if (position<=getCount()&&position>=0){
                Meeting meeting = meetingList.get(position);
                vh.dateTv.setText(meeting.getStartTime().substring(11,16)+"~"+meeting.getEndTime().substring(11,16));
            }

        }catch (Exception e){

        }

        return convertView;
    }


    private  class ViewHolder {
        private View viewRl;
        private TextView dateTv;
        private TextView titleTv;
    }
}
