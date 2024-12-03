package com.example.setting;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Alarm extends AppCompatActivity {

    public  TextView dateTextView;
    private ImageView alarmCal;
    private Calendar selectedDate;
    private RecyclerView recyclerView;
    private AlarmAdapter alarmAdapter;
    private SQLiteHelper dbHelper;
    private ArrayList<AlarmItem> alarmList;  // 알림 데이터 리스트

    // BroadcastReceiver 선언
    private BroadcastReceiver notificationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        // 시스템 바 인셋 적용
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // SQLiteHelper 초기화
        dbHelper = new SQLiteHelper(this);

        // RecyclerView 초기화
        recyclerView = findViewById(R.id.alarm_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 알림 리스트 초기화 및 어댑터 설정
        alarmList = new ArrayList<>();
        alarmAdapter = new AlarmAdapter(alarmList);
        recyclerView.setAdapter(alarmAdapter);

        // 날짜 선택 및 TextView 초기화
        dateTextView = findViewById(R.id.date);
        alarmCal = findViewById(R.id.alarm_cal);

        // 현재 날짜를 설정하고 알림 표시
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        dateTextView.setText(currentDate);

        // 기본적으로 현재 날짜에 해당하는 알림을 표시
        showNotificationsForDate(currentDate);

        alarmCal.setOnClickListener(view -> showCalendarPopup());

        // 현재 Activity를 종료하고 이전 Activity로 돌아갑니다.
        ImageView alarmBack = findViewById(R.id.alarm_back);
        alarmBack.setOnClickListener(v -> finish());

        // 알림 창이 실행될 때 모든 푸시 알림 삭제
        clearAllNotifications();

        // BroadcastReceiver 설정 및 등록
        setupBroadcastReceiver();
    }

    // 모든 푸시 알림 삭제
    private void clearAllNotifications() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();  // 모든 알림 삭제
        }
    }

    private void showCalendarPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View popupView = inflater.inflate(R.layout.popup_cal, null);
        builder.setView(popupView);

        AlertDialog dialog = builder.create();

        TextView yearTextView = popupView.findViewById(R.id.year);
        TextView datePopupTextView = popupView.findViewById(R.id.date_popup);
        CalendarView calendarView = popupView.findViewById(R.id.calendar);
        ImageView calBack = popupView.findViewById(R.id.cal_back);
        ImageView calOk = popupView.findViewById(R.id.cal_ok);

        // 현재 날짜 설정
        Calendar calendar = Calendar.getInstance();
        selectedDate = (Calendar) calendar.clone();
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.KOREAN);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM월 dd일 EEE", Locale.KOREAN);
        String year = yearFormat.format(calendar.getTime());
        String date = dateFormat.format(calendar.getTime());

        yearTextView.setText(year);
        datePopupTextView.setText(date);

        // 날짜 선택 설정
        calendarView.setOnDateChangeListener((view, year1, month, dayOfMonth) -> {
            selectedDate.set(year1, month, dayOfMonth);
            String selectedYear = yearFormat.format(selectedDate.getTime());
            String selectedDateStr = dateFormat.format(selectedDate.getTime());
            yearTextView.setText(selectedYear);
            datePopupTextView.setText(selectedDateStr);

        });

        // cal_back을 누르면 팝업 닫기
        calBack.setOnClickListener(v -> dialog.dismiss());

        // cal_ok를 누르면 선택한 날짜를 MainActivity의 TextView에 설정
        calOk.setOnClickListener(v -> {
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
            String formattedDate = outputFormat.format(selectedDate.getTime());
            dateTextView.setText(formattedDate);
            showNotificationsForDate(formattedDate);
            dialog.dismiss();
        });

        dialog.show();
    }

    // BroadcastReceiver 설정 및 등록
    private void setupBroadcastReceiver() {
        notificationReceiver = new AlarmReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Alarm", "Resuming activity, refreshing notifications.");
        String currentDisplayedDate = dateTextView.getText().toString();
        showNotificationsForDate(currentDisplayedDate);
        // BroadcastReceiver 재등록
        //registerReceiver(notificationReceiver, new IntentFilter("com.example.setting.NOTIFICATION_RECEIVED"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Alarm", "Pausing activity, unregistering receiver.");
        //if (notificationReceiver != null) {
            //unregisterReceiver(notificationReceiver);
        //}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Alarm", "Destroying activity, cleaning up.");
    }

    // 특정 날짜에 저장된 알림 정보를 SQLite에서 가져오는 메서드
    protected void showNotificationsForDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());

        try {
            Date selectedDateParsed = sdf.parse(date);
            long startOfDay = selectedDateParsed.getTime();
            long endOfDay = startOfDay + (24 * 60 * 60 * 1000) - 1;

            // SQLite 데이터베이스에서 해당 날짜에 저장된 알림을 가져오기
            Cursor cursor = dbHelper.getNotificationsForDate(startOfDay, endOfDay);
            if (cursor != null) {
                alarmList.clear();  // 기존 알림 데이터 초기화
                while (cursor.moveToNext()) {
                    String content = cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_NOTIFICATION_CONTENT));
                    long timestamp = cursor.getLong(cursor.getColumnIndex(SQLiteHelper.COLUMN_NOTIFICATION_TIMESTAMP));

                    // 시간 형식으로 변환 (시:분:초 포함)
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                    String formattedTime = timeFormat.format(new Date(timestamp));

                    // 알림 데이터를 리스트에 추가
                    alarmList.add(new AlarmItem(formattedTime, content));
                }
                cursor.close();
                alarmAdapter.notifyDataSetChanged();  // 어댑터에 데이터 변경 알림
            } else {
                Log.w("Alarm", "No notifications found for the selected date.");
                alarmList.clear();
                alarmAdapter.notifyDataSetChanged();
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
