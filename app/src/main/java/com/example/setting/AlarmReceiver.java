package com.example.setting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmReceiver", "Notification received, refreshing UI.");

        // 알림을 받은 시점에 알림을 갱신하는 메서드 호출
        if (context instanceof Alarm) {
            Alarm alarmActivity = (Alarm) context;
            String currentDisplayedDate = alarmActivity.dateTextView.getText().toString();
            alarmActivity.showNotificationsForDate(currentDisplayedDate);
        }
    }
}