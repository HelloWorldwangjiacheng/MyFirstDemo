package com.example.myfirstdemo.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myfirstdemo.MainActivity;
import com.example.myfirstdemo.R;
import com.example.myfirstdemo.RecognizeActivity;


public class AdminFragment extends Fragment {
  @Nullable

  Button login_button ;
  EditText input_login_account;
  EditText input_login_password;
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//    return super.onCreateView(inflater, container, savedInstanceState);
    View view = inflater.inflate(R.layout.admin_fragment,container,false);
    login_button = (Button)view.findViewById(R.id.login_button);
    input_login_account = (EditText)view.findViewById(R.id.input_login_account);
    input_login_password = (EditText)view.findViewById(R.id.input_login_password);
    login_button.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              String account = input_login_account.getText().toString();
              String password = input_login_password.getText().toString();
              if (account.equals("16401190109")&&password.equals("123456")){
                MainActivity activity = (MainActivity)getActivity();
                AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
                dialog.setTitle("人脸识别界面退出提醒");
                dialog.setMessage("管理员您好，门禁已开，该门禁开启记录已提交至后台。");
                dialog.setCancelable(false);
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    input_login_account.setText("");
                    input_login_password.setText("");
                    Toast.makeText(getContext(), "门禁已开", Toast.LENGTH_SHORT).show();
                  }
                });
                dialog.show();
              }
          }
      });
    return view;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

  }
}