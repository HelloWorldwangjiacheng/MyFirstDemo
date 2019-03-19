package com.example.myfirstdemo.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.myfirstdemo.R;


public class HelpFragment extends Fragment {
  @Nullable
  ImageView help_fragment_Image1;
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//    return super.onCreateView(inflater, container, savedInstanceState);
    View view = inflater.inflate(R.layout.help_fragment,container,false);
//    help_fragment_Image1 = (ImageView)view.findViewById(R.id.help_fragment_Image_1);
//    Glide.with(this).load(R.drawable.help_fragment_11).override(500, 500).into(help_fragment_Image1);
    return view;
  }
}