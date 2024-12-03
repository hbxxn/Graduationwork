package com.example.setting;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeleteVideoTask {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();  // 스레드 풀 생성
    private final Handler handler = new Handler(Looper.getMainLooper());  // 메인 스레드 핸들러

    public void execute(String deleteUrl) {
        executorService.execute(() -> {
            try {
                // URL 생성
                URL url = new URL(deleteUrl);

                // HttpURLConnection 생성 및 설정
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");  // GET 메소드로 삭제 요청(삭제 구현은 서버에서...)

                // 응답 코드 확인
                int responseCode = connection.getResponseCode();

                // 서버로부터의 응답을 읽기
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            } catch (Exception e) {
                Log.e("DELETE", "Error: " + e.toString());
            }
        });
    }
}