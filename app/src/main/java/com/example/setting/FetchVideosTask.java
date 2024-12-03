package com.example.setting;

import android.os.AsyncTask;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FetchVideosTask extends AsyncTask<Void, Void, List<RecordedVideo>> {
    private String API_URL = "https://ict.nanum.info/videoList?deviceID=";
    private final RecordedVideoPagerAdapter adapter;
    private final IndoorFragment fragment;
    private static final String TAG = "FetchVideosTask";
    private String deviceID;

    public FetchVideosTask(IndoorFragment fragment, RecordedVideoPagerAdapter adapter, String deviceID) {
        this.fragment = fragment;
        this.adapter = adapter;
        this.deviceID = deviceID;
    }

    @Override
    protected List<RecordedVideo> doInBackground(Void... voids) {
        List<RecordedVideo> data = new ArrayList<>();
        try {
            URL url = new URL("https://ict.nanum.info/videoList?deviceID=" + deviceID);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // HTML 파싱 및 비디오 리스트 추출
            Document doc = Jsoup.parse(response.toString());
            Element ulElement = doc.getElementById("videoList");
            Elements liElements = ulElement.getElementsByTag("li");

            // 날짜 패턴 정의
            Pattern datePattern = Pattern.compile("(\\d{4})년 (\\d{2})월 (\\d{2})일 (\\d{2})시 (\\d{2})분");

            for (Element li : liElements) {
                Element aElement = li.getElementsByTag("a").first();
                String href = aElement.attr("href");
                String text = aElement.text();

                Matcher matcher = datePattern.matcher(text);
                if (matcher.find()) {
                    // 날짜, 시간 정보를 추출
                    String year = matcher.group(1);
                    String month = matcher.group(2);
                    String day = matcher.group(3);
                    String hour = matcher.group(4);
                    String minute = matcher.group(5);

                    // 포맷된 문자열 생성
                    String formattedString = year + "-" + month + "-" + day + "_" + hour + minute;

                    // 비디오 URL 생성
                    String videoUrl = "https://ict.nanum.info/videoView?deviceID=" + deviceID + "&dirName=" + formattedString;

                    // 비디오 객체 리스트에 추가 (isSelected는 false, dirName는 formattedString 사용)
                    data.add(new RecordedVideo(formattedString, text, videoUrl, false, formattedString));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching data", e);
        }
        return data;
    }

    @Override
    protected void onPostExecute(List<RecordedVideo> result) {
        // 어댑터에 데이터 업데이트
        adapter.updateData(result);
    }
}