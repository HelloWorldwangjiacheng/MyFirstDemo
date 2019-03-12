package com.example.myfirstdemo.Util.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myfirstdemo.Helper.Meeting;
import com.example.myfirstdemo.R;

import java.util.List;

public class HRecyclerViewAdapter extends RecyclerView.Adapter<HRecyclerViewAdapter.ViewHolder> {

    private List<Meeting> meetingList;

    public HRecyclerViewAdapter(List<Meeting> meetings){
        meetingList = meetings;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView during_time;
        public ViewHolder(View view){
            super(view);
            during_time = (TextView)view.findViewById(R.id.tv_date);

//            view.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
////                    Toast.makeText(v.getContext(),"123123123",Toast.LENGTH_SHORT).show();
//                    if(onItemClickListener != null){
//                        onItemClickListener.onItemClick(v, datas.get(getLayoutPosition()));
//
//                    }
//                }
//            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.horizontallistview_item,viewGroup,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        Meeting meeting = meetingList.get(i);
        String mStartTime = meeting.getStartTime().substring(11,16);
        String mEndTime = meeting.getEndTime().substring(11,16);
        viewHolder.during_time.setText(mStartTime+"~"+mEndTime);


//        // 点击事件一般都写在绑定数据这里，当然写到上边的创建布局时候也是可以的
//        if (mItemClickListener != null){
//            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    // 这里利用回调来给RecyclerView设置点击事件
//                    mItemClickListener.onItemClick(i);
//                }
//            });
//        }


//        // 给RecyclerView中item中的单独控件设置点击事件 可以直接在adapter中使用setOnClickListener即可
//        viewHolder.action_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(mContext , item.getButtons() +"position -> "+position , Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return meetingList.size();
    }


    //点击 RecyclerView 某条的监听
    public interface OnItemClickListener{

        /**
         * 当RecyclerView某个被点击的时候回调
         * @param view 点击item的视图
         * @param data 点击得到的数据
         */
        void onItemClick(View view, String data);

    }

    private OnItemClickListener onItemClickListener;

    /**
     * 设置RecyclerView某个的监听
     * @param onItemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
}
