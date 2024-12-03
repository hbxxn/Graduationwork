    package com.example.setting;

    import android.content.Context;
    import android.content.SharedPreferences;
    import android.graphics.Bitmap;
    import android.graphics.BitmapFactory;
    import android.os.AsyncTask;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ImageView;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.appcompat.app.AlertDialog;
    import androidx.core.graphics.Insets;
    import androidx.core.view.ViewCompat;
    import androidx.core.view.WindowInsetsCompat;
    import androidx.fragment.app.Fragment;

    import org.json.JSONObject;

    import java.io.BufferedReader;
    import java.io.InputStream;
    import java.io.InputStreamReader;
    import java.net.HttpURLConnection;
    import java.net.URL;
    import java.security.cert.X509Certificate;
    import java.util.List;
    import java.util.regex.Matcher;
    import java.util.regex.Pattern;

    import javax.net.ssl.HttpsURLConnection;
    import javax.net.ssl.SSLContext;
    import javax.net.ssl.TrustManager;
    import javax.net.ssl.X509TrustManager;

    public class WindowFragment extends Fragment {
        private static final String ARG_PARAM1 = "param1";
        private static final String ARG_PARAM2 = "param2";

        private String mParam1;
        private String mParam2;
        private String deviceId; // Device ID를 저장할 변수

        private boolean isRainOn = false;
        private boolean isWindowOpen = false;

        private FetchSensorDataTask fetchSensorDataTask;

        private ImageView weatherIcon;
        private TextView temperature;
        private TextView humidity;
        private TextView airQuality;
        private SQLiteHelper dbHelper;

        public WindowFragment() {
            // Required empty public constructor
        }
        public static WindowFragment newInstance(String param1, String param2) {
            WindowFragment fragment = new WindowFragment();
            Bundle args = new Bundle();
            args.putString(ARG_PARAM1, param1);
            args.putString(ARG_PARAM2, param2);
            fragment.setArguments(args);
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_window, container, false);

            // Myapp에서 deviceId 가져오기
            deviceId = Myapp.getInstance().getMacDeviceId();

            // 시스템 바 인셋 적용
            ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

            ImageView ic_rain = view.findViewById(R.id.ic_rain_off);
            ImageView ic_window = view.findViewById(R.id.ic_window_closed);

            // 이전 상태 복원
            isRainOn = getStateFromPreferences("rainStatus", false); // 기본값은 꺼짐 상태
            isWindowOpen = getStateFromPreferences("windowStatus", false); // 기본값은 닫힌 상태

            // 저장된 상태에 따라 아이콘 초기화
            if (isRainOn) {
                ic_rain.setImageResource(R.drawable.ic_rain_on);
            } else {
                ic_rain.setImageResource(R.drawable.ic_rain_off);
            }

            if (isWindowOpen) {
                ic_window.setImageResource(R.drawable.ic_window_open);
            } else {
                ic_window.setImageResource(R.drawable.ic_window_closed);
            }

            // 빗물 감지 버튼 클릭 리스너
            ic_rain.setOnClickListener(v -> {
                if (deviceId == null) {
                    Toast.makeText(getContext(), "기기를 먼저 등록해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isRainOn) {
                    ic_rain.setImageResource(R.drawable.ic_rain_off);
                    Log.d("WindowFragment", "빗물 감지가 꺼져있습니다.");
                } else {
                    ic_rain.setImageResource(R.drawable.ic_rain_on);
                    Log.d("WindowFragment", "빗물 감지가 켜졌습니다. 센서 데이터를 가져오고 있습니다.");

                    // 센서 데이터 가져오기
                    new FetchSensorDataTask(getContext(), new FetchSensorDataTask.Listener() {
                        @Override
                        public void onDataFetched(JSONObject sensorData) {
                            try {
                                // Rain 값 가져오기
                                int rainValue = sensorData.getInt("Rain"); // 정수형으로 가져오기
                                boolean rainDetected = (rainValue == 1); // 1이면 true, 0이면 false

                                // Person 값 가져오기
                                int humanValue = sensorData.getInt("Person"); // 정수형으로 가져오기
                                boolean humanDetected = (humanValue == 1); // 1이면 true, 0이면 false

                                Log.d("WindowFragment", "Sensor data fetched successfully.");
                                onSensorDataFetched(rainDetected, humanDetected);
                            } catch (Exception e) {
                                Log.e("WindowFragment", "센서 오류났습니다.", e);
                            }
                        }

                        @Override
                        public void onDataFetchFailed() {
                            Log.e("WindowFragment", "Failed to fetch sensor data");
                        }
                    }).execute(deviceId);
                }
                isRainOn = !isRainOn;
                saveStateToPreferences("rainStatus", isRainOn); // 상태 저장
            });

            // 창문 버튼 클릭 리스너
            ic_window.setOnClickListener(v -> {
                String urlString;
                if (deviceId == null) {
                    Toast.makeText(getContext(), "기기를 먼저 등록해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isWindowOpen) {
                    urlString = "https://ict.nanum.info/window?deviceID=" + deviceId + "&mode=false";
                    ic_window.setImageResource(R.drawable.ic_window_closed);
                    Log.d("WindowFragment", "Window is now CLOSED.");
                } else {
                    urlString = "https://ict.nanum.info/window?deviceID=" + deviceId + "&mode=true";
                    ic_window.setImageResource(R.drawable.ic_window_open);
                    Log.d("WindowFragment", "Window is now OPEN.");
                }
                isWindowOpen = !isWindowOpen;
                saveStateToPreferences("windowStatus", isWindowOpen); // 창문 상태 저장
                new SendWindowStateTask().execute(urlString);
            });

            // 날씨 정보 UI 요소 초기화
            weatherIcon = view.findViewById(R.id.w_icon);
            temperature = view.findViewById(R.id.temperature);
            humidity = view.findViewById(R.id.humidity);
            airQuality = view.findViewById(R.id.air_quality);

            dbHelper = new SQLiteHelper(getContext()); // 데이터베이스 헬퍼 초기화

            // 저장된 위치 정보 가져오기
            double[] location = getStoredLocation();
            if (location != null) {
                double latitude = location[0];
                double longitude = location[1];

                // 날씨 정보 요청 URL 생성
                String[] weatherUrls = new String[]{
                        String.format("https://ict.nanum.info/weather?type=i&lat=%f&lon=%f", latitude, longitude),
                        String.format("https://ict.nanum.info/weather?type=t&lat=%f&lon=%f", latitude, longitude),
                        String.format("https://ict.nanum.info/weather?type=h&lat=%f&lon=%f", latitude, longitude),
                        String.format("https://ict.nanum.info/weather?type=a&lat=%f&lon=%f", latitude, longitude)
                };
                new FetchDataTask().execute(weatherUrls); // 날씨 정보 비동기 요청
            } else {
                temperature.setText("N/A"); // 위치 정보가 없을 때
                humidity.setText("N/A");
                airQuality.setText("N/A");
            }
            // 센서 데이터 가져오기 및 창문 상태 업데이트
            fetchSensorDataTask = new FetchSensorDataTask(getContext(), new FetchSensorDataTask.Listener() {
                @Override
                public void onDataFetched(JSONObject sensorData) {
                    try {
                        int windowValue = sensorData.getInt("Window"); // 정수형으로 가져오기
                        boolean windowOpen = (windowValue == 1); // 1이면 true, 0이면 false
                        if (windowOpen) ic_window.setImageResource(R.drawable.ic_window_open);
                        else ic_window.setImageResource(R.drawable.ic_window_closed);

                        onSensorDataFetched(false, windowOpen);
                    } catch (Exception e) {
                        Log.e("WindowFragment", "Error parsing sensor data", e);
                    }
                }

                @Override
                public void onDataFetchFailed() {
                    Log.e("WindowFragment", "Failed to fetch sensor data");
                }
            });
            fetchSensorDataTask.execute(deviceId);

            return view;
        }

        private void saveStateToPreferences(String key, boolean value) {
            SharedPreferences sharedPreferences = getContext().getSharedPreferences("WindowFragmentPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(key, value);
            editor.apply();
        }
        private boolean getStateFromPreferences(String key, boolean defaultValue) {
            SharedPreferences sharedPreferences = getContext().getSharedPreferences("WindowFragmentPrefs", Context.MODE_PRIVATE);
            return sharedPreferences.getBoolean(key, defaultValue);
        }


        private void onSensorDataFetched(boolean rainDetected, boolean humanDetected) {
            if (rainDetected || humanDetected) {
                if (isWindowOpen) {
                    ImageView ic_window = getView().findViewById(R.id.ic_window_closed);
                    ic_window.setImageResource(R.drawable.ic_window_closed);
                    isWindowOpen = false;
                    String urlString = "https://ict.nanum.info/window?deviceID=" + deviceId + "&mode=false";
                    new SendWindowStateTask().execute(urlString);
                }
            }
        }

        // 저장된 위치 정보를 가져오는 메서드
        private double[] getStoredLocation() {
            List<double[]> locations = dbHelper.getLocations();
            if (!locations.isEmpty()) {
                return locations.get(0);
            }
            return null;
        }

        //추가
        // 창 상태를 서버에 전송하는 AsyncTask
        private class SendWindowStateTask extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {
                String urlString = params[0];
                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL(urlString);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // 서버의 응답을 읽어오는 부분
                    InputStream inputStream = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    return stringBuilder.toString();  // 서버 응답 반환
                } catch (Exception e) {
                    Log.e("SendWindowStateTask", "Error during HTTP request", e);
                    return null;
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    Log.d("SendWindowStateTask", "Server response: " + result);
                } else {
                    Log.e("SendWindowStateTask", "Failed to get a response from the server");
                }
            }
        }

        private class FetchDataTask extends AsyncTask<String, Void, String[]> {

            @Override
            protected String[] doInBackground(String... urls) {
                String[] results = new String[urls.length];
                HttpURLConnection urlConnection = null;
                for (int i = 0; i < urls.length; i++) {
                    try {
                        URL url = new URL(urls[i]);

                        urlConnection = (HttpURLConnection) url.openConnection();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            stringBuilder.append(line);
                        }
                        results[i] = stringBuilder.toString().trim();  // 양 끝의 공백 및 줄 바꿈 제거
                    } catch (Exception e) {
                        Log.e("FetchDataTask", "Error during HTTP request", e);
                    } finally {
                        if (urlConnection != null) {
                            urlConnection.disconnect();
                        }
                    }
                }
                return results;
            }

            @Override
            protected void onPostExecute(String[] results) {
                if (results.length >= 4) {
                    // API에서 받아온 이미지 URL에서 실제 URL 추출
                    String iconHtml = results[0].trim(); // HTML 태그 형태의 이미지 URL
                    String iconUrl = extractImageUrl(iconHtml); // HTML 태그에서 URL 추출

                    // 이미지 다운로드 및 ImageView 설정
                    new DownloadImageTask(weatherIcon).execute(iconUrl);

                    // 기온 데이터 소수점 한 자리로 포맷
                    String tempString = results[1].trim(); // 공백 제거

                    try {
                        // "°C"를 제거
                        if (tempString.endsWith("°C")) {
                            tempString = tempString.substring(0, tempString.length() - 2).trim();
                        }

                        double temp = Double.parseDouble(tempString);  // 문자열을 double로 변환
                        String formattedTemp = String.format("%.1f", temp);  // 소수점 한 자리로 포맷
                        temperature.setText(formattedTemp + "°C");  // 포맷된 문자열과 단위 설정
                    } catch (NumberFormatException e) {
                        Log.e("WindowFragment", "Failed to parse temperature data: " + tempString, e);
                        temperature.setText("N/A");  // 포맷 실패 시 기본 값 설정
                    }

                    // 습도
                    humidity.setText(results[2]);

                    // 미세먼지 데이터 포맷
                    String airQualityString = results[3].trim(); // 공백 제거

                    try {
                        // 숫자와 소수점만 추출
                        String airQualityNumber = airQualityString.replaceAll("[^0-9.]", "").trim();

                        if (airQualityNumber.isEmpty()) {
                            airQuality.setText("N/A");  // 숫자가 없는 경우
                            return;
                        }

                        // 소수점을 버리고 정수로 변환
                        int airQualityInt = (int) Double.parseDouble(airQualityNumber);  // 문자열을 double로 변환 후 정수로 변환

                        // 미세먼지 수치에 따라 이미지뷰와 텍스트 설정
                        ImageView airIconColor = getView().findViewById(R.id.air_icon_color); // 이미지뷰 초기화

                        String airQualityText;
                        if (airQualityInt >= 151) {
                            airIconColor.setImageResource(R.drawable.air_verybad);
                            airQualityText = String.format("매우 나쁨(%d μg/m³)", airQualityInt);
                        } else if (airQualityInt >= 81) {
                            airIconColor.setImageResource(R.drawable.air_bad);
                            airQualityText = String.format("나쁨(%d μg/m³)", airQualityInt);
                        } else if (airQualityInt >= 31) {
                            airIconColor.setImageResource(R.drawable.air_notbad);
                            airQualityText = String.format("보통(%d μg/m³)", airQualityInt);
                        } else {
                            airIconColor.setImageResource(R.drawable.air_good);
                            airQualityText = String.format("좋음(%d μg/m³)", airQualityInt);
                        }

                        airQuality.setText(airQualityText);  // 포맷된 문자열과 단위 설정

                    } catch (NumberFormatException e) {
                        Log.e("HomeFragment", "Failed to parse air quality data: " + airQualityString, e);
                        airQuality.setText("N/A");  // 포맷 실패 시 기본 값 설정
                    }
                }
            }

            private String extractImageUrl(String html) {
                String imageUrl = "";
                Pattern pattern = Pattern.compile("src=\"(.*?)\"");
                Matcher matcher = pattern.matcher(html);
                if (matcher.find()) {
                    imageUrl = matcher.group(1);
                }
                return imageUrl;
            }
        }

        private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
            private ImageView imageView;

            public DownloadImageTask(ImageView imageView) {
                this.imageView = imageView;
            }

            @Override
            protected Bitmap doInBackground(String... urls) {
                String url = urls[0];
                Bitmap bitmap = null;
                try {
                    InputStream inputStream = new URL(url).openStream();
                    bitmap = BitmapFactory.decodeStream(inputStream);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                if (result != null) {
                    imageView.setImageBitmap(result);
                } else {
                    imageView.setImageResource(R.drawable.logo); // 기본 아이콘 설정
                }
            }
        }
    }
