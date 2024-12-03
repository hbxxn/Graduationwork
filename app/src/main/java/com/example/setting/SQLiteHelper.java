package com.example.setting;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SQLiteHelper extends SQLiteOpenHelper {

    // 데이터베이스 이름과 버전
    public static final String DATABASE_NAME = "Signup1.db";
    public static final int DATABASE_VERSION = 2;

    // Member 테이블 및 컬럼 이름 상수
    public static final String TABLE_MEMBER = "Member";
    public static final String COLUMN_ID = "user_id";
    public static final String COLUMN_PASSWORD = "user_password";
    public static final String COLUMN_LATITUDE = "lat";
    public static final String COLUMN_LONGITUDE = "lon";
    public static final String COLUMN_GU = "gu"; // 사용자 주소의 구 정보 저장을 위한 컬럼 추가

    // Device 테이블 및 컬럼 이름 상수
    public static final String TABLE_DEVICE = "Device";
    public static final String COLUMN_DEVICE_ID = "device_id";
    public static final String COLUMN_USER_ID = "user_id";

    // Notification 테이블 및 컬럼 이름 상수
    public static final String TABLE_NOTIFICATIONS = "notifications";
    public static final String COLUMN_NOTIFICATION_ID = "_id";
    public static final String COLUMN_NOTIFICATION_TITLE = "title";
    public static final String COLUMN_NOTIFICATION_CONTENT = "content";
    public static final String COLUMN_NOTIFICATION_TIMESTAMP = "timestamp";

    private Context context;

    // 생성자
    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Member 테이블 생성 쿼리
        String createMemberTable = "CREATE TABLE " + TABLE_MEMBER + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_PASSWORD + " TEXT, " +
                COLUMN_LATITUDE + " REAL, " +
                COLUMN_LONGITUDE + " REAL, " +
                COLUMN_GU + " TEXT )";

        // Device 테이블 생성 쿼리
        String createDeviceTable = "CREATE TABLE " + TABLE_DEVICE + " (" +
                COLUMN_USER_ID + " TEXT, " +
                COLUMN_DEVICE_ID + " TEXT PRIMARY KEY, " +
                "FOREIGN KEY (" + COLUMN_USER_ID + ") REFERENCES " + TABLE_MEMBER + "(" + COLUMN_ID + "))";

        // Notification 테이블 생성 쿼리
        String createNotificationTable = "CREATE TABLE " + TABLE_NOTIFICATIONS + " (" +
                COLUMN_NOTIFICATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NOTIFICATION_TITLE + " TEXT, " +
                COLUMN_NOTIFICATION_CONTENT + " TEXT, " +
                COLUMN_NOTIFICATION_TIMESTAMP + " INTEGER )";

        // 테이블 생성
        db.execSQL(createMemberTable);
        db.execSQL(createDeviceTable);
        db.execSQL(createNotificationTable);

        Log.d("SQLiteHelper", "Tables created: " + TABLE_MEMBER + ", " + TABLE_DEVICE + ", " + TABLE_NOTIFICATIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 데이터베이스 버전이 업그레이드되면, 테이블 삭제 후 다시 생성
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEMBER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        onCreate(db);

        Log.d("SQLiteHelper", "Database upgraded from version " + oldVersion + " to " + newVersion);
    }

    // 알림 데이터를 SQLite 데이터베이스에 저장하는 메서드
    public void saveNotification(String title, String content, long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTIFICATION_TITLE, title);
        values.put(COLUMN_NOTIFICATION_CONTENT, content);
        values.put(COLUMN_NOTIFICATION_TIMESTAMP, timestamp);

        long result = db.insert(TABLE_NOTIFICATIONS, null, values);
        if (result != -1) {
            Log.d("SQLiteHelper", "Notification saved to DB");
        } else {
            Log.d("SQLiteHelper", "Failed to save notification");
        }
        // 데이터베이스 닫기
        db.close();
    }

    // 회원 정보 업데이트 메서드
    public boolean updateMember(String userId, String newPassword, String newAddress) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Geocoder를 사용하여 주소를 위도와 경도로 변환
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        double lat = 0.0;
        double lon = 0.0;
        String gu = "";

        try {
            List<Address> addresses = geocoder.getFromLocationName(newAddress, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0);
                lat = location.getLatitude();
                lon = location.getLongitude();
                gu = location.getSubLocality(); // 동 정보 추출
            }
        } catch (IOException e) {
            e.printStackTrace(); // 에러 로그를 기록
        }

        // 업데이트할 값 설정
        values.put(COLUMN_PASSWORD, newPassword);
        values.put(COLUMN_LATITUDE, lat);
        values.put(COLUMN_LONGITUDE, lon);
        values.put(COLUMN_GU, gu);

        // 회원 정보 업데이트
        int rowsAffected = db.update(TABLE_MEMBER, values, COLUMN_ID + " = ?", new String[]{userId});
        //db.close();
        return rowsAffected > 0;
    }

    // 회원 정보를 데이터베이스에 추가하는 메서드
    public boolean addMember(String userId, String userPassword, String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        double lat = 0.0;
        double lon = 0.0;
        String gu = "";

        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                lat = addresses.get(0).getLatitude();
                lon = addresses.get(0).getLongitude();
                gu = addresses.get(0).getSubLocality(); // 동 정보 추출
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        values.put(COLUMN_ID, userId);
        values.put(COLUMN_PASSWORD, userPassword);
        values.put(COLUMN_LATITUDE, lat);
        values.put(COLUMN_LONGITUDE, lon);
        values.put(COLUMN_GU, gu); // 동 정보 저장

        long result = db.insert(TABLE_MEMBER, null, values);
        //db.close();
        return result != -1;
    }

    // 회원가입 화면에서 아이디 중복 확인하는 메서드
    public boolean checkId(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            String query = "SELECT 1 FROM " + TABLE_MEMBER + " WHERE " + COLUMN_ID + " = ?";
            Log.d("SQLiteHelper", "Executing query: " + query + " with userId: " + userId);
            cursor = db.rawQuery(query, new String[]{userId});
            boolean exists = cursor.getCount() > 0;
            Log.d("SQLiteHelper", "ID exists: " + exists);
            return exists;
        } catch (Exception e) {
            Log.e("SQLiteHelper", "Error checking ID", e);
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            //db.close();    이 줄을 제거해야 DB가 closed가 안됨
        }
    }

    // 회원가입 화면에서 아이디와 비밀번호가 모두 일치하는지 확인하는 메서드
    public Boolean checkIdPassword(String userId, String userPassword) {
        User user = getUserById(userId);
        return user != null && user.getPassword().equals(userPassword);
    }

    // 위도와 경도 값을 반환하는 메서드
    public List<double[]> getLocations() {
        List<double[]> locations = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_LATITUDE + ", " + COLUMN_LONGITUDE + " FROM " + TABLE_MEMBER + " ORDER BY " + COLUMN_ID + " DESC", null);
        if (cursor.moveToFirst()) {
            do {
                double lat = cursor.getDouble(0);
                double lon = cursor.getDouble(1);
                locations.add(new double[]{lat, lon});
            } while (cursor.moveToNext());
        }
        cursor.close();
        //db.close();
        return locations;
    }

    // 특정 사용자 ID의 동 정보 조회
    public String getGu(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_GU + " FROM " + TABLE_MEMBER + " WHERE " + COLUMN_ID + " = ?", new String[]{userId});
        if (cursor.moveToFirst()) {
            String gu = cursor.getString(0);
            cursor.close();
            //db.close();
            return gu != null ? gu : "구 정보 없음";
        }
        cursor.close();
        //db.close();
        return "구 정보 없음";
    }

    // 회원탈퇴
    public boolean deleteMember(String userId, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        // id와 비밀번호가 일치하는지 확인
        boolean isValid = checkIdPassword(userId, password);
        if (isValid) {
            // 일치하면 데이터 삭제
            int memberResult = db.delete(TABLE_MEMBER, COLUMN_ID + " = ?", new String[]{userId});

            // Device 테이블에서 연관된 데이터 삭제
            int deviceResult = db.delete(TABLE_DEVICE, COLUMN_USER_ID + " = ?", new String[]{userId});

            // 삭제 성공 여부 반환
            return memberResult > 0 && deviceResult >= 0; // deviceResult >= 0은 삭제가 성공적으로 진행되었음을 의미
        } else {
            Log.d("SQLiteHelper", "아이디나 비밀번호가 일치하지 않습니다.");
            return false; // 비밀번호 불일치 시 false 반환
        }
    }

    // 특정 사용자 ID로 사용자 정보를 조회
    public User getUserById(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        User user = null;

        try {
            String query = "SELECT * FROM " + TABLE_MEMBER + " WHERE " + COLUMN_ID + " = ?";
            cursor = db.rawQuery(query, new String[]{userId});

            if (cursor.moveToFirst()) {
                @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex(COLUMN_ID));
                @SuppressLint("Range") String password = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD));
                @SuppressLint("Range") double latitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE));
                @SuppressLint("Range") double longitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE));
                @SuppressLint("Range") String gu = cursor.getString(cursor.getColumnIndex(COLUMN_GU));

                user = new User(id, password, latitude, longitude, gu);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            //db.close();
        }

        return user;
    }

    // '구' 정보를 업데이트하는 메서드
    public boolean updateGu(String userId, String gu) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_GU, gu); // "gu" 컬럼에 업데이트할 값

        int result = db.update(TABLE_MEMBER, contentValues, COLUMN_ID + " = ?", new String[]{userId});
        return result > 0;
    }

    // 사용자 ID로 주소를 조회하는 메서드
    public String getAddressByUserId(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_GU + " FROM " + TABLE_MEMBER + " WHERE " + COLUMN_ID + " = ?", new String[]{userId});
        if (cursor.moveToFirst()) {
            String gu = cursor.getString(0);
            cursor.close();
            return gu != null ? gu : "주소 정보 없음";
        }
        cursor.close();
        return "주소 정보 없음";
    }

    // 특정 날짜에 해당하는 알림 데이터 가져오기
    public Cursor getNotificationsForDate(long startOfDay, long endOfDay) {
        SQLiteDatabase db = this.getReadableDatabase();
        // 타임스탬프 기준 내림차순 정렬하여 가져오기 (가장 최근 알림부터)
        return db.rawQuery("SELECT * FROM " + TABLE_NOTIFICATIONS + " WHERE " + COLUMN_NOTIFICATION_TIMESTAMP + " BETWEEN ? AND ? ORDER BY " + COLUMN_NOTIFICATION_TIMESTAMP + " DESC",
                new String[]{String.valueOf(startOfDay), String.valueOf(endOfDay)});
    }

    // 특정 사용자 ID의 기기 정보 조회
    private String getDeviceID(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_DEVICE_ID + " FROM " + TABLE_DEVICE + " WHERE " + COLUMN_USER_ID + " = ?", new String[]{userId});
        if (cursor.moveToFirst()) {
            String deviceID = cursor.getString(0);
            cursor.close();
            return deviceID != null ? deviceID : "기기 정보 없음";
        }
        cursor.close();
        return "기기 정보 없음";
    }
}
