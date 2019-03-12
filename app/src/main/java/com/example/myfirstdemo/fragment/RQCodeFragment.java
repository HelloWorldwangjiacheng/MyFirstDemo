package com.example.myfirstdemo.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.myfirstdemo.R;
import com.example.myfirstdemo.Util.RQCodeUtil;


public class RQCodeFragment extends Fragment {
    ImageButton RQCodeBackButton;
    ImageView RQCode;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.rqcode_fragment,container,false);
//        RQCodeBackButton = (ImageButton)view.findViewById(R.id.rqcode_back);
        RQCode = (ImageView)view.findViewById(R.id.show_rqcode);
        return view ;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bitmap mBitmap = RQCodeUtil.createQRCodeBitmap("https://www.baidu.com",200,200);
        RQCode.setImageBitmap(mBitmap);

//        RQCodeBackButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (v.getId()==R.id.rqcode_back){
//                    MainActivity activity = (MainActivity)getActivity();
//                    activity.dl.closeDrawers();
//                }
//            }
//        });
    }
}
