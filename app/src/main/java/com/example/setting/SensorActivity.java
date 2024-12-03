package com.example.setting;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONObject;

public class SensorActivity extends AppCompatActivity {

    private Button assignButton;
    private EditText searchBar;
    private ImageView backButton;
    private SQLiteHelper dbHelper;
    private LinearLayout registeredDevicesLayout;
    private String currentUserId;
    private static final String MAC_ADDRESS_REGEX = "^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$";
    private static final String TAG = "SensorActivity"; // 로그 태그

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        searchBar = findViewById(R.id.search_bar);
        backButton = findViewById(R.id.back_button);
        assignButton = findViewById(R.id.assign_button);
        registeredDevicesLayout = findViewById(R.id.registered_devices_layout);

        dbHelper = new SQLiteHelper(this);

        Myapp app = (Myapp) getApplicationContext();
        User currentUser = app.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUserId();
        } else {
            currentUserId = "";
        }

        backButton.setOnClickListener(v -> finish());

        assignButton.setOnClickListener(v -> {
            String deviceId = searchBar.getText().toString().trim();

            // MAC 주소 유효성 검사
            if (!isValidMacAddress(deviceId)) {
                showCustomToast("MAC 주소 형식이 올바르지 않습니다. 다시 확인해 주세요.");
                resetSearchBar();
                return;
            }

            // 이미 등록된 기기가 있는지 확인
            if (registeredDevicesLayout.getChildCount() > 0) {
                showCustomToast("기기는 하나만 등록할 수 있습니다.");
                resetSearchBar();
                return;
            }

            // 중복 등록 방지
            if (isDeviceAlreadyRegistered(deviceId)) {
                showCustomToast("이미 등록된 기기입니다.");
                resetSearchBar();
                return;
            }

            if (!deviceId.isEmpty()) {
                // Myapp 클래스와 User 객체에 deviceId 저장
                app.setDeviceId(deviceId);
                if (currentUser != null) {
                    currentUser.setDeviceId(deviceId);
                }

                // DB에 기기 정보 저장
                addDevice(deviceId);
                resetSearchBar();

                // 센서 데이터 가져오기
                fetchAndDisplaySensorData();
            } else {
                showCustomToast("제품 모델명을 입력하세요.");
            }
        });

        // DB에서 deviceId 불러오기
        String savedDeviceId = getDeviceIdFromDB();
        if (savedDeviceId != null) {
            app.setDeviceId(savedDeviceId);
            if (currentUser != null) {
                currentUser.setDeviceId(savedDeviceId);
            }
        }

        // 앱 실행 시 등록된 기기들을 표시하고 센서 데이터 가져오기
        displayRegisteredDevices();
        fetchAndDisplaySensorData();
    }

    // DB에서 deviceId 가져오기
    private String getDeviceIdFromDB() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                SQLiteHelper.TABLE_DEVICE,
                new String[]{SQLiteHelper.COLUMN_DEVICE_ID},
                SQLiteHelper.COLUMN_USER_ID + "=?",
                new String[]{currentUserId},
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

    // DB에 이미 등록된 기기인지 확인
    private boolean isDeviceAlreadyRegistered(String deviceId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(SQLiteHelper.TABLE_DEVICE, null,
                SQLiteHelper.COLUMN_DEVICE_ID + "=? AND " + SQLiteHelper.COLUMN_USER_ID + "=?",
                new String[]{deviceId, currentUserId}, null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    // Myapp에서 deviceId 가져오기
    private String getDeviceIdFromApp() {
        Myapp app = (Myapp) getApplicationContext();
        return app.getMacDeviceId(); // Myapp 인스턴스에서 deviceId를 가져옴
    }

    // MAC 주소 유효성 검사
    private boolean isValidMacAddress(String deviceId) {
        return deviceId.matches(MAC_ADDRESS_REGEX);
    }

    // 검색창 리셋 및 키보드 숨김 처리
    private void resetSearchBar() {
        searchBar.setText("");
        searchBar.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
        }
    }

    private void fetchAndDisplaySensorData() {
        String deviceId = getDeviceIdFromApp();
        if (deviceId != null && !deviceId.isEmpty()) {
            new FetchSensorDataTask(SensorActivity.this, new FetchSensorDataTask.Listener() {
                @Override
                public void onDataFetched(JSONObject sensorData) {
                    try {
                        int doorOpen = sensorData.getInt("Door");
                        int rainDetected = sensorData.getInt("Rain");
                        //int humanDetected = sensorData.getInt("Person");
                        //int windowOpen = sensorData.getInt("Window");

                        Toast.makeText(SensorActivity.this, "현관 열림: " + doorOpen + "\n" +
                                "빗물 감지: " + rainDetected + "\n"
                                //"인체 감지: " + humanDetected + "\n" +
                                ,Toast.LENGTH_LONG).show();

                    } catch (Exception e) {
                        Log.e(TAG, "센서 데이터 처리 중 오류 발생", e);
                        showCustomToast("센서 데이터 처리 중 오류 발생.");
                    }
                }

                @Override
                public void onDataFetchFailed() {
                    Log.e(TAG, "센서 데이터를 가져오는 데 실패했습니다.");
                    showCustomToast("센서 데이터를 가져오는 데 실패했습니다.");
                }
            }).execute(deviceId);
        }
    }


    private void addDevice(String deviceId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_USER_ID, currentUserId); // 현재 로그인된 사용자 ID
        values.put(SQLiteHelper.COLUMN_DEVICE_ID, deviceId); // 입력된 deviceId
        long newRowId = db.insert(SQLiteHelper.TABLE_DEVICE, null, values);

        if (newRowId != -1) {
            Log.d(TAG, "기기 등록 성공: " + deviceId); // 로그 추가
        } else {
            Log.e(TAG, "기기 등록 실패: " + deviceId); // 오류 로그 추가
        }

        displayNewDevice(deviceId);
    }

    private void displayNewDevice(String deviceId) {
        View deviceView = LayoutInflater.from(this).inflate(R.layout.device_item_add, null);
        TextView deviceIdTextView = deviceView.findViewById(R.id.device_id);
        Button deleteButton = deviceView.findViewById(R.id.delete_button);

        deviceIdTextView.setText(deviceId);
        registeredDevicesLayout.addView(deviceView);

        deleteButton.setOnClickListener(v -> deleteDevice(deviceView, deviceId));
    }

    private void displayRegisteredDevices() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(SQLiteHelper.TABLE_DEVICE, null, SQLiteHelper.COLUMN_USER_ID + "=?", new String[]{currentUserId}, null, null, null);

        while (cursor.moveToNext()) {
            @SuppressLint("Range") String deviceId = cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_DEVICE_ID));
            displayNewDevice(deviceId);
        }
        cursor.close();
    }

    private void deleteDevice(View deviceView, String deviceId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted = db.delete(SQLiteHelper.TABLE_DEVICE, SQLiteHelper.COLUMN_DEVICE_ID + "=?", new String[]{deviceId});

        if (rowsDeleted > 0) {
            Log.d(TAG, "기기 삭제 성공: " + deviceId); // 로그 추가
        } else {
            Log.e(TAG, "기기 삭제 실패: " + deviceId); // 오류 로그 추가
        }

        registeredDevicesLayout.removeView(deviceView);

        Myapp app = (Myapp) getApplicationContext();
        app.setDeviceId(null);
        User currentUser = app.getCurrentUser();
        if (currentUser != null) {
            currentUser.setDeviceId(null);
        }
    }

    private void showCustomToast(String message) {
        Toast.makeText(SensorActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
