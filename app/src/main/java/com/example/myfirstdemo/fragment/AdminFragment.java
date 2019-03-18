package com.example.myfirstdemo.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.myfirstdemo.R;


public class AdminFragment extends Fragment {
  @Nullable

  Button back_login ;
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//    return super.onCreateView(inflater, container, savedInstanceState);
    View view = inflater.inflate(R.layout.admin_fragment,container,false);
//    back_login = (Button)view.findViewById(R.id.back_login);
//      back_login.setOnClickListener(new View.OnClickListener() {
//          @Override
//          public void onClick(View v) {
//              finish();
//          }
//      });
    return view;
  }

//  @Override
//  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
//    super.onActivityCreated(savedInstanceState);
//
//  }
}