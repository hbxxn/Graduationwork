package com.example.setting;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final int LOCATION_REQUEST_CODE = 1000;
    private static final float DISTANCE_THRESHOLD = 500; // 거리 임계값 (미터 단위)

    private TextView guText;
    private Switch switch1;
    private ImageView imageView;
    private ImageView topPlus;
    private ImageView sleepPlay;
    private boolean isSleepStop = false;
    private ImageView weatherIcon;
    private TextView temperature;
    private MediaPlayer mediaPlayer;

    private View rootView;
    private SQLiteHelper dbHelper;
    private User currentUser;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private UserViewModel userViewModel; // ViewModel 추가
    private String deviceID;

    public HomeFragment() {
        // 기본 생성자
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    // DB에서 deviceId 가져오기
    private String getDeviceIdFromDB() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                SQLiteHelper.TABLE_DEVICE,
                new String[]{SQLiteHelper.COLUMN_DEVICE_ID},
                SQLiteHelper.COLUMN_USER_ID + "=?",
                new String[]{currentUser.getUserId()},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String deviceId = cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_DEVICE_ID));
            cursor.close();
            return deviceId;
        }

        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Myapp app = (Myapp) getActivity().getApplication();
        currentUser = app.getCurrentUser();
        dbHelper = new SQLiteHelper(getContext());

        // deviceId 설정
        if (currentUser != null) {
            deviceID = getDeviceIdFromDB();
        }

        if (deviceID == null) {
            // 기기가 등록되어 있지 않은 경우 처리 로직
            //startActivity(new Intent(getActivity(), SensorActivity.class));
        }

        // ViewModel 초기화
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.getAddress().observe(this, newAddress -> {
            if (guText != null) {
                guText.setText(newAddress);
            }
        });

        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        // 위치 업데이트 콜백 설정
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                // isSleepStop이 true일 때는 위치 업데이트 중단
                if (isSleepStop) {
                    Log.d("HomeFragment", "Location updates paused due to sleep stop");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateSwitchState(location);
                }
            }
        };

        // 위치 권한 요청
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // 엣지-투-엣지 인셋 적용
        ViewCompat.setOnApplyWindowInsetsListener(rootView.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 뷰 초기화
        guText = rootView.findViewById(R.id.guText);
        switch1 = rootView.findViewById(R.id.switch1);
        imageView = rootView.findViewById(R.id.main_off);
        topPlus = rootView.findViewById(R.id.top_plus);
        sleepPlay = rootView.findViewById(R.id.sleep_play);
        weatherIcon = rootView.findViewById(R.id.w_icon);
        temperature = rootView.findViewById(R.id.temperature);

        topPlus.setOnClickListener(v -> startActivity(new Intent(getActivity(), SensorActivity.class)));
        rootView.findViewById(R.id.top_alarm).setOnClickListener(v -> startActivity(new Intent(getActivity(), Alarm.class)));
        rootView.findViewById(R.id.main_2).setOnClickListener(v -> showPopup());

        switch1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 스위치 상태에 따른 동작
            if (isSleepStop && !isChecked) {
                // 취침모드가 활성화된 상태에서 스위치를 끄려고 하면
                switch1.setChecked(true); // 스위치를 다시 켬 상태로 변경
                Toast.makeText(getContext(), "취침모드를 취소하고 싶으면 중단 버튼을 클릭하세요.", Toast.LENGTH_SHORT).show();
            } else {
                // 보안 모드 ON/OFF를 위한 URL 설정
                String securityUrl = isChecked
                        ? "https://ict.nanum.info/mode?deviceID=" + deviceID
                        : "https://ict.nanum.info/mode?deviceID=" + deviceID;

                // 스위치 상태에 따라 이미지 변경
                imageView.setImageResource(isChecked ? R.drawable.main_on : R.drawable.main_off);
                Log.d("HomeFragment", "Security mode set to: " + (isChecked ? "ON" : "OFF"));

                // 비동기로 보안 모드 변경 URL 요청 보내기
                new SecurityModeTask().execute(securityUrl);
            }
        });

        sleepPlay.setOnClickListener(v -> {
            isSleepStop = !isSleepStop; // 클릭 시 상태를 토글

            if (isSleepStop) {
                sleepPlay.setImageResource(R.drawable.sleep_stop);
                switch1.setChecked(true); // 취침 모드 시 스위치를 true로 설정
                imageView.setImageResource(R.drawable.main_on); // 이미지뷰도 on 상태로 변경

                // onResume에서 Toast 메시지를 출력하기 위해 SharedPreferences에 플래그 설정
                if (getContext() != null) {
                    Toast.makeText(getContext(), "취침모드가 오전 6시까지 지속됩니다.", Toast.LENGTH_LONG).show();
                }
            } else {
                sleepPlay.setImageResource(R.drawable.sleep_play);
                showSleepPopup();
                sleepPlay.setImageResource(R.drawable.sleep_play);
                isSleepStop=false;
                switch1.setChecked(false);
                imageView.setImageResource(R.drawable.main_off); // 취침모드 중단 시 보안도 중단
                if(getContext() != null) {
                    Toast.makeText(getContext(), "취침모드가 중단되었습니다.", Toast.LENGTH_SHORT).show(); //취침모드 중단 메시지
                }
                Log.d("HomeFragment", "Sleep mode deactivated");
            }
        });

        return rootView;
    }

    // URL 요청을 처리하는 AsyncTask 클래스
    private static class SecurityModeTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... urls) {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                int responseCode = urlConnection.getResponseCode();
                Log.d("SecurityModeTask", "Response Code: " + responseCode);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAddress();

        // 현재 시간 가져오기
        Calendar now = Calendar.getInstance();
        Calendar sixAM = Calendar.getInstance();
        sixAM.set(Calendar.HOUR_OF_DAY, 6);
        sixAM.set(Calendar.MINUTE, 0);
        sixAM.set(Calendar.SECOND, 0);

        // 취침 모드가 활성화 되어 있을 경우
        if (Myapp.getInstance().isSleepStop()) {
            if (now.before(sixAM)) {
                long delay = sixAM.getTimeInMillis() - now.getTimeInMillis();

                Log.d("HomeFragment", "Current time: " + now.getTime());
                Log.d("HomeFragment", "Scheduled time: " + sixAM.getTime());
                Log.d("HomeFragment", "Delay: " + delay);

                if (getContext() != null) {
                    Toast.makeText(getContext(), "취침모드가 오전 6시까지 지속됩니다.", Toast.LENGTH_LONG).show();
                }

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Log.d("HomeFragment", "Handler executed at 6 AM");
                    if (Myapp.getInstance().isSleepStop()) {
                        Myapp.getInstance().setSleepStop(false);
                        switch1.setChecked(false);
                        imageView.setImageResource(R.drawable.main_off);
                        Log.d("HomeFragment", "Sleep stop reset at 6 AM");
                    }
                }, delay);
            } else {
                // 오전 6시가 지난 경우 바로 취침 모드 중지
                Myapp.getInstance().setSleepStop(false);
                switch1.setChecked(false);
                imageView.setImageResource(R.drawable.main_off);
                Log.d("HomeFragment", "Sleep stop reset after 6 AM");
            }
        }
    }
    public void setSleepStop(boolean sleepStop) {
        this.isSleepStop = sleepStop;
        // SharedPreferences에 저장
        SharedPreferences prefs = getSharedPreferences("sleepPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isSleepStop", sleepStop);
        editor.apply();
    }

    private SharedPreferences getSharedPreferences(String sleepPrefs, int modePrivate) {
        return getActivity().getSharedPreferences(sleepPrefs, modePrivate);
    }

    public boolean isSleepStop() {
        // SharedPreferences에서 값 불러오기
        SharedPreferences prefs = getSharedPreferences("sleepPrefs", MODE_PRIVATE);
        return prefs.getBoolean("isSleepStop", false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000); // 위치 업데이트 간격 (밀리초)
        locationRequest.setFastestInterval(5000); // 가장 빠른 위치 업데이트 간격 (밀리초)
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // 높은 정확도 우선

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    // 주소 업데이트 메서드
    private void updateAddress() {
        if (currentUser != null) {
            String guFromDb = dbHelper.getGu(currentUser.getUserId());
            if (guFromDb != null && !guFromDb.isEmpty()) {
                currentUser.setGu(guFromDb);
                userViewModel.setAddress(guFromDb); // ViewModel을 통해 주소 설정
            } else {
                userViewModel.setAddress("구 정보 없음");
            }

            double[] location = getStoredLocation();
            if (location != null) {
                double latitude = location[0];
                double longitude = location[1];
                String[] weatherUrls = new String[]{
                        String.format("https://ict.nanum.info/weather?type=i&lat=%f&lon=%f", latitude, longitude),
                        String.format("https://ict.nanum.info/weather?type=t&lat=%f&lon=%f", latitude, longitude),
                        String.format("https://ict.nanum.info/weather?type=h&lat=%f&lon=%f", latitude, longitude),
                        String.format("https://ict.nanum.info/weather?type=a&lat=%f&lon=%f", latitude, longitude)
                };
                new FetchDataTask().execute(weatherUrls);
            } else {
                userViewModel.setAddress("위치 정보 없음");
            }
        } else {
            userViewModel.setAddress("사용자 정보 없음");
        }
    }

    private double[] getStoredLocation() {
        List<double[]> locations = dbHelper.getLocations();
        return (locations.isEmpty()) ? null : locations.get(0);
    }

    private void updateSwitchState(Location currentLocation) {
        if (isSleepStop) {
            // isSleepStop이 true인 경우 switch1을 무조건 true로 설정하고 main_on 이미지를 설정
            switch1.setChecked(true);
            imageView.setImageResource(R.drawable.main_on);
            Log.d("HomeFragment", "Switch set to TRUE due to sleep stop");
            return; // 함수 종료하여 더 이상 상태를 변경하지 않음
        }

        // 기존 로직: isSleepStop이 false일 때만 거리 계산 및 switch 상태 변경
        double[] storedLocation = getStoredLocation();
        if (storedLocation != null) {
            Location storedLoc = new Location("");
            storedLoc.setLatitude(storedLocation[0]);
            storedLoc.setLongitude(storedLocation[1]);

            float distance = currentLocation.distanceTo(storedLoc);

            Log.d("HomeFragment", "Current Location: Lat=" + currentLocation.getLatitude() + ", Lon=" + currentLocation.getLongitude());
            Log.d("HomeFragment", "Stored Location: Lat=" + storedLoc.getLatitude() + ", Lon=" + storedLoc.getLongitude());
            Log.d("HomeFragment", "Distance to stored location: " + distance + " meters");

            if (distance >= DISTANCE_THRESHOLD) {
                switch1.setChecked(true);
                imageView.setImageResource(R.drawable.main_on);
                Log.d("HomeFragment", "Switch set to TRUE");
            } else {
                switch1.setChecked(false);
                imageView.setImageResource(R.drawable.main_off);
                Log.d("HomeFragment", "Switch set to FALSE");
            }
        } else {
            Log.d("HomeFragment", "Stored location is null");
        }
    }

    private class FetchDataTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... urls) {
            String[] results = new String[urls.length];
            HttpURLConnection urlConnection = null;
            try {
                for (int i = 0; i < urls.length; i++) {
                    URL url = new URL(urls[i]);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    results[i] = stringBuilder.toString().trim();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return results;
        }

        @Override
        protected void onPostExecute(String[] results) {
            if (rootView != null && results.length >= 4) {
                try {
                    String iconHtml = (results[0] != null) ? results[0].trim() : "";
                    String iconUrl = extractImageUrl(iconHtml);
                    if (iconUrl != null) {
                        new DownloadImageTask(weatherIcon).execute(iconUrl);
                    }

                    String tempString = (results[1] != null) ? results[1].trim() : "N/A";
                    if (tempString.endsWith("°C")) {
                        tempString = tempString.substring(0, tempString.length() - 2).trim();
                    }
                    double temp;
                    try {
                        temp = Double.parseDouble(tempString);
                    } catch (NumberFormatException e) {
                        temp = Double.NaN;
                    }
                    String formattedTemp = (Double.isNaN(temp)) ? "N/A" : String.format("%.1f", temp);
                    temperature.setText(formattedTemp + "°C");

                } catch (Exception e) {
                    e.printStackTrace();
                    temperature.setText("N/A");
                }
            } else {
                temperature.setText("N/A");
            }
        }

        private String extractImageUrl(String html) {
            String url = null;
            int startIndex = html.indexOf("src=\"");
            if (startIndex != -1) {
                startIndex += 5;
                int endIndex = html.indexOf("\"", startIndex);
                if (endIndex != -1) {
                    url = html.substring(startIndex, endIndex);
                }
            }
            return url;
        }
    }


    private void showPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View popupView = inflater.inflate(R.layout.popup_home, null);
        builder.setView(popupView);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        popupView.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
    private void showSleepPopup() {
        // 현재 sleepPlay가 sleep_stop에서 sleepPlay로 바뀌었는지 확인 (팝업을 열지 않음)
        if (sleepPlay.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.sleep_play).getConstantState())) {
            return; // sleepPlay 상태라면 팝업을 열지 않음
        }

        // 팝업을 띄우는 코드 (sleep_stop 상태일 때만 열림)
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View popupSleep = inflater.inflate(R.layout.popup_sleep, null);
        builder.setView(popupSleep);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // 팝업 안의 뷰 클릭 시 동작
        popupSleep.setOnClickListener(v -> {
            sleepPlay.setImageResource(R.drawable.sleep_stop); // sleep_stop 이미지로 변경
            isSleepStop = true; // 수면 모드 중단 상태로 변경
            dialog.dismiss(); // 팝업 닫기
        });

        dialog.setOnDismissListener(d -> {
            sleepPlay.setImageResource(R.drawable.sleep_stop); // 팝업 닫힐 때도 sleep_stop 이미지로 변경
            isSleepStop = true; // 수면 모드 중단 상태로 변경
        });

        dialog.show(); // 팝업을 화면에 표시
    }


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
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
            if (result != null && imageView != null) {
                imageView.setImageBitmap(result);
            }
        }
    }

    public void updateAddress(String newAddress) {
        if (guText != null) {
            userViewModel.setAddress(newAddress); // ViewModel을 통해 주소 설정
        }
    }
}
