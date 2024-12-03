package com.example.setting;

public class AlarmItem {

    private String time;
    private String content;

    public AlarmItem(String time, String content) {
        this.time = time;
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public String getContent() {
        return content;
    }
}
