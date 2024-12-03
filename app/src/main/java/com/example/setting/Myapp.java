package com.example.setting;

import android.app.Application;
import android.content.SharedPreferences;

public class Myapp extends Application {

    private static Myapp instance;
    private User currentUser;
    private String deviceId; // deviceId를 저장할 변수

    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String DEVICE_ID_KEY = "deviceId";
    private boolean isSleepStop;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this; // Singleton 인스턴스 초기화

        // SharedPreferences에서 저장된 deviceId 가져오기
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        deviceId = prefs.getString(DEVICE_ID_KEY, null);
    }

    // Singleton 인스턴스를 반환하는 메서드
    public static Myapp getInstance() {
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    // deviceId를 반환하는 메서드
    public String getMacDeviceId() {
        return deviceId;
    }

    // deviceId를 설정하고 SharedPreferences에 저장하는 메서드
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        // SharedPreferences에 저장
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(DEVICE_ID_KEY, deviceId);
        editor.apply();
    }

    public boolean isSleepStop() {
        return isSleepStop;
    }

    public void setSleepStop(boolean sleepStop) {
        this.isSleepStop = sleepStop;
    }
}
