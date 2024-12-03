package com.example.setting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class setting_help extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_help);


        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                finish();  // 현재 Activity를 종료하고 이전 Activity로 돌아갑니다.
            }
        });

        // device_change 버튼 클릭 이벤트 설정
        ImageView deviceChange = findViewById(R.id.device_change);
        deviceChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(setting_help.this, device_change.class);
                startActivity(intent);
            }
        });

        // device_add 버튼 클릭 이벤트 설정
        ImageView deviceAdd = findViewById(R.id.device_add);
        deviceAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(setting_help.this, device_add.class);
                startActivity(intent);
            }
        });

        // address_change 버튼 클릭 이벤트 설정
        ImageView addressChange = findViewById(R.id.address_change);
        addressChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(setting_help.this, address_change.class);
                startActivity(intent);
            }
        });

    }
}